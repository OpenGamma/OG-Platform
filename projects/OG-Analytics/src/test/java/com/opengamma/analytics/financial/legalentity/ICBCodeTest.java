/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.legalentity;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.legalentity.ICBCode;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the ICB code object.
 */
@Test(groups = TestGroup.UNIT)
public class ICBCodeTest {

  /**
   * Tests failure for a null code
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCode() {
    ICBCode.of(null);
  }

  /**
   * Tests failure when the number is too low
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLowCode() {
    ICBCode.of(999);
  }

  /**
   * Tests failure when the number is too high
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighCode() {
    ICBCode.of(10000);
  }

  /**
   * Tests failure for invalid code
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalidCode() {
    ICBCode.of("345r");
  }

  /**
   * Tests getters, hashCode and equals
   */
  @Test
  public void testObject() {
    final String n = "5347";
    final ICBCode code = ICBCode.of(Integer.parseInt(n));
    assertEquals(n, code.getCode());
    ICBCode other = ICBCode.of("5347");
    assertEquals(code, other);
    assertEquals(code.hashCode(), other.hashCode());
    other = ICBCode.of(5347);
    assertEquals(code, other);
    other = ICBCode.of("5346");
    assertFalse(code.equals(other));
  }
}
