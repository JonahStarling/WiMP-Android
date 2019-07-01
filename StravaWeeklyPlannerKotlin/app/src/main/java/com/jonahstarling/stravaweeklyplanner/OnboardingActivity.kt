package com.jonahstarling.stravaweeklyplanner

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.preference.PreferenceManager
import android.support.constraint.ConstraintLayout
import android.view.animation.LinearInterpolator
import android.widget.TextView
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        val profileImage = findViewById<CircleImageView>(R.id.profile_image)
        val onboardingText = findViewById<TextView>(R.id.onboarding_text)
        val tapToStartText = findViewById<TextView>(R.id.tap_to_start)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val profile = preferences.getString("profile", "")
        val name = preferences.getString("first_name", "")

        if (!profile.isNullOrEmpty()) {
            Glide.with(this@OnboardingActivity).load(profile).into(profileImage)
        }

        var welcomeText = "Welcome"
        if (!name.isNullOrEmpty()) {
            welcomeText += " $name"
        }
        welcomeText += ", we're here to help you plan and track your daily/weekly mileage goals"
        onboardingText.text = welcomeText

        val onboardingAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
        onboardingAnimator.duration = 750L
        onboardingAnimator.startDelay = 500L
        onboardingAnimator.interpolator = LinearInterpolator()
        onboardingAnimator.addUpdateListener {
            profileImage.alpha = onboardingAnimator.animatedValue as Float
            val profileImageParams = profileImage.layoutParams as ConstraintLayout.LayoutParams
            profileImageParams.verticalBias = 0.45f - ((onboardingAnimator.animatedValue as Float) / 10.0f)
            profileImage.layoutParams = profileImageParams
            onboardingText.alpha = onboardingAnimator.animatedValue as Float
            //TODO: Logic for other onboarding info points | come back to if you have time
        }
        onboardingAnimator.start()

        val tapToStartAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
        tapToStartAnimator.duration = 1000L
        tapToStartAnimator.startDelay = 2000L
        tapToStartAnimator.repeatMode = ValueAnimator.REVERSE
        tapToStartAnimator.repeatCount = ValueAnimator.INFINITE
        tapToStartAnimator.interpolator = LinearInterpolator()
        tapToStartAnimator.addUpdateListener {
            tapToStartText.alpha = tapToStartAnimator.animatedValue as Float
        }
        tapToStartAnimator.start()

        findViewById<ConstraintLayout>(R.id.onboarding).setOnClickListener {
            val intent = Intent(this@OnboardingActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            startActivity(intent)
        }

    }

}