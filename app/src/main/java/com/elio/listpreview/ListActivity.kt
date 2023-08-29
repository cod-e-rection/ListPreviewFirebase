package com.elio.listpreview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.ArrayList


class ListActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private var listAdapter : ListAdapter? = null
    private var stringList = ArrayList<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        val backButton = findViewById<TextView>(R.id.backButton)
        backButton.setOnClickListener { finish() }
        val profileButton = findViewById<TextView>(R.id.profileButton)
        profileButton.setOnClickListener {
            val openProfileActivity = Intent(this, ProfileActivity::class.java)
            startActivity(openProfileActivity)
        }
        val switchOption = findViewById<SwitchMaterial>(R.id.switchOption)
        val listItemsRecycler = findViewById<RecyclerView>(R.id.listItems)


        database = Firebase.database.reference

//        // Read from the database
//        myRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                // This method is called once with the initial value and again
//                // whenever data at this location is updated.
//                val value = dataSnapshot.getValue<String>()
//                Log.d(TAG, "Value is: $value")
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Failed to read value
//                Log.w(TAG, "Failed to read value.", error.toException())
//            }
//        })

        //write(database)
    }

    fun addAdapter(recyclerView: RecyclerView) {

        recyclerView.layoutManager = GridLayoutManager(applicationContext, 1)
        recyclerView.setPadding(0, 0, 0, 0)
        val itemAnimator = DefaultItemAnimator()
        recyclerView.itemAnimator = itemAnimator

        listAdapter = ListAdapter(stringList)
        // add horizontal line
        recyclerView.addItemDecoration(DividerItemDecoration(applicationContext, DividerItemDecoration.VERTICAL))
        recyclerView.itemAnimator = DefaultItemAnimator()

        recyclerView.adapter = listAdapter
    }


    fun writeMessage() {
        // Write a message to the database
        val database = Firebase.database
        val myRef = database.getReference("message")
        myRef.setValue("string 1")
    }


}