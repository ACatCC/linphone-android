/*
 * Copyright (c) 2010-2020 Belledonne Communications SARL.
 *
 * This file is part of linphone-android
 * (see https://www.linphone.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.linphone.activities.main.settings.viewmodels

import androidx.lifecycle.MutableLiveData
import org.linphone.LinphoneApplication.Companion.coreContext
import org.linphone.R
import org.linphone.activities.main.settings.SettingListenerStub
import org.linphone.core.Factory
import org.linphone.mediastream.Version
import org.linphone.utils.AppUtils
import org.linphone.utils.Event

class AdvancedSettingsViewModel : GenericSettingsViewModel() {
    val debugModeListener = object : SettingListenerStub() {
        override fun onBoolValueChanged(newValue: Boolean) {
            prefs.debugLogs = newValue
            val appName = AppUtils.getString(R.string.app_name)
            Factory.instance().setDebugMode(prefs.debugLogs, appName)
        }
    }
    val debugMode = MutableLiveData<Boolean>()

    val backgroundModeListener = object : SettingListenerStub() {
        override fun onBoolValueChanged(newValue: Boolean) {
            prefs.keepServiceAlive = newValue

            if (newValue) {
                coreContext.notificationsManager.startForeground()
            } else {
                coreContext.notificationsManager.stopForegroundNotificationIfPossible()
            }
        }
    }
    val backgroundMode = MutableLiveData<Boolean>()
    val backgroundModeEnabled = MutableLiveData<Boolean>()

    val autoStartListener = object : SettingListenerStub() {
        override fun onBoolValueChanged(newValue: Boolean) {
            prefs.autoStart = newValue
        }
    }
    val autoStart = MutableLiveData<Boolean>()

    val darkModeListener = object : SettingListenerStub() {
        override fun onListValueChanged(position: Int) {
            darkModeIndex.value = position
            val value = darkModeValues[position]
            prefs.darkMode = value
            setNightModeEvent.value = Event(value)
        }
    }
    val darkModeIndex = MutableLiveData<Int>()
    val darkModeLabels = MutableLiveData<ArrayList<String>>()
    private val darkModeValues = arrayListOf(-1, 0, 1)
    val setNightModeEvent = MutableLiveData<Event<Int>>()

    val fullScreenListener = object : SettingListenerStub() {
        override fun onBoolValueChanged(newValue: Boolean) {
            prefs.fullScreen = newValue
            fullScreenChangedEvent.value = Event(newValue)
        }
    }
    val fullScreen = MutableLiveData<Boolean>()
    val fullScreenChangedEvent = MutableLiveData<Event<Boolean>>()

    val deviceNameListener = object : SettingListenerStub() {
        override fun onTextValueChanged(newValue: String) {
            prefs.deviceName = newValue
        }
    }
    val deviceName = MutableLiveData<String>()

    val remoteProvisioningUrlListener = object : SettingListenerStub() {
        override fun onTextValueChanged(newValue: String) {
            if (newValue.isEmpty()) {
                core.provisioningUri = null
            } else {
                core.provisioningUri = newValue
            }
        }
    }
    val remoteProvisioningUrl = MutableLiveData<String>()

    val logsServerUrlListener = object : SettingListenerStub() {
        override fun onTextValueChanged(newValue: String) {
            core.logCollectionUploadServerUrl = newValue
        }
    }
    val logsServerUrl = MutableLiveData<String>()

    val goToBatterySettingsListener = object : SettingListenerStub() {
        override fun onClicked() {
            goToBatterySettingsEvent.value = Event(true)
        }
    }
    val goToBatterySettingsEvent = MutableLiveData<Event<Boolean>>()
    val batterySettingsVisibility = MutableLiveData<Boolean>()

    val goToPowerManagerSettingsListener = object : SettingListenerStub() {
        override fun onClicked() {
            goToPowerManagerSettingsEvent.value = Event(true)
        }
    }
    val goToPowerManagerSettingsEvent = MutableLiveData<Event<Boolean>>()
    val powerManagerSettingsVisibility = MutableLiveData<Boolean>()

    val goToAndroidSettingsListener = object : SettingListenerStub() {
        override fun onClicked() {
            goToAndroidSettingsEvent.value = Event(true)
        }
    }
    val goToAndroidSettingsEvent = MutableLiveData<Event<Boolean>>()

    init {
        debugMode.value = prefs.debugLogs
        backgroundMode.value = prefs.keepServiceAlive
        autoStart.value = prefs.autoStart

        val labels = arrayListOf<String>()
        labels.add(prefs.getString(R.string.advanced_settings_dark_mode_label_auto))
        labels.add(prefs.getString(R.string.advanced_settings_dark_mode_label_no))
        labels.add(prefs.getString(R.string.advanced_settings_dark_mode_label_yes))
        darkModeLabels.value = labels
        darkModeIndex.value = darkModeValues.indexOf(prefs.darkMode)

        fullScreen.value = prefs.fullScreen
        deviceName.value = prefs.deviceName
        remoteProvisioningUrl.value = core.provisioningUri
        logsServerUrl.value = core.logCollectionUploadServerUrl

        batterySettingsVisibility.value = Version.sdkAboveOrEqual(Version.API23_MARSHMALLOW_60)
    }
}