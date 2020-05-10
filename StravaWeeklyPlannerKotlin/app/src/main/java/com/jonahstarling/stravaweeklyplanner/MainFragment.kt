package com.jonahstarling.stravaweeklyplanner

import android.animation.ValueAnimator
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.help_dialog.*
import kotlinx.android.synthetic.main.settings_dialog.*
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.roundToInt


class MainFragment : Fragment() {

    var profile: String? = null
    var id: String? = null
    var accessToken: String? = null
    private var preferences: SharedPreferences? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_main, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferences = PreferenceManager.getDefaultSharedPreferences(context)

        profile = preferences?.getString("profile", "")
        id = preferences?.getString("athlete_id", "") ?: ""
        accessToken = preferences?.getString("access_token", "") ?: ""

        if (!profile.isNullOrEmpty()) {
            Glide.with(this@MainFragment).load(profile).into(profileImage)
        }

        logo.setOnClickListener { showHelpDialog() }
        fadedBackgroundHelp.setOnClickListener { hideHelpDialog() }
        dismissHelp.setOnClickListener { hideHelpDialog() }
        helpAbout.setOnClickListener { navigateToMediumPost() }

        profileImage.setOnClickListener { showSettingsDialog() }
        fadedBackgroundSettings.setOnClickListener { hideSettingsDialog() }
        dismissSettings.setOnClickListener { hideSettingsDialog() }
        logoutButton.setOnClickListener { logout() }
        measurementMile.setOnClickListener { milesSelected() }
        measurementKilometer.setOnClickListener { kilometerSelected() }

        val mainAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
        mainAnimator.duration = 500L
        mainAnimator.startDelay = 500L
        mainAnimator.interpolator = LinearInterpolator()
        mainAnimator.addUpdateListener {
            mondayRow.alpha = mainAnimator.animatedValue as Float
            tuesdayRow.alpha = mainAnimator.animatedValue as Float
            wednesdayRow.alpha = mainAnimator.animatedValue as Float
            thursdayRow.alpha = mainAnimator.animatedValue as Float
            fridayRow.alpha = mainAnimator.animatedValue as Float
            saturdayRow.alpha = mainAnimator.animatedValue as Float
            sundayRow.alpha = mainAnimator.animatedValue as Float

            val mondayParams = mondayRow.layoutParams as ConstraintLayout.LayoutParams
            mondayParams.verticalBias = 0.1f - ((mainAnimator.animatedValue as Float) / 10.0f)
            mondayRow.layoutParams = mondayParams
        }
        mainAnimator.start()

        refresh()
    }

    private fun showSettingsDialog() {
        settingsDialog.visibility = View.VISIBLE
        if (!profile.isNullOrEmpty()) {
            Glide.with(this@MainFragment).load(profile).into(settingsProfileImage)
        }
        val isMeasurementPreferenceMiles = preferences?.getBoolean("isMeasurementPreferenceMiles", true) ?: true
        if (isMeasurementPreferenceMiles) {
            measurementMile.setBackgroundColor(resources.getColor(R.color.colorPrimary, activity?.theme))
            measurementMile.setTextColor(Color.WHITE)
            measurementKilometer.setBackgroundColor(Color.WHITE)
            measurementKilometer.setTextColor(resources.getColor(R.color.colorText, activity?.theme))
        } else {
            measurementMile.setBackgroundColor(Color.WHITE)
            measurementMile.setTextColor(resources.getColor(R.color.colorText, activity?.theme))
            measurementKilometer.setBackgroundColor(resources.getColor(R.color.colorPrimary, activity?.theme))
            measurementKilometer.setTextColor(Color.WHITE)
        }
        settingsDialog.visibility = View.VISIBLE
        val settingsAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
        settingsAnimator.duration = 200L
        settingsAnimator.interpolator = LinearInterpolator()
        settingsAnimator.addUpdateListener {
            fadedBackgroundSettings.alpha = (settingsAnimator.animatedValue as Float) * 0.7f
            settingsProfileImage.alpha = settingsAnimator.animatedValue as Float
            settingsCardView.alpha = settingsAnimator.animatedValue as Float

            val settingsCardViewParams = settingsCardView.layoutParams as ConstraintLayout.LayoutParams
            settingsCardViewParams.verticalBias = 1.0f - ((settingsAnimator.animatedValue as Float) / 2.0f)
            settingsCardView.layoutParams = settingsCardViewParams
        }
        settingsAnimator.start()
    }

    private fun hideSettingsDialog() {
        val settingsAnimator = ValueAnimator.ofFloat(1.0f, 0.0f)
        settingsAnimator.duration = 200L
        settingsAnimator.interpolator = LinearInterpolator()
        settingsAnimator.addUpdateListener {
            fadedBackgroundSettings.alpha = (settingsAnimator.animatedValue as Float) * 0.7f
            settingsProfileImage.alpha = settingsAnimator.animatedValue as Float
            settingsCardView.alpha = settingsAnimator.animatedValue as Float

            val settingsCardViewParams = settingsCardView.layoutParams as ConstraintLayout.LayoutParams
            settingsCardViewParams.verticalBias = 1.0f - ((settingsAnimator.animatedValue as Float) / 2.0f)
            settingsCardView.layoutParams = settingsCardViewParams
            if (settingsAnimator.animatedValue == 0.0f) {
                settingsDialog.visibility = View.GONE
            }
        }
        settingsAnimator.start()
    }

    private fun showHelpDialog() {
        helpDialog.visibility = View.VISIBLE
        val helpAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
        helpAnimator.duration = 200L
        helpAnimator.interpolator = LinearInterpolator()
        helpAnimator.addUpdateListener {
            fadedBackgroundHelp.alpha = (helpAnimator.animatedValue as Float) * 0.7f
            helpCard.alpha = helpAnimator.animatedValue as Float

            val helpCardParams = helpCard.layoutParams as ConstraintLayout.LayoutParams
            helpCardParams.verticalBias = 1.0f - ((helpAnimator.animatedValue as Float) / 2.0f)
            helpCard.layoutParams = helpCardParams
        }
        helpAnimator.start()
    }

    private fun hideHelpDialog() {
        val helpAnimator = ValueAnimator.ofFloat(1.0f, 0.0f)
        helpAnimator.duration = 200L
        helpAnimator.interpolator = LinearInterpolator()
        helpAnimator.addUpdateListener {
            fadedBackgroundHelp.alpha = (helpAnimator.animatedValue as Float) * 0.7f
            helpCard.alpha = helpAnimator.animatedValue as Float

            val helpCardParams = helpCard.layoutParams as ConstraintLayout.LayoutParams
            helpCardParams.verticalBias = 1.0f - ((helpAnimator.animatedValue as Float) / 2.0f)
            helpCard.layoutParams = helpCardParams
            if (helpAnimator.animatedValue == 0.0f) {
                helpDialog.visibility = View.GONE
            }
        }
        helpAnimator.start()
    }

    private fun navigateToMediumPost() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://medium.com/@starling.jonah/a-weekly-mileage-planner-powered-by-strava-4f76ca9cae66?source=friends_link&sk=b1ae0119516aede21fca8569716e0eca"))
        startActivity(browserIntent)
    }

    private fun milesSelected() {
        measurementMile.setBackgroundColor(resources.getColor(R.color.colorPrimary, activity?.theme))
        measurementMile.setTextColor(Color.WHITE)
        measurementKilometer.setBackgroundColor(resources.getColor(R.color.white, activity?.theme))
        measurementKilometer.setTextColor(resources.getColor(R.color.colorText, activity?.theme))
        preferences?.edit()?.putBoolean("isMeasurementPreferenceMiles", true)?.apply().apply {
            refresh()
        }
    }

    private fun kilometerSelected() {
        measurementMile.setBackgroundColor(resources.getColor(R.color.white, activity?.theme))
        measurementMile.setTextColor(resources.getColor(R.color.colorText, activity?.theme))
        measurementKilometer.setBackgroundColor(resources.getColor(R.color.colorPrimary, activity?.theme))
        measurementKilometer.setTextColor(Color.WHITE)
        preferences?.edit()?.putBoolean("isMeasurementPreferenceMiles", false)?.apply().apply {
            refresh()
        }
    }

    private fun refresh() {
        id?.let { id ->
            accessToken?.let { accessToken ->
                fetchAthletesActivities(id, accessToken)
            } ?: logout()
        } ?: logout()
    }

    private fun fetchAthletesActivities(id: String, accessToken: String) {
        val requestQueue = Volley.newRequestQueue(context)
        val url = "https://www.strava.com/api/v3/athletes/$id/activities"
        val request = object: JsonArrayRequest(
            Method.GET, url, null,
            Response.Listener<JSONArray> { response ->
                parseActivities(response)
            },
            Response.ErrorListener {
                Toast.makeText(context, "There was a problem loading your activities. Please try again later.", Toast.LENGTH_LONG).show()
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $accessToken"
                return headers
            }
        }
        requestQueue.add(request)
    }

    private fun parseActivities(activities: JSONArray) {
        val thisWeeksActivities = HashMap<String, JSONArray>()
        thisWeeksActivities["Monday"] = JSONArray()
        thisWeeksActivities["Tuesday"] = JSONArray()
        thisWeeksActivities["Wednesday"] = JSONArray()
        thisWeeksActivities["Thursday"] = JSONArray()
        thisWeeksActivities["Friday"] = JSONArray()
        thisWeeksActivities["Saturday"] = JSONArray()
        thisWeeksActivities["Sunday"] = JSONArray()

        val calendar = Calendar.getInstance()

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.clear(Calendar.MINUTE)
        calendar.clear(Calendar.SECOND)
        calendar.clear(Calendar.MILLISECOND)

        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek + 1) // Add one to get Monday (the true first day of the week)

        val beginningOfWeekMillis = calendar.timeInMillis

        var allFromThisWeekFound = false
        var i = 0
        while (i < activities.length() && !allFromThisWeekFound) {
            val activity = activities.getJSONObject(i)

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)
            val activityDate = LocalDate.parse(activity.getString("start_date_local"), formatter)
            val activityMillis = activityDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            if (activityMillis >= beginningOfWeekMillis) {
                val activityDayOfWeek = activityDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                val daysActivities = thisWeeksActivities[activityDayOfWeek] as JSONArray
                daysActivities.put(activity)
            } else {
                allFromThisWeekFound = true
            }
            i +=1
        }

        renderActivities(thisWeeksActivities)
    }

    private fun renderActivities(thisWeeksActivities: HashMap<String, JSONArray>) {
        // Monday
        thisWeeksActivities["Monday"]?.let {
            val dailyTotals = calculateDailyTotals(it)
            mondayRow?.setDaysActivityData(dailyTotals)
        }

        // Tuesday
        thisWeeksActivities["Tuesday"]?.let {
            val dailyTotals = calculateDailyTotals(it)
            tuesdayRow?.setDaysActivityData(dailyTotals)
        }

        // Wednesday
        thisWeeksActivities["Wednesday"]?.let {
            val dailyTotals = calculateDailyTotals(it)
            wednesdayRow?.setDaysActivityData(dailyTotals)
        }

        // Thursday
        thisWeeksActivities["Thursday"]?.let {
            val dailyTotals = calculateDailyTotals(it)
            thursdayRow?.setDaysActivityData(dailyTotals)
        }

        // Friday
        thisWeeksActivities["Friday"]?.let {
            val dailyTotals = calculateDailyTotals(it)
            fridayRow?.setDaysActivityData(dailyTotals)
        }

        // Saturday
        thisWeeksActivities["Saturday"]?.let {
            val dailyTotals = calculateDailyTotals(it)
            saturdayRow?.setDaysActivityData(dailyTotals)
        }

        // Sunday
        thisWeeksActivities["Sunday"]?.let {
            val dailyTotals = calculateDailyTotals(it)
            sundayRow?.setDaysActivityData(dailyTotals)
        }
    }

    private fun calculateDailyTotals(dayActivities: JSONArray): HashMap<String, String> {
        val dailyTotals = HashMap<String, String>()
        var mileage = 0L
        var time = 0L
        var climb = 0.0
        var activityUrl = ""

        if (dayActivities.length() > 0) {
            val id = (dayActivities[0] as JSONObject).getLong("id")
            activityUrl = "https://www.strava.com/activities/$id"
            for (i in 0 until dayActivities.length()) {
                val activity = dayActivities[i] as JSONObject
                mileage += activity.getLong("distance")
                time += activity.getLong("moving_time")
                climb += activity.getDouble("total_elevation_gain")
            }
        }

        var mileageString = "0"
        var climbString = "${climb.roundToInt()}m"
        var mileageConverted: Double = mileage / 1000.0
        val isMeasurementPreferenceMiles = preferences?.getBoolean("isMeasurementPreferenceMiles", true) ?: true
        if (isMeasurementPreferenceMiles) {
            mileageConverted *= 0.621371
            climb *= 3.28084
            climbString = "${climb.roundToInt()}ft"
        }
        when {
            mileageConverted > 100.0 -> mileageString = mileageConverted.roundToInt().toString()
            mileageConverted > 10.0 -> mileageString = "%.1f".format(mileageConverted)
            mileageConverted > 0.0 -> mileageString = "%.2f".format(mileageConverted)
        }

        dailyTotals["actualMileage"] = mileageString
        dailyTotals["statBoxOne"] = "${time / 60}m ${time % 60}s"
        dailyTotals["statBoxTwo"] = climbString
        dailyTotals["activityUrl"] = activityUrl

        return dailyTotals
    }

    private fun logout() {
        preferences?.edit()?.clear()?.apply()
        (activity as MainActivity).replaceFragment(LoginFragment.newInstance(), LoginFragment.TAG)
    }

    companion object {
        val TAG = MainFragment::class.java.simpleName

        fun newInstance() = MainFragment()
    }
}
