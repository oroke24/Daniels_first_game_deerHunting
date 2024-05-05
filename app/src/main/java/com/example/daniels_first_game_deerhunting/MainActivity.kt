package com.example.daniels_first_game_deerhunting
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {

    private lateinit var recordListView: ListView
    private lateinit var classicVersionButton: Button
    private lateinit var timedVersionButton: Button
    data class Record(val username: String, val score: Int)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recordListView = findViewById(R.id.recordListView)
        classicVersionButton = findViewById(R.id.version1Button)
        timedVersionButton = findViewById(R.id.timedVersionButton)

        classicVersionButton.setOnClickListener {
            startActivity(Intent(this, Version1::class.java))
        }
        timedVersionButton.setOnClickListener {
            startActivity(Intent(this, TimedVersion::class.java))
        }
        populateListView()
    }

    override fun onResume() {
        super.onResume()
        populateListView()
    }
    private fun populateListView() {
        val sharedPreferences = getSharedPreferences("records", Context.MODE_PRIVATE)
        val recordListJson = sharedPreferences.getString("recordList", null)
        val gson = Gson()
        val recordList = if (recordListJson != null) {
            gson.fromJson<ArrayList<Record>>(recordListJson, object : TypeToken<ArrayList<Record>>() {}.type)
        } else {
            ArrayList() // Initialize as an empty ArrayList
        }
        recordList.sortByDescending { it.score }
        val recordStrings = ArrayList<String>()
        for (record in recordList) {
            val recordString = "User: ${record.username}, Score: ${record.score}"
            recordStrings.add(recordString)
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, recordStrings)
        recordListView.adapter = adapter
    }
}
