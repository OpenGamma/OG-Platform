/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SealableUtilsTest {

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
