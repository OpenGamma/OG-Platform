/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskfactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Collections;

import org.junit.Test;

/**
 * @author emcleod
 *
 */
public class RiskFactorResultTest {

  @Test(expected = IllegalArgumentException.class)
  public void testSingleResultConstructor() {
    new SingleRiskFactorResult(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMultipleResultWithNull() {
    new MultipleRiskFactorResult(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMultipleResultWithEmptyMap() {
    new MultipleRiskFactorResult(Collections.<Object, Double> emptyMap());
  }

  @Test
  public void testEquals() {
    assertEquals(new SingleRiskFactorResult(3.4), new SingleRiskFactorResult(3.4));
    assertEquals(new MultipleRiskFactorResult(Collections.<Object, Double> singletonMap("A", 1.2)), new MultipleRiskFactorResult(Collections
        .<Object, Double> singletonMap("A", 1.2)));
    assertFalse(new MultipleRiskFactorResult(Collections.<Object, Double> singletonMap("A1", 1.2)).equals(new MultipleRiskFactorResult(Collections.<Object, Double> singletonMap(
        "A", 1.2))));
  }
}
