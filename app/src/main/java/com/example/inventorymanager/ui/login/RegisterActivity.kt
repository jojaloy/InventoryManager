package com.inventory.manager.ui.login

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.inventory.manager.databinding.ActivityRegisterBinding
import com.inventory.manager.model.ApiResult
import com.inventory.manager.model.User
import com.inventory.manager.utils.hideKeyboard
import com.inventory.manager.utils.snackbar
import com.inventory.manager.viewmodel.AuthViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            hideKeyboard()
            attemptRegister()
        }

        binding.tvLoginRedirect.setOnClickListener {
            finish() // Return to LoginActivity
        }

        observeViewModel()
    }

    private fun attemptRegister() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        var isValid = true

        if (username.isBlank()) {
            binding.tilUsername.error = "Username is required"
            isValid = false
        } else binding.tilUsername.error = null

        if (password.isBlank()) {
            binding.tilPassword.error = "Password is required"
            isValid = false
        } else binding.tilPassword.error = null

        if (!isValid) return

        authViewModel.register(User(username = username, password = password))
    }

    private fun observeViewModel() {
        authViewModel.registerResult.observe(this) { result ->
            when (result) {
                is ApiResult.Loading -> {
                    binding.btnRegister.isEnabled = false
                    binding.btnRegister.text = "Registering..."
                }
                is ApiResult.Success -> {
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Register"
                    binding.root.snackbar("Registration successful! You can now log in.")
                    // Optional delay to let user read the snackbar, or just finish immediately
                    binding.root.postDelayed({ finish() }, 1500)
                }
                is ApiResult.Error -> {
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Register"
                    binding.root.snackbar(result.message)
                }
            }
        }
    }
}