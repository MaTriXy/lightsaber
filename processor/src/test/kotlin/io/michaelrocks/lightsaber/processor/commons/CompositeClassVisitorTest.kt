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

package io.michaelrocks.lightsaber.processor.commons

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.objectweb.asm.Attribute
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.TypeReference

class CompositeClassVisitorTest {
  @Test
  fun testIsEmpty() {
    val compositeClassVisitor = CompositeClassVisitor()
    assertTrue(compositeClassVisitor.isEmpty)
  }

  @Test
  fun testIsNotEmpty() {
    val compositeClassVisitor = CompositeClassVisitor()
    compositeClassVisitor.addVisitor(mock<ClassVisitor>(ClassVisitor::class.java))
    assertFalse(compositeClassVisitor.isEmpty)
  }

  @Test
  fun testEmptyVisit() {
    val compositeClassVisitor = CompositeClassVisitor()
    compositeClassVisitor.visitEnd()
  }

  @Test
  fun testVisit() {
    val interfaces = arrayOf("Interface")
    verifyMethodInvocations(CompositeClassVisitor::class) {
      visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, "Name", "Signature", "Super", interfaces)
    }
  }

  @Test
  @Throws(Exception::class)
  fun testVisitSource() {
    verifyMethodInvocations(CompositeClassVisitor::class) { visitSource("Source", "Debug") }
  }

  @Test
  fun testVisitOuterClass() {
    verifyMethodInvocations(CompositeClassVisitor::class) { visitOuterClass("Owner", "Name", "Desc") }
  }

  @Test
  fun testVisitAnnotation() {
    verifyCompositeMethodInvocations(CompositeClassVisitor::class,
        { visitAnnotation("Desc", true) },
        { visitEnd() }
    )
  }

  @Test
  fun testVisitTypeAnnotation() {
    verifyCompositeMethodInvocations(CompositeClassVisitor::class,
        { visitTypeAnnotation(TypeReference.FIELD, null, "Desc", true) },
        { visitEnd() }
    )
  }

  @Test
  fun testVisitAttribute() {
    val attribute = mock<Attribute>(Attribute::class.java)
    verifyMethodInvocations(CompositeClassVisitor::class) { visitAttribute(attribute) }
  }

  @Test
  fun testVisitInnerClass() {
    verifyMethodInvocations(CompositeClassVisitor::class) {
      visitInnerClass("Name", "Outer", "Inner", Opcodes.ACC_PUBLIC)
    }
  }

  @Test
  fun testVisitField() {
    val value = Object()
    verifyCompositeMethodInvocations(CompositeClassVisitor::class,
        { visitField(Opcodes.ACC_PUBLIC, "Name", "Desc", "Signature", value) },
        { visitEnd() }
    )
  }

  @Test
  fun testVisitMethod() {
    val exceptions = arrayOf("Exception")
    verifyCompositeMethodInvocations(CompositeClassVisitor::class,
        { visitMethod(Opcodes.ACC_PUBLIC, "Name", "Desc", "Signature", exceptions) },
        { visitEnd() }
    )
  }

  @Test
  fun testVisitEnd() {
    verifyMethodInvocations(CompositeClassVisitor::class) { visitEnd() }
  }
}
