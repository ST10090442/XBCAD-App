package za.co.varsitycollege.st10090442.egovernmentserviceportal.health

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10090442.egovernmentserviceportal.databinding.ActivityHealthBinding

class HealthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHealthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHealthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnApply.setOnClickListener {
            startActivity(Intent(this, MedicalAidRegistrationActivity::class.java))
        }

        binding.btnSchedule.setOnClickListener {
            startActivity(Intent(this, HealthScheduleActivity::class.java))
        }
    }
}