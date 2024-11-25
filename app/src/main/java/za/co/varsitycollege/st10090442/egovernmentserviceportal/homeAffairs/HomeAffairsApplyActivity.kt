package za.co.varsitycollege.st10090442.egovernmentserviceportal.homeAffairs

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10090442.egovernmentserviceportal.databinding.ActivityHomeAffairsApplyBinding

class HomeAffairsApplyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeAffairsApplyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeAffairsApplyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnNewId.setOnClickListener {
            // Handle new ID application
            startActivity(Intent(this, IdApplicationActivity::class.java))
        }

        binding.btnNewPassport.setOnClickListener {
            // Handle new passport application
            startActivity(Intent(this, PassportApplicationActivity::class.java))
        }
    }
}