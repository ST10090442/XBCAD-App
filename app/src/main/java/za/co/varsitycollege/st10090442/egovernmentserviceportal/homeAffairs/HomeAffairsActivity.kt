package za.co.varsitycollege.st10090442.egovernmentserviceportal.homeAffairs

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10090442.egovernmentserviceportal.databinding.ActivityHomeAffairsBinding
import za.co.varsitycollege.st10090442.egovernmentserviceportal.homeAffairs.HomeAffairsApplyActivity
import za.co.varsitycollege.st10090442.egovernmentserviceportal.homeAffairs.ScheduleAppointmentActivity

class HomeAffairsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeAffairsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeAffairsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnApplyNow.setOnClickListener {
            startActivity(Intent(this, HomeAffairsApplyActivity::class.java))
        }

        binding.btnSchedule.setOnClickListener {
            startActivity(Intent(this, ScheduleAppointmentActivity::class.java))
        }
    }
}