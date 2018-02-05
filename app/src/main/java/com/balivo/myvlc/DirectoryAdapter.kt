package com.balivo.myvlc

import android.database.DataSetObserver
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import org.videolan.libvlc.util.Extensions


/**
 * List adapter used to drive the ListView in the activity.
 */
class DirectoryAdapter : BaseAdapter() {

    private val mFiles = ArrayList<String>()
    var isAudioMode: Boolean = false

    init {
        isAudioMode = true
        refresh()
    }

    fun refresh() {
        Log.d(TAG, "Refreshing adapter in " + if (isAudioMode) "audio mode" else "video mode")
        val files = Environment.getExternalStorageDirectory().listFiles()
        mFiles.clear()
        for (f in files!!) {
            // Filter using libVLC's 'supported audio formats' filter.
            if (f.name.contains(".")) {
                val i = f.name.lastIndexOf(".")
                if (i > 0) {
                    if (isAudioMode && Extensions.AUDIO.contains(f.name
                                    .substring(i)) || !isAudioMode && Extensions.VIDEO.contains(f
                                    .name.substring(i))) {
                        mFiles.add(f.name)
                    }
                }
            }
        }
        this.notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return mFiles.size
    }

    override fun getItem(position: Int): Any {
        return Environment.getExternalStorageDirectory().absolutePath + '/'.toString() + mFiles[position]
    }

    override fun getItemId(arg0: Int): Long {
        // TODO Auto-generated method stub
        return 0
    }

    override fun getItemViewType(arg0: Int): Int {
        return 0
    }

    override fun getView(position: Int, v: View?, parent: ViewGroup): View {
        var v = v
        if (v == null) {
            v = TextView(parent.context)
        }
        (v as TextView).text = mFiles[position]

        return v
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun hasStableIds(): Boolean {
        // TODO Auto-generated method stub
        return false
    }

    override fun isEmpty(): Boolean {
        return mFiles.isEmpty()
    }

    override fun registerDataSetObserver(arg0: DataSetObserver) {
        super.registerDataSetObserver(arg0)
    }

    override fun unregisterDataSetObserver(arg0: DataSetObserver) {
        super.unregisterDataSetObserver(arg0)
    }

    override fun areAllItemsEnabled(): Boolean {
        // TODO Auto-generated method stub
        return false
    }

    override fun isEnabled(arg0: Int): Boolean {
        return true
    }

    companion object {
        val TAG = "MyVlc/DirectoryAdapter"
    }

}
