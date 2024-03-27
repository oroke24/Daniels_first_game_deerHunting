package com.example.daniels_first_game_deerhunting
import android.app.AlertDialog
import android.content.Context
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

    private var score = 0
    private var timer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 60000 // 1 minute
    private val deerHitPoints = 1 // Changed to increment score by 1
    private lateinit var deerImageView: ImageView
    private lateinit var scoreTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var recordListView: ListView
    private lateinit var startButton: Button
    private var gameStarted = false
    data class Record(val username: String, val score: Int)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        deerImageView = findViewById(R.id.deerImageView)
        scoreTextView = findViewById(R.id.scoreTextView)
        timerTextView = findViewById(R.id.timerTextView)

        startButton = findViewById(R.id.startButton)
        // Initialize ListView
        recordListView = findViewById(R.id.recordListView)
        deerImageView.visibility=View.GONE

        // Set up onClickListener for the start button
        startButton.setOnClickListener {
            startButton.visibility = View.GONE
            deerImageView.visibility = View.VISIBLE
            // Show countdown dialog to start the game
            showCountdownDialog()
        }
        // Populate ListView with data
        populateListView()

        // Set up onClickListener for the deer image
        deerImageView.setOnClickListener {
            if (gameStarted) {
                // Increment score when the deer is clicked
                score += deerHitPoints
                updateScore()
                // Randomly move the deer
                moveDeerRandomly()
            }
        }
    }

    private fun showCountdownDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_countdown, null)
        val countdownTextView = dialogView.findViewById<TextView>(R.id.countdownTextView)


        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(dialogView)
        val dialog = dialogBuilder.create()
        dialog.setCancelable(false)

        val countDownTimer = object : CountDownTimer(3000, 1000) { // 3-second countdown
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000 + 1 // Add 1 to show 3, 2, 1 instead of 2, 1, 0
                countdownTextView.text = secondsLeft.toString()
            }

            override fun onFinish() {
                dialog.dismiss()
                startGame()
            }
        }
        dialog.setOnShowListener {
            countDownTimer.start()
        }
        dialog.show()
    }

    private fun startGame() {
        // Reset score and update UI
        score = 0
        updateScore()
        gameStarted = true

        // Start the countdown timer
        startTimer()
    }

    private fun startTimer() {
        timer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()
            }

            override fun onFinish() {
                timeLeftInMillis = 0
                updateTimerText()
                endGame()
            }
        }.start()
    }

    private fun endGame() {
        // Display final score and allow restarting the game
        gameStarted = false
        timer = null

        // Show enter record dialog
        showEnterRecordDialog()
    }

    private fun updateTimerText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeLeftFormatted = String.format("%02d:%02d", minutes, seconds)
        timerTextView.text = "$timeLeftFormatted"
    }

    private fun updateScore() {
        // Update the score text view
        scoreTextView.text = "$score pts"
    }

    private fun moveDeerRandomly() {
        // Randomly move the deer to a new position within the layout
        val randomX = (0..(deerImageView.parent as View).width - deerImageView.width).random()
        val randomY = (0..(deerImageView.parent as View).height - deerImageView.height).random()
        deerImageView.x = randomX.toFloat()
        deerImageView.y = randomY.toFloat()

        // Change the size of the deer by a small amount
        val scaleFactor = 0.8f + (Math.random() * 0.4f).toFloat() // Adjust the range as needed
        deerImageView.scaleX = scaleFactor
        deerImageView.scaleY = scaleFactor
    }

    private fun showEnterRecordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_enter_record, null)
        val usernameEditText = dialogView.findViewById<EditText>(R.id.usernameEditText)
        val saveButton = dialogView.findViewById<Button>(R.id.saveButton)

        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(dialogView)
        val dialog = dialogBuilder.create()
        dialog.setCancelable(false)

        saveButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            if (username.isNotEmpty()) {
                // Save record locally (You can implement your own local storage mechanism)
                saveRecordLocally(username, score)
                populateListView()
                dialog.dismiss()
                // Restarting game
                resetGame()
            } else {
                usernameEditText.error = "Username cannot be empty"
            }
        }

        dialog.show()
    }
    private fun resetGame() {
        // Reset any game-related variables or UI elements
        startButton.visibility = View.VISIBLE
        score = 0
        updateScore()
        gameStarted = false
        timer?.cancel()
        timeLeftInMillis = 60000L
        // You may also need to reset the timer text and other game-related UI elements
    }


    private fun saveRecordLocally(username: String, score: Int) {
        val sharedPreferences = getSharedPreferences("records", Context.MODE_PRIVATE)
        val recordListJson = sharedPreferences.getString("recordList", null)
        val gson = Gson()
        val recordList = if (recordListJson != null) {
            gson.fromJson<ArrayList<Record>>(recordListJson, object : TypeToken<ArrayList<Record>>() {}.type)
        } else {
            ArrayList() // Initialize as an empty ArrayList
        }
        recordList.add(Record(username, score))
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString("recordList", gson.toJson(recordList))
        editor.apply()
    }
    // Declare ListView

    private fun populateListView() {
        val sharedPreferences = getSharedPreferences("records", Context.MODE_PRIVATE)
        val recordListJson = sharedPreferences.getString("recordList", null)
        val gson = Gson()
        val recordList = if (recordListJson != null) {
            gson.fromJson<ArrayList<Record>>(recordListJson, object : TypeToken<ArrayList<Record>>() {}.type)
        } else {
            ArrayList() // Initialize as an empty ArrayList
        }

        // Sort the recordList by score in descending order
        recordList.sortByDescending { it.score }

        // Create an ArrayList of strings to hold formatted record information
        val recordStrings = ArrayList<String>()
        for (record in recordList) {
            val recordString = "User: ${record.username}, Score: ${record.score}"
            recordStrings.add(recordString)
        }

        // Create an adapter to bind data to the ListView
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, recordStrings)

        // Set the adapter to the ListView
        recordListView.adapter = adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
