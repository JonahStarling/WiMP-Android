package com.jonahstarling.stravaweeklyplanner

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.util.*
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import org.json.JSONObject


class LoginFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        view.findViewById<ProgressBar>(R.id.loader).indeterminateDrawable.setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.MULTIPLY)
        view.findViewById<Button>(R.id.login_button).setOnClickListener {
            loginWithStrava()
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val responseData = activity?.intent?.data
        if (responseData != null) {
            animateToLoading(true)
            val error = responseData.getQueryParameter("error")
            val status = responseData.getQueryParameter("status")
            if (error == null && status == null) {
                val code: String? = responseData.getQueryParameter("code")
                code?.let {
                    finishStravaAuth(code)
                }
            } else {
                animateToLoading(false)
                Toast.makeText(
                    context,
                    "There was a problem logging in. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            animateToLoading(false)
        }
    }

    private fun loginWithStrava() {
        animateToLoading(true)
        val intentUri = Uri.parse("https://www.strava.com/oauth/mobile/authorize")
            .buildUpon()
            .appendQueryParameter("client_id", getString(R.string.strava_client_id))
            .appendQueryParameter("redirect_uri", getString(R.string.redirect_uri))
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("approval_prompt", "auto")
            .appendQueryParameter("scope", "activity:read_all")
            .build()

        val intent = Intent(Intent.ACTION_VIEW, intentUri)
        startActivity(intent)
    }

    private fun finishStravaAuth(code: String) {
        val requestQueue = Volley.newRequestQueue(context)
        val url = "https://www.strava.com/oauth/token"
        val request = object: StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->
                val jsonResponse = JSONObject(response)
                val preferences = PreferenceManager.getDefaultSharedPreferences(context)
                val editor = preferences.edit()
                editor.putString("refresh_token", jsonResponse.getString("refresh_token"))
                editor.putString("access_token", jsonResponse.getString("access_token"))
                // TODO: Save the following to Firebase
                editor.putString("athlete_id", jsonResponse.getJSONObject("athlete").getString("id"))
                editor.putString("first_name", jsonResponse.getJSONObject("athlete").getString("firstname"))
                editor.putString("profile_medium", jsonResponse.getJSONObject("athlete").getString("profile_medium"))
                editor.putString("profile", jsonResponse.getJSONObject("athlete").getString("profile"))
                editor.apply()
                (activity as MainActivity).replaceFragment(OnboardingFragment.newInstance(), OnboardingFragment.TAG)
            },
            Response.ErrorListener {
                animateToLoading(false)
                Toast.makeText(context, "There was a problem logging in. Please try again.", Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["client_id"] = getString(R.string.strava_client_id)
                params["client_secret"] = getString(R.string.strava_client_secret)
                params["code"] = code
                params["grant_type"] = "authorization_code"
                return params
            }
        }
        requestQueue.add(request)
    }

    private fun animateToLoading(loading: Boolean) {
        if (loading) {
            view?.findViewById<Button>(R.id.login_button)?.visibility = View.INVISIBLE
            view?.findViewById<ProgressBar>(R.id.loader)?.visibility = View.VISIBLE
        } else {
            view?.findViewById<Button>(R.id.login_button)?.visibility = View.VISIBLE
            view?.findViewById<ProgressBar>(R.id.loader)?.visibility = View.INVISIBLE
        }
    }

    companion object {
        val TAG = LoginFragment::class.java.simpleName

        fun newInstance() = LoginFragment()
    }
}
