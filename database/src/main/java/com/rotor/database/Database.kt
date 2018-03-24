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
import com.rotor.database.models.PrimaryReferece.Companion.ACTION_REFERENCE_REMOVED
import com.rotor.database.models.PrimaryReferece.Companion.EMPTY_OBJECT
import com.rotor.database.models.PrimaryReferece.Companion.NULL
import com.rotor.database.models.PrimaryReferece.Companion.OS
import com.rotor.database.models.PrimaryReferece.Companion.PATH
import com.rotor.database.request.CreateListener
import com.rotor.database.request.RemoveListener
import com.rotor.database.request.RemoveReference
import com.rotor.database.request.UpdateToServer
import com.rotor.database.utils.ReferenceUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import java.util.*


/**
 * Created by efrainespada on 12/03/2018.
 */
class Database  {

    companion object {

        private val TAG: String = Database::class.java.simpleName!!
        @JvmStatic private var pathMap: HashMap<String, KReference<*>> ? = null
        val api by lazy {
            ReferenceUtils.service(Rotor.urlServer!!)
        }

        @JvmStatic fun initialize() {
            pathMap.let {
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
                                } else if (ACTION_REFERENCE_REMOVED.equals(info)) {
                                    if (pathMap!!.containsKey(path)) {
                                        val handler = Handler()
                                        handler.postDelayed({ removePrim(path) }, 200)
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

        @JvmStatic fun <T> listen(path: String, reference: Reference<T>) {
            if (pathMap == null) {
                Log.e(TAG, "Use Database.initialize(Context context, String urlServer, String token, StatusListener) before create real time references")
                return
            }

            if (Rotor.rotorService == null || Rotor.rotorService?.getMoment() == null) {
                Rotor.statusListener.reconnecting()
                return
            }

            val blowerCreation = Date().time

            if (pathMap!!.containsKey(path)) {
                Log.d(TAG, "Listener already added for: $path")
                pathMap!![path]!!.addBlower(blowerCreation, reference)
                pathMap!![path]!!.loadCachedReference()
                return
            }

            Log.d(TAG, "Creating reference: $path")

            val objectReference = KReference<T>(Rotor.context!!, path, reference, Rotor.rotorService!!.getMoment() as Long)
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


            val createListener = CreateListener("listen_reference", path, Rotor.id!!, OS, sha1, content.length)

            api.createReference(createListener)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { result ->
                                result.status?.let {
                                    Log.e(TAG, result.status)
                                }
                            },
                            { error -> error.printStackTrace() }
                    )
        }

        @JvmStatic fun unlisten(path: String) {
            if (pathMap!!.containsKey(path)) {
                val removeListener = RemoveListener("unlisten_reference", path, Rotor.id!!)
                api.removeListener(removeListener)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { result ->
                                    result.status?.let {
                                        Log.e(TAG, result.status)
                                    }
                                },
                                { error -> error.printStackTrace() }
                        )
            }
        }

        @JvmStatic fun remove(path: String) {
            if (pathMap!!.containsKey(path)) {
                val removeReference = RemoveReference("remove_reference", path, Rotor.id!!)
                api.removeReference(removeReference)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { result ->
                                    result.status?.let {
                                        Log.e(TAG, result.status)
                                    }
                                },
                                { error -> error.printStackTrace() }
                        )
            }
        }

        @JvmStatic private fun refreshToServer(path: String, differences: String, len: Int, clean: Boolean) {
            if (differences == PrimaryReferece.EMPTY_OBJECT) {
                Log.e(TAG, "no differences: $differences")
                return
            } else {
                Log.d(TAG, "differences: $differences")
            }

            val updateToServer = UpdateToServer("update_reference", path, Rotor.id!!, "android", differences, len, clean)
            api.refreshToServer(updateToServer)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { result ->
                                result.status?.let {
                                    Log.e(TAG, result.status)
                                }
                            },
                            { error -> error.printStackTrace() }
                    )
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

        @JvmStatic fun removePrim(path: String) {
            if (pathMap!!.containsKey(path)) {
                pathMap!![path]!!.remove()
                pathMap!!.remove(path)
            }
        }
    }

}