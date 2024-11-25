package za.co.varsitycollege.st10090442.egovernmentserviceportal.transport

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10090442.egovernmentserviceportal.databinding.ActivityTransportBinding

class TransportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnApply.setOnClickListener {
            startActivity(Intent(this, TransportApplyActivity::class.java))
        }

        binding.btnSchedule.setOnClickListener {
            startActivity(Intent(this, TransportScheduleActivity::class.java))
        }
    }
}