package za.co.varsitycollege.st10090442.egovernmentserviceportal.health

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10090442.egovernmentserviceportal.R
import za.co.varsitycollege.st10090442.egovernmentserviceportal.databinding.ActivityHealthScheduleBinding
import za.co.varsitycollege.st10090442.egovernmentserviceportal.homeAffairs.AppointmentConfirmationActivity
import za.co.varsitycollege.st10090442.egovernmentserviceportal.utils.Validators
import java.util.Calendar

class HealthScheduleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHealthScheduleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHealthScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinners()
        setupDateTimePicker()
        setupClickListeners()
    }

    private fun setupSpinners() {
        try {
            // Setup service spinner
            val services = arrayOf(
                "Select Service",
                "General Checkup",
                "Vaccination",
                "Medical Test",
                "Specialist Consultation"
            )
            val serviceAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, services)
            serviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerService.adapter = serviceAdapter

            // Setup location spinner
            val locations = arrayOf(
                "Select Location",
                "Pretoria Central Hospital",
                "Johannesburg General Hospital",
                "Cape Town Medical Center",
                "Durban Health Clinic"
            )
            val locationAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, locations)
            locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerLocation.adapter = locationAdapter
        } catch (e: Exception) {
            Toast.makeText(this, "Error setting up spinners: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupDateTimePicker() {
        binding.etDateTime.setOnClickListener {
            try {
                val calendar = Calendar.getInstance()
                
                DatePickerDialog(
                    this,
                    { _, year, month, day ->
                        TimePickerDialog(
                            this,
                            { _, hour, minute ->
                                binding.etDateTime.setText(
                                    "$day/${month + 1}/$year $hour:${String.format("%02d", minute)}"
                                )
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
            } catch (e: Exception) {
                Toast.makeText(this, "Error showing date picker: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnConfirm.setOnClickListener {
            if (validateForm()) {
                submitAppointment()
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        with(binding) {
            if (etIdNumber.text.toString().isEmpty()) {
                etIdNumber.error = "ID Number is required"
                isValid = false
            } else if (!Validators.isValidSouthAfricanId(etIdNumber.text.toString())) {
                etIdNumber.error = "ID Number must be 13 digits"
                isValid = false
            }
            if (etFullNames.text.toString().isEmpty()) {
                etFullNames.error = "Full Names are required"
                isValid = false
            }
            if (spinnerService.selectedItemPosition == 0) {
                Toast.makeText(this@HealthScheduleActivity, 
                    "Please select a service", Toast.LENGTH_SHORT).show()
                isValid = false
            }
            if (etPhoneNumber.text.toString().isEmpty()) {
                etPhoneNumber.error = "Phone Number is required"
                isValid = false
            }
            if (etDateTime.text.toString().isEmpty()) {
                etDateTime.error = "Date and Time is required"
                isValid = false
            }
            if (spinnerLocation.selectedItemPosition == 0) {
                Toast.makeText(this@HealthScheduleActivity, 
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