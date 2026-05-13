package com.inventory.manager.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.inventory.manager.databinding.ActivitySplashBinding
import com.inventory.manager.ui.dashboard.DashboardActivity
import com.inventory.manager.ui.login.LoginActivity
import com.inventory.manager.utils.Constants

/**
 * SplashActivity — shown for 2 seconds on launch.
 * Routes to LoginActivity or DashboardActivity based on login state.
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)

        // Navigate after splash delay
        Handler(Looper.getMainLooper()).postDelayed({
            navigate()
        }, 2200)
    }

    private fun navigate() {
        val isLoggedIn = prefs.getBoolean(Constants.PREF_IS_LOGGED_IN, false)
        val intent = if (isLoggedIn) {
            Intent(this, DashboardActivity::class.java)
        } else {
            Intent(this, LoginActivity::class.java)
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
