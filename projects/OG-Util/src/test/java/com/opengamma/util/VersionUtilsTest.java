/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test VersionUtils.
 */
@Test(groups = TestGroup.UNIT)
public class VersionUtilsTest {

  @SuppressWarnings("unchecked")
  public void test_constructor() throws Exception {
    Constructor<?>[] cons = VersionUtils.class.getDeclaredConstructors();
    assertEquals(1, cons.length);
    assertEquals(0, cons[0].getParameterTypes().length);
    assertEquals(true, Modifier.isPrivate(cons[0].getModifiers()));
    Constructor<VersionUtils> con = (Constructor<VersionUtils>) cons[0];
    con.setAccessible(true);
    con.newInstance();
  }

  //-------------------------------------------------------------------------
  public void validPropertyFile() {
    assertEquals("FIN-507", VersionUtils.getVersion("VersionUtilTest-1"));
  }

  public void invalidPropertyFile() {
    checkLocalVersionOk(VersionUtils.getVersion("VersionUtilTest-2"));
  }

  public void noPropertyFile() {
    checkLocalVersionOk(VersionUtils.getVersion("VersionUtilTest-3"));
  }

  private void checkLocalVersionOk(String version) {
    assertTrue(version.startsWith("local-"));

    long currentTimeMillis = Long.parseLong(version.substring("local-".length()));
    assertTrue(currentTimeMillis >= 0);
    assertTrue(currentTimeMillis <= System.currentTimeMillis());
  }

}
