package com.elio.listpreview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button


class HomeActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val listButton = findViewById<Button>(R.id.listButton)
        listButton.setOnClickListener {
            // open list screen
            val openListActivity = Intent(this, ListActivity::class.java)
            startActivity(openListActivity)
        }

    }
}