/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.BondFuturesDataSets;
import com.opengamma.analytics.financial.instrument.future.BondFuturesSecurityDefinition;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to bond futures option security derivative construction.
 */
@Test(groups = TestGroup.UNIT)
public class BondFuturesOptionMarginSecurityTest {

  private static final BondFuturesSecurityDefinition BOBLM4_DEFINITION = BondFuturesDataSets.boblM4Definition();
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2014, 3, 31);
  private static final BondFuturesSecurity BOBLM4 = BOBLM4_DEFINITION.toDerivative(REFERENCE_DATE);

  // Option
  private static final double STRIKE_125 = 1.25;
  private static final ZonedDateTime EXPIRY_DATE_OPT = DateUtils.getUTCDate(2014, 6, 5);
  private static final double EXPIRY_TIME_OPT = TimeCalculator.getTimeBetween(REFERENCE_DATE, EXPIRY_DATE_OPT);
  private static final ZonedDateTime LAST_TRADING_DATE_OPT = DateUtils.getUTCDate(2014, 6, 4);
  private static final double LAST_TRADIND_TIME_OPT = TimeCalculator.getTimeBetween(REFERENCE_DATE, LAST_TRADING_DATE_OPT);
  private static final boolean IS_CALL = true;
  private static final BondFuturesOptionMarginSecurity CALL_BOBLM4 = new BondFuturesOptionMarginSecurity(BOBLM4, LAST_TRADIND_TIME_OPT, EXPIRY_TIME_OPT, STRIKE_125, IS_CALL);

  /**
   * Tests the getter methods.
   */
  public void getter() {
    assertEquals("BondFuturesOptionMarginSecurity: getter", BOBLM4, CALL_BOBLM4.getUnderlyingFuture());
    assertEquals("BondFuturesOptionMarginSecurity: getter", EXPIRY_TIME_OPT, CALL_BOBLM4.getExpirationTime());
    assertEquals("BondFuturesOptionMarginSecurity: getter", STRIKE_125, CALL_BOBLM4.getStrike());
    assertEquals("BondFuturesOptionMarginSecurity: getter", IS_CALL, CALL_BOBLM4.isCall());
  }

  /**
   * Tests the equal and hashCode methods.
   */
  public void equalHash() {
    assertTrue("BondFuturesOptionMarginSecurity: equal-hash", CALL_BOBLM4.equals(CALL_BOBLM4));
    final BondFuturesOptionMarginSecurity duplicated = new BondFuturesOptionMarginSecurity(BOBLM4, LAST_TRADIND_TIME_OPT, EXPIRY_TIME_OPT, STRIKE_125, IS_CALL);
    assertTrue("BondFuturesOptionMarginSecurity: equal-hash", CALL_BOBLM4.equals(duplicated));
    assertTrue("BondFuturesOptionMarginSecurity: equal-hash", CALL_BOBLM4.hashCode() == duplicated.hashCode());
    BondFuturesOptionMarginSecurity modified;
    final BondFuturesSecurity futuresModified = new BondFuturesSecurity(BOBLM4.getTradingLastTime(), BOBLM4.getNoticeFirstTime(), BOBLM4.getNoticeLastTime(),
        BOBLM4.getDeliveryFirstTime(), BOBLM4.getDeliveryLastTime(), BOBLM4.getNotional() + 100, BOBLM4.getDeliveryBasketAtDeliveryDate(), BOBLM4.getDeliveryBasketAtSpotDate(),
        BOBLM4.getConversionFactor());
    modified = new BondFuturesOptionMarginSecurity(futuresModified, LAST_TRADIND_TIME_OPT, EXPIRY_TIME_OPT, STRIKE_125, IS_CALL);
    assertFalse("BondFuturesOptionMarginSecurityDefinition: equal-hash", CALL_BOBLM4.equals(modified));
    modified = new BondFuturesOptionMarginSecurity(BOBLM4, LAST_TRADIND_TIME_OPT + 0.01, EXPIRY_TIME_OPT, STRIKE_125, IS_CALL);
    assertFalse("BondFuturesOptionMarginSecurityDefinition: equal-hash", CALL_BOBLM4.equals(modified));
    modified = new BondFuturesOptionMarginSecurity(BOBLM4, LAST_TRADIND_TIME_OPT, EXPIRY_TIME_OPT + 0.01, STRIKE_125, IS_CALL);
    assertFalse("BondFuturesOptionMarginSecurityDefinition: equal-hash", CALL_BOBLM4.equals(modified));
    modified = new BondFuturesOptionMarginSecurity(BOBLM4, LAST_TRADIND_TIME_OPT, EXPIRY_TIME_OPT, STRIKE_125 + 0.01, IS_CALL);
    assertFalse("BondFuturesOptionMarginSecurityDefinition: equal-hash", CALL_BOBLM4.equals(modified));
    modified = new BondFuturesOptionMarginSecurity(BOBLM4, LAST_TRADIND_TIME_OPT, EXPIRY_TIME_OPT, STRIKE_125, !IS_CALL);
    assertFalse("BondFuturesOptionMarginSecurityDefinition: equal-hash", CALL_BOBLM4.equals(modified));
  }

}
