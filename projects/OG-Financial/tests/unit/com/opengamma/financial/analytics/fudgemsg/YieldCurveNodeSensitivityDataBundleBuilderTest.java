/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.core.common.Currency;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.fixedincome.YieldCurveNodeSensitivityDataBundle;

/**
 * 
 */
public class YieldCurveNodeSensitivityDataBundleBuilderTest extends AnalyticsTestBase {

  @Test
  public void test() {
    final Double[] keys = new Double[] {1., 2., 3., 4., 5.};
    final Object[] labels = new Object[] {"1y", "2y", "3y", "4y", "5y"};
    final double[] values = new double[] {0.1, 0.2, 0.3, 0.4, 0.5};
    final DoubleLabelledMatrix1D m = new DoubleLabelledMatrix1D(keys, labels, values);
    final Currency ccy = Currency.getInstance("USD");
    final String curveName = "S";
    final YieldCurveNodeSensitivityDataBundle d1 = new YieldCurveNodeSensitivityDataBundle(ccy, m, curveName);
    final YieldCurveNodeSensitivityDataBundle d2 = cycleObject(YieldCurveNodeSensitivityDataBundle.class, d1);
    assertEquals(d1, d2);
  }

}
