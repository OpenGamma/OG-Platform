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
public class FXForwardDataBundleTest {
  private static final DiscountCurve FOREIGN = new ConstantInterestRateDiscountCurve(0.03);
  private static final DiscountCurve DOMESTIC = new ConstantInterestRateDiscountCurve(0.05);
  private static final double SPOT = 1.5;
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 1, 1);
  private static final FXForwardDataBundle DATA = new FXForwardDataBundle(DOMESTIC, FOREIGN, SPOT, DATE);

  @Test(expected = IllegalArgumentException.class)
  public void testNullDomesticCurveConstructor() {
    new FXForwardDataBundle(null, FOREIGN, SPOT, DATE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testForeignCurveConstructor() {
    new FXForwardDataBundle(DOMESTIC, null, -SPOT, DATE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeSpotConstructor() {
    new FXForwardDataBundle(DOMESTIC, FOREIGN, -SPOT, DATE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDateConstructor() {
    new FXForwardDataBundle(DOMESTIC, FOREIGN, SPOT, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDomesticCurveBuilder() {
    DATA.withDiscountCurve(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullForeignCurveBuilder() {
    DATA.withForeignCurve(null);
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
    final ForwardDataBundle data1 = new FXForwardDataBundle(DOMESTIC, FOREIGN, SPOT, DATE);
    final ForwardDataBundle data2 = new FXForwardDataBundle(DOMESTIC, FOREIGN, SPOT + 1, DATE);
    assertEquals(data1, DATA);
    assertEquals(data1.hashCode(), DATA.hashCode());
    assertFalse(data1.equals(data2));
  }

  @Test
  public void testBuilders() {
    final DiscountCurve curve = new ConstantInterestRateDiscountCurve(0.02);
    final double spot = 2;
    final ZonedDateTime date = DateUtil.getUTCDate(2010, 2, 1);
    assertEquals(DATA.withDate(date), new FXForwardDataBundle(DOMESTIC, FOREIGN, SPOT, date));
    assertEquals(DATA.withDiscountCurve(curve), new FXForwardDataBundle(curve, FOREIGN, SPOT, DATE));
    assertEquals(DATA.withForeignCurve(curve), new FXForwardDataBundle(DOMESTIC, curve, SPOT, DATE));
    assertEquals(DATA.withSpot(spot), new FXForwardDataBundle(DOMESTIC, FOREIGN, spot, DATE));
  }
}
