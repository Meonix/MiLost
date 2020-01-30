package com.example.milost

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.IBinder
import android.provider.Settings
import com.google.firebase.database.*


public class Service :Service(){

    private var medialayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var NoitificationRef: DatabaseReference? = null
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        NoitificationRef = FirebaseDatabase.getInstance().reference.child("Notifications").child("Mi")
        NoitificationRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.value=="alarm"){
                    medialayer = MediaPlayer.create(this@Service, Settings.System.DEFAULT_ALARM_ALERT_URI)
                    if(medialayer!!.isPlaying)
                    {
                        medialayer!!.isLooping = false
                        medialayer!!.stop()
                    }
                    audioManager = applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    medialayer!!.isLooping = true
                    medialayer!!.start()
//        val mainHandler = Handler(Looper.getMainLooper())

//        mainHandler.post(object : Runnable {
//            override fun run() {
                    audioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, 100, 0)
//                mainHandler.postDelayed(this, 2000)
//            }
//        })
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })



        return START_REDELIVER_INTENT + START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        medialayer!!.stop()
        medialayer!!.stop()
    }

}