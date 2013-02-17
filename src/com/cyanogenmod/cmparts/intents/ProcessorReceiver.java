/*
 * Copyright (C) 2011 The CyanogenMod Project
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

package com.cyanogenmod.cmparts.intents;

import com.cyanogenmod.cmparts.activities.ProcessorActivity;
import com.cyanogenmod.cmparts.activities.IOSchedulerActivity;
import com.cyanogenmod.cmparts.activities.MemoryManagementActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

public class ProcessorReceiver extends BroadcastReceiver {

    private static final String TAG = "ProcessorSettings";

    private static final String CPU_SETTINGS_PROP = "sys.cpufreq.restored";
    private static final String IOSCHED_SETTINGS_PROP = "sys.iosched.restored";
    private static final String KSM_SETTINGS_PROP = "sys.ksm.restored";

    @Override
    public void onReceive(Context ctx, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                setScreenOffCPU(ctx, true);
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                setScreenOffCPU(ctx, false);
        } else if (SystemProperties.getBoolean(CPU_SETTINGS_PROP, false) == false
                && intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
            SystemProperties.set(CPU_SETTINGS_PROP, "true");
            configureCPU(ctx);
        } else {
            SystemProperties.set(CPU_SETTINGS_PROP, "false");
        }

        if (ProcessorActivity.fileExists(IOSchedulerActivity.IOSCHED_LIST_FILE)
                && intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
            SystemProperties.set(IOSCHED_SETTINGS_PROP, "true");
            configureIOSched(ctx);
        } else {
            SystemProperties.set(IOSCHED_SETTINGS_PROP, "false");
        }

        if (ProcessorActivity.fileExists(MemoryManagementActivity.KSM_RUN_FILE)
                && intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
            SystemProperties.set(KSM_SETTINGS_PROP, "true");
            configureKSM(ctx);
        } else {
            SystemProperties.set(KSM_SETTINGS_PROP, "false");
        }

    }

    private void configureCPU(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        if (prefs.getBoolean(ProcessorActivity.SOB_PREF, false) == false) {
            Log.i(TAG, "Restore disabled by user preference.");
            return;
        }

        String governor = prefs.getString(ProcessorActivity.GOV_PREF, null);
        String minFrequency = prefs.getString(ProcessorActivity.MIN_FREQ_PREF, null);
        String maxFrequency = prefs.getString(ProcessorActivity.MAX_FREQ_PREF, null);
        String availableFrequenciesLine = ProcessorActivity.readOneLine(ProcessorActivity.FREQ_LIST_FILE);
        String availableGovernorsLine = ProcessorActivity.readOneLine(ProcessorActivity.GOVERNORS_LIST_FILE);
        boolean noSettings = ((availableGovernorsLine == null) || (governor == null)) && 
                             ((availableFrequenciesLine == null) || ((minFrequency == null) && (maxFrequency == null)));
        List<String> frequencies = null;
        List<String> governors = null;
        
        if (noSettings) {
            Log.d(TAG, "No settings saved. Nothing to restore.");
        } else {
            if (availableGovernorsLine != null){
                governors = Arrays.asList(availableGovernorsLine.split(" "));  
            }
            if (availableFrequenciesLine != null){
                frequencies = Arrays.asList(availableFrequenciesLine.split(" "));  
            }
            if (governor != null && governors != null && governors.contains(governor)) {
                ProcessorActivity.writeOneLine(ProcessorActivity.GOVERNOR, governor);
            }
            if (maxFrequency != null && frequencies != null && frequencies.contains(maxFrequency)) {
                ProcessorActivity.writeOneLine(ProcessorActivity.FREQ_MAX_FILE, maxFrequency);
            }
            if (minFrequency != null && frequencies != null && frequencies.contains(minFrequency)) {
                ProcessorActivity.writeOneLine(ProcessorActivity.FREQ_MIN_FILE, minFrequency);
            }
            Log.d(TAG, "CPU settings restored.");
        }
    }

    private void configureIOSched(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        if (prefs.getBoolean(IOSchedulerActivity.SOB_PREF, false) == false) {
            Log.i(TAG, "Restore disabled by user preference.");
            return;
        }

        String ioscheduler = prefs.getString(IOSchedulerActivity.IOSCHED_PREF, null);
        String availableIOSchedulersLine = ProcessorActivity.readOneLine(IOSchedulerActivity.IOSCHED_LIST_FILE);
        boolean noSettings = ((availableIOSchedulersLine == null) || (ioscheduler == null));
        List<String> ioschedulers = null;

        if (noSettings) {
            Log.d(TAG, "No I/O scheduler settings saved. Nothing to restore.");
        } else {
            if (availableIOSchedulersLine != null){
                ioschedulers = Arrays.asList(availableIOSchedulersLine.replace("[", "").replace("]", "").split(" "));
            }
            if (ioscheduler != null && ioschedulers != null && ioschedulers.contains(ioscheduler)) {
                ProcessorActivity.writeOneLine(IOSchedulerActivity.IOSCHED_LIST_FILE, ioscheduler);
            }
            Log.d(TAG, "I/O scheduler settings restored.");
        }
    }

    private void configureKSM(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        boolean ksm = prefs.getBoolean(MemoryManagementActivity.KSM_PREF, false);
        ProcessorActivity.writeOneLine(MemoryManagementActivity.KSM_SLEEP_RUN_FILE, prefs.getString(MemoryManagementActivity.KSM_SLEEP_PREF,
                                 MemoryManagementActivity.KSM_SLEEP_PREF_DEFAULT));
        ProcessorActivity.writeOneLine(MemoryManagementActivity.KSM_SCAN_RUN_FILE, prefs.getString(MemoryManagementActivity.KSM_SCAN_PREF,
                                 MemoryManagementActivity.KSM_SCAN_PREF_DEFAULT));
        ProcessorActivity.writeOneLine(MemoryManagementActivity.KSM_RUN_FILE, ksm ? "1" : "0");
        Log.d(TAG, "KSM settings restored.");
    }

    private void setScreenOffCPU(Context ctx, boolean screenOff) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String maxFrequency = prefs.getString(ProcessorActivity.MAX_FREQ_PREF, null);
        String maxSoFrequency = prefs.getString(ProcessorActivity.SO_MAX_FREQ_PREF, null);
        if (maxSoFrequency == null || maxFrequency == null) {
            Log.i(TAG, "Screen off or normal max CPU freq not saved. No change.");
        } else {
            if (screenOff) {
                ProcessorActivity.writeOneLine(ProcessorActivity.FREQ_MAX_FILE, maxSoFrequency);
                Log.i(TAG, "Screen off max CPU freq set");
            } else {
                ProcessorActivity.writeOneLine(ProcessorActivity.FREQ_MAX_FILE, maxFrequency);
                Log.i(TAG, "Normal (screen on) max CPU freq restored");
            }
        }
    }

}
