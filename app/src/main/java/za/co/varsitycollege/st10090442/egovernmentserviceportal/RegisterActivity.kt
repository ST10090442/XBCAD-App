package za.co.varsitycollege.st10090442.egovernmentserviceportal

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import za.co.varsitycollege.st10090442.egovernmentserviceportal.databinding.ActivityRegisterBinding
import za.co.varsitycollege.st10090442.egovernmentserviceportal.utils.Validators

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            if (validateInputs()) {
                registerUser()
            }
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validateInputs(): Boolean {
        val idNumber = binding.etIdNumber.text.toString().trim()
        val fullNames = binding.etFullNames.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        // Check empty fields
        when {
            idNumber.isEmpty() -> {
                binding.etIdNumber.error = "ID Number required"
                return false
            }
            fullNames.isEmpty() -> {
                binding.etFullNames.error = "Full Names required"
                return false
            }
            email.isEmpty() -> {
                binding.etEmail.error = "Email required"
                return false
            }
            phone.isEmpty() -> {
                binding.etPhone.error = "Phone Number required"
                return false
            }
            password.isEmpty() -> {
                binding.etPassword.error = "Password required"
                return false
            }
            confirmPassword.isEmpty() -> {
                binding.etConfirmPassword.error = "Confirm Password required"
                return false
            }
        }

        // Validate email format
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Invalid email format"
            return false
        }

        // Validate password
        if (password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
            return false
        }

        // Check if passwords match
        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Passwords do not match"
            return false
        }

        if (!Validators.isValidSouthAfricanId(idNumber)) {
            binding.etIdNumber.error = "ID Number must be 13 digits"
            return false
        }

        return true
    }

    private fun registerUser() {
        if (!validateInputs()) {
            return
        }

        // Disable button to prevent double submission
        binding.btnRegister.isEnabled = false
        
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        // First check if email exists
        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods ?: emptyList<String>()
                    if (signInMethods.isNotEmpty()) {
                        // Email exists
                        binding.btnRegister.isEnabled = true
                        binding.etEmail.error = "Email already in use"
                        Toast.makeText(this, "This email is already registered", Toast.LENGTH_LONG).show()
                    } else {
                        // Email doesn't exist, proceed with registration
                        createNewUser(email, password)
                    }
                } else {
                    // Error checking email
                    binding.btnRegister.isEnabled = true
                    Toast.makeText(this, "Error checking email: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun createNewUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    saveUserData(user?.uid)
                } else {
                    binding.btnRegister.isEnabled = true
                    handleRegistrationError(task.exception)
                }
            }
    }

    private fun saveUserData(userId: String?) {
        userId?.let {
            val userData = hashMapOf(
                "idNumber" to binding.etIdNumber.text.toString().trim(),
                "fullNames" to binding.etFullNames.text.toString().trim(),
                "email" to binding.etEmail.text.toString().trim(),
                "phone" to binding.etPhone.text.toString().trim(),
                "createdAt" to FieldValue.serverTimestamp()
            )

            db.collection("users").document(it)
                .set(userData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                    
                    // Navigate to MainActivity
                    val intent = Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    binding.btnRegister.isEnabled = true
                    Toast.makeText(this, 
                        "Error saving user data: ${e.message}", 
                        Toast.LENGTH_LONG
                    ).show()
                }
        } ?: run {
            binding.btnRegister.isEnabled = true
            Toast.makeText(this, "Error: User ID is null", Toast.LENGTH_LONG).show()
            auth.signOut()
        }
    }

    private fun handleRegistrationError(exception: Exception?) {
        binding.btnRegister.isEnabled = true
        when (exception) {
            is FirebaseAuthWeakPasswordException -> {
                binding.etPassword.error = "Password must be at least 6 characters"
                Toast.makeText(this, "Password is too weak", Toast.LENGTH_LONG).show()
            }
            is FirebaseAuthInvalidCredentialsException -> {
                binding.etEmail.error = "Invalid email format"
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_LONG).show()
            }
            is FirebaseAuthUserCollisionException -> {
                binding.etEmail.error = "Email already in use"
                Toast.makeText(this, "This email is already registered", Toast.LENGTH_LONG).show()
            }
            else -> {
                Toast.makeText(
                    this,
                    "Registration failed: ${exception?.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}