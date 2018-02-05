package com.balivo.myvlc

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.net.Uri
import android.view.Gravity
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup.LayoutParams
import android.widget.Toast
import android.view.View
import android.view.MotionEvent
import android.view.Display
import android.graphics.Point


import org.videolan.libvlc.IVLCVout
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.AndroidUtil

import java.lang.ref.WeakReference
import java.util.ArrayList

class VideoActivity : Activity(), IVLCVout.Callback {

    private var mFilePath: String? = null

    // display surface
    private var mSurface: SurfaceView? = null
    private var holder: SurfaceHolder? = null

    // media player
    private var libvlc: LibVLC? = null
    private var mMediaPlayer: MediaPlayer? = null
    private var mVideoWidth: Int = 0
    private var mVideoHeight: Int = 0

    /*************
     * Events
     */

    private val mPlayerListener = MyPlayerListener(this)

    /*************
     * Activity
     */

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sample)

        // Receive path to play from intent
        val intent = intent
        mFilePath = intent.extras!!.getString(LOCATION)

        Log.d(TAG, "Playing back " + mFilePath!!)

        mSurface = findViewById(R.id.surface) as SurfaceView
        holder = mSurface!!.holder
        //holder.addCallback(this);

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setSize(mVideoWidth, mVideoHeight)
    }

    override fun onResume() {
        super.onResume()
        createPlayer(mFilePath)
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    /*************
     * Surface
     */
    private fun setSize(width: Int, height: Int) {
        mVideoWidth = width
        mVideoHeight = height
        if (mVideoWidth * mVideoHeight <= 1)
            return

        if (holder == null || mSurface == null)
            return

        // get screen size
        var w = window.decorView.width
        var h = window.decorView.height

        // getWindow().getDecorView() doesn't always take orientation into
        // account, we have to correct the values
        val isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        if (w > h && isPortrait || w < h && !isPortrait) {
            val i = w
            w = h
            h = i
        }

        val videoAR = mVideoWidth.toFloat() / mVideoHeight.toFloat()
        val screenAR = w.toFloat() / h.toFloat()

        if (screenAR < videoAR)
            h = (w / videoAR).toInt()
        else
            w = (h * videoAR).toInt()

        // force surface buffer size
        holder!!.setFixedSize(mVideoWidth, mVideoHeight)

        // set display size
        val lp = mSurface!!.layoutParams
        lp.width = w
        lp.height = h
        mSurface!!.layoutParams = lp
        mSurface!!.invalidate()
    }

    /*************
     * Player
     */

    private fun createPlayer(media: String?) {
        releasePlayer()
        try {
            if (media!!.length > 0) {
                val toast = Toast.makeText(this, media, Toast.LENGTH_LONG)
                toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0,
                        0)
                toast.show()
            }

            // Create LibVLC
            // TODO: make this more robust, and sync with audio demo
            val options = ArrayList<String>()
//            options.add("--subsdec-encoding <encoding>") // was disabled!
            options.add("--aout=opensles")
            options.add("--audio-time-stretch") // time stretching
            options.add("-vvv") // verbosity
            options.add("--http-reconnect")
            options.add("--network-caching=" + 6 * 1000)

            libvlc = LibVLC(options)
            //libvlc.setOnHardwareAccelerationError(this);
            holder!!.setKeepScreenOn(true)

            // Create media player
            mMediaPlayer = MediaPlayer(libvlc)
            mMediaPlayer!!.setEventListener(mPlayerListener)

            // Set up video output
            val vout = mMediaPlayer!!.getVLCVout()
            vout.setVideoView(mSurface)
            //vout.setSubtitlesView(mSurfaceSubtitles);
            vout.addCallback(this)
            vout.attachViews()

            val m = Media(libvlc, Uri.parse(media))
            mMediaPlayer!!.setMedia(m)
            mMediaPlayer!!.play()
        } catch (e: Exception) {
            Toast.makeText(this, "Error creating player!", Toast.LENGTH_LONG).show()
        }

        mSurface!!.rootView.setOnTouchListener{view, event ->
            //Toast.makeText(this,event.downTime.toString(), Toast.LENGTH_LONG).show()
            if (event.actionMasked == MotionEvent.ACTION_DOWN){
                releasePlayer()
                val intent = Intent(this@VideoActivity, MainActivity::class.java)
                startActivity(intent)
            }
            true
        }

  /*      mSurface!!.rootView.setOnTouchListener { view, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                val display = windowManager.defaultDisplay
                val size = Point()
                display.getSize(size)

                val p = event.x / size.x
                val pos = (mMediaPlayer!!.getLength() / p) as Long
                Log.d(TAG, "seek to " + p + " / " + pos + " state is " + mMediaPlayer!!.getPlayerState())
                if (mMediaPlayer!!.isSeekable()) {
                    //mLibVLC.setTime( pos );
                    mMediaPlayer!!.setPosition(p)
                } else {
                    Log.w(TAG, "Non-seekable input")
                }
            }

            true
        } */
    }

    // TODO: handle this cleaner
    private fun releasePlayer() {
        if (libvlc == null)
            return
        mMediaPlayer!!.stop()
        val vout = mMediaPlayer!!.getVLCVout()
        vout.removeCallback(this)
        vout.detachViews()
        holder = null
        libvlc!!.release()
        libvlc = null

        mVideoWidth = 0
        mVideoHeight = 0
    }

    override fun onNewLayout(vout: IVLCVout, width: Int, height: Int, visibleWidth: Int, visibleHeight: Int, sarNum: Int, sarDen: Int) {
        if (width * height == 0)
            return

        // store video size
        mVideoWidth = width
        mVideoHeight = height
        setSize(mVideoWidth, mVideoHeight)
    }

    override fun onSurfacesCreated(vout: IVLCVout) {

    }

    override fun onSurfacesDestroyed(vout: IVLCVout) {

    }

    private class MyPlayerListener(owner: VideoActivity) : MediaPlayer.EventListener {
        private val mOwner: WeakReference<VideoActivity>

        init {
            mOwner = WeakReference(owner)
        }

        override fun onEvent(event: MediaPlayer.Event) {
            val player = mOwner.get()
            Log.d(TAG, "Player EVENT")
            when (event.type) {
                MediaPlayer.Event.EndReached -> {
                    Log.d(TAG, "MediaPlayerEndReached")
                    player!!.releasePlayer()
                }
                MediaPlayer.Event.EncounteredError -> Log.d(TAG, "Media Player Error, re-try")
                MediaPlayer.Event.Playing, MediaPlayer.Event.Paused, MediaPlayer.Event.Stopped -> {
                }
                else -> {
                }
            }//player.releasePlayer();
        }
    }


    override fun onHardwareAccelerationError(vout: IVLCVout) {
        // Handle errors with hardware acceleration
        Log.e(TAG, "Error with hardware acceleration")
        this.releasePlayer()
        Toast.makeText(this, "Error with hardware acceleration", Toast.LENGTH_LONG).show()
    }

    companion object {
        val TAG = "MyVlc/VideoActivity"

        val LOCATION = "com.balivo.myvlc.VideoActivity.location"
        private val VideoSizeChanged = -1
    }
}
