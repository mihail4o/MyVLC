package com.balivo.myvlc

/**
 * Created by balivo on 2/4/18.
 */

import java.io.FileNotFoundException
import java.io.InputStream
import java.util.ArrayList
import java.util.NoSuchElementException
import java.util.Scanner


class M3UParser {

    fun convertStreamToString(ist: InputStream): String {
        try {
            return Scanner(ist).useDelimiter("\\A").next()
        } catch (e: NoSuchElementException) {
            return ""
        }

    }

    @Throws(FileNotFoundException::class)
    fun parseFile(inputStream: String): M3UPlaylist {
        val m3UPlaylist = M3UPlaylist()
        val playlistItems = ArrayList<M3UItem>()
        //val stream = convertStreamToString(inputStream)
        // alredy is string
        val stream = inputStream
        val linesArray = stream.split(EXT_INF.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (i in linesArray.indices) {
            val currLine = linesArray[i]
            if (currLine.contains(EXT_M3U)) {
                //header of file
                if (currLine.contains(EXT_PLAYLIST_NAME)) {
                    val fileParams = currLine.substring(EXT_M3U.length, currLine.indexOf(EXT_PLAYLIST_NAME))
                    val playListName = currLine.substring(currLine.indexOf(EXT_PLAYLIST_NAME) + EXT_PLAYLIST_NAME.length).replace(":", "")
                    m3UPlaylist.playlistName = playListName
                    m3UPlaylist.playlistParams = fileParams
                } else {
                    //m3UPlaylist.setPlaylistName("Noname Playlist")
                    m3UPlaylist.playlistName="Noname Playlist"

                    //m3UPlaylist.PlaylistParams("No Params")
                    m3UPlaylist.playlistParams= "No Params"
                }
            } else {
                val playlistItem = M3UItem()
                val dataArray = currLine.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (dataArray[0].contains(EXT_TITLE)) {
                    val chID_ = dataArray[0].substring(0, dataArray[0].indexOf(EXT_TITLE)).replace(":", "").replace("\n", "")
                    //val chID = dataArray[0].substring(dataArray[0].indexOf(EXT_ID) + EXT_ID.length).replace("=", "").replace("\"", "").replace("\n", "")
                    val chID: String = chID_.substring(chID_.indexOf(EXT_ID) + EXT_ID.length).replace("=", "").replace("\"", "").replace("\n", "")
                    val title = dataArray[0].substring(dataArray[0].indexOf(EXT_TITLE) + EXT_TITLE.length).replace("=", "").replace("\"", "").replace("\n", "")

                    playlistItem.itemID = chID
                    playlistItem.itemTitle = title
                } else {
                    val chID = dataArray[0].replace(":", "").replace("\n", "")
                    playlistItem.itemID = chID
                    playlistItem.itemTitle = ""
                }
                val name = dataArray[1].substring(0, dataArray[1].indexOf(EXT_URL)).replace("\n", "")
                val url = dataArray[1].substring(dataArray[1].indexOf(EXT_URL)).replace("\n", "").replace("\r", "")
                playlistItem.itemName = name
                playlistItem.itemUrl = url
                playlistItems.add(playlistItem)
            }
        }
        m3UPlaylist.playlistItems = playlistItems
        return m3UPlaylist
    }

    companion object {

        private val EXT_M3U = "#EXTM3U"
        private val EXT_INF = "#EXTINF:-1 "
        private val EXT_PLAYLIST_NAME = "#PLAYLIST"
        private val EXT_TITLE = "group-title"
        private val EXT_ID = "tvg-name"
        private val EXT_URL = "http://"
    }
}