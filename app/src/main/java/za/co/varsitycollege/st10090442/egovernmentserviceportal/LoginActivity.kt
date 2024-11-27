package za.co.varsitycollege.st10090442.egovernmentserviceportal

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import za.co.varsitycollege.st10090442.egovernmentserviceportal.databinding.ActivityLoginBinding
import za.co.varsitycollege.st10090442.egovernmentserviceportal.utils.Validators
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        setupBiometric()
        setupClickListeners()
        checkBiometricAvailability()
    }

    private fun setupBiometric() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                showToast("Authentication error: $errString")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                navigateToMainActivity()
            }

            override fun onAuthenticationFailed() {
                showToast("Fingerprint not recognized")
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()
    }

    private fun checkBiometricAvailability() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                binding.btnFingerprint.isEnabled = true
                binding.btnFingerprint.visibility = View.VISIBLE
            }
            else -> {
                binding.btnFingerprint.visibility = View.GONE
                showToast("Biometric authentication not available")
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnFingerprint.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val idNumber = binding.etIdNumber.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val phoneNumber = binding.etPhone.text.toString().trim()

            if (validateInputs(email, idNumber, password, phoneNumber)) {
                loginUser(email, password, phoneNumber)
            }
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun validateInputs(email: String, idNumber: String, password: String, phoneNumber: String): Boolean {
        return when {
            email.isEmpty() -> {
                binding.etEmail.error = "Email required"
                false
            }
            idNumber.isEmpty() -> {
                binding.etIdNumber.error = "ID Number required"
                false
            }
            !Validators.isValidSouthAfricanId(idNumber) -> {
                binding.etIdNumber.error = "ID Number must be 13 digits"
                false
            }
            password.isEmpty() -> {
                binding.etPassword.error = "Password required"
                false
            }
            phoneNumber.isEmpty() -> {
                binding.etPhone.error = "Phone number required"
                false
            }
            !isValidSouthAfricanPhoneNumber(phoneNumber) -> {
                binding.etPhone.error = "Enter valid SA number (e.g., 0821234567)"
                false
            }
            else -> true
        }
    }

    private fun isValidSouthAfricanPhoneNumber(phone: String): Boolean {
        val regex = """^(?:(?:\+27|27)|0)([0-9]{9})$""".toRegex()
        return regex.matches(phone)
    }

    private fun loginUser(email: String, password: String, phoneNumber: String) {
        if (!isNetworkAvailable()) {
            showToast("No internet connection available")
            return
        }

        binding.btnLogin.isEnabled = false
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                binding.btnLogin.isEnabled = true
                if (task.isSuccessful) {
                    startPhoneVerification(phoneNumber)
                } else {
                    showToast("Login failed: ${task.exception?.message}")
                }
            }
    }

    private fun startPhoneVerification(phoneNumber: String) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                binding.btnLogin.isEnabled = true
                if (e is FirebaseAuthInvalidCredentialsException) {
                    showToast("Invalid phone number format")
                } else {
                    showToast("Verification failed: ${e.message}")
                }
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                val intent = Intent(this@LoginActivity, SMSVerificationActivity::class.java).apply {
                    putExtra("verificationId", verificationId)
                    putExtra("phoneNumber", phoneNumber)
                    putExtra("email", binding.etEmail.text.toString().trim())
                    putExtra("password", binding.etPassword.text.toString())
                    putExtra("isRegistration", false)
                }
                startActivity(intent)
            }
        }

        val formattedPhone = if (!phoneNumber.startsWith("+")) "+$phoneNumber" else phoneNumber
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(formattedPhone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navigateToMainActivity()
                } else {
                    showToast("Verification failed: ${task.exception?.message}")
                }
            }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                )
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

