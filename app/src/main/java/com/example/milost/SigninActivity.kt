package com.example.milost

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId

class SigninActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var UserEmail: EditText? = null
    private var registerButton: Button? = null
    private var loginButton: Button? = null
    private var loadingBar: ProgressDialog? = null
    private var UserPassword: EditText? = null
    private var UsersRef: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        mAuth = FirebaseAuth.getInstance()
        UsersRef = FirebaseDatabase.getInstance().reference.child("Users")

        InitializeFields()

        registerButton!!.setOnClickListener{ SendUserToRegisterActivity() }

        loginButton!!.setOnClickListener{ AllowUserToLogin() }
    }
    private fun SendUserToRegisterActivity() {
        val registerIntent = Intent(this@SigninActivity, RegisterActivity::class.java)
        startActivity(registerIntent)
    }
    private fun SendUserToMainActivity() {
        val mainIntent = Intent(this@SigninActivity, MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        startActivity(mainIntent)
        finish()
    }

    private fun AllowUserToLogin() {
        val email = UserEmail!!.text.toString()
        val password = UserPassword!!.text.toString()
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter email....", Toast.LENGTH_SHORT).show()
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter password....", Toast.LENGTH_SHORT).show()
        } else {
            loadingBar!!.setTitle("Sign In")
            loadingBar!!.setMessage("Please wait....")
            loadingBar!!.setCanceledOnTouchOutside(true)
            loadingBar!!.show()

            mAuth!!.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val CurrentUserID = mAuth!!.currentUser!!.uid
                    val deviceToken = FirebaseInstanceId.getInstance().token

                    UsersRef!!.child(CurrentUserID).child("device_token")
                        .setValue(deviceToken)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                SendUserToMainActivity()
                                Toast.makeText(this@SigninActivity, "Logged in  Successful....", Toast.LENGTH_SHORT).show()
                                loadingBar!!.dismiss()
                            }
                        }
                } else {
                    val message = task.exception!!.toString()
                    Toast.makeText(this@SigninActivity, "Error :$message", Toast.LENGTH_SHORT).show()
                    loadingBar!!.dismiss()
                }
            }
        }
    }

    private fun InitializeFields() {
        registerButton = findViewById(R.id.btRegister)
        loginButton =findViewById(R.id.btLogin)
        UserEmail = findViewById(R.id.tietEmailLogin)
        UserPassword = findViewById(R.id.tietPasswordLogin)

        loadingBar = ProgressDialog(this)
    }
}
