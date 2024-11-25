package za.co.varsitycollege.st10090442.egovernmentserviceportal

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import za.co.varsitycollege.st10090442.egovernmentserviceportal.databinding.ActivityLoginBinding
import za.co.varsitycollege.st10090442.egovernmentserviceportal.utils.Validators
import java.util.concurrent.Executor

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
    }

    private fun setupBiometric() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Authentication error: $errString", 
                            Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    runOnUiThread {
                        // Check if user exists in Firebase before proceeding
                        auth.currentUser?.let {
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        } ?: run {
                            Toast.makeText(applicationContext, 
                                "Please login with credentials first", 
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Authentication failed", 
                            Toast.LENGTH_SHORT).show()
                    }
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use password instead")
            .build()

        // Check if biometric authentication is available
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                binding.btnFingerprint.isEnabled = true
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                binding.btnFingerprint.isEnabled = false
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                binding.btnFingerprint.isEnabled = false
                Toast.makeText(this, "No fingerprint enrolled", Toast.LENGTH_LONG).show()
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

            if (validateInputs(email, idNumber, password)) {
                loginUser(email, password)
            }
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun validateInputs(email: String, idNumber: String, password: String): Boolean {
        when {
            email.isEmpty() -> {
                binding.etEmail.error = "Email required"
                return false
            }
            idNumber.isEmpty() -> {
                binding.etIdNumber.error = "ID Number required"
                return false
            }
            !Validators.isValidSouthAfricanId(idNumber) -> {
                binding.etIdNumber.error = "ID Number must be 13 digits"
                return false
            }
            password.isEmpty() -> {
                binding.etPassword.error = "Password required"
                return false
            }
        }
        return true
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}