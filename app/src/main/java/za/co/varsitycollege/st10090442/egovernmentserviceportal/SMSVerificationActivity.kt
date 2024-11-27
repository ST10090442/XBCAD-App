package za.co.varsitycollege.st10090442.egovernmentserviceportal

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthOptions
import java.util.concurrent.TimeUnit
import za.co.varsitycollege.st10090442.egovernmentserviceportal.databinding.ActivitySmsVerificationBinding
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class SMSVerificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySmsVerificationBinding
    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var phoneNumber: String? = null
    private var timer: CountDownTimer? = null
    private var isRegistration: Boolean = false
    private var email: String? = null
    private var password: String? = null
    private var fullNames: String? = null
    private var idNumber: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmsVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        with(intent) {
            verificationId = getStringExtra("verificationId")
            phoneNumber = getStringExtra("phoneNumber")
            isRegistration = getBooleanExtra("isRegistration", false)
            if (isRegistration) {
                email = getStringExtra("email")
                password = getStringExtra("password")
                fullNames = getStringExtra("fullNames")
                idNumber = getStringExtra("idNumber")
            }
        }
        
        setupUI()
        startResendTimer()
    }

    private fun setupUI() {
        binding.btnVerify.setOnClickListener {
            val code = binding.etCode.text.toString().trim()
            if (code.length == 6) {
                verifyCode(code)
            } else {
                binding.etCode.error = "Please enter valid code"
            }
        }

        binding.btnResend.setOnClickListener {
            resendVerificationCode()
        }
    }

    private fun verifyCode(code: String) {
        try {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnVerify.isEnabled = false

            val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
            signInWithPhoneAuthCredential(credential)
        } catch (e: Exception) {
            Toast.makeText(this, "Verification Error: ${e.message}", Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility = View.GONE
            binding.btnVerify.isEnabled = true
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        if (!isRegistration) {
            // Login flow
            val email = intent.getStringExtra("email")
            val password = intent.getStringExtra("password")

            if (email != null && password != null) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { emailTask ->
                        if (emailTask.isSuccessful) {
                            // phone credential
                            auth.currentUser?.linkWithCredential(credential)
                                ?.addOnCompleteListener(this) { linkTask ->
                                    if (linkTask.isSuccessful) {
                                        startActivity(Intent(this, MainActivity::class.java)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                                        finish()
                                    } else {
                                        Toast.makeText(this,
                                            "Phone verification failed: ${linkTask.exception?.message}",
                                            Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            Toast.makeText(this,
                                "Authentication failed: ${emailTask.exception?.message}",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        } else {

            auth.createUserWithEmailAndPassword(email!!, password!!)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        saveUserData(user?.uid)
                    } else {
                        handleRegistrationError(task.exception)
                    }
                }
        }
    }

    private fun saveUserData(userId: String?) {
        userId?.let {
            val userData = hashMapOf(
                "idNumber" to idNumber,
                "fullNames" to fullNames,
                "email" to email,
                "phone" to phoneNumber,
                "phoneVerified" to true,
                "createdAt" to FieldValue.serverTimestamp()
            )

            FirebaseFirestore.getInstance().collection("users").document(it)
                .set(userData)
                .addOnSuccessListener {
                    startActivity(Intent(this, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, 
                        "Error saving user data: ${e.message}", 
                        Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun resendVerificationCode() {
        try {
            binding.btnResend.isEnabled = false
            
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    binding.btnResend.isEnabled = true
                    Toast.makeText(baseContext, 
                        "Verification failed: ${e.message}", 
                        Toast.LENGTH_SHORT).show()
                }

                override fun onCodeSent(
                    newVerificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    verificationId = newVerificationId
                    resendToken = token
                    Toast.makeText(baseContext, 
                        "Verification code resent", 
                        Toast.LENGTH_SHORT).show()
                    startResendTimer()
                }
            }

            val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber!!)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(callbacks)

            resendToken?.let { token ->
                optionsBuilder.setForceResendingToken(token)
            }

            PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error resending code: ${e.message}", 
                Toast.LENGTH_SHORT).show()
            binding.btnResend.isEnabled = true
        }
    }

    private fun startResendTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.btnResend.text = "Resend in ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                binding.btnResend.isEnabled = true
                binding.btnResend.text = "Resend Code"
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }

    private fun handleRegistrationError(exception: Exception?) {
        binding.progressBar.visibility = View.GONE
        binding.btnVerify.isEnabled = true
        
        when (exception) {
            is FirebaseAuthWeakPasswordException -> {
                Toast.makeText(this, "Password is too weak", Toast.LENGTH_LONG).show()
            }
            is FirebaseAuthInvalidCredentialsException -> {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_LONG).show()
            }
            is FirebaseAuthUserCollisionException -> {
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