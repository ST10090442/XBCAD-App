package za.co.varsitycollege.st10090442.egovernmentserviceportal

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.FirebaseException
import java.util.concurrent.TimeUnit
import za.co.varsitycollege.st10090442.egovernmentserviceportal.databinding.ActivityRegisterBinding
import za.co.varsitycollege.st10090442.egovernmentserviceportal.utils.Validators
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.auth.api.phone.SmsRetrieverClient

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.apply {
            btnRegister.setOnClickListener {
                if (validateInputs()) {
                    initiatePhoneVerification()
                }
            }
            tvLogin.setOnClickListener {
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                finish()
            }
        }
    }

    private fun validateInputs(): Boolean {
        binding.apply {
            val idNumber = etIdNumber.text.toString().trim()
            val fullNames = etFullNames.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            return when {
                idNumber.isEmpty() -> {
                    etIdNumber.error = "ID Number required"
                    false
                }
                !Validators.isValidSouthAfricanId(idNumber) -> {
                    etIdNumber.error = "Invalid ID Number"
                    false
                }
                fullNames.isEmpty() -> {
                    etFullNames.error = "Full Names required"
                    false
                }
                email.isEmpty() -> {
                    etEmail.error = "Email required"
                    false
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    etEmail.error = "Invalid email format"
                    false
                }
                phone.isEmpty() -> {
                    etPhone.error = "Phone Number required"
                    false
                }
                !isValidSouthAfricanPhoneNumber(phone) -> {
                    etPhone.error = "Enter valid SA number (e.g., 0821234567)"
                    false
                }
                password.isEmpty() -> {
                    etPassword.error = "Password required"
                    false
                }
                password.length < 6 -> {
                    etPassword.error = "Password must be at least 6 characters"
                    false
                }
                confirmPassword.isEmpty() -> {
                    etConfirmPassword.error = "Confirm Password required"
                    false
                }
                password != confirmPassword -> {
                    etConfirmPassword.error = "Passwords do not match"
                    false
                }
                else -> true
            }
        }
    }

    private fun initiatePhoneVerification() {
        if (!isNetworkAvailable()) {
            showToast("Please check your internet connection")
            return
        }

        binding.apply {
            btnRegister.isEnabled = false
            val phoneNumber = etPhone.text.toString().trim()
            val formattedPhone = formatPhoneNumber(phoneNumber)

            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    btnRegister.isEnabled = true
                    createUserWithEmail()
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    btnRegister.isEnabled = true
                    when (e) {
                        is FirebaseAuthInvalidCredentialsException -> {
                            etPhone.error = "Invalid phone number"
                            showToast("Please enter a valid phone number")
                        }
                        else -> showToast("Verification failed: ${e.message}")
                    }
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    val intent = Intent(this@RegisterActivity, SMSVerificationActivity::class.java).apply {
                        putExtra("verificationId", verificationId)
                        putExtra("phoneNumber", etPhone.text.toString().trim())
                        putExtra("email", etEmail.text.toString().trim())
                        putExtra("password", etPassword.text.toString())
                        putExtra("idNumber", etIdNumber.text.toString().trim())
                        putExtra("fullNames", etFullNames.text.toString().trim())
                        putExtra("isRegistration", true)
                    }
                    startActivity(intent)
                }
            }

            try {
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(formattedPhone)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this@RegisterActivity)
                    .setCallbacks(callbacks)
                    .build()

                PhoneAuthProvider.verifyPhoneNumber(options)
            } catch (e: Exception) {
                btnRegister.isEnabled = true
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun createUserWithEmail() {
        binding.apply {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        saveUserToDatabase(auth.currentUser?.uid)
                    } else {
                        btnRegister.isEnabled = true
                        handleRegistrationError(task.exception)
                    }
                }
        }
    }

    private fun saveUserToDatabase(userId: String?) {
        userId?.let { uid ->
            binding.apply {
                val userData = hashMapOf(
                    "idNumber" to etIdNumber.text.toString().trim(),
                    "fullNames" to etFullNames.text.toString().trim(),
                    "email" to etEmail.text.toString().trim(),
                    "phone" to etPhone.text.toString().trim(),
                    "createdAt" to FieldValue.serverTimestamp()
                )

                db.collection("users").document(uid)
                    .set(userData)
                    .addOnSuccessListener {
                        showToast("Registration successful!")
                        startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        btnRegister.isEnabled = true
                        showToast("Error saving user data: ${e.message}")
                    }
            }
        } ?: run {
            binding.btnRegister.isEnabled = true
            showToast("Error: User ID is null")
            auth.signOut()
        }
    }

    private fun handleRegistrationError(exception: Exception?) {
        binding.apply {
            when (exception) {
                is FirebaseAuthWeakPasswordException -> etPassword.error = "Password too weak"
                is FirebaseAuthInvalidCredentialsException -> etEmail.error = "Invalid email format"
                is FirebaseAuthUserCollisionException -> etEmail.error = "Email already in use"
                else -> showToast("Registration failed: ${exception?.message}")
            }
        }
    }

    private fun isValidSouthAfricanPhoneNumber(phone: String): Boolean =
        "^((?:\\+27|27)|0)[6-8][0-9]{8}$".toRegex().matches(phone)

    private fun formatPhoneNumber(phone: String): String = when {
        phone.startsWith("+27") -> phone
        phone.startsWith("27") -> "+$phone"
        phone.startsWith("0") -> "+27${phone.substring(1)}"
        else -> "+27$phone"
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.run {
            hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || 
            hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } ?: false
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}