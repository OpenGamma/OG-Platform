/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimplyCompoundedForwardSensitivity;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ForwardSensitivityTest {
  private static final double VALUE = 12345.6;
  private static final double START = 1.25;
  private static final double END = 1.50;
  private static final double ACCRUAL_FACTOR = 0.251;
  private static final SimplyCompoundedForwardSensitivity SENSITIVITY = new SimplyCompoundedForwardSensitivity(START, END, ACCRUAL_FACTOR, VALUE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEndBeforeStart() {
    new SimplyCompoundedForwardSensitivity(END, START, ACCRUAL_FACTOR, VALUE);
  }

  @Test
  public void testGetter() {
    assertEquals(START, SENSITIVITY.getStartTime());
    assertEquals(END, SENSITIVITY.getEndTime());
    assertEquals(ACCRUAL_FACTOR, SENSITIVITY.getAccrualFactor());
    assertEquals(VALUE, SENSITIVITY.getValue());
  }

  @Test
  public void testObject() {
    ForwardSensitivity other = new SimplyCompoundedForwardSensitivity(START, END, ACCRUAL_FACTOR, VALUE);
    assertEquals(SENSITIVITY, other);
    assertEquals(SENSITIVITY.hashCode(), other.hashCode());
    other = new SimplyCompoundedForwardSensitivity(START * 0.5, END, ACCRUAL_FACTOR, VALUE);
    assertFalse(SENSITIVITY.equals(other));
    other = new SimplyCompoundedForwardSensitivity(START, END * 1.5, ACCRUAL_FACTOR, VALUE);
    assertFalse(SENSITIVITY.equals(other));
    other = new SimplyCompoundedForwardSensitivity(START, END, ACCRUAL_FACTOR * 1.2, VALUE);
    assertFalse(SENSITIVITY.equals(other));
    other = new SimplyCompoundedForwardSensitivity(START, END, ACCRUAL_FACTOR, VALUE + 1);
    assertFalse(SENSITIVITY.equals(other));
  }

}
