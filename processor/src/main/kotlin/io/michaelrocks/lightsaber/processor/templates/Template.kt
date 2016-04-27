/*
 * Copyright 2016 Michael Rozumyanskiy
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

package io.michaelrocks.lightsaber.processor.templates

import io.michaelrocks.lightsaber.processor.commons.immutable
import java.util.*

interface Template {
  fun newRenderer(): Renderer

  class Builder {
    private val lines = ArrayList<Line>()

    fun text(text: String) = apply {
      lines += Line.Text(text)
    }

    fun parameter(name: String, prefix: String) = apply {
      lines += Line.Parameter(name, prefix)
    }

    fun build(): Template = TemplateImpl(this)

    private class TemplateImpl(builder: Builder) : Template {
      val lines = builder.lines.immutable()

      override fun newRenderer(): Renderer {
        return RendererImpl(lines)
      }
    }
  }
}
