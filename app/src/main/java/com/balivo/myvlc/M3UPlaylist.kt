package com.balivo.myvlc

/**
 * Created by balivo on 2/4/18.
 */
class M3UPlaylist {

    var playlistName: String? = null

    var playlistParams: String? = null

    var playlistItems: List<M3UItem>? = null

    fun getSingleParameter(paramName: String): String {
        val paramsArray = this.playlistParams!!.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (i in paramsArray.indices) {
            val parameter = paramsArray[i]
            if (parameter.contains(paramName)) {
                return parameter.substring(parameter.indexOf(paramName) + paramName.length).replace("=", "")
            }
        }
        return ""
    }
}