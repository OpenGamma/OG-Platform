/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.future.definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.util.time.DateUtil;

/**
 *
 */
public class CostOfCarryFutureDataBundleTest {
  private static final double YIELD = 0.04;
  private static final YieldAndDiscountCurve CURVE = new YieldCurve(ConstantDoublesCurve.from(0.05));
  private static final double SPOT = 100;
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 1, 1);
  private static final double STORAGE = 2;
  private static final StandardFutureDataBundle DATA = new StandardFutureDataBundle(YIELD, CURVE, SPOT, DATE, STORAGE);

  @Test(expected = IllegalArgumentException.class)
  public void testNullCurveConstructor() {
    new StandardFutureDataBundle(YIELD, null, SPOT, DATE, STORAGE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeSpotConstructor() {
    new StandardFutureDataBundle(YIELD, CURVE, -SPOT, DATE, STORAGE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDateConstructor() {
    new StandardFutureDataBundle(YIELD, CURVE, SPOT, null, STORAGE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeStorageConstructor() {
    new StandardFutureDataBundle(YIELD, CURVE, SPOT, DATE, -STORAGE);
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

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeStorageCostBuilder() {
    DATA.withStorageCost(-2);
  }

  @Test
  public void testEqualsAndHashCode() {
    FutureDataBundle data = new StandardFutureDataBundle(YIELD, CURVE, SPOT, DATE, STORAGE);
    assertEquals(data, DATA);
    assertEquals(data.hashCode(), DATA.hashCode());
    data = new StandardFutureDataBundle(YIELD + 1, CURVE, SPOT, DATE, STORAGE);
    assertFalse(data.equals(DATA));
    data = new StandardFutureDataBundle(YIELD, new YieldCurve(ConstantDoublesCurve.from(0.1)), SPOT, DATE, STORAGE);
    assertFalse(data.equals(DATA));
    data = new StandardFutureDataBundle(YIELD, CURVE, SPOT + 1, DATE, STORAGE);
    assertFalse(data.equals(DATA));
    data = new StandardFutureDataBundle(YIELD, CURVE, SPOT, DATE.minusDays(2), STORAGE);
    assertFalse(data.equals(DATA));
    data = new StandardFutureDataBundle(YIELD, CURVE, SPOT, DATE, STORAGE - 1);
    assertFalse(data.equals(DATA));
  }

  @Test
  public void testBuilders() {
    final double yield = -0.01;
    final YieldAndDiscountCurve curve = new YieldCurve(ConstantDoublesCurve.from(0.02));
    final double spot = 110;
    final ZonedDateTime date = DateUtil.getUTCDate(2010, 2, 1);
    final double storageCost = 4;
    assertEquals(DATA.withDate(date), new StandardFutureDataBundle(YIELD, CURVE, SPOT, date, STORAGE));
    assertEquals(DATA.withDiscountCurve(curve), new StandardFutureDataBundle(YIELD, curve, SPOT, DATE, STORAGE));
    assertEquals(DATA.withSpot(spot), new StandardFutureDataBundle(YIELD, CURVE, spot, DATE, STORAGE));
    assertEquals(DATA.withStorageCost(storageCost), new StandardFutureDataBundle(YIELD, CURVE, SPOT, DATE, storageCost));
    assertEquals(DATA.withYield(yield), new StandardFutureDataBundle(yield, CURVE, SPOT, DATE, STORAGE));
  }
}
