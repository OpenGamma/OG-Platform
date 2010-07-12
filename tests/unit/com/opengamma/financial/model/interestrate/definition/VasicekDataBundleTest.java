/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class VasicekDataBundleTest {
  private static final double SHORT_RATE = 0.02;
  private static final double LONG_RATE = 0.05;
  private static final double SPEED = 0.1;
  private static final double SIGMA = 0.4;
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 1, 1);
  private static final VasicekDataBundle DATA = new VasicekDataBundle(SHORT_RATE, LONG_RATE, SPEED, SIGMA, DATE);

  @Test(expected = IllegalArgumentException.class)
  public void testNullDate() {
    new VasicekDataBundle(SHORT_RATE, LONG_RATE, SPEED, SIGMA, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testZeroSpeed() {
    new VasicekDataBundle(SHORT_RATE, LONG_RATE, 0, SIGMA, DATE);
  }

  @Test
  public void testGetters() {
    assertEquals(DATA.getShortRate(), SHORT_RATE, 0);
    assertEquals(DATA.getLongTermInterestRate(), LONG_RATE, 0);
    assertEquals(DATA.getReversionSpeed(), SPEED, 0);
    assertEquals(DATA.getShortRateVolatility(), SIGMA, 0);
    assertEquals(DATA.getDate(), DATE);
  }

  @Test
  public void testHashCodeAndEquals() {
    VasicekDataBundle other = new VasicekDataBundle(SHORT_RATE, LONG_RATE, SPEED, SIGMA, DATE);
    assertEquals(other, DATA);
    assertEquals(other.hashCode(), DATA.hashCode());
    other = new VasicekDataBundle(SHORT_RATE + 0.1, LONG_RATE, SPEED, SIGMA, DATE);
    assertFalse(other.equals(DATA));
    other = new VasicekDataBundle(SHORT_RATE, LONG_RATE + 0.1, SPEED, SIGMA, DATE);
    assertFalse(other.equals(DATA));
    other = new VasicekDataBundle(SHORT_RATE, LONG_RATE, SPEED + 0.1, SIGMA, DATE);
    assertFalse(other.equals(DATA));
    other = new VasicekDataBundle(SHORT_RATE, LONG_RATE, SPEED, SIGMA + 0.1, DATE);
    assertFalse(other.equals(DATA));
    other = new VasicekDataBundle(SHORT_RATE, LONG_RATE, SPEED, SIGMA, DATE.plusDays(56));
    assertFalse(other.equals(DATA));
  }
}
