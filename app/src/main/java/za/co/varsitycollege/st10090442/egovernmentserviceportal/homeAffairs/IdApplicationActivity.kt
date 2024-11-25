package za.co.varsitycollege.st10090442.egovernmentserviceportal.homeAffairs

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10090442.egovernmentserviceportal.databinding.ActivityIdApplicationBinding
import java.util.Calendar
import android.os.Handler
import android.os.Looper
import android.view.View

class IdApplicationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIdApplicationBinding
    private var proofOfResidenceUri: Uri? = null
    private var idCopyUri: Uri? = null
    private val calendar: Calendar = Calendar.getInstance()

    private val residencePicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            proofOfResidenceUri = result.data?.data
            val fileName = getFileName(proofOfResidenceUri)
            binding.etProofOfResidence.setText(fileName)
        }
    }

    private val idCopyPicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            idCopyUri = result.data?.data
            val fileName = getFileName(idCopyUri)
            binding.etIdCopy.setText(fileName)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIdApplicationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        setupDatePicker()
    }

    private fun setupClickListeners() {
        binding.btnSubmit.setOnClickListener {
            if (validateInputs()) {
                submitApplication()
            }
        }

        // File chooser buttons
        binding.btnChooseResidence.setOnClickListener {
            openFilePicker(residencePicker)
        }

        binding.btnChooseId.setOnClickListener {
            openFilePicker(idCopyPicker)
        }
    }

    private fun setupDatePicker() {
        binding.etDateOfBirth.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                // Format the date as YYYY-MM-DD
                val formattedMonth = String.format("%02d", selectedMonth + 1)
                val formattedDay = String.format("%02d", selectedDay)
                binding.etDateOfBirth.setText("$selectedYear-$formattedMonth-$formattedDay")
            }, year, month, day).show()
        }

        // Make the EditText not editable directly
        binding.etDateOfBirth.isFocusable = false
        binding.etDateOfBirth.isClickable = true
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // ID Number validation
        val idNumber = binding.etIdNumber.text.toString()
        if (idNumber.isEmpty()) {
            binding.etIdNumber.error = "ID Number is required"
            isValid = false
        } else if (!isValidSouthAfricanID(idNumber)) {
            binding.etIdNumber.error = "Please enter a valid 13-digit South African ID number"
            isValid = false
        }

        // Full Names validation
        val fullNames = binding.etFullNames.text.toString()
        if (fullNames.isEmpty()) {
            binding.etFullNames.error = "Full Names are required"
            isValid = false
        } else if (!fullNames.matches(Regex("^[a-zA-Z ]+$"))) {
            binding.etFullNames.error = "Full Names should only contain letters"
            isValid = false
        }

        if (binding.etAddress.text.toString().isEmpty()) {
            binding.etAddress.error = "Address is required"
            isValid = false
        }

        val phoneNumber = binding.etPhoneNumber.text.toString()
        if (phoneNumber.isEmpty()) {
            binding.etPhoneNumber.error = "Phone Number is required"
            isValid = false
        } else if (!phoneNumber.matches(Regex("^0\\d{9}$"))) {
            binding.etPhoneNumber.error = "Please enter a valid 10-digit phone number starting with 0"
            isValid = false
        }

        if (binding.etDateOfBirth.text.toString().isEmpty()) {
            binding.etDateOfBirth.error = "Date of Birth is required"
            isValid = false
        }

        if (binding.etProofOfResidence.text.toString().isEmpty()) {
            Toast.makeText(this, "Please upload proof of residence", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (binding.etIdCopy.text.toString().isEmpty()) {
            Toast.makeText(this, "Please upload ID copy", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun isValidSouthAfricanID(idNumber: String): Boolean {
        try {
            // Check length
            if (idNumber.length != 13) return false

            // Check if all characters are digits
            if (!idNumber.all { it.isDigit() }) return false

            // Extract date components
            val year = idNumber.substring(0, 2).toInt()
            val month = idNumber.substring(2, 4).toInt()
            val day = idNumber.substring(4, 6).toInt()

            // Validate date components
            if (month < 1 || month > 12) return false
            if (day < 1 || day > 31) return false

            return true
        } catch (e: Exception) {
            return false
        }
    }

    private fun submitApplication() {
        try {
            // Disable button to prevent double submission
            binding.btnSubmit.isEnabled = false

            // Show success message
            Toast.makeText(this, "Application submitted successfully!", Toast.LENGTH_SHORT).show()
            
            // Add a small delay before finishing the activity
            Handler(Looper.getMainLooper()).postDelayed({
                finish()
            }, 1500) // 1.5 seconds delay

        } catch (e: Exception) {
            binding.btnSubmit.isEnabled = true
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun openFilePicker(picker: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "image/*",
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            ))
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        picker.launch(intent)
    }

    private fun getFileName(uri: Uri?): String? {
        uri?.let {
            var fileName: String? = null
            contentResolver.query(it, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        fileName = cursor.getString(displayNameIndex)
                    }
                }
            }
            if (fileName == null) {
                fileName = uri.lastPathSegment
            }
            return fileName
        }
        return null
    }
}