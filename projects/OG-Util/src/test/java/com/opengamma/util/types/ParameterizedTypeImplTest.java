/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.types;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.ParameterizedType;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.reflect.TypeToken;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 * Tests the {@link ParameterizedTypeImpl} class.
 */
@Test(groups = TestGroup.UNIT)
public class ParameterizedTypeImplTest {

  private static class FiveArg<A, B, C, D, E> {
  }

  private static class FourArg<A, B, C, D> {
  }

  public void testVararg() {
    final ParameterizedType implType = ParameterizedTypeImpl.of(FiveArg.class, Byte.class, Short.class, Integer.class, Long.class, Double.class);
    @SuppressWarnings("serial")
    final ParameterizedType refType = (ParameterizedType) (new TypeToken<FiveArg<Byte, Short, Integer, Long, Double>>() {
    }).getType();
    assertTrue(implType.equals(refType));
    assertTrue(refType.equals(implType));
    assertEquals(implType.hashCode(), refType.hashCode());
  }

  public void test1Arg() {
    final ParameterizedType implType = ParameterizedTypeImpl.of(Set.class, Integer.class);
    @SuppressWarnings("serial")
    final ParameterizedType refType = (ParameterizedType) (new TypeToken<Set<Integer>>() {
    }).getType();
    assertTrue(implType.equals(refType));
    assertTrue(refType.equals(implType));
    assertEquals(implType.hashCode(), refType.hashCode());
  }

  public void test2Arg() {
    final ParameterizedType implType = ParameterizedTypeImpl.of(Pair.class, Integer.class, String.class);
    @SuppressWarnings("serial")
    final ParameterizedType refType = (ParameterizedType) (new TypeToken<Pair<Integer, String>>() {
    }).getType();
    assertTrue(implType.equals(refType));
    assertTrue(refType.equals(implType));
    assertEquals(implType.hashCode(), refType.hashCode());
  }

  public void test3Arg() {
    final ParameterizedType implType = ParameterizedTypeImpl.of(Triple.class, Integer.class, Double.class, String.class);
    @SuppressWarnings("serial")
    final ParameterizedType refType = (ParameterizedType) (new TypeToken<Triple<Integer, Double, String>>() {
    }).getType();
    assertTrue(implType.equals(refType));
    assertTrue(refType.equals(implType));
    assertEquals(implType.hashCode(), refType.hashCode());
  }

  public void test4Arg() {
    final ParameterizedType implType = ParameterizedTypeImpl.of(FourArg.class, Byte.class, Short.class, Integer.class, Long.class);
    @SuppressWarnings("serial")
    final ParameterizedType refType = (ParameterizedType) (new TypeToken<FourArg<Byte, Short, Integer, Long>>() {
    }).getType();
    assertTrue(implType.equals(refType));
    assertTrue(refType.equals(implType));
    assertEquals(implType.hashCode(), refType.hashCode());
  }

}
