/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.market.description;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.util.tuple.Triple;

/**
 * Tests related to the MarketForwardSensitivity object representing a one point sensitivity to a forward curve.
 */
public class MarketForwardSensitivityTest {

  private static final double VALUE = 12345.6;
  private static final double START = 1.25;
  private static final double END = 1.50;
  private static final double ACCRUAL_FACTOR = 0.251;
  private static final Triple<Double, Double, Double> POINT = new Triple<Double, Double, Double>(START, END, ACCRUAL_FACTOR);
  private static final MarketForwardSensitivity SENSI = new MarketForwardSensitivity(POINT, VALUE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullPoint() {
    new MarketForwardSensitivity(null, VALUE);
  }

  @Test
  /**
   * Test the getters.
   */
  public void getter() {
    assertEquals("MarketForwardSensitivity: getter", POINT, SENSI.getPoint());
    assertEquals("MarketForwardSensitivity: getter", VALUE, SENSI.getValue());
  }

  @Test
  /**
   * Test the equal and hashCode methods.
   */
  public void equalHash() {
    assertEquals("MarketForwardSensitivity: equal-hash code", SENSI, SENSI);
    MarketForwardSensitivity other = new MarketForwardSensitivity(POINT, VALUE);
    assertEquals("MarketForwardSensitivity: equal-hash code", SENSI, other);
    assertEquals("MarketForwardSensitivity: equal-hash code", SENSI.hashCode(), other.hashCode());
    MarketForwardSensitivity modified;
    modified = new MarketForwardSensitivity(POINT, VALUE + 1.0);
    assertFalse("MarketForwardSensitivity: equal-hash code", SENSI.equals(modified));
    modified = new MarketForwardSensitivity(new Triple<Double, Double, Double>(START, END, ACCRUAL_FACTOR + 0.1), VALUE);
    assertFalse("MarketForwardSensitivity: equal-hash code", SENSI.equals(modified));
  }

}
