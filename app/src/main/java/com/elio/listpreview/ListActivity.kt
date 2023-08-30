package com.elio.listpreview

import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.elio.listpreview.helper.HelperClass
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
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
    private var floatingActionButton: FloatingActionButton? = null

    private var exampleList = arrayListOf("kotlin", "java", "javascript", "css", "php", "ruby", "golang")
    private var carsList = arrayListOf("fiat", "ferrari", "mercedes", "audi", "bmw", "lamborghini")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        listItemsRecycler = findViewById(R.id.listItems)
        val backButton = findViewById<TextView>(R.id.backButton)
        backButton.setOnClickListener { finish() }
        val profileButton = findViewById<TextView>(R.id.profileButton)
        profileButton.setOnClickListener {
            val openProfileActivity = Intent(this, ProfileActivity::class.java)
            startActivity(openProfileActivity)
        }
        val switchOption = findViewById<SwitchMaterial>(R.id.switchOption)
        switchOption.isChecked = false
        // load hardcoded data
        addAdapter(carsList)

        switchOption.setOnCheckedChangeListener { _, isChecked ->
            // first time we load hardcoded data for DB also in order to fill it
            // if is checked load data from Firebase else load other hardcoded data
            if (isChecked) {
                val userHasSeen = HelperClass().loadBoolean(applicationContext, "firstSaved", false)
                if (userHasSeen) {
                    readDataFromDB()
                } else {
                    writeDataToDB(exampleList)
                }
            } else {
                // load hardcoded data
                addAdapter(carsList)
            }

        }
        // floating button
        floatingActionButton = findViewById(R.id.floatingActionButton)
        floatingActionButton?.setOnClickListener {
            showDialog()
        }

        // init db
        database = Firebase.database
        // get reference of the db
        myDbRef = database.getReference("list")

    }

    override fun onResume() {
        super.onResume()
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
                    exampleList = stringList!!
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

    private fun writeDataToDB(exampleList: ArrayList<String>) {
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
            // user has seen the added value
            HelperClass().saveBoolean(applicationContext, "firstSaved", true)
            myDbRef?.setValue(arrayList)
            // read db
            readDataFromDB()
        } catch (e : Exception) {
            e.printStackTrace()
        }
    }

    // create adapter and load the view
    fun addAdapter(stringList: ArrayList<String>) {
        // clear recycler
        listItemsRecycler?.invalidate()
        listItemsRecycler?.layoutManager = GridLayoutManager(applicationContext, 1)
        listAdapter = ListAdapter(stringList)
        // add horizontal line
        listItemsRecycler?.addItemDecoration(DividerItemDecoration(applicationContext, DividerItemDecoration.VERTICAL))
        listItemsRecycler?.itemAnimator = DefaultItemAnimator()
        listItemsRecycler?.adapter = listAdapter
    }

    // add new value on db
    private fun showDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.custom_pop_up_dialog)
        val enteredText = dialog.findViewById(R.id.enteredValue) as EditText
        val stringCounter = dialog.findViewById(R.id.stringCounter) as TextView
        val saveButton = dialog.findViewById(R.id.saveButton) as Button
        // adding max chars for editText
        enteredText.maxEms = 40
        enteredText.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) { }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    stringCounter.visibility = View.VISIBLE
                    val counter = s?.length.toString()
                    stringCounter.text = "$counter/40"
                } else {
                    stringCounter.visibility = View.INVISIBLE
                }
            }
        })

        // logic for the save button
        saveButton.setOnClickListener {
            if (enteredText.text.toString().isNotEmpty()) {
                // add value to db
                exampleList.add(enteredText.text.toString())
                writeDataToDB(exampleList)
            } else {
                Toast.makeText(applicationContext, "No value was saved", Toast.LENGTH_LONG).show()
            }
            dialog.dismiss()
        }
        dialog.show()
    }

}