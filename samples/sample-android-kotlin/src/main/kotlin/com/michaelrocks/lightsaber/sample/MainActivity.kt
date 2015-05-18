/*
 * Copyright 2015 Michael Rozumyanskiy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.michaelrocks.lightsaber.sample

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.michaelrocks.lightsaber.Lightsaber
import com.michaelrocks.lightsaber.kotlin.R
import javax.inject.Inject
import javax.inject.Provider

public class MainActivity : Activity() {
    Inject
    private var wookiee: Wookiee? = null
    Inject
    private var wookieeProvider: Provider<Wookiee>? = null

    Inject
    private var droid: Droid? = null
    Inject
    private var droidProvider: Provider<Droid>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        val beforeInjectionTextView = findViewById(R.id.beforeInjectionTextView) as TextView
        val afterInjectionTextView = findViewById(R.id.afterInjectionTextView) as TextView

        print(beforeInjectionTextView, "Wookiee: $wookiee")
        print(beforeInjectionTextView, "Droid: $droid")

        val injector = Lightsaber.createInjector(LightsaberModule())
        injector.injectMembers(this)

        print(afterInjectionTextView, "Wookiee: $wookiee from ${wookiee!!.planet}")
        val anotherWookiee = wookieeProvider!!.get()
        print(afterInjectionTextView, "Another wookiee: $anotherWookiee from ${anotherWookiee.planet}")
        print(afterInjectionTextView, "Droid: $droid")
        val anotherDroid = droidProvider!!.get()
        print(afterInjectionTextView, "Another droid: $anotherDroid")
    }

    private fun print(textView: TextView, message: CharSequence) {
        textView.append("\n")
        textView.append(message)
    }
}