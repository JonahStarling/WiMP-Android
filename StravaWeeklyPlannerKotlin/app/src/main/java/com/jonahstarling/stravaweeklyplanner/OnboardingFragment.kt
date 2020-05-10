package com.jonahstarling.stravaweeklyplanner

import android.animation.ValueAnimator
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.TextView
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView

class OnboardingFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_onboarding, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val profileImage = view.findViewById<CircleImageView>(R.id.profileImage)
        val onboardingText = view.findViewById<TextView>(R.id.onboarding_text)
        val tapToStartText = view.findViewById<TextView>(R.id.tap_to_start)

        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val profile = preferences.getString("profile", "")
        val name = preferences.getString("first_name", "")

        if (!profile.isNullOrEmpty()) {
            Glide.with(this@OnboardingFragment).load(profile).into(profileImage)
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

        view.findViewById<ConstraintLayout>(R.id.onboarding).setOnClickListener {
            (activity as MainActivity).replaceFragment(MainFragment.newInstance(), MainFragment.TAG)
        }
    }

    companion object {
        val TAG = OnboardingFragment::class.java.simpleName

        fun newInstance() = OnboardingFragment()
    }
}