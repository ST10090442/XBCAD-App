package za.co.varsitycollege.st10090442.egovernmentserviceportal.social

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10090442.egovernmentserviceportal.databinding.ActivitySocialScheduleAppointmentBinding
import za.co.varsitycollege.st10090442.egovernmentserviceportal.homeAffairs.AppointmentConfirmationActivity
import za.co.varsitycollege.st10090442.egovernmentserviceportal.utils.Validators
import java.util.Calendar

class SocialScheduleAppointmentActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySocialScheduleAppointmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySocialScheduleAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinners()
        setupDateTimePicker()
        setupClickListeners()
    }

    private fun setupSpinners() {
        // Setup service spinner
        val services = arrayOf(
            "Select Service",
            "Grant Application Assistance",
            "Social Worker Consultation",
            "Housing Support",
            "Family Services"
        )
        val serviceAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, services)
        binding.spinnerService.adapter = serviceAdapter

        // Setup location spinner
        val locations = arrayOf(
            "Select Location",
            "Cape Town Office",
            "Johannesburg Office",
            "Durban Office",
            "Pretoria Office",
            "Port Elizabeth Office"
        )
        val locationAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, locations)
        binding.spinnerLocation.adapter = locationAdapter
    }

    private fun setupDateTimePicker() {
        binding.etDateTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    TimePickerDialog(
                        this,
                        { _, hour, minute ->
                            binding.etDateTime.setText("$day/${month + 1}/$year $hour:${String.format("%02d", minute)}")
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    ).show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupClickListeners() {
        binding.btnConfirmAppointment.setOnClickListener {
            if (validateForm()) {
                submitAppointment()
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        with(binding) {
            // ID Number validation
            if (etIdNumber.text.toString().isEmpty()) {
                etIdNumber.error = "ID Number is required"
                isValid = false
            } else if (!Validators.isValidSouthAfricanId(etIdNumber.text.toString())) {
                etIdNumber.error = "ID Number must be 13 digits"
                isValid = false
            }

            // Full Names validation
            if (etFullNames.text.toString().isEmpty()) {
                etFullNames.error = "Full Names are required"
                isValid = false
            }

            // Service validation
            if (spinnerService.selectedItemPosition == 0) {
                Toast.makeText(this@SocialScheduleAppointmentActivity, 
                    "Please select a service", Toast.LENGTH_SHORT).show()
                isValid = false
            }

            // Phone Number validation
            if (etPhoneNumber.text.toString().isEmpty()) {
                etPhoneNumber.error = "Phone Number is required"
                isValid = false
            }

            // Date and Time validation
            if (etDateTime.text.toString().isEmpty()) {
                etDateTime.error = "Date and Time is required"
                isValid = false
            }

            // Location validation
            if (spinnerLocation.selectedItemPosition == 0) {
                Toast.makeText(this@SocialScheduleAppointmentActivity, 
                    "Please select a location", Toast.LENGTH_SHORT).show()
                isValid = false
            }
        }

        return isValid
    }

    private fun submitAppointment() {
        try {
            val appointment = hashMapOf(
                "idNumber" to binding.etIdNumber.text.toString(),
                "fullNames" to binding.etFullNames.text.toString(),
                "service" to binding.spinnerService.selectedItem.toString(),
                "phoneNumber" to binding.etPhoneNumber.text.toString(),
                "dateTime" to binding.etDateTime.text.toString(),
                "location" to binding.spinnerLocation.selectedItem.toString()
            )

            // Navigate to confirmation screen
            startActivity(Intent(this, AppointmentConfirmationActivity::class.java))
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Error submitting appointment: ${e.message}", 
                Toast.LENGTH_SHORT).show()
        }
    }
}