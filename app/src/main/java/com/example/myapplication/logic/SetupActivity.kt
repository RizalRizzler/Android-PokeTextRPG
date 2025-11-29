package com.example.myapplication.logic

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.models.createPokemon

class SetupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        val etPlayerName = findViewById<EditText>(R.id.etPlayerName)
        val rgStarter = findViewById<RadioGroup>(R.id.rgStarter)
        val btnStart = findViewById<Button>(R.id.btnStartAdventure)

        btnStart.setOnClickListener {
            val name = etPlayerName.text.toString().trim()

            if (name.isEmpty()) {
                etPlayerName.error = "Please enter a name!"
                return@setOnClickListener
            }

            GameRepository.initializeGame(name)

            val starterName = when (rgStarter.checkedRadioButtonId) {
                R.id.rbTreecko -> "TREECKO"
                R.id.rbTorchic -> "TORCHIC"
                R.id.rbMudkip -> "MUDKIP"
                else -> "TREECKO"
            }

            val starterPokemon = createPokemon(starterName)

            if (starterPokemon != null) {
                GameRepository.player?.addPokemon(starterPokemon)

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)

                finish()
            } else {
                Toast.makeText(this, "Error creating starter!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //dummy
    override fun onPause() {
        super.onPause()
        println("Activity Paused")
    }

    override fun onStop() {
        super.onStop()
        println("Activity Stoped")
    }

}