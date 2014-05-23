/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.legalentity;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.legalentity.GICSCode;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the GICS code object.
 */
@Test(groups = TestGroup.UNIT)
public class GICSCodeTest {

  /**
   * Tests failure for a null code
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCode() {
    GICSCode.of(null);
  }

  /**
   * Tests failure when the number is too low
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLowCode() {
    GICSCode.of(9);
  }

  /**
   * Tests failure when the number is too high
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighCode() {
    GICSCode.of(100000000);
  }

  /**
   * Tests failure for invalid code
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInvalidCode() {
    GICSCode.of("1020304r");
  }

  /**
   * Tests getters, hashCode and equals
   */
  @Test
  public void testObject() {
    final String n = "10203040";
    final GICSCode code = GICSCode.of(Integer.parseInt(n));
    assertEquals(n, code.getCode());
    assertEquals("10", code.getSector());
    assertEquals("1020", code.getIndustryGroup());
    assertEquals("102030", code.getIndustry());
    assertEquals("10203040", code.getSubIndustry());
    GICSCode other = GICSCode.of("10203040");
    assertEquals(code, other);
    assertEquals(code.hashCode(), other.hashCode());
    other = GICSCode.of(10203040);
    assertEquals(code, other);
    other = GICSCode.of("10203041");
    assertFalse(code.equals(other));
  }
}
