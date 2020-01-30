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

class RegisterActivity : AppCompatActivity() {
    private var CreateAccountButton: Button? = null
    private var UserEmail: EditText? = null
    private var UserPassword: EditText? = null
    private var AlreadyHaveAccountLink: TextView? = null

    private var mAuth: FirebaseAuth? = null
    private var RootRef: DatabaseReference? = null
    private var loadingBar: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        mAuth = FirebaseAuth.getInstance()
        RootRef = FirebaseDatabase.getInstance().reference
        InitializeFields()

        AlreadyHaveAccountLink!!.setOnClickListener { SendUserToLoginActivity() }
        CreateAccountButton!!.setOnClickListener { CreateNewAccount() }



    }

    private fun CreateNewAccount() {
        val email = UserEmail!!.text.toString()
        val password = UserPassword!!.text.toString()
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter email....", Toast.LENGTH_SHORT).show()
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter password....", Toast.LENGTH_SHORT).show()
        } else {
            loadingBar!!.setTitle("Creating New Account")
            loadingBar!!.setMessage("Please wait, while we are creating new account for you...")
            loadingBar!!.setCanceledOnTouchOutside(true)
            loadingBar!!.show()

            mAuth!!.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val deviceToken = FirebaseInstanceId.getInstance().token


                    val currentUserID = mAuth!!.currentUser!!.uid
                    RootRef!!.child("Users").child(currentUserID).setValue("")

                    RootRef!!.child("Users").child(currentUserID).child("device_token")
                        .setValue(deviceToken)
                    SendUserToMainActivity()
                    Toast.makeText(this@RegisterActivity, "Account created Successfully...", Toast.LENGTH_SHORT).show()
                    loadingBar!!.dismiss()
                } else {
                    val message = task.exception!!.toString()
                    Toast.makeText(this@RegisterActivity, "Error :$message", Toast.LENGTH_SHORT).show()
                    loadingBar!!.dismiss()
                }
            }
        }
    }

    private fun InitializeFields() {
        CreateAccountButton = findViewById(R.id.register_button)
        UserEmail = findViewById(R.id.register_email)
        UserPassword = findViewById(R.id.register_password)
        AlreadyHaveAccountLink = findViewById(R.id.already_have_account_link)
        loadingBar = ProgressDialog(this)

    }

    private fun SendUserToLoginActivity() {
        val loginIntent = Intent(this@RegisterActivity, SigninActivity::class.java)
        startActivity(loginIntent)
    }

    private fun SendUserToMainActivity() {
        val mainIntent = Intent(this@RegisterActivity, MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        startActivity(mainIntent)
        finish()
    }
}
