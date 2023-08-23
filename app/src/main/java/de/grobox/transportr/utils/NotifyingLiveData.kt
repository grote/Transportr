/*
 *  Copyright 2017 Google Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package de.grobox.transportr.utils

import androidx.annotation.MainThread
import javax.annotation.ParametersAreNonnullByDefault
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import de.grobox.transportr.utils.NotifyingLiveData
import java.util.concurrent.atomic.AtomicBoolean

//todo: change description!
/**
 * A lifecycle-aware observable that sends only new updates after subscription, used for events like
 * navigation and Snackbar messages.
 *
 *
 * This avoids a common problem with events: on configuration change (like rotation) an update
 * can be emitted if the observer is active. This LiveData only calls the observable if there's an
 * explicit call to setValue() or call().
 *
 *
 * Note that only one observer is going to be notified of changes.
 */
@ParametersAreNonnullByDefault
class NotifyingLiveData<T>(private val callback: OnActivationCallback) : MutableLiveData<T>() {

    @MainThread
    override fun onActive() {
        super.onActive()
        callback.onActive()
    }

    override fun onInactive() {
        super.onInactive()
        callback.onInactive()
    }

    interface OnActivationCallback {
        fun onActive()
        fun onInactive()
    }
}