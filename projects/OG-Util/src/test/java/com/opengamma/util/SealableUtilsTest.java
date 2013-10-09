/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.testng.AssertJUnit.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SealableUtilsTest {

  @SuppressWarnings("unchecked")
  public void test_constructor() throws Exception {
    Constructor<?>[] cons = SealableUtils.class.getDeclaredConstructors();
    assertEquals(1, cons.length);
    assertEquals(0, cons[0].getParameterTypes().length);
    assertEquals(true, Modifier.isPrivate(cons[0].getModifiers()));
    Constructor<SealableUtils> con = (Constructor<SealableUtils>) cons[0];
    con.setAccessible(true);
    con.newInstance();
  }

  //-------------------------------------------------------------------------
  private static class MockSealed implements Sealable {
    public void seal() {
    }
    public boolean isSealed() {
      return true;
    }
  }
  private static class MockUnsealed implements Sealable {
    public void seal() {
    }
    public boolean isSealed() {
      return false;
    }
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void test_sealed() {
    SealableUtils.checkSealed(new MockSealed());
  }

  public void test_unsealed() {
    SealableUtils.checkSealed(new MockUnsealed());
  }

}
