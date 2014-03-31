/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesSecurity;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to bond futures option security Definition construction.
 */
public class BondFuturesOptionMarginSecurityDefinitionTest {

  private static final BondFuturesSecurityDefinition BOBLM4_DEFINITION = BondFuturesDataSets.boblM4Definition();

  private static final double STRIKE_125 = 1.25;
  private static final ZonedDateTime EXPIRY_DATE_OPT = DateUtils.getUTCDate(2014, 6, 5);
  private static final ZonedDateTime LAST_TRADING_DATE_OPT = DateUtils.getUTCDate(2014, 6, 4);
  private static final boolean IS_CALL = true;
  private static final BondFuturesOptionMarginSecurityDefinition CALL_BOBLM4_DEFINITION =
      new BondFuturesOptionMarginSecurityDefinition(BOBLM4_DEFINITION, LAST_TRADING_DATE_OPT, EXPIRY_DATE_OPT, STRIKE_125, IS_CALL);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullUnderlying() {
    new BondFuturesOptionMarginSecurityDefinition(null, LAST_TRADING_DATE_OPT, EXPIRY_DATE_OPT, STRIKE_125, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullExpiry() {
    new BondFuturesOptionMarginSecurityDefinition(BOBLM4_DEFINITION, LAST_TRADING_DATE_OPT, null, STRIKE_125, true);
  }

  @Test
  /**
   * Tests the getter methods.
   */
  public void getter() {
    assertEquals("BondFuturesOptionMarginSecurityDefinition: getter", BOBLM4_DEFINITION, CALL_BOBLM4_DEFINITION.getUnderlyingFuture());
    assertEquals("BondFuturesOptionMarginSecurityDefinition: getter", EXPIRY_DATE_OPT, CALL_BOBLM4_DEFINITION.getExpirationDate());
    assertEquals("BondFuturesOptionMarginSecurityDefinition: getter", STRIKE_125, CALL_BOBLM4_DEFINITION.getStrike());
    assertEquals("BondFuturesOptionMarginSecurityDefinition: getter", true, CALL_BOBLM4_DEFINITION.isCall());
  }

  @Test
  /**
   * Tests the equal and hashCode methods.
   */
  public void equalHash() {
    assertTrue("BondFuturesOptionMarginSecurityDefinition: equal-hash", CALL_BOBLM4_DEFINITION.equals(CALL_BOBLM4_DEFINITION));
    final BondFuturesOptionMarginSecurityDefinition duplicated = new BondFuturesOptionMarginSecurityDefinition(BOBLM4_DEFINITION, LAST_TRADING_DATE_OPT, EXPIRY_DATE_OPT, STRIKE_125, true);
    assertTrue("BondFuturesOptionMarginSecurityDefinition: equal-hash", CALL_BOBLM4_DEFINITION.equals(duplicated));
    assertTrue("BondFuturesOptionMarginSecurityDefinition: equal-hash", CALL_BOBLM4_DEFINITION.hashCode() == duplicated.hashCode());
    BondFuturesOptionMarginSecurityDefinition modified;
    final BondFuturesSecurityDefinition futuresModified = new BondFuturesSecurityDefinition(BOBLM4_DEFINITION.getLastTradingDate(), BOBLM4_DEFINITION.getNoticeFirstDate(),
        BOBLM4_DEFINITION.getNoticeLastDate().plusDays(1), BOBLM4_DEFINITION.getNotional(), BOBLM4_DEFINITION.getDeliveryBasket(), BOBLM4_DEFINITION.getConversionFactor());
    modified = new BondFuturesOptionMarginSecurityDefinition(futuresModified, LAST_TRADING_DATE_OPT, EXPIRY_DATE_OPT, STRIKE_125, true);
    assertFalse("BondFuturesOptionMarginSecurityDefinition: equal-hash", CALL_BOBLM4_DEFINITION.equals(modified));
    modified = new BondFuturesOptionMarginSecurityDefinition(BOBLM4_DEFINITION, LAST_TRADING_DATE_OPT, EXPIRY_DATE_OPT.plusDays(1), STRIKE_125, true);
    assertFalse("BondFuturesOptionMarginSecurityDefinition: equal-hash", CALL_BOBLM4_DEFINITION.equals(modified));
    modified = new BondFuturesOptionMarginSecurityDefinition(BOBLM4_DEFINITION, LAST_TRADING_DATE_OPT.plusDays(1), EXPIRY_DATE_OPT, STRIKE_125, true);
    assertFalse("BondFuturesOptionMarginSecurityDefinition: equal-hash", CALL_BOBLM4_DEFINITION.equals(modified));
    modified = new BondFuturesOptionMarginSecurityDefinition(BOBLM4_DEFINITION, LAST_TRADING_DATE_OPT, EXPIRY_DATE_OPT, STRIKE_125 + 1, true);
    assertFalse("BondFuturesOptionMarginSecurityDefinition: equal-hash", CALL_BOBLM4_DEFINITION.equals(modified));
    modified = new BondFuturesOptionMarginSecurityDefinition(BOBLM4_DEFINITION, LAST_TRADING_DATE_OPT, EXPIRY_DATE_OPT, STRIKE_125 + 1, false);
    assertFalse("BondFuturesOptionMarginSecurityDefinition: equal-hash", CALL_BOBLM4_DEFINITION.equals(modified));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullToDerivativeDate() {
    CALL_BOBLM4_DEFINITION.toDerivative(null);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivative() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2014, 3, 31);
    final double expirationTime = TimeCalculator.getTimeBetween(referenceDate, EXPIRY_DATE_OPT);
    final double lastTradingTime = TimeCalculator.getTimeBetween(referenceDate, LAST_TRADING_DATE_OPT);
    final BondFuturesSecurity underlyingFuture = BOBLM4_DEFINITION.toDerivative(referenceDate);
    final BondFuturesOptionMarginSecurity optionExpected = new BondFuturesOptionMarginSecurity(underlyingFuture, lastTradingTime, expirationTime, STRIKE_125, IS_CALL);
    final BondFuturesOptionMarginSecurity optionConverted = CALL_BOBLM4_DEFINITION.toDerivative(referenceDate);
    assertEquals("BondFuturesOptionMarginSecurityDefinition: toDerivative", optionExpected, optionConverted);
  }

}
