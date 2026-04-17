package com.kalpcg.pulserelay.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.kalpcg.pulserelay.BuildConfig
import com.kalpcg.pulserelay.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        findPreference<Preference>("transient.app_version")?.summary =
            "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            findPreference<Preference>("system")?.isEnabled = false
            findPreference<Preference>("system.disable_battery_optimizations")?.summary =
                getString(R.string.battery_optimization_is_not_supported_on_this_device)
        } else {
            findPreference<Preference>("system.disable_battery_optimizations")?.summary =
                if (isIgnoringBatteryOptimizations()) getString(R.string.disabled) else getString(R.string.enabled)
        }
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Remove dividers and add padding to allow the preferences to scroll completely
        setDivider(null)
        listView.setPadding(listView.paddingLeft, listView.paddingTop, listView.paddingRight, 300)
        listView.clipToPadding = false
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        if (preference.key == "system.disable_battery_optimizations") {
            requestIgnoreBatteryOptimizations()
            return true
        }

        return super.onPreferenceTreeClick(preference)
    }

    private fun requestIgnoreBatteryOptimizations() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Toast.makeText(
                requireContext(),
                getString(R.string.battery_optimization_is_not_supported_on_this_device),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (isIgnoringBatteryOptimizations()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.battery_optimization_already_disabled),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        intent.data = Uri.parse("package:${requireContext().packageName}")
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(requireContext().packageName)
    }

    companion object {
        fun newInstance() = SettingsFragment()
    }
}