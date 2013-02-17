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

public class MemoryManagementActivity extends PreferenceActivity implements
        OnPreferenceChangeListener {

    private static final String COMPCACHE_PREF = "pref_compcache_size";
    private static final String COMPCACHE_PERSIST_PROP = "persist.service.compcache";
    private static final String COMPCACHE_DEFAULT = SystemProperties.get("ro.compcache.default");
    private static final String PURGEABLE_ASSETS_PREF = "pref_purgeable_assets";
    private static final String PURGEABLE_ASSETS_PERSIST_PROP = "persist.sys.purgeable_assets";
    private static final String PURGEABLE_ASSETS_DEFAULT = "0";
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
    private static final String LOCK_HOME_PREF = "pref_lock_home";
    private static final String LOCK_MMS_PREF = "pref_lock_mms";
    private static final int LOCK_HOME_DEFAULT = 0;
    private static final int LOCK_MMS_DEFAULT = 0;
    private static final String SCROLLINGCACHE_PREF = "pref_scrollingcache";
    private static final String SCROLLINGCACHE_PERSIST_PROP = "persist.sys.scrollingcache";
    private static final String SCROLLINGCACHE_DEFAULT = "1";
    private static final String HEAPSIZE_PREF = "pref_heapsize";
    private static final String HEAPSIZE_PROP = "dalvik.vm.heapsize";
    private static final String HEAPSIZE_PERSIST_PROP = "persist.sys.vm.heapsize";
    private static final String HEAPSIZE_DEFAULT = "16m";

    private CheckBoxPreference mPurgeableAssetsPref;
    private CheckBoxPreference mKSMPref;
    private CheckBoxPreference mLockHomePref;
    private CheckBoxPreference mLockMmsPref;
    private ListPreference mCompcachePref;
    private ListPreference mKSMSleepPref;
    private ListPreference mKSMScanPref;
    private ListPreference mScrollingCachePref;
    private ListPreference mHeapsizePref;
    private int swapAvailable = -1;
    private int ksmAvailable = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getPreferenceManager() != null) {

            addPreferencesFromResource(R.xml.memory_management);

            PreferenceScreen prefSet = getPreferenceScreen();

            String temp;

            mCompcachePref = (ListPreference) prefSet.findPreference(COMPCACHE_PREF);
            mPurgeableAssetsPref = (CheckBoxPreference) prefSet.findPreference(PURGEABLE_ASSETS_PREF);
            mKSMPref = (CheckBoxPreference) prefSet.findPreference(KSM_PREF);
            mKSMSleepPref = (ListPreference) prefSet.findPreference(KSM_SLEEP_PREF);
            mKSMScanPref = (ListPreference) prefSet.findPreference(KSM_SCAN_PREF);
            mLockHomePref = (CheckBoxPreference) prefSet.findPreference(LOCK_HOME_PREF);
            mLockMmsPref = (CheckBoxPreference) prefSet.findPreference(LOCK_MMS_PREF);
            mScrollingCachePref = (ListPreference) prefSet.findPreference(SCROLLINGCACHE_PREF);
            mHeapsizePref = (ListPreference) prefSet.findPreference(HEAPSIZE_PREF);

            if (isSwapAvailable()) {
                if (SystemProperties.get(COMPCACHE_PERSIST_PROP) == "1")
                    SystemProperties.set(COMPCACHE_PERSIST_PROP, COMPCACHE_DEFAULT);
                mCompcachePref.setValue(SystemProperties.get(COMPCACHE_PERSIST_PROP, COMPCACHE_DEFAULT));
                mCompcachePref.setOnPreferenceChangeListener(this);
            } else {
                prefSet.removePreference(mCompcachePref);
            }

            String purgeableAssets = SystemProperties.get(PURGEABLE_ASSETS_PERSIST_PROP,
                    PURGEABLE_ASSETS_DEFAULT);
            mPurgeableAssetsPref.setChecked("1".equals(purgeableAssets));

            if (isKsmAvailable()) {
                mKSMPref.setChecked(KSM_PREF_ENABLED.equals(ProcessorActivity.readOneLine(KSM_RUN_FILE)));
            } else {
                prefSet.removePreference(mKSMPref);
            }

            if (isKsmAvailable()) {
                temp = ProcessorActivity.readOneLine(KSM_SLEEP_RUN_FILE);
                mKSMSleepPref.setValue(temp);
                mKSMSleepPref.setOnPreferenceChangeListener(this);
            } else {
                prefSet.removePreference(mKSMSleepPref);
            }

            if (isKsmAvailable()) {
                temp = ProcessorActivity.readOneLine(KSM_SCAN_RUN_FILE);
                mKSMScanPref.setValue(temp);
                mKSMScanPref.setOnPreferenceChangeListener(this);
            } else {
                prefSet.removePreference(mKSMScanPref);
            }

            mLockHomePref.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.LOCK_HOME_IN_MEMORY, LOCK_HOME_DEFAULT) == 1);

            mLockMmsPref.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.LOCK_MMS_IN_MEMORY, LOCK_MMS_DEFAULT) == 1);

            mScrollingCachePref.setValue(SystemProperties.get(SCROLLINGCACHE_PERSIST_PROP,
                    SystemProperties.get(SCROLLINGCACHE_PERSIST_PROP, SCROLLINGCACHE_DEFAULT)));
            mScrollingCachePref.setOnPreferenceChangeListener(this);

            mHeapsizePref.setValue(SystemProperties.get(HEAPSIZE_PERSIST_PROP,
                    SystemProperties.get(HEAPSIZE_PROP, HEAPSIZE_DEFAULT)));
            mHeapsizePref.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public void onResume() {
        String temp;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        super.onResume();

        if (isKsmAvailable()) {
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
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (preference == mPurgeableAssetsPref) {
            SystemProperties.set(PURGEABLE_ASSETS_PERSIST_PROP,
                    mPurgeableAssetsPref.isChecked() ? "1" : "0");
            return true;
        }

        if (preference == mKSMPref) {
            ProcessorActivity.writeOneLine(KSM_RUN_FILE, mKSMPref.isChecked() ? "1" : "0");
            return true;
        }

        if (preference == mLockHomePref) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCK_HOME_IN_MEMORY,mLockHomePref.isChecked() ? 1 : 0);
            return true;
        }

        if (preference == mLockMmsPref) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCK_MMS_IN_MEMORY, mLockMmsPref.isChecked() ? 1 : 0);
            return true;
        }

        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mCompcachePref) {
            if (newValue != null) {
                SystemProperties.set(COMPCACHE_PERSIST_PROP, (String) newValue);
                return true;
            }
        }

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

        if (preference == mScrollingCachePref) {
            if (newValue != null) {
                SystemProperties.set(SCROLLINGCACHE_PERSIST_PROP, (String)newValue);
                return true;
            }
        }

        if (preference == mHeapsizePref) {
            if (newValue != null) {
                SystemProperties.set(HEAPSIZE_PERSIST_PROP, (String)newValue);
                return true;
            }
        }

        return false;
    }

    /**
     * Check if swap support is available on the system
     */
    private boolean isSwapAvailable() {
        if (swapAvailable < 0) {
            swapAvailable = new File("/proc/swaps").exists() ? 1 : 0;
        }
        return swapAvailable > 0;
    }

    /**
     * Check if KSM support is available on the system
     */
    private boolean isKsmAvailable() {
        if (ksmAvailable < 0) {
            ksmAvailable = new File(KSM_RUN_FILE).exists() ? 1 : 0;
        }
        return ksmAvailable > 0;
    }

}
