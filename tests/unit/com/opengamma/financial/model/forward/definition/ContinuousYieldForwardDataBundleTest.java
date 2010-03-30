/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.forward.definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.ConstantInterestRateDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.util.time.DateUtil;

/**
 * @author emcleod
 *
 */
public class ContinuousYieldForwardDataBundleTest {
  private static final double YIELD = 0.04;
  private static final DiscountCurve CURVE = new ConstantInterestRateDiscountCurve(0.05);
  private static final double SPOT = 100;
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 1, 1);
  private static final ContinuousYieldForwardDataBundle DATA = new ContinuousYieldForwardDataBundle(YIELD, CURVE, SPOT, DATE);

  @Test(expected = IllegalArgumentException.class)
  public void testNullCurveConstructor() {
    new ContinuousYieldForwardDataBundle(YIELD, null, SPOT, DATE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeSpotConstructor() {
    new ContinuousYieldForwardDataBundle(YIELD, CURVE, -SPOT, DATE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDateConstructor() {
    new ContinuousYieldForwardDataBundle(YIELD, CURVE, SPOT, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCurveBuilder() {
    DATA.withDiscountCurve(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeSpotBuilder() {
    DATA.withSpot(-SPOT);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDateBuilder() {
    DATA.withDate(null);
  }

  @Test
  public void testEqualsAndHashCode() {
    final ForwardDataBundle data1 = new ContinuousYieldForwardDataBundle(YIELD, CURVE, SPOT, DATE);
    final ForwardDataBundle data2 = new ContinuousYieldForwardDataBundle(YIELD, CURVE, SPOT + 1, DATE);
    assertEquals(data1, DATA);
    assertEquals(data1.hashCode(), DATA.hashCode());
    assertFalse(data1.equals(data2));
  }

  @Test
  public void testBuilders() {
    final double yield = -0.01;
    final DiscountCurve curve = new ConstantInterestRateDiscountCurve(0.02);
    final double spot = 110;
    final ZonedDateTime date = DateUtil.getUTCDate(2010, 2, 1);
    assertEquals(DATA.withDate(date), new ContinuousYieldForwardDataBundle(YIELD, CURVE, SPOT, date));
    assertEquals(DATA.withDiscountCurve(curve), new ContinuousYieldForwardDataBundle(YIELD, curve, SPOT, DATE));
    assertEquals(DATA.withSpot(spot), new ContinuousYieldForwardDataBundle(YIELD, CURVE, spot, DATE));
    assertEquals(DATA.withYield(yield), new ContinuousYieldForwardDataBundle(yield, CURVE, SPOT, DATE));
  }
}
