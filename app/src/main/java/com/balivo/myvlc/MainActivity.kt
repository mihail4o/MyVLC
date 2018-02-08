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
import com.bumptech.glide.Glide
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.channel_ticket.view.*
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

class MainActivity : Activity() {

    internal var mAdapter: DirectoryAdapter? = null
    internal var mLibVLC: LibVLC? = null
    internal var mMediaPlayer: MediaPlayer? = null

    val parser = M3UParser()
    var playlist: M3UPlaylist?=null

   // var listOfChannels=ArrayList<M3UItem>()
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

    fun getChID(channel: String):Int {

        val id = getResources().getIdentifier(channel, "drawable", getPackageName())
        return id
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

            //Check google for Picasso Android!!!
            // Add to build.gradle: [compile 'com.squareup.picasso:picasso:2.5.2']

            var chLogo = ""

            when(channel.itemID.trim().toLowerCase()){
                "nova" -> chLogo ="http://logos.kodibg.org/novatv.png"
                "eurocom" -> chLogo ="http://logos.kodibg.org/evrokom.png"
                "tveurope" -> chLogo ="http://logos.kodibg.org/tveuropa.png"
                "comedycentral" -> chLogo ="http://logos.kodibg.org/comedycentralextra.png"
                "bulgariaonair" -> chLogo ="http://logos.kodibg.org/bgonair.png"
                "filmboxarthousehd" -> chLogo ="http://logos.kodibg.org/filmboxarthouse.png"
                "hbohd"  -> chLogo ="http://logos.kodibg.org/hbo.png"
                "eurosport1"-> chLogo ="http://logos.kodibg.org/eurosport.png"

                else -> chLogo = "http://logos.kodibg.org/" + channel.itemID.trim().toLowerCase() + ".png"
            }

            when(channel.itemName.trim().toLowerCase()){
                "nktv evrokom" -> chLogo ="http://logos.kodibg.org/evrokom.png"
                "diva universal" -> chLogo ="http://logos.kodibg.org/diva.png"
                "kanal 4" -> chLogo ="http://logos.kodibg.org/kanal4.png"
                "moviestar" -> chLogo ="http://logos.kodibg.org/moviestar.png"
                "arena sport 1" -> chLogo ="http://www.tvarenasport.com/images/logo-arenaA1.png"
                "arena sport 2" -> chLogo ="http://www.tvarenasport.com/images/logo-arenaA2.png"
                "arena sport 3" -> chLogo ="http://www.tvarenasport.com/images/logo-arenaA3.png"
                "arena sport 4" -> chLogo ="http://www.tvarenasport.com/images/logo-arenaA4.png"
                "arena sport 5" -> chLogo ="http://www.tvarenasport.com/images/logo-arenaA5.png"
                "mat4! hd" -> chLogo = "https://getsiptv.ru/img/ch_icons/match_tv.png"
                "boec" -> chLogo = "http://tivix.co/uploads/posts/2014-02/1392653251_boets.png"
            }

// TODO: Remove Picasso module from Gradle, when done!

            Glide.with(context!!)
                    .load(chLogo)
                    //.placeholder(R.drawable.iptv)
                    //.error(R.drawable.iptv)
                    //.resize(900,0)
                    //.resize(50, 50)
                    //.centerCrop()
                    //.fit()
                    //.rotate(270F)
                    .into(myView.ivImage)


            myView.linearLayoutID.setOnClickListener{
                val intent = Intent(context, VideoActivity::class.java)
                intent.putExtra(VideoActivity.LOCATION, channel.itemUrl)
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


           // Load dummy channels
       /* listOfChannels.add(M3UItem("BNT1", "Канал 1", "http://play.3mux.ml/555abc333abc/4001", "Политематични"))
        listOfChannels.add(M3UItem("bTV","bTV","http://play.3mux.ml/555abc333abc/4002", "Политематични" ))
        listOfChannels.add(M3UItem("Nova","Nova TV","http://play.3mux.ml/555abc333abc/4003", "Политематични" ))
        listOfChannels.add(M3UItem("HBO1","HBO 1","http://play.3mux.ml/555abc333abc/5232", "Филми" ))
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

        // Load Playlist from url -  channels
        val url = "http://play.3mux.ml/555abc333abc"
        if (listOfChannels.size==0) {
            MyAsyncTask().execute(url)
        }else {
            Toast.makeText(this, listOfChannels.size.toString(), Toast.LENGTH_LONG).show()
            adapter = ChannelAdapter(this, listOfChannels)
            lvListChannel.adapter = adapter
            }

        
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

                //Cannot access to ui
                publishProgress(inString)
            }catch (ex:Exception){}

            return " "
        }

        override fun onProgressUpdate(vararg values: String?) {

                playlist = parser.parseFile(values!![0].toString())

                //listOfChannels. = playlist!!.playlistItems as ArrayList<M3UItem>
                //Log.d("List of Channels: ", listOfChannels.toString())
            listOfChannels.addAll(playlist!!.playlistItems as ArrayList<M3UItem>)

            Toast.makeText(this@MainActivity, listOfChannels.size.toString(), Toast.LENGTH_LONG).show()
            adapter = ChannelAdapter(this@MainActivity, listOfChannels)
            lvListChannel.adapter = adapter

        }

        override fun onPostExecute(result: String?) {

            //after task done
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
        var listOfChannels=ArrayList<M3UItem>()
        val urlIPTV = "http://play.3mux.ml/555abc333abc/"
        val TAG = "MyVlc/MainActivity"
    }
}
