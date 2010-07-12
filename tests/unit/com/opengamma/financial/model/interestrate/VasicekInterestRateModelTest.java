/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.definition.VasicekDataBundle;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class VasicekInterestRateModelTest {
  private static final int YEARS = 10;
  private static final ZonedDateTime TODAY = DateUtil.getUTCDate(2010, 7, 1);
  private static final ZonedDateTime START = DateUtil.getUTCDate(2011, 7, 1);
  private static final ZonedDateTime MATURITY = DateUtil.getDateOffsetWithYearFraction(START, 10);
  private static final VasicekInterestRateModel MODEL = new VasicekInterestRateModel();

  @Test(expected = IllegalArgumentException.class)
  public void testNullTime() {
    MODEL.getInterestRateFunction(null, MATURITY);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullMaturity() {
    MODEL.getInterestRateFunction(START, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getInterestRateFunction(START, MATURITY).evaluate((VasicekDataBundle) null);
  }

  @Test
  public void test() {
    final double shortRate = 0.05;
    final double longRate = 0.06;
    final double speed = 0.01;
    final double sigma = 0.01;
    VasicekDataBundle data = new VasicekDataBundle(shortRate, longRate, speed, sigma, TODAY);
    assertEquals(MODEL.getInterestRateFunction(START, START).evaluate(data), 1, 0);
    data = new VasicekDataBundle(shortRate, longRate, speed, 0, TODAY);
    final double factor = (1 - Math.exp(-speed * YEARS));
    final double lnA = longRate * (factor / speed - YEARS);
    assertEquals(MODEL.getInterestRateFunction(START, MATURITY).evaluate(data), Math.exp(lnA - shortRate * factor / speed), 0);
  }
}
