package za.co.varsitycollege.st10090442.egovernmentserviceportal.transport

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10090442.egovernmentserviceportal.databinding.ActivityTransportApplyBinding

class TransportApplyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransportApplyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransportApplyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLicenseRenewal.setOnClickListener {
            startActivity(Intent(this, LicenseRenewalActivity::class.java))
        }

        binding.btnVehicleRegistration.setOnClickListener {
            startActivity(Intent(this, VehicleRegistrationActivity::class.java))
        }
    }
}