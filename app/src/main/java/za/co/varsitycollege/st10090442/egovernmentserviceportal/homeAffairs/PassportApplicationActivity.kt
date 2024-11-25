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
import za.co.varsitycollege.st10090442.egovernmentserviceportal.databinding.ActivityPassportApplicationBinding
import za.co.varsitycollege.st10090442.egovernmentserviceportal.utils.Validators
import java.util.Calendar
import android.os.Build
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.view.View

class PassportApplicationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPassportApplicationBinding
    private var proofOfResidenceUri: Uri? = null
    private var idCopyUri: Uri? = null
    private var currentFileType = ""

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            // All permissions granted, launch file picker
            when (currentFileType) {
                "residence" -> launchProofOfResidencePicker()
                "id" -> launchIdCopyPicker()
            }
        } else {
            Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    private val proofOfResidencePicker = registerForActivityResult(
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
        binding = ActivityPassportApplicationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        setupDatePicker()
    }

    private fun setupClickListeners() {
        binding.btnChooseResidence.setOnClickListener {
            checkAndRequestPermissions("residence")
        }

        binding.btnChooseId.setOnClickListener {
            checkAndRequestPermissions("id")
        }

        binding.btnSubmit.setOnClickListener {
            if (validateForm()) {
                submitApplication()
            }
        }
    }

    private fun checkAndRequestPermissions(fileType: String) {
        currentFileType = fileType
        
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

        requestPermissionLauncher.launch(arrayOf(permission))
    }

    private fun setupDatePicker() {
    binding.etDateOfBirth.setOnClickListener {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                // Format the date as YYYY-MM-DD
                val formattedMonth = String.format("%02d", month + 1)
                val formattedDay = String.format("%02d", day)
                binding.etDateOfBirth.setText("$year-$formattedMonth-$formattedDay")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // Make the EditText not editable directly
    binding.etDateOfBirth.isFocusable = false
    binding.etDateOfBirth.isClickable = true
}

    private fun validateForm(): Boolean {
        var isValid = true

        // ID Number validation
        if (binding.etIdNumber.text.toString().isEmpty()) {
            binding.etIdNumber.error = "ID Number is required"
            isValid = false
        } else if (!Validators.isValidSouthAfricanId(binding.etIdNumber.text.toString())) {
            binding.etIdNumber.error = "ID Number must be 13 digits"
            isValid = false
        }

        // Full Names validation
        if (binding.etFullNames.text.toString().isEmpty()) {
            binding.etFullNames.error = "Full Names are required"
            isValid = false
        }

        // Address validation
        if (binding.etAddress.text.toString().isEmpty()) {
            binding.etAddress.error = "Address is required"
            isValid = false
        }

        // Phone Number validation
        if (binding.etPhoneNumber.text.toString().isEmpty()) {
            binding.etPhoneNumber.error = "Phone Number is required"
            isValid = false
        }

        // Date of Birth validation
        if (binding.etDateOfBirth.text.toString().isEmpty()) {
            binding.etDateOfBirth.error = "Date of Birth is required"
            isValid = false
        }

        // File validation
        if (proofOfResidenceUri == null) {
            Toast.makeText(this, "Please upload proof of residence", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (idCopyUri == null) {
            Toast.makeText(this, "Please upload ID copy", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
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

    private fun launchProofOfResidencePicker() {
        openFilePicker(proofOfResidencePicker)
    }

    private fun launchIdCopyPicker() {
        openFilePicker(idCopyPicker)
    }

    private fun openFilePicker(picker: ActivityResultLauncher<Intent>) {
        try {
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
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening file picker: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}