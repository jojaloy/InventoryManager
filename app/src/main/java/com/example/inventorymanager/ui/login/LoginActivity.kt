package com.inventory.manager.ui.login

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.inventory.manager.databinding.ActivityLoginBinding
import com.inventory.manager.model.ApiResult
import com.inventory.manager.ui.dashboard.DashboardActivity
import com.inventory.manager.utils.Constants
import com.inventory.manager.utils.hideKeyboard
import com.inventory.manager.utils.snackbar
import com.inventory.manager.viewmodel.AuthViewModel

/**
 * LoginActivity — handles authentication with the MockAPI endpoint.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var prefs: SharedPreferences
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)

        binding.btnLogin.setOnClickListener {
            hideKeyboard()
            attemptLogin()
        }

        binding.etPassword.setOnEditorActionListener { _, _, _ ->
            hideKeyboard()
            attemptLogin()
            true
        }

        // Open Register screen
        binding.tvRegisterRedirect.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        observeViewModel()
    }

    private fun attemptLogin() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

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

        authViewModel.login(username)
    }

    private fun observeViewModel() {
        authViewModel.loginResult.observe(this) { result ->
            when (result) {
                is ApiResult.Loading -> {
                    binding.btnLogin.isEnabled = false
                    binding.btnLogin.text = "Signing in..."
                }
                is ApiResult.Success -> {
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "Sign In"

                    val user = result.data
                    if (user != null && user.password == binding.etPassword.text.toString().trim()) {
                        prefs.edit()
                            .putBoolean(Constants.PREF_IS_LOGGED_IN, true)
                            .putString(Constants.PREF_USERNAME, user.username)
                            .apply()

                        startActivity(Intent(this, DashboardActivity::class.java))
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        finish()
                    } else {
                        binding.root.snackbar("Invalid password.")
                    }
                }
                is ApiResult.Error -> {
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "Sign In"
                    binding.root.snackbar(result.message)
                }
            }
        }
    }
}