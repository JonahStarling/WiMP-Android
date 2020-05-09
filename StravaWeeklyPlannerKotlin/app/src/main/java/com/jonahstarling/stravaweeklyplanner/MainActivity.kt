package com.jonahstarling.stravaweeklyplanner

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity

class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (checkIfLoggedIn()) {
            showMainFragment()
        } else {
            showLoginFragment()
        }
    }

    private fun checkIfLoggedIn(): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        return preferences.getString("access_token", "") != ""
    }

    private fun showLoginFragment() {
        supportFragmentManager.beginTransaction()
            .add(R.id.fragmentContainer, LoginFragment.newInstance())
            .commit()
    }

    private fun showMainFragment() {
        supportFragmentManager.beginTransaction()
            .add(R.id.fragmentContainer, MainFragment.newInstance())
            .commit()
    }

    fun replaceFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment, tag)
            .commit()
    }
}
