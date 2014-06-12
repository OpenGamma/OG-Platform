/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.config.impl;

import static org.testng.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link ConfigItem} class.
 */
@Test(groups = TestGroup.UNIT)
public class ConfigItemTest {

  public static final class TestDocument {

    private int _foo;

    public void setFoo(final int foo) {
      _foo = foo;
    }

    public int getFoo() {
      return _foo;
    }

    @Override
    public String toString() {
      return "ConfigDocument[" + _foo + "]";
    }

    @Override
    public boolean equals(final Object o) {
      return ((o instanceof TestDocument) && (((TestDocument) o)._foo == _foo));
    }

  }

  @SuppressWarnings("rawtypes")
  public void testJavaSerialization() throws Exception {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final ObjectOutputStream out = new ObjectOutputStream(baos);
    final TestDocument d1 = new TestDocument();
    d1.setFoo(42);
    final ConfigItem<?> item1 = ConfigItem.of(d1, "SerializationTest1", TestDocument.class);
    final TestDocument d2 = new TestDocument();
    d2.setFoo(96);
    final ConfigItem<?> item2 = ConfigItem.of(d2, "SerializationTest2", TestDocument.class);
    out.writeObject(item1);
    out.writeObject(item2);
    out.flush();
    final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    final ObjectInputStream in = new ObjectInputStream(bais);
    ConfigItem item = (ConfigItem) in.readObject();
    assertEquals(item.getType(), TestDocument.class);
    assertEquals(item.getName(), "SerializationTest1");
    assertEquals(item.getValue(), d1);
    assertEquals(item, item1);
    item = (ConfigItem) in.readObject();
    assertEquals(item.getType(), TestDocument.class);
    assertEquals(item.getName(), "SerializationTest2");
    assertEquals(item.getValue(), d2);
    assertEquals(item, item2);
  }

}
