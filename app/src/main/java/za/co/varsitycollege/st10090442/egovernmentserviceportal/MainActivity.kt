package za.co.varsitycollege.st10090442.egovernmentserviceportal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import za.co.varsitycollege.st10090442.egovernmentserviceportal.homeAffairs.HomeAffairsActivity
import za.co.varsitycollege.st10090442.egovernmentserviceportal.databinding.ActivityMainBinding
import za.co.varsitycollege.st10090442.egovernmentserviceportal.transport.TransportActivity
import za.co.varsitycollege.st10090442.egovernmentserviceportal.health.HealthActivity
import za.co.varsitycollege.st10090442.egovernmentserviceportal.social.SocialDevelopmentActivity
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.GravityCompat
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Check if user is logged in
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setupDrawer()
        setupUI()
    }

    private fun setupDrawer() {
        try {
            drawerLayout = binding.drawerLayout
            val menuButton = binding.menuButton

            menuButton.setOnClickListener {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    drawerLayout.openDrawer(GravityCompat.START)
                }
            }

            binding.navigationView.setNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.nav_payment -> {
                        startActivity(Intent(this, PaymentActivity::class.java))
                        drawerLayout.closeDrawer(GravityCompat.START)
                        true
                    }
                    R.id.nav_feedback -> {
                        startActivity(Intent(this, FeedbackActivity::class.java))
                        drawerLayout.closeDrawer(GravityCompat.START)
                        true
                    }
                    R.id.nav_logout -> {
                        showLogoutConfirmationDialog()
                        true
                    }
                    else -> false
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error setting up navigation: ${e.message}", 
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLogoutConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun setupUI() {
        try {
            binding.homeAffairsCard.findViewById<Button>(R.id.btnHomeAffairsSelect)
                .setOnClickListener {
                    startActivity(Intent(this, HomeAffairsActivity::class.java))
                }

            binding.healthCard.findViewById<Button>(R.id.btnHealthSelect)
                .setOnClickListener {
                    startActivity(Intent(this, HealthActivity::class.java))
                }

            binding.transportCard.findViewById<Button>(R.id.btnTransportSelect)
                .setOnClickListener {
                    startActivity(Intent(this, TransportActivity::class.java))
                }

            binding.socialDevelopmentCard.findViewById<Button>(R.id.btnSocialSelect)
                .setOnClickListener {
                    startActivity(Intent(this, SocialDevelopmentActivity::class.java))
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Error setting up UI: ${e.message}", 
                Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}