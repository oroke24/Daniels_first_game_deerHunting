package com.example.daniels_first_game_deerhunting
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private var score = 0
    private var timer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 60000 // 1 minute
    private val deerHitPoints = 10
    private lateinit var startButton: Button
    private lateinit var deerImageView: ImageView
    private lateinit var scoreTextView: TextView
    private lateinit var timerTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Hide system UI
        hideSystemUI()

        // Initialize views
        startButton = findViewById(R.id.startButton)
        deerImageView = findViewById(R.id.deerImageView)
        scoreTextView = findViewById(R.id.scoreTextView)
        timerTextView = findViewById(R.id.timerTextView)

        // Start the game when the start button is clicked
        startButton.setOnClickListener {
            startGame()
        }

        // Set up onClickListener for the deer image
        deerImageView.setOnClickListener {
            // Increment score when the deer is clicked
            score += deerHitPoints
            updateScore()
            // Randomly move the deer
            moveDeerRandomly()
        }
    }

    private fun startGame() {
        // Reset score and update UI
        score = 0
        updateScore()

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
        scoreTextView.text = "Game Over! Final Score: $score"
        timer?.cancel()
        startButton.visibility = View.VISIBLE
    }

    private fun updateTimerText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeLeftFormatted = String.format("%02d:%02d", minutes, seconds)
        timerTextView.text = "Time Left: $timeLeftFormatted"
    }

    private fun hideSystemUI() {
        val decorView = window.decorView
        decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
    }

    private fun updateScore() {
        // Update the score text view
        scoreTextView.text = "Score: $score"
    }

    private fun moveDeerRandomly() {
        // Randomly move the deer to a new position within the layout
        val randomX = Random.nextInt(0, (deerImageView.parent as View).width - deerImageView.width)
        val randomY = Random.nextInt(0, (deerImageView.parent as View).height - deerImageView.height)
        deerImageView.x = randomX.toFloat()
        deerImageView.y = randomY.toFloat()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
