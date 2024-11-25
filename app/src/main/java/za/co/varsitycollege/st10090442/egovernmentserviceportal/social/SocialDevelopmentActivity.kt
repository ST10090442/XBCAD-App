package za.co.varsitycollege.st10090442.egovernmentserviceportal.social

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10090442.egovernmentserviceportal.databinding.ActivitySocialDevelopmentBinding

class SocialDevelopmentActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySocialDevelopmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySocialDevelopmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnApplyNow.setOnClickListener {
            startActivity(Intent(this, SocialGrantApplicationActivity::class.java))
        }

        binding.btnScheduleAppointment.setOnClickListener {
            // TODO: Implement schedule appointment functionality
            startActivity(Intent(this, SocialScheduleAppointmentActivity::class.java))
        }
    }
}