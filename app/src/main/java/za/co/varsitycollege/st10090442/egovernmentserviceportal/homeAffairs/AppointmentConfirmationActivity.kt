package za.co.varsitycollege.st10090442.egovernmentserviceportal.homeAffairs

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10090442.egovernmentserviceportal.MainActivity
import za.co.varsitycollege.st10090442.egovernmentserviceportal.databinding.ActivityAppointmentConfirmationBinding

class AppointmentConfirmationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAppointmentConfirmationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnMenu.setOnClickListener {
            // Clear activity stack and go to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}