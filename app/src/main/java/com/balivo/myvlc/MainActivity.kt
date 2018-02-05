package com.balivo.myvlc

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.BaseAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.channel_ticket.view.*
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : Activity() {

    internal var mAdapter: DirectoryAdapter? = null
    internal var mLibVLC: LibVLC? = null
    internal var mMediaPlayer: MediaPlayer? = null

    val parser = M3UParser()
    var playlist: M3UPlaylist?=null

    var listOfChannels=ArrayList<M3UItem>()
    var adapter:ChannelAdapter?=null

    internal var mPlayingVideo = false // Don't destroy libVLC if the video activity is playing.

    /**
     * Demonstrates how to play a certain media at a given path.
     * TODO: demonstrate other LibVLC features like media lists, etc.
     */
    private fun playMediaAtPath(path: String) {
        // To play with LibVLC, we need a media player object.
        // Let's get one, if needed.
        if (mMediaPlayer == null)
            mMediaPlayer = MediaPlayer(mLibVLC)

        // Create a new Media object for the file.
        // Each media - a song, video, or stream is represented by a Media object for LibVLC.
        val m = Media(mLibVLC, path)

        // Tell the media player to play the new Media.
        mMediaPlayer!!.setMedia(m)

        // Finally, play it!
        mMediaPlayer!!.play()
    }

    class ChannelAdapter:BaseAdapter{

        //var listOfChannels=ArrayList<Channel>()
        //var listOfChannels=ArrayList<Channel>()
        var listOfChannels=ArrayList<M3UItem>()
        var context:Context?=null

        constructor(context: Context, listOfChannels:ArrayList<M3UItem>):super(){
            this.listOfChannels=listOfChannels
            this.context=context
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val channel = listOfChannels[position]
            var inflator = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            var myView = inflator.inflate(R.layout.channel_ticket, null)
            myView.tvName.text = channel.itemName.toString()
            myView.tvDes.text = channel.itemTitle.toString()
            myView.ivImage.setImageResource(R.drawable.iptv)
            myView.linearLayoutID.setOnClickListener{
                val intent = Intent(context, VideoActivity::class.java)
                intent.putExtra(VideoActivity.LOCATION, channel.itemUrl.toString())
                context!!.startActivity(intent)

            }
            return myView
        }

        override fun getItem(position: Int): Any {
            return listOfChannels[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return listOfChannels.size
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize the LibVLC multimedia framework.
        // This is required before doing anything with LibVLC.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load Playlist from url -  channels
        val url = "http://play.3mux.ml/555abc333abc"
        MyAsyncTask().execute(url)

        /*    // Load dummy channels
        listOfChannels.add(Channel("Канал 1", "Денят започва с култура", R.drawable.bnt1,"4001"))
        listOfChannels.add(Channel("bTV", "Черешката на тортата", R.drawable.btv,"4002"))
        listOfChannels.add(Channel("Nova", "Господари на ефира", R.drawable.novatv,"4003"))
        listOfChannels.add(Channel("HBO 1", "Терминатор 3", R.drawable.hbo,"5232"))
        listOfChannels.add(Channel("HBO 2", "Star Wars - Episode 2", R.drawable.hbo2,"5409"))
        listOfChannels.add(Channel("bTV Action", "Шампионска лига: Лудогорец - Милан", R.drawable.btvaction,"5330"))
        listOfChannels.add(Channel("Diema Sport 1", "Премиер лийг: Арсенал - Евертън", R.drawable.diemasport,"5151"))
        listOfChannels.add(Channel("Diema Sport 2", "Тенис, Ролан Гарос - директно от Париж", R.drawable.diemasport2,"5152"))
        listOfChannels.add(Channel("Eurosport", "Биатлон - Световната купа, директно от Хелзинки, Финландия", R.drawable.eurosport,"5123"))
        listOfChannels.add(Channel("Eurosport 2", "Ски скокове - директно от Гремио", R.drawable.eurosport2,"5144"))
        listOfChannels.add(Channel("Fashion TV", "Fashion week in Milano, Italy", R.drawable.fashiontv,"5164"))
*/
        try {
            mLibVLC = LibVLC()
        } catch (e: IllegalStateException) {
            Toast.makeText(this@MainActivity,
                    "Error initializing the libVLC multimedia framework!",
                    Toast.LENGTH_LONG).show()
            finish()
        }

            Toast.makeText(this, listOfChannels.size.toString(), Toast.LENGTH_LONG).show()
            adapter = ChannelAdapter(this, listOfChannels)
            lvListChannel.adapter = adapter

//        val intent = Intent(this@MainActivity, VideoActivity::class.java)
//        //intent.putExtra(VideoActivity.LOCATION, "http://dl.strem.io/BigBuckBunny_512kb.mp4");
//        // http://play.3mux.ml/555abc333abc/4380
//        // http://mediaserverdemo.leadtools.com/vod/sample.mp4
//        // intent.putExtra(VideoActivity.LOCATION, "http://mediaserverdemo.leadtools.com/vod/sample.mp4");
//        //intent.putExtra(VideoActivity.LOCATION, "http://play.3mux.ml/555abc333abc/4380")
//        //intent.putExtra(VideoActivity.LOCATION, "http://play.3mux.ml/555abc333abc/")
//        intent.putExtra(VideoActivity.LOCATION, "http://play.3mux.ml/555abc333abc/4235")
//        startActivity(intent)
    }

    inner class MyAsyncTask: AsyncTask<String, String, String>() {

        override fun onPreExecute() {
            //Before task started
        }
        override fun doInBackground(vararg p0: String?): String {
            try {

                val url= URL(p0[0])

                val urlConnect=url.openConnection() as HttpURLConnection
                urlConnect.connectTimeout=7000



                var inString= ConvertStreamToString(urlConnect.inputStream)


                // return inString
                //Cannot access to ui
                //publishProgress(inString)
            }catch (ex:Exception){}


            return " "

        }

        override fun onProgressUpdate(vararg values: String?) {

        }

        override fun onPostExecute(result: String?) {

            //after task done

            try {
                playlist = parser.parseFile(result!![0].toString())

                listOfChannels = playlist!!.playlistItems as ArrayList<M3UItem>

            }catch (ex:Exception){}
        }


    }


    fun ConvertStreamToString(inputStream: InputStream):String{

        val bufferReader= BufferedReader(InputStreamReader(inputStream))
        var line:String
        var AllString:String=""

        try {
            do{
                line=bufferReader.readLine()
                if(line!=null){
                    AllString+=line
                }
            }while (line!=null)
            inputStream.close()
        }catch (ex:Exception){}



        return AllString
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                Log.d(TAG, "Setting item selected.")
                return true
            }
            R.id.action_refresh -> {
                mAdapter!!.refresh()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    companion object {
        val urlIPTV = "http://play.3mux.ml/555abc333abc/"
        val TAG = "MyVlc/MainActivity"
    }
}