package za.co.varsitycollege.st10090442.egovernmentserviceportal.health

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10090442.egovernmentserviceportal.databinding.ActivityMedicalAidRegistrationBinding
import za.co.varsitycollege.st10090442.egovernmentserviceportal.utils.Validators

class MedicalAidRegistrationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMedicalAidRegistrationBinding
    private var medicalHistoryUri: Uri? = null
    private var residenceUri: Uri? = null
    private var idCopyUri: Uri? = null

    private val medicalHistoryPicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            medicalHistoryUri = result.data?.data
            val fileName = getFileName(medicalHistoryUri)
            binding.etMedicalHistory.setText(fileName)
        }
    }

    private val residencePicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            residenceUri = result.data?.data
            val fileName = getFileName(residenceUri)
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
        binding = ActivityMedicalAidRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnChooseMedicalHistory.setOnClickListener {
            openFilePicker(medicalHistoryPicker)
        }

        binding.btnChooseResidence.setOnClickListener {
            openFilePicker(residencePicker)
        }

        binding.btnChooseId.setOnClickListener {
            openFilePicker(idCopyPicker)
        }

        binding.btnSubmit.setOnClickListener {
            if (validateForm()) {
                submitApplication()
            }
        }
    }

    private fun openFilePicker(picker: androidx.activity.result.ActivityResultLauncher<Intent>) {
        try {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            picker.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening file picker: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileName(uri: Uri?): String {
        try {
            uri?.let {
                contentResolver.query(it, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    return cursor.getString(nameIndex)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error getting file name: ${e.message}", Toast.LENGTH_SHORT).show()
        }
        return "No file selected"
    }

    private fun validateForm(): Boolean {
        var isValid = true

        with(binding) {
            if (etIdNumber.text.isNullOrEmpty()) {
                etIdNumber.error = "Required"
                isValid = false
            } else if (!Validators.isValidSouthAfricanId(etIdNumber.text.toString())) {
                etIdNumber.error = "ID Number must be 13 digits"
                isValid = false
            }
            if (etFullNames.text.isNullOrEmpty()) {
                etFullNames.error = "Required"
                isValid = false
            }
            if (etAddress.text.isNullOrEmpty()) {
                etAddress.error = "Required"
                isValid = false
            }
            if (etPhoneNumber.text.isNullOrEmpty()) {
                etPhoneNumber.error = "Required"
                isValid = false
            }
            if (medicalHistoryUri == null) {
                Toast.makeText(this@MedicalAidRegistrationActivity, 
                    "Please upload medical history", Toast.LENGTH_SHORT).show()
                isValid = false
            }
            if (residenceUri == null) {
                Toast.makeText(this@MedicalAidRegistrationActivity, 
                    "Please upload proof of residence", Toast.LENGTH_SHORT).show()
                isValid = false
            }
            if (idCopyUri == null) {
                Toast.makeText(this@MedicalAidRegistrationActivity, 
                    "Please upload ID copy", Toast.LENGTH_SHORT).show()
                isValid = false
            }
        }

        return isValid
    }

    private fun submitApplication() {
        try {
            Toast.makeText(this, "Application submitted successfully", Toast.LENGTH_SHORT).show()
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Error submitting application: ${e.message}", 
                Toast.LENGTH_SHORT).show()
        }
    }
}