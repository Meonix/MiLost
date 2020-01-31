package com.example.milost

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.log
import android.R.attr.key
import android.app.ProgressDialog
import androidx.core.app.NotificationCompat.getExtras
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File


class MainActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var buttonAlarm: Button? = null
    private var buttonStop: Button? = null
    private var buttonStart: Button? = null
    private var btChangeMusic: Button? = null
    private var tvMain: TextView? = null
    private val MyPick = 2
    private var srMusic: StorageReference? = null
    private var medialayer: MediaPlayer? = null
    private var currentUser: FirebaseUser? = null
    private var RootRef: DatabaseReference? = null
    private var NoitificationRef: DatabaseReference? = null
    private var mToolbar: Toolbar? = null
    private var mProgress: ProgressDialog? = null
    private var UriAudio:Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mProgress = ProgressDialog(this@MainActivity)
        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth!!.currentUser
        RootRef = FirebaseDatabase.getInstance().reference
        NoitificationRef = FirebaseDatabase.getInstance().reference.child("Notifications").child("Mi")
        srMusic = FirebaseStorage.getInstance().reference.child("Music")
        InitializeFields()
        NoitificationRef!!.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                    tvMain!!.text = p0.value.toString()
                if(p0.value=="alarm"&&currentUser!= null &&currentUser!!.uid != "8es7wtcTI6Q0YQYxmZQD6i9DDvC3"){
                    btStop.visibility= View.INVISIBLE
                }
                else{
                    btStop.visibility= View.VISIBLE
                }
            }

        })
//        mToolbar = findViewById(R.id.main_app_bar)
//        setSupportActionBar(mToolbar!!)
//        supportActionBar!!.title = "Mi Lost"
        buttonStart!!.visibility = View.INVISIBLE
        if(currentUser!= null &&currentUser!!.uid != "8es7wtcTI6Q0YQYxmZQD6i9DDvC3"){
                buttonAlarm!!.visibility = View.INVISIBLE
                buttonStart!!.visibility = View.VISIBLE
        }

        buttonAlarm!!.setOnClickListener {
                if(NoitificationRef!!.key.toString() == "stop"){
                    NoitificationRef!!
                        .setValue("alarm")
                }
                NoitificationRef!!
                    .setValue("alarm")
        }
        if(currentUser!= null &&currentUser!!.uid == "8es7wtcTI6Q0YQYxmZQD6i9DDvC3"){
            buttonStop!!.setOnClickListener {
                NoitificationRef!!.setValue("stop")
            }
        }
        else{
            NoitificationRef!!.addValueEventListener(object :ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.value=="stop"){
//                        stopService(Intent(this@MainActivity,Service::class.java))
                    }
                }

            })
            buttonStop!!.setOnClickListener {
                actionOnService(Action.STOP)
            }
        }

        buttonStart!!.setOnClickListener{
//            startService(Intent(this@MainActivity,Service::class.java))
//            val intent = Intent("com.android.ServiceStopped")
//            sendBroadcast(intent)
            actionOnService(Action.START
            )
        }

        btChangeMusic!!.setOnClickListener {
            val GIntent = Intent()
            GIntent.action = Intent.ACTION_GET_CONTENT
            GIntent.type = "*/*"
            startActivityForResult(GIntent, MyPick)
        }
    }

    override fun onStart() {
        super.onStart()
        if (currentUser == null) {
            sendUserToLoginActivity()
        } else {
            verifyUserExistance()
        }
    }

    private fun sendUserToLoginActivity() {
        val loginIntent = Intent(this@MainActivity, SigninActivity::class.java)
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(loginIntent)
        finish()
    }

    private fun verifyUserExistance() {
        val currentUerID = mAuth!!.currentUser!!.uid

        RootRef!!.child("Users").child(currentUerID).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child("name").exists()) {

                    Toast.makeText(this@MainActivity
                        , "Welcome" + "  " + dataSnapshot
                            .child("name")
                            .value!!
                            .toString()
                        , Toast.LENGTH_SHORT).show()

                }
                else {
//                    SendUserToSettingsActivity()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }
    private fun InitializeFields() {
            buttonAlarm = findViewById(R.id.btArlam)
            buttonStop = findViewById(R.id.btStop)
            buttonStart = findViewById(R.id.btStart)
            tvMain = findViewById(R.id.tvMain)
            btChangeMusic = findViewById(R.id.btChangeMusic)
    }

    private fun actionOnService(action: Action) {
        if (getServiceState(this) == ServiceState.STOPPED && action == Action.STOP) return
        Intent(this, EndlessService::class.java).also {
            it.action = action.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                log("Starting the service in >=26 Mode")
                startForegroundService(it)
                return
            }
//            log("Starting the service in < 26 Mode")
            startService(it)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MyPick && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            UriAudio = data.data!!
            val cr = this.contentResolver
            val Type = cr.getType(UriAudio!!)

            if (Type!!.contains("audio/")) {
                if (UriAudio != null) {
                    mProgress!!.setMessage("Sending Your Audio....")
                    mProgress!!.show()
                    val filepath = srMusic!!.child("music.mp3")
                    filepath.putFile(UriAudio!!).addOnCompleteListener{ task ->
                        val AudiodownloadUrl = task.result.downloadUrl.toString()
                        RootRef!!.child("Music").child("song").setValue(AudiodownloadUrl)

                    }
                    srMusic!!.path

                    Toast.makeText(applicationContext, "done", Toast.LENGTH_LONG).show()
                    mProgress!!.dismiss()
                }
            }
        }
    }

}
