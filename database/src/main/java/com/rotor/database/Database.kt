package com.rotor.database

import android.os.Handler
import android.util.Log
import com.rotor.core.Builder
import com.rotor.core.Rotor
import com.rotor.core.interfaces.BuilderFace
import com.rotor.database.abstr.Reference
import com.rotor.database.models.KReference
import com.rotor.database.models.PrimaryReferece
import com.rotor.database.models.PrimaryReferece.Companion.ACTION_NEW_OBJECT
import com.rotor.database.models.PrimaryReferece.Companion.EMPTY_OBJECT
import com.rotor.database.models.PrimaryReferece.Companion.NULL
import com.rotor.database.models.PrimaryReferece.Companion.OS
import com.rotor.database.models.PrimaryReferece.Companion.PATH
import com.rotor.database.request.CreateListener
import com.rotor.database.request.RemoveListener
import com.rotor.database.request.SyncResponse
import com.rotor.database.request.UpdateToServer
import com.rotor.database.utils.ReferenceUtils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*


/**
 * Created by efrainespada on 12/03/2018.
 */
class Database  {

    companion object {

        private val TAG: String = Database::class.java.simpleName!!
        private var pathMap: HashMap<String, KReference<*>> ? = null

        @JvmStatic fun initialize() {
            pathMap?.let {
                pathMap = HashMap()
            }

            Rotor.prepare(Builder.DATABASE, object: BuilderFace {
                override fun onMessageReceived(jsonObject: JSONObject) {
                    try {
                        if (jsonObject.has("data")) {
                            val data = jsonObject.get("data") as JSONObject
                            if (data.has("info") && data.has(PATH)) {
                                val info = data.getString("info")
                                val path = data.getString(PATH)
                                if (ACTION_NEW_OBJECT.equals(info)) {
                                    if (pathMap!!.containsKey(path)) {
                                        val handler = Handler()
                                        handler.postDelayed({ sync(path) }, 200)
                                    }
                                }
                            } else if (data.has(PATH)) {
                                val path = data.getString(PATH)
                                if (pathMap!!.containsKey(path)) {
                                    pathMap!![path]!!.onMessageReceived(data)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            })
        }

        @JvmStatic fun <T> listener(path: String, reference: Reference<T>) {
            if (pathMap == null) {
                Log.e(TAG, "Use Database.initialize(Context context, String urlServer, String token, StatusListener) before create real time references")
                return
            }

            if (Rotor.rotorService == null || Rotor.rotorService?.getMoment() == null) {
                Rotor.statusListener.reconnecting()
                return
            }

            val blowerCreation = Date().time

            if (pathMap!!.containsKey(path) && (Rotor.rotorService?.getMoment() as Long).equals(pathMap!!.get(path)!!.moment)) {
                if (Rotor.debug!!) {
                    Log.d(TAG, "Listener already added for: $path")
                }
                pathMap!![path]!!.addBlower(blowerCreation, reference)
                pathMap!![path]!!.loadCachedReference()
                return
            }

            val objectReference = KReference<T>(Rotor.context!!, path, reference, reference.clazz(), Rotor.rotorService!!.getMoment() as Long)
            pathMap!![path] = objectReference

            objectReference.loadCachedReference()

            syncWithServer(path)
        }

        @JvmStatic private fun syncWithServer(path: String) {
            var content = ReferenceUtils.getElement(path)
            if (content == null) {
                content = PrimaryReferece.EMPTY_OBJECT
            }

            // val sha1 = ReferenceUtils.SHA1(content)
            val sha1 = ""

            val createListener = CreateListener("create_listener", path, Rotor.id!!, OS, sha1, content.length)

            val call = ReferenceUtils.service(Rotor.urlServer!!).createReference(createListener)
            call.enqueue(object : Callback<SyncResponse> {

                override fun onResponse(call: Call<SyncResponse>, response: Response<SyncResponse>) {
                    if (response.errorBody() != null && !response.isSuccessful()) {
                        try {
                            Log.e(TAG, response.errorBody()!!.string())
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                    }
                }

                override fun onFailure(call: Call<SyncResponse>, t: Throwable) {
                    if (t.stackTrace != null) {
                        Log.e(TAG, "error")
                        t.printStackTrace()
                    } else {
                        Log.e(TAG, "create listener response error")
                    }
                }
            })
        }

        @JvmStatic fun removeListener(path: String) {
            if (pathMap!!.containsKey(path)) {
                val removeListener = RemoveListener("remove_listener", path, Rotor.id!!)
                val call = ReferenceUtils.service(Rotor.urlServer!!).removeListener(removeListener)
                call.enqueue(object : Callback<SyncResponse> {

                    override fun onResponse(call: Call<SyncResponse>, response: Response<SyncResponse>) {
                        if (response.errorBody() != null && !response.isSuccessful()) {
                            try {
                                Log.e(TAG, response.errorBody()!!.string())
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }

                        }
                    }

                    override fun onFailure(call: Call<SyncResponse>, t: Throwable) {
                        if (t.stackTrace != null) {
                            Log.e(TAG, "error")
                            t.printStackTrace()
                        } else {
                            Log.e(TAG, "remove listener response error")
                        }
                    }
                })
            }
        }

        @JvmStatic private fun refreshToServer(path: String, differences: String, len: Int, clean: Boolean) {
            if (differences == PrimaryReferece.EMPTY_OBJECT) {
                Log.e(TAG, "no differences: $differences")
                return
            } else {
                Log.d(TAG, "differences: $differences")
            }

            val updateToServer = UpdateToServer("update_data", path, Rotor.id!!, "android", differences, len, clean)
            val call = ReferenceUtils.service(Rotor.urlServer!!).refreshToServer(updateToServer)

            call.enqueue(object : Callback<SyncResponse> {

                override fun onResponse(call: Call<SyncResponse>, response: Response<SyncResponse>) {
                    if (response.errorBody() != null && !response.isSuccessful()) {
                        try {
                            Log.e(TAG, response.errorBody()!!.string())
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                    }
                }

                override fun onFailure(call: Call<SyncResponse>, t: Throwable) {
                    if (t.stackTrace != null) {
                        Log.e(TAG, "error")
                        t.printStackTrace()
                    } else {
                        Log.e(TAG, "update to server response error")
                    }
                }
            })
        }

        @JvmStatic fun sync(path: String) {
            sync(path, false)
        }

        @JvmStatic fun sync(path: String, clean: Boolean) {
            if (pathMap!!.containsKey(path)) {
                val result = pathMap!![path]!!.syncReference(clean)
                val diff = result[1] as String
                val len = result[0] as Int
                if (!EMPTY_OBJECT.equals(diff)) {
                    refreshToServer(path, diff, len, clean)
                } else {
                    val blower = pathMap!![path]!!.getLastest()
                    val value = pathMap!![path]!!.getReferenceAsString()
                    if (value.equals(EMPTY_OBJECT) || value.equals(NULL)) {
                        blower.onCreate()
                    }
                }
            }
        }
    }

}