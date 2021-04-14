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
package org.linphone.activities.call.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.linphone.LinphoneApplication.Companion.coreContext
import org.linphone.core.Call
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.utils.Event
import org.linphone.utils.PermissionHelper

class CallsViewModel : ViewModel() {
    val currentCallViewModel = MutableLiveData<CallViewModel>()

    val callPausedByRemote = MutableLiveData<Boolean>()

    val pausedCalls = MutableLiveData<ArrayList<CallViewModel>>()

    val conferenceCalls = MutableLiveData<ArrayList<CallViewModel>>()

    val isConferencePaused = MutableLiveData<Boolean>()

    val noMoreCallEvent: MutableLiveData<Event<Boolean>> by lazy {
        MutableLiveData<Event<Boolean>>()
    }

    val callUpdateEvent: MutableLiveData<Event<Call>> by lazy {
        MutableLiveData<Event<Call>>()
    }

    val askWriteExternalStoragePermissionEvent: MutableLiveData<Event<Boolean>> by lazy {
        MutableLiveData<Event<Boolean>>()
    }

    private val listener = object : CoreListenerStub() {
        override fun onCallStateChanged(
            core: Core,
            call: Call,
            state: Call.State,
            message: String
        ) {
            callPausedByRemote.value = state == Call.State.PausedByRemote
            isConferencePaused.value = !coreContext.core.isInConference

            val currentCall = core.currentCall
            if (currentCall == null) {
                currentCallViewModel.value = null
            } else if (currentCallViewModel.value == null) {
                currentCallViewModel.value = CallViewModel(currentCall)
            }

            if (state == Call.State.End || state == Call.State.Released || state == Call.State.Error) {
                if (core.callsNb == 0) {
                    noMoreCallEvent.value = Event(true)
                    conferenceCalls.value = arrayListOf()
                } else {
                    removeCallFromPausedListIfPresent(call)
                    removeCallFromConferenceIfPresent(call)
                }
            } else if (state == Call.State.Paused) {
                addCallToPausedList(call)
                removeCallFromConferenceIfPresent(call)
            } else if (state == Call.State.Resuming) {
                removeCallFromPausedListIfPresent(call)
            } else if (call.state == Call.State.UpdatedByRemote) {
                // If the correspondent asks to turn on video while audio call,
                // defer update until user has chosen whether to accept it or not
                val remoteVideo = call.remoteParams?.videoEnabled() ?: false
                val localVideo = call.currentParams.videoEnabled()
                val autoAccept = call.core.videoActivationPolicy.automaticallyAccept
                if (remoteVideo && !localVideo && !autoAccept) {
                    call.deferUpdate()
                    callUpdateEvent.value = Event(call)
                }
            } else {
                if (state == Call.State.StreamsRunning) {
                    callUpdateEvent.value = Event(call)
                }

                if (call.conference != null) {
                    addCallToConferenceListIfNotAlreadyInIt(call)
                } else {
                    removeCallFromConferenceIfPresent(call)
                }
            }
        }
    }

    init {
        coreContext.core.addListener(listener)

        val currentCall = coreContext.core.currentCall
        if (currentCall != null) {
            currentCallViewModel.value = CallViewModel(currentCall)
        }
        callPausedByRemote.value = currentCall?.state == Call.State.PausedByRemote
        isConferencePaused.value = !coreContext.core.isInConference

        val conferenceList = arrayListOf<CallViewModel>()
        for (call in coreContext.core.calls) {
            if (call.state == Call.State.Paused || call.state == Call.State.Pausing) {
                addCallToPausedList(call)
            } else {
                if (call.conference != null) {
                    conferenceList.add(CallViewModel(call))
                }
            }
        }
        conferenceCalls.value = conferenceList
    }

    override fun onCleared() {
        coreContext.core.removeListener(listener)

        super.onCleared()
    }

    fun answerCallVideoUpdateRequest(call: Call, accept: Boolean) {
        coreContext.answerCallVideoUpdateRequest(call, accept)
    }

    fun pauseConference() {
        if (coreContext.core.isInConference) {
            coreContext.core.leaveConference()
            isConferencePaused.value = true
        }
    }

    fun resumeConference() {
        if (!coreContext.core.isInConference) {
            coreContext.core.enterConference()
            isConferencePaused.value = false
        }
    }

    fun takeScreenshot() {
        if (!PermissionHelper.get().hasWriteExternalStorage()) {
            askWriteExternalStoragePermissionEvent.value = Event(true)
        } else {
            currentCallViewModel.value?.takeScreenshot()
        }
    }

    private fun addCallToPausedList(call: Call) {
        val list = arrayListOf<CallViewModel>()
        list.addAll(pausedCalls.value.orEmpty())

        val viewModel = CallViewModel(call)
        list.add(viewModel)
        pausedCalls.value = list
    }

    private fun removeCallFromPausedListIfPresent(call: Call) {
        val list = arrayListOf<CallViewModel>()
        list.addAll(pausedCalls.value.orEmpty())

        for (pausedCallViewModel in list) {
            if (pausedCallViewModel.call == call) {
                list.remove(pausedCallViewModel)
                break
            }
        }

        pausedCalls.value = list
    }

    private fun addCallToConferenceListIfNotAlreadyInIt(call: Call) {
        val list = arrayListOf<CallViewModel>()
        list.addAll(conferenceCalls.value.orEmpty())

        for (viewModel in list) {
            if (viewModel.call == call) return
        }

        val viewModel = CallViewModel(call)
        list.add(viewModel)
        conferenceCalls.value = list
    }

    private fun removeCallFromConferenceIfPresent(call: Call) {
        val list = arrayListOf<CallViewModel>()
        list.addAll(conferenceCalls.value.orEmpty())

        for (viewModel in list) {
            if (viewModel.call == call) {
                list.remove(viewModel)
                break
            }
        }

        conferenceCalls.value = list
    }
}