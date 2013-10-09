/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.testng.AssertJUnit.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.math.RoundingMode;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class EnumUtilsTest {

  @SuppressWarnings("unchecked")
  public void test_constructor() throws Exception {
    Constructor<?>[] cons = EnumUtils.class.getDeclaredConstructors();
    assertEquals(1, cons.length);
    assertEquals(0, cons[0].getParameterTypes().length);
    assertEquals(true, Modifier.isPrivate(cons[0].getModifiers()));
    Constructor<EnumUtils> con = (Constructor<EnumUtils>) cons[0];
    con.setAccessible(true);
    con.newInstance();
  }

  //-------------------------------------------------------------------------
  public void test_safeValueOf() {
    assertEquals(RoundingMode.FLOOR, EnumUtils.safeValueOf(RoundingMode.class, "FLOOR"));
    assertEquals(null, EnumUtils.safeValueOf(RoundingMode.class, null));
    assertEquals(null, EnumUtils.<RoundingMode>safeValueOf(null, "FLOOR"));
    assertEquals(null, EnumUtils.safeValueOf(RoundingMode.class, "RUBBISH"));
  }

}
