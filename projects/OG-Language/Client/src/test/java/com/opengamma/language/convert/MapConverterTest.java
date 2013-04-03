/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.language.Value;
import com.opengamma.language.ValueUtils;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.test.AbstractConverterTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link MapConverter} class.
 */
@Test(groups = TestGroup.UNIT)
public class MapConverterTest extends AbstractConverterTest {

  private final MapConverter _converter = new MapConverter();

  private Value[][] createValues() {
    return new Value[][] { {ValueUtils.of("Foo"), ValueUtils.of("42") }, {ValueUtils.of("Bar"), ValueUtils.of(42) }, {ValueUtils.of("Cow"), new Value() } };
  }

  public void testToValues() {
    final Map<String, Object> map = new HashMap<String, Object>();
    map.put("Foo", "42");
    map.put("Bar", 42);
    map.put("Cow", null);
    assertValidConversion(_converter, map, JavaTypeInfo.builder(Value[][].class).get(), createValues());
  }

  public void testToUntypedMap() {
    final Map<Object, Object> map = new HashMap<Object, Object>();
    // Untyped conversion will leave us with Value instances
    map.put(ValueUtils.of("Foo"), ValueUtils.of("42"));
    map.put(ValueUtils.of("Bar"), ValueUtils.of(42));
    map.put(ValueUtils.of("Cow"), new Value());
    assertValidConversion(_converter, createValues(), JavaTypeInfo.builder(Map.class).get(), map);
  }

  public void testToTypedMap() {
    final Map<String, Object> map = new HashMap<String, Object>();
    // Typed conversion will give us String keys, but the Value values (as they match "Object")
    map.put("Foo", ValueUtils.of("42"));
    map.put("Bar", ValueUtils.of(42));
    map.put("Cow", new Value());
    assertValidConversion(_converter, createValues(), JavaTypeInfo.builder(Map.class).parameter(String.class).parameter(Object.class).get(), map);
  }

}
