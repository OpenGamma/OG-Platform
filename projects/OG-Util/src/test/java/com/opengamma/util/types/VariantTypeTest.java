/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.types;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link VariantType} class.
 */
@Test(groups = TestGroup.UNIT)
public class VariantTypeTest {

  private static void assertVariantEquals(final Type type, final Type... types) {
    assertEquals(type.getClass(), VariantType.class);
    assertEquals(ImmutableSet.copyOf(((VariantType) type).getLogicalTypes()), ImmutableSet.copyOf(types));
  }

  public void testEither() {
    assertEquals(VariantType.either(null, String.class), String.class);
    assertEquals(VariantType.either(String.class, null), String.class);
    assertEquals(VariantType.either(String.class, String.class), String.class);
    assertVariantEquals(VariantType.either(VariantType.either(Integer.class, Double.class), String.class), Integer.class, Double.class, String.class);
    assertVariantEquals(VariantType.either(String.class, VariantType.either(Integer.class, Double.class)), Integer.class, Double.class, String.class);
    assertVariantEquals(VariantType.either(String.class, Integer.class), String.class, Integer.class);
    assertVariantEquals(VariantType.either(VariantType.either(Integer.class, Double.class), VariantType.either(Double.class, String.class)), Integer.class, Double.class, String.class);
  }

  public void testEquals() {
    @SuppressWarnings("serial")
    final ParameterizedType setType = (ParameterizedType) (new TypeToken<Set<? extends Number>>() {
    }).getType();
    final WildcardType wildType = (WildcardType) setType.getActualTypeArguments()[0];
    assertTrue(wildType.equals(VariantType.either(Integer.class, Double.class)));
    assertTrue(VariantType.either(Integer.class, Double.class).equals(wildType));
  }

  public void testHashCode() {
    @SuppressWarnings("serial")
    final ParameterizedType setType = (ParameterizedType) (new TypeToken<Set<? extends Number>>() {
    }).getType();
    final WildcardType wildType = (WildcardType) setType.getActualTypeArguments()[0];
    assertEquals(wildType.hashCode(), VariantType.either(Integer.class, Double.class).hashCode());
  }

}
