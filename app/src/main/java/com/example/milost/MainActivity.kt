package com.example.milost

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlin.math.log

class MainActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var buttonAlarm: Button? = null
    private var buttonStop: Button? = null
    private var buttonStart: Button? = null
    private var currentUser: FirebaseUser? = null
    private var RootRef: DatabaseReference? = null
    private var NoitificationRef: DatabaseReference? = null
    private var mToolbar: Toolbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth!!.currentUser
        RootRef = FirebaseDatabase.getInstance().reference
        NoitificationRef = FirebaseDatabase.getInstance().reference.child("Notifications").child("Mi")
        InitializeFields()

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
    }

    private fun ChangeMessage(key : String?){
        if(key=="stop")
        {

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
}
