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
package org.linphone.activities.main.chat.viewmodels

import androidx.lifecycle.MutableLiveData
import org.linphone.LinphoneApplication.Companion.coreContext
import org.linphone.R
import org.linphone.contact.GenericContactViewModel
import org.linphone.core.ChatRoomSecurityLevel
import org.linphone.core.Participant
import org.linphone.utils.LinphoneUtils

class DevicesListGroupViewModel(private val participant: Participant) : GenericContactViewModel(participant.address) {
    override val securityLevel: ChatRoomSecurityLevel
        get() = participant.securityLevel

    private val device = if (participant.devices.isEmpty()) null else participant.devices.first()

    val securityLevelIcon: Int by lazy {
        when (device?.securityLevel) {
            ChatRoomSecurityLevel.Safe -> R.drawable.security_2_indicator
            ChatRoomSecurityLevel.Encrypted -> R.drawable.security_1_indicator
            else -> R.drawable.security_alert_indicator
        }
    }

    val securityLevelContentDescription: Int by lazy {
        when (device?.securityLevel) {
            ChatRoomSecurityLevel.Safe -> R.string.content_description_security_level_safe
            ChatRoomSecurityLevel.Encrypted -> R.string.content_description_security_level_encrypted
            else -> R.string.content_description_security_level_unsafe
        }
    }

    val sipUri: String get() = LinphoneUtils.getDisplayableAddress(participant.address)

    val isExpanded = MutableLiveData<Boolean>()

    val devices = MutableLiveData<ArrayList<DevicesListChildViewModel>>()

    init {
        isExpanded.value = false

        val list = arrayListOf<DevicesListChildViewModel>()
        for (device in participant.devices) {
            list.add(DevicesListChildViewModel((device)))
        }
        devices.value = list
    }

    fun toggleExpanded() {
        isExpanded.value = isExpanded.value != true
    }

    fun onClick() {
        if (device?.address != null) coreContext.startCall(device.address, true)
    }
}