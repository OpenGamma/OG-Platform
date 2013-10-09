/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.testng.AssertJUnit.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class JdkUtilsTest {

  @SuppressWarnings("unchecked")
  public void test_constructor() throws Exception {
    Constructor<?>[] cons = JdkUtils.class.getDeclaredConstructors();
    assertEquals(1, cons.length);
    assertEquals(0, cons[0].getParameterTypes().length);
    assertEquals(true, Modifier.isPrivate(cons[0].getModifiers()));
    Constructor<JdkUtils> con = (Constructor<JdkUtils>) cons[0];
    con.setAccessible(true);
    con.newInstance();
  }

  //-------------------------------------------------------------------------
  public void test_strip() {
    assertEquals(BigDecimal.valueOf(0, 0), JdkUtils.stripTrailingZeros(BigDecimal.valueOf(0, 2)));
    assertEquals(BigDecimal.valueOf(1, 0), JdkUtils.stripTrailingZeros(BigDecimal.valueOf(1, 0)));
  }

}
