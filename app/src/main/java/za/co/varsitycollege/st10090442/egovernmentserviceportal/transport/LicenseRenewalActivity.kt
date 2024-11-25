package za.co.varsitycollege.st10090442.egovernmentserviceportal.transport

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10090442.egovernmentserviceportal.databinding.ActivityLicenseRenewalBinding
import za.co.varsitycollege.st10090442.egovernmentserviceportal.utils.Validators
import java.util.Calendar

class LicenseRenewalActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLicenseRenewalBinding
    private var residenceUri: Uri? = null
    private var idCopyUri: Uri? = null

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
        binding = ActivityLicenseRenewalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        setupDatePicker()
    }

    private fun setupClickListeners() {
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

    private fun setupDatePicker() {
        binding.etDateOfBirth.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    binding.etDateOfBirth.setText("$day/${month + 1}/$year")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun openFilePicker(picker: androidx.activity.result.ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        picker.launch(intent)
    }

    private fun getFileName(uri: Uri?): String {
        uri?.let {
            contentResolver.query(it, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                return cursor.getString(nameIndex)
            }
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
            if (etDateOfBirth.text.isNullOrEmpty()) {
                etDateOfBirth.error = "Required"
                isValid = false
            }
            if (etProofOfResidence.text.isNullOrEmpty()) {
                etProofOfResidence.error = "Required"
                isValid = false
            }
            if (etIdCopy.text.isNullOrEmpty()) {
                etIdCopy.error = "Required"
                isValid = false
            }
        }

        return isValid
    }

    private fun submitApplication() {
        Toast.makeText(this, "Application submitted successfully", Toast.LENGTH_SHORT).show()
        finish()
    }
}