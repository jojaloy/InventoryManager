package com.inventory.manager.ui.login

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.inventory.manager.databinding.ActivityLoginBinding
import com.inventory.manager.ui.dashboard.DashboardActivity
import com.inventory.manager.utils.Constants
import com.inventory.manager.utils.hideKeyboard
import com.inventory.manager.utils.snackbar

/**
 * LoginActivity — simple credential screen.
 * Demo credentials: admin / admin123
 * (In a real app, this would hit an auth API.)
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)

        binding.btnLogin.setOnClickListener {
            hideKeyboard()
            attemptLogin()
        }

        // Allow "Enter" on keyboard to submit
        binding.etPassword.setOnEditorActionListener { _, _, _ ->
            hideKeyboard()
            attemptLogin()
            true
        }
    }

    private fun attemptLogin() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Reset errors
        binding.tilUsername.error = null
        binding.tilPassword.error = null

        var isValid = true

        if (username.isBlank()) {
            binding.tilUsername.error = "Username is required"
            isValid = false
        }

        if (password.isBlank()) {
            binding.tilPassword.error = "Password is required"
            isValid = false
        }

        if (!isValid) return

        // Demo login check
        if (username == "admin" && password == "admin123") {
            // Save login state
            prefs.edit()
                .putBoolean(Constants.PREF_IS_LOGGED_IN, true)
                .putString(Constants.PREF_USERNAME, username)
                .apply()

            startActivity(Intent(this, DashboardActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        } else {
            binding.root.snackbar("Invalid credentials. Try admin / admin123")
        }
    }
}
