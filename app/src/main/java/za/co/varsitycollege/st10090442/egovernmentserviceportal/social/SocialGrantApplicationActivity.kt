package za.co.varsitycollege.st10090442.egovernmentserviceportal.social

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10090442.egovernmentserviceportal.databinding.ActivitySocialGrantApplicationBinding
import za.co.varsitycollege.st10090442.egovernmentserviceportal.utils.Validators

class SocialGrantApplicationActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySocialGrantApplicationBinding
    private var proofOfIncomeUri: Uri? = null
    private var proofOfResidenceUri: Uri? = null
    private var idCopyUri: Uri? = null

    private val incomePicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            proofOfIncomeUri = result.data?.data
            val fileName = getFileName(proofOfIncomeUri)
            binding.etProofOfIncome.setText(fileName)
        }
    }

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
        binding = ActivitySocialGrantApplicationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinner()
        setupClickListeners()
    }

    private fun setupSpinner() {
        val grantTypes = arrayOf(
            "Child Support Grant",
            "Disability Grant",
            "Old Age Grant",
            "Foster Child Grant"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, grantTypes)
        binding.spinnerGrantType.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnChooseIncome.setOnClickListener {
            openFilePicker(incomePicker)
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
            if (etPhoneNumber.text.isNullOrEmpty()) {
                etPhoneNumber.error = "Required"
                isValid = false
            }
            if (proofOfIncomeUri == null) {
                Toast.makeText(this@SocialGrantApplicationActivity, 
                    "Please upload proof of income", Toast.LENGTH_SHORT).show()
                isValid = false
            }
            if (proofOfResidenceUri == null) {
                Toast.makeText(this@SocialGrantApplicationActivity, 
                    "Please upload proof of residence", Toast.LENGTH_SHORT).show()
                isValid = false
            }
            if (idCopyUri == null) {
                Toast.makeText(this@SocialGrantApplicationActivity, 
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