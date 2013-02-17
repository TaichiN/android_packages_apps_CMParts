/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyanogenmod.cmparts.activities;

import com.cyanogenmod.cmparts.R;

import java.io.File;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;

public class MemoryManagementKsmActivity extends PreferenceActivity implements
        OnPreferenceChangeListener {

    public static final String KSM_RUN_FILE = "/sys/kernel/mm/ksm/run";
    public static final String KSM_PREF = "pref_ksm";
    public static final String KSM_PREF_DISABLED = "0";
    public static final String KSM_PREF_ENABLED = "1";
    public static final String KSM_SLEEP_RUN_FILE = "/sys/kernel/mm/ksm/sleep_millisecs";
    public static final String KSM_SLEEP_PREF = "pref_ksm_sleep";
    private static final String KSM_SLEEP_PROP = "ksm_sleep_time";
    public static final String KSM_SLEEP_PREF_DEFAULT = "1500";
    public static final String KSM_SCAN_RUN_FILE = "/sys/kernel/mm/ksm/pages_to_scan";
    public static final String KSM_SCAN_PREF = "pref_ksm_scan";
    private static final String KSM_SCAN_PROP = "ksm_scan_time";
    public static final String KSM_SCAN_PREF_DEFAULT = "128";

    private CheckBoxPreference mKSMPref;
    private ListPreference mKSMSleepPref;
    private ListPreference mKSMScanPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getPreferenceManager() != null) {
            addPreferencesFromResource(R.xml.ksm_settings);
            PreferenceScreen prefSet = getPreferenceScreen();
            String temp;

            mKSMPref = (CheckBoxPreference) prefSet.findPreference(KSM_PREF);
            mKSMPref.setChecked(KSM_PREF_ENABLED.equals(ProcessorActivity.readOneLine(KSM_RUN_FILE)));

            mKSMSleepPref = (ListPreference) prefSet.findPreference(KSM_SLEEP_PREF);
            temp = ProcessorActivity.readOneLine(KSM_SLEEP_RUN_FILE);
            mKSMSleepPref.setValue(temp);
            mKSMSleepPref.setOnPreferenceChangeListener(this);

            mKSMScanPref = (ListPreference) prefSet.findPreference(KSM_SCAN_PREF);
            temp = ProcessorActivity.readOneLine(KSM_SCAN_RUN_FILE);
            mKSMScanPref.setValue(temp);
            mKSMScanPref.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public void onResume() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String temp;

        super.onResume();

        temp = prefs.getString(KSM_SCAN_PREF, null);
        if (temp == null) {
            temp = ProcessorActivity.readOneLine(KSM_SCAN_RUN_FILE);
            mKSMScanPref.setValue(temp);
        }

        temp = prefs.getString(KSM_SLEEP_PREF, null);
        if (temp == null) {
            temp = ProcessorActivity.readOneLine(KSM_SLEEP_RUN_FILE);
            mKSMSleepPref.setValue(temp);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(
        PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mKSMPref) {
            ProcessorActivity.writeOneLine(KSM_RUN_FILE, mKSMPref.isChecked() ? "1" : "0");
            return true;
        }

        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mKSMSleepPref) {
            if (newValue != null) {
                SystemProperties.set(KSM_SLEEP_PROP, (String)newValue);
                ProcessorActivity.writeOneLine(KSM_SLEEP_RUN_FILE, (String)newValue);
                return true;
            }
        }

        if (preference == mKSMScanPref) {
            if (newValue != null) {
                SystemProperties.set(KSM_SCAN_PROP, (String)newValue);
                ProcessorActivity.writeOneLine(KSM_SCAN_RUN_FILE, (String)newValue);
                return true;
            }
        }

        return false;
    }

}
