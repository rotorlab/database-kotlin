package com.rotor.database.models

import android.content.Context
import com.efraespada.jsondiff.JSONDiff
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.rotor.core.Rotor
import com.rotor.database.Docker
import com.rotor.database.utils.ReferenceUtils
import com.stringcare.library.SC
import org.json.JSONException
import org.json.JSONObject
import java.text.Normalizer
import java.util.*

/**
 * Created by efraespada on 14/03/2018.
 */
abstract class PrimaryReferece<T>(context: Context, path: String) {

    companion object {
        internal var EMPTY_OBJECT = "{}"
        internal var OS = "android"
        internal var ACTION_SIMPLE_UPDATE = "simple_update"
        internal var ACTION_SLICE_UPDATE = "slice_update"
        internal var ACTION_NO_UPDATE = "no_update"
        internal var ACTION_SIMPLE_CONTENT = "simple_content"
        internal var ACTION_SLICE_CONTENT = "slice_content"
        internal var ACTION_NO_CONTENT = "no_content"
        internal var ACTION_NEW_OBJECT = "new_object"
        internal var STAG = "tag"
        internal var PATH = "id"
        internal var REFERENCE = "reference"
        internal var SIZE = "size"
        internal var INDEX = "index"
        internal var ACTION = "action"
        internal var NULL = "null"
    }

    private val mapParts: HashMap<String, Array<String?>>
    var database: Docker? = null
    private val context: Context
    protected var gson: Gson
    var len: Int = 0
    var serverLen: Int = 0
    var moment: Long ? = null
    protected var path: String
    protected var stringReference: String? = null

    private val TAG = KReference::class.java.simpleName
    protected val blowerMap = HashMap<Long, T>()

    init {
        this.context = context
        this.path = path
        this.gson = getGsonBuilder()
        this.serverLen = 0
        SC.init(this.context)
        this.mapParts = HashMap()
        this.stringReference = ReferenceUtils.getElement(path)
        this.len = if (stringReference == null) 0 else stringReference!!.length
    }

    /**
     * notify update percent
     */
    abstract fun getLastest(): T

    abstract fun progress(value: Int)

    // abstract fun addBlower(creation: Long, blower: *)

    /**
     * tag or identifier used to identify incoming object updates
     * from server cluster
     *
     * @return String
     */
    fun getTag(): String {
        return path + "_sync"
    }

    /**
     * returns actual reference in string format
     * @return String
     */
    abstract fun getReferenceAsString(): String

    /**
     * loads stored JSON object on db. if not exists,
     * gets current reference and stores
     *
     */
    abstract fun loadCachedReference()

    /**
     * returns the result of applying differences to current JSON object
     * after being stored on local DB
     * @param value
     */
    abstract fun blowerResult(value: String)

    private fun getGsonBuilder(): Gson {
        return GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation().create()
    }

    fun syncReference(clean: Boolean): Array<Any?> {
        val len: Int
        val objects = arrayOfNulls<Any>(2)

        if (clean || stringReference == null) {
            this.stringReference = "{}"
        }

        try {
            val actual = getReferenceAsString()
            JSONDiff.setDebug(Rotor.debug!!)
            val diff = JSONDiff.diff(JSONObject(stringReference), JSONObject(actual))

            val jsonObject = JSONObject()

            // max 3
            for ((key, value) in diff) {
                jsonObject.put(key, value)
            }

            len = actual.length

            objects[0] = len
            objects[1] = jsonObject.toString()

            return objects
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return objects
    }

    fun onMessageReceived(json: JSONObject) {
        try {
            val tag = json.getString(STAG)
            val action = json.getString(ACTION)
            val data = if (json.has(REFERENCE)) json.getString(REFERENCE) else null
            val path = json.getString(PATH)
            val rData = if (data == null) "{}" else ReferenceUtils.hex2String(data)


            if (!tag.equals(getTag(), ignoreCase = true)) {
                return
            }

            when (action) {

                ACTION_SIMPLE_UPDATE -> parseUpdateResult(path, rData)

                ACTION_SLICE_UPDATE -> {
                    val size = json.getInt(SIZE)
                    val index = json.getInt(INDEX)
                    if (mapParts.containsKey(path)) {
                        mapParts[path]!![index] = rData
                    } else {
                        val parts = arrayOfNulls<String>(size)
                        parts[index] = rData
                        mapParts[path] = parts
                    }

                    var ready = true
                    var alocated = 0
                    for (p in mapParts[path]!!.size - 1 downTo 0) {
                        if (mapParts[path]!![p] == null) {
                            ready = false
                        } else {
                            alocated++
                        }
                    }

                    val percent = 100f / size.toFloat() * alocated
                    progress(percent.toInt())

                    if (ready && mapParts[path]!!.size - 1 == index) {
                        val complete = StringBuilder()
                        for (i in 0 until mapParts[path]!!.size) {
                            complete.append(mapParts[path]!![i])
                        }
                        mapParts.remove(path)
                        val result = complete.toString()
                        parseUpdateResult(path, result)
                    }
                }

                ACTION_NO_UPDATE -> blowerResult(stringReference!!)

                ACTION_SIMPLE_CONTENT -> parseContentResult(path, rData)

                ACTION_SLICE_CONTENT -> {
                    val sizeContent = json.getInt(SIZE)
                    val indexContent = json.getInt(INDEX)
                    if (mapParts.containsKey(path)) {
                        mapParts[path]!![indexContent] = rData
                    } else {
                        val partsContent = arrayOfNulls<String>(sizeContent)
                        partsContent[indexContent] = rData
                        mapParts[path] = partsContent
                    }

                    var readyContent = true
                    var alocatedContent = 0
                    for (p in mapParts[path]!!.size - 1 downTo 0) {
                        if (mapParts[path]!![p] == null) {
                            readyContent = false
                        } else {
                            alocatedContent++
                        }
                    }

                    val percentContent = 100f / sizeContent.toFloat() * alocatedContent
                    progress(percentContent.toInt())

                    if (readyContent && mapParts[path]!!.size - 1 == indexContent) {
                        val completeContent = StringBuilder()
                        for (i in 0 until mapParts[path]!!.size) {
                            completeContent.append(mapParts[path]!![i])
                        }
                        mapParts.remove(path)
                        val resultContent = completeContent.toString()
                        parseContentResult(path, resultContent)
                    }
                }

                ACTION_NO_CONTENT -> blowerResult("{}")

                else -> {
                }
            }// nothing to do here ..

            //Log.e(TAG, data);
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun parseUpdateResult(path: String, data: String) {
        try {
            val jsonObject: JSONObject

            var prev: String? = getReferenceAsString()

            if (prev != null) {
                prev = Normalizer.normalize(prev, Normalizer.Form.NFC)
                jsonObject = JSONObject(prev)
            } else {
                jsonObject = JSONObject()
            }

            val differences = JSONObject(data)

            if (differences.has("\$unset")) {
                val set = differences.getJSONObject("\$unset")
                val keys = set.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val p = key.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    var aux = jsonObject

                    for (w in p.indices) {
                        val currentIndex = p[w]
                        if (aux.has(currentIndex) && w != p.size - 1) {
                            aux = aux.getJSONObject(currentIndex)
                        } else if (w != p.size - 1) {
                            aux.put(currentIndex, JSONObject())
                            aux = aux.getJSONObject(currentIndex)
                        }

                        if (w == p.size - 1 && aux.has(currentIndex)) {
                            aux.remove(currentIndex)
                        }
                    }
                }
            }

            if (differences.has("\$set")) {
                val set = differences.getJSONObject("\$set")
                val keys = set.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val p = key.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    var aux = jsonObject
                    for (w in p.indices) {
                        val currentIndex = p[w]
                        if (aux.has(currentIndex) && w != p.size - 1) {
                            aux = aux.getJSONObject(currentIndex)
                        } else if (w != p.size - 1) {
                            aux.put(currentIndex, JSONObject())
                            aux = aux.getJSONObject(currentIndex)
                        }

                        if (w == p.size - 1) {
                            if (aux.has(currentIndex)) {
                                if (aux.get(currentIndex) is JSONObject) {
                                    try {
                                        aux = aux.getJSONObject(currentIndex)
                                        val toExport = set.getJSONObject(key)
                                        val y = toExport.keys()
                                        while (y.hasNext()) {
                                            val k = y.next()
                                            aux.put(k, toExport.get(k))
                                        }
                                    } catch (e: Exception) {
                                        aux.put(currentIndex, set.get(key))
                                    }

                                } else {
                                    aux.put(currentIndex, set.get(key))
                                }
                            } else {
                                try {
                                    // test if element to save is JSON object
                                    var cached = set.getJSONObject(key).toString()
                                    cached = Normalizer.normalize(cached, Normalizer.Form.NFC)
                                    val `object` = JSONObject(cached)
                                    aux.put(currentIndex, `object`)
                                } catch (e: Exception) {
                                    aux.put(currentIndex, set.get(key))
                                }

                            }
                        }
                    }
                }
            }

            ReferenceUtils.addElement(path, jsonObject.toString())
            stringReference = jsonObject.toString()
            this.len = stringReference!!.length
            blowerResult(stringReference!!)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    /**
     * updates current string object with incoming data
     * @param path
     * @param data
     */
    private fun parseContentResult(path: String, data: String) {
        ReferenceUtils.addElement(path, data)
        stringReference = data
        this.len = stringReference!!.length
        blowerResult(stringReference!!)
    }
}