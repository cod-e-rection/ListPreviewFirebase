package com.elio.listpreview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth


class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()


        val backButton = findViewById<TextView>(R.id.backButton)
        backButton.setOnClickListener { finish() }
        val emailDesc = findViewById<TextView>(R.id.emailDesc)
        emailDesc.text = getData()
        val logOutButton = findViewById<Button>(R.id.logOutButton)
        logOutButton.setOnClickListener {
            // LoginRepository().logout()
            FirebaseAuth.getInstance().signOut()
            finish()
        }

    }

    // get email data
    private fun getData() : String {
        val user = auth.currentUser
        var email = ""
        user?.let {
            // Name, email address
            //val name = it.displayName
            email = it.email.toString()
        }

        return email
    }

}