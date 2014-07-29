/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.util;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.util.serialization.InvokedSerializedForm;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link InvokedSerializedForm} class.
 */
@Test(groups = TestGroup.UNIT)
public class InvokedSerializedFormTest {

  protected static Object staticExample() {
    return "static-no-arg";
  }

  protected static Object getStaticExample() {
    return "static-prefixed-no-arg";
  }

  protected static Object staticExamplePrimitive(double foo) {
    return "static-primitive-arg-" + foo;
  }

  protected static Object getStaticExamplePrimitive(double foo) {
    return "static-prefixed-primitive-arg-" + foo;
  }

  protected static Object staticExample(Double foo) {
    return "static-object-arg-" + foo;
  }

  protected static Object getStaticExample(Double foo) {
    return "static-prefixed-object-arg-" + foo;
  }

  protected Object dynamicExample() {
    return "dynamic-no-arg";
  }

  protected Object getDynamicExample() {
    return "dynamic-prefixed-no-arg";
  }

  protected Object dynamicExamplePrimitive(double foo) {
    return "dynamic-primitive-arg-" + foo;
  }

  protected Object getDynamicExamplePrimitive(double foo) {
    return "dynamic-prefixed-primitive-arg-" + foo;
  }

  protected Object dynamicExample(Double foo) {
    return "dynamic-object-arg-" + foo;
  }

  protected Object getDynamicExample(Double foo) {
    return "dynamic-prefixed-object-arg-" + foo;
  }

  public void testNoArgStaticMethod() {
    InvokedSerializedForm obj = new InvokedSerializedForm(InvokedSerializedFormTest.class, "staticExample");
    assertEquals(obj.toString(), "InvokedSerializedForm[com.opengamma.analytics.util.InvokedSerializedFormTest, staticExample]");
    assertEquals(obj.readReplace(), "static-no-arg");
    obj = new InvokedSerializedForm(InvokedSerializedFormTest.class, "getStaticExample");
    assertEquals(obj.toString(), "InvokedSerializedForm[com.opengamma.analytics.util.InvokedSerializedFormTest, StaticExample]");
    assertEquals(obj.readReplace(), "static-prefixed-no-arg");
  }

  public void testNoArgDynamicMethod() {
    InvokedSerializedForm obj = new InvokedSerializedForm(this, "dynamicExample");
    assertEquals(obj.toString(), "InvokedSerializedForm[Test, dynamicExample]");
    assertEquals(obj.readReplace(), "dynamic-no-arg");
    obj = new InvokedSerializedForm(this, "getDynamicExample");
    assertEquals(obj.toString(), "InvokedSerializedForm[Test, DynamicExample]");
    assertEquals(obj.readReplace(), "dynamic-prefixed-no-arg");
  }

  public void testPrimitiveArgStaticMethod() {
    InvokedSerializedForm obj = new InvokedSerializedForm(InvokedSerializedFormTest.class, "staticExamplePrimitive", 42d);
    assertEquals(obj.toString(), "InvokedSerializedForm[com.opengamma.analytics.util.InvokedSerializedFormTest, staticExamplePrimitive, 42.0]");
    assertEquals(obj.readReplace(), "static-primitive-arg-42.0");
    obj = new InvokedSerializedForm(InvokedSerializedFormTest.class, "getStaticExamplePrimitive", 42d);
    assertEquals(obj.toString(), "InvokedSerializedForm[com.opengamma.analytics.util.InvokedSerializedFormTest, StaticExamplePrimitive, 42.0]");
    assertEquals(obj.readReplace(), "static-prefixed-primitive-arg-42.0");
  }

  public void testPrimitiveArgDynamicMethod() {
    InvokedSerializedForm obj = new InvokedSerializedForm(this, "dynamicExamplePrimitive", 42d);
    assertEquals(obj.toString(), "InvokedSerializedForm[Test, dynamicExamplePrimitive, 42.0]");
    assertEquals(obj.readReplace(), "dynamic-primitive-arg-42.0");
    obj = new InvokedSerializedForm(this, "getDynamicExamplePrimitive", 42d);
    assertEquals(obj.toString(), "InvokedSerializedForm[Test, DynamicExamplePrimitive, 42.0]");
    assertEquals(obj.readReplace(), "dynamic-prefixed-primitive-arg-42.0");
  }

  public void testObjectArgStaticMethod() {
    InvokedSerializedForm obj = new InvokedSerializedForm(InvokedSerializedFormTest.class, "staticExample", 42d);
    assertEquals(obj.toString(), "InvokedSerializedForm[com.opengamma.analytics.util.InvokedSerializedFormTest, staticExample, 42.0]");
    assertEquals(obj.readReplace(), "static-object-arg-42.0");
    obj = new InvokedSerializedForm(InvokedSerializedFormTest.class, "getStaticExample", 42d);
    assertEquals(obj.toString(), "InvokedSerializedForm[com.opengamma.analytics.util.InvokedSerializedFormTest, StaticExample, 42.0]");
    assertEquals(obj.readReplace(), "static-prefixed-object-arg-42.0");
  }

  public void testObjectArgDynamicMethod() {
    InvokedSerializedForm obj = new InvokedSerializedForm(this, "dynamicExample", 42d);
    assertEquals(obj.toString(), "InvokedSerializedForm[Test, dynamicExample, 42.0]");
    assertEquals(obj.readReplace(), "dynamic-object-arg-42.0");
    obj = new InvokedSerializedForm(this, "getDynamicExample", 42d);
    assertEquals(obj.toString(), "InvokedSerializedForm[Test, DynamicExample, 42.0]");
    assertEquals(obj.readReplace(), "dynamic-prefixed-object-arg-42.0");
  }

  @Override
  public String toString() {
    return "Test";
  }

}
