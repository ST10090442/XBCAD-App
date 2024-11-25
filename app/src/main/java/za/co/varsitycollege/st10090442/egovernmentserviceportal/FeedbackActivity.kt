package za.co.varsitycollege.st10090442.egovernmentserviceportal

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10090442.egovernmentserviceportal.databinding.ActivityFeedbackBinding

class FeedbackActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFeedbackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinner()
        setupClickListeners()
    }

    private fun setupSpinner() {
        try {
            val services = arrayOf(
                "Select Service",
                "Home Affairs",
                "Health Services",
                "Transport Services",
                "Social Development",
                "General Feedback"
            )
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                services
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerService.adapter = adapter
        } catch (e: Exception) {
            Toast.makeText(this, "Error setting up services: ${e.message}", 
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        binding.btnSubmit.setOnClickListener {
            if (validateForm()) {
                submitFeedback()
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        with(binding) {
            if (etFullName.text.isNullOrEmpty()) {
                etFullName.error = "Name is required"
                isValid = false
            }

            if (etEmail.text.isNullOrEmpty()) {
                etEmail.error = "Email is required"
                isValid = false
            } else if (!android.util.Patterns.EMAIL_ADDRESS
                .matcher(etEmail.text.toString()).matches()) {
                etEmail.error = "Invalid email address"
                isValid = false
            }

            if (spinnerService.selectedItemPosition == 0) {
                Toast.makeText(this@FeedbackActivity, 
                    "Please select a service", Toast.LENGTH_SHORT).show()
                isValid = false
            }

            if (ratingBar.rating == 0f) {
                Toast.makeText(this@FeedbackActivity, 
                    "Please provide a rating", Toast.LENGTH_SHORT).show()
                isValid = false
            }

            if (etFeedback.text.isNullOrEmpty()) {
                etFeedback.error = "Feedback is required"
                isValid = false
            }
        }

        return isValid
    }

    private fun submitFeedback() {
        try {

            Toast.makeText(this, 
                "Thank you for your feedback!", Toast.LENGTH_LONG).show()
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, 
                "Error submitting feedback: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}