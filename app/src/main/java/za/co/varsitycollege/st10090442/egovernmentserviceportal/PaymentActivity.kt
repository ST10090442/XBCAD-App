package za.co.varsitycollege.st10090442.egovernmentserviceportal

import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import za.co.varsitycollege.st10090442.egovernmentserviceportal.databinding.ActivityPaymentBinding
import za.co.varsitycollege.st10090442.egovernmentserviceportal.utils.Validators
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import android.util.Base64
import java.text.SimpleDateFormat
import java.util.*

class PaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentBinding
    private var attemptCount = 0
    private val MAX_ATTEMPTS = 3
    private val LOCK_DURATION = 300000L // 5 minutes in milliseconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Prevent screenshots and screen recording
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSecurityFeatures()
        setupClickListeners()
        setupInputFormatting()
    }

    private fun setupSecurityFeatures() {
        // Set input types for sensitive fields
        binding.etCardNumber.inputType = InputType.TYPE_CLASS_NUMBER or 
            InputType.TYPE_NUMBER_VARIATION_PASSWORD
        binding.etCvv.inputType = InputType.TYPE_CLASS_NUMBER or 
            InputType.TYPE_NUMBER_VARIATION_PASSWORD

        // Limit input lengths
        binding.etCardNumber.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(16))
        binding.etCvv.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(3))
        binding.etExpiryDate.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(5))

        // Clear sensitive data when activity loses focus
        binding.root.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                clearSensitiveData()
            }
        }
    }

    private fun setupInputFormatting() {
        // Format card number with spaces
        binding.etCardNumber.addTextChangedListener { text ->
            if (text != null && !text.toString().contains(" ")) {
                val formatted = text.toString().chunked(4).joinToString(" ")
                if (formatted != text.toString()) {
                    binding.etCardNumber.setText(formatted)
                    binding.etCardNumber.setSelection(formatted.length)
                }
            }
        }

        // Format expiry date (MM/YY)
        binding.etExpiryDate.addTextChangedListener { text ->
            if (text != null && text.length == 2 && !text.toString().contains("/")) {
                binding.etExpiryDate.setText("$text/")
                binding.etExpiryDate.setSelection(3)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnPay.setOnClickListener {
            if (validateForm()) {
                processPayment()
            }
        }

        binding.etExpiryDate.setOnClickListener {
        }
    }

    private fun validateForm(): Boolean {
        if (isTemporarilyLocked()) {
            Toast.makeText(this, "Too many attempts. Please try again later.", 
                Toast.LENGTH_LONG).show()
            return false
        }

        var isValid = true

        with(binding) {
            // ID Number validation
            if (etIdNumber.text.isNullOrEmpty()) {
                etIdNumber.error = "ID Number is required"
                isValid = false
            } else if (!Validators.isValidSouthAfricanId(etIdNumber.text.toString())) {
                etIdNumber.error = "Invalid ID Number"
                isValid = false
            }

            // Card number validation (Luhn algorithm)
            if (etCardNumber.text.isNullOrEmpty()) {
                etCardNumber.error = "Card number is required"
                isValid = false
            } else if (!isValidCreditCard(etCardNumber.text.toString().replace(" ", ""))) {
                etCardNumber.error = "Invalid card number"
                isValid = false
            }

            // Expiry date validation
            if (etExpiryDate.text.isNullOrEmpty()) {
                etExpiryDate.error = "Expiry date is required"
                isValid = false
            } else if (!isValidExpiryDate(etExpiryDate.text.toString())) {
                etExpiryDate.error = "Card has expired or invalid date"
                isValid = false
            }

            // CVV validation
            if (etCvv.text.isNullOrEmpty()) {
                etCvv.error = "CVV is required"
                isValid = false
            } else if (!isValidCVV(etCvv.text.toString())) {
                etCvv.error = "Invalid CVV"
                isValid = false
            }
        }

        if (!isValid) {
            attemptCount++
            if (attemptCount >= MAX_ATTEMPTS) {
                lockTemporarily()
            }
        }

        return isValid
    }

    private fun processPayment() {
        try {
            // Encrypt sensitive data before processing
            val encryptedCard = encryptData(binding.etCardNumber.text.toString())
            val encryptedCVV = encryptData(binding.etCvv.text.toString())
            
            // Show success and clear sensitive data
            Toast.makeText(this, "Payment processed successfully", Toast.LENGTH_LONG).show()
            clearSensitiveData()
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Payment failed: ${e.message}", Toast.LENGTH_LONG).show()
            logSecurityEvent("Payment processing error: ${e.message}")
        }
    }

    private fun isValidCreditCard(number: String): Boolean {
        var sum = 0
        var alternate = false
        for (i in number.length - 1 downTo 0) {
            var n = number.substring(i, i + 1).toInt()
            if (alternate) {
                n *= 2
                if (n > 9) {
                    n = (n % 10) + 1
                }
            }
            sum += n
            alternate = !alternate
        }
        return (sum % 10 == 0)
    }

    private fun isValidExpiryDate(date: String): Boolean {
        try {
            val parts = date.split("/")
            if (parts.size != 2) return false

            val month = parts[0].toInt()
            val year = 2000 + parts[1].toInt()

            if (month < 1 || month > 12) return false

            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH) + 1

            return when {
                year > currentYear -> true
                year == currentYear -> month >= currentMonth
                else -> false
            }
        } catch (e: Exception) {
            return false
        }
    }

    private fun isValidCVV(cvv: String): Boolean {
        return cvv.length == 3 && cvv.all { it.isDigit() }
    }

    private fun encryptData(data: String): String {
        val key = SecretKeySpec("YourSecretKey123".toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encrypted = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(encrypted, Base64.DEFAULT)
    }

    private fun clearSensitiveData() {
        binding.etCardNumber.text?.clear()
        binding.etCvv.text?.clear()
        binding.etExpiryDate.text?.clear()
    }

    private fun isTemporarilyLocked(): Boolean {
        val lockUntil = getSharedPreferences("PaymentSecurity", MODE_PRIVATE)
            .getLong("lockUntil", 0)
        return System.currentTimeMillis() < lockUntil
    }

    private fun lockTemporarily() {
        val lockUntil = System.currentTimeMillis() + LOCK_DURATION
        getSharedPreferences("PaymentSecurity", MODE_PRIVATE)
            .edit()
            .putLong("lockUntil", lockUntil)
            .apply()
        
        Toast.makeText(this, 
            "Too many failed attempts. Please try again in 5 minutes.", 
            Toast.LENGTH_LONG).show()
    }

    private fun logSecurityEvent(event: String) {
        println("Security Event: $event")
    }

    override fun onPause() {
        super.onPause()
        clearSensitiveData()
    }

    override fun onDestroy() {
        super.onDestroy()
        clearSensitiveData()
    }
}