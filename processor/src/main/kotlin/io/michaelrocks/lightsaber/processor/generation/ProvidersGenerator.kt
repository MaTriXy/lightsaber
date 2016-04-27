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

package io.michaelrocks.lightsaber.processor.generation

import io.michaelrocks.grip.ClassRegistry
import io.michaelrocks.lightsaber.processor.generation.model.GenerationContext
import io.michaelrocks.lightsaber.processor.logging.getLogger
import io.michaelrocks.lightsaber.processor.model.InjectionContext

class ProvidersGenerator(
    private val classProducer: ClassProducer,
    private val classRegistry: ClassRegistry
) {
  private val logger = getLogger()

  fun generate(injectionContext: InjectionContext, generationContext: GenerationContext) {
    injectionContext.allComponents.asSequence()
        .flatMap { it.modules.asSequence() }
        .flatMap { it.providers.asSequence() }
        .forEach { provider ->
          logger.debug("Generating provider {}", provider.type.internalName)
          val generator = ProviderClassGenerator(classRegistry, generationContext.keyRegistry, provider)
          val providerClassData = generator.generate()
          classProducer.produceClass(provider.type.internalName, providerClassData)
        }
  }
}
