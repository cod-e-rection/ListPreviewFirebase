package com.elio.listpreview

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class ListActivity : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private var listAdapter : ListAdapter? = null
    private var stringList: ArrayList<String>? = null
    private var myDbRef : DatabaseReference? = null
    private var listItemsRecycler: RecyclerView? = null
    private lateinit var auth: FirebaseAuth

    private var exampleList = arrayOf("kotlin", "java", "javascript", "css", "php", "ruby", "golang")
    private var carsList = arrayListOf("fiat", "ferrari", "mercedes", "audi", "bmw", "lamborghini")

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
        switchOption.isChecked = true
        switchOption.setOnCheckedChangeListener { _, isChecked ->
            // if is checked load data from Firebase else load hardcoded
            if (isChecked) {
                writeDataToDB()
                listItemsRecycler?.visibility = View.VISIBLE
            } else {
                addAdapter(carsList)
            }

        }
        listItemsRecycler = findViewById(R.id.listItems)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // init db
        database = Firebase.database
        // get reference of the db
        myDbRef = database.getReference("list")



    }

    override fun onResume() {
        super.onResume()
        writeDataToDB()
        swipeOption()
    }

    // swipe option on recyclerView
    private fun swipeOption () {
        val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                // display the name of the removed from the list
                Toast.makeText(this@ListActivity, "Deleted : ${
                    stringList?.get(viewHolder.adapterPosition).toString()}", Toast.LENGTH_SHORT).show()
                // Remove swiped item from list and notify the RecyclerView
                val position = viewHolder.adapterPosition
                stringList?.removeAt(position)
                listAdapter?.notifyItemRemoved(position)
            }
        }
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(listItemsRecycler)
    }

    // read db
    private fun readDataFromDB() {
        try {
            // Read from the database
            myDbRef?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // This method is called once with the initial value and again whenever data at location is updated
                    stringList = ((dataSnapshot.value) as? ArrayList<String>)
                    // load the screen
                    addAdapter(stringList!!)
                }
                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException())
                }
            })

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun writeDataToDB() {
        try {
            var i = 0
            val arrayList = arrayListOf<String>()
            // read all hardcoded items
            while (i < exampleList.size) {
                val name = exampleList[i]
                arrayList.add(name)
                i++
            }

            // Write a list to the database // the first time the user loads the app
            if (auth.currentUser?.email != null) {
                readDataFromDB()
            } else {
                // add hardcoded data's
                myDbRef?.setValue(arrayList)
            }
            // read db
            readDataFromDB()
        } catch (e : Exception) {
            e.printStackTrace()
        }
    }

    // create adapter and load the view
    fun addAdapter(stringList: ArrayList<String>) {

        listItemsRecycler?.layoutManager = GridLayoutManager(applicationContext, 1)
        val itemAnimator = DefaultItemAnimator()
        listItemsRecycler?.itemAnimator = itemAnimator

        listAdapter = ListAdapter(stringList)
        // add line
        listItemsRecycler?.addItemDecoration(DividerItemDecoration(applicationContext, DividerItemDecoration.VERTICAL))
        listItemsRecycler?.itemAnimator = DefaultItemAnimator()
        listItemsRecycler?.adapter = listAdapter
    }


}