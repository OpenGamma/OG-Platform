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
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginTransaction;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to bond futures option security Definition construction.
 */
public class BondFuturesOptionMarginTransactionDefinitionTest {

  private static final BondFuturesSecurityDefinition BOBLM4_DEFINITION = BondFuturesDataSets.boblM4Definition();

  // Option - security
  private static final double STRIKE_125 = 1.25;
  private static final ZonedDateTime EXPIRY_DATE_OPT = DateUtils.getUTCDate(2014, 6, 5);
  private static final ZonedDateTime LAST_TRADING_DATE_OPT = DateUtils.getUTCDate(2014, 6, 4);
  private static final boolean IS_CALL = true;
  private static final BondFuturesOptionMarginSecurityDefinition CALL_BOBLM4_SEC_DEFINITION =
      new BondFuturesOptionMarginSecurityDefinition(BOBLM4_DEFINITION, LAST_TRADING_DATE_OPT, EXPIRY_DATE_OPT, STRIKE_125, IS_CALL);
  // Option - Transaction
  private static final int QUANTITY = 1234;
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2014, 3, 31);
  private static final double TRADE_PRICE = 0.01;
  private static final BondFuturesOptionMarginTransactionDefinition CALL_BOBLM4_TRA_DEFINITION =
      new BondFuturesOptionMarginTransactionDefinition(CALL_BOBLM4_SEC_DEFINITION, QUANTITY, TRADE_DATE, TRADE_PRICE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullSecurity() {
    new BondFuturesOptionMarginTransactionDefinition(null, QUANTITY, TRADE_DATE, TRADE_PRICE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullTradeDate() {
    new BondFuturesOptionMarginTransactionDefinition(CALL_BOBLM4_SEC_DEFINITION, QUANTITY, null, TRADE_PRICE);
  }

  @Test
  /**
   * Tests the getter methods.
   */
  public void getter() {
    assertEquals("BondFuturesOptionMarginTransactionDefinition: getter", CALL_BOBLM4_SEC_DEFINITION, CALL_BOBLM4_TRA_DEFINITION.getUnderlyingSecurity());
    assertEquals("BondFuturesOptionMarginTransactionDefinition: getter", QUANTITY, CALL_BOBLM4_TRA_DEFINITION.getQuantity());
    assertEquals("BondFuturesOptionMarginTransactionDefinition: getter", TRADE_DATE, CALL_BOBLM4_TRA_DEFINITION.getTradeDate());
    assertEquals("BondFuturesOptionMarginTransactionDefinition: getter", TRADE_PRICE, CALL_BOBLM4_TRA_DEFINITION.getTradePrice());
  }

  @Test
  /**
   * Tests the equal and hashCode methods.
   */
  public void equalHash() {
    assertTrue("BondFuturesOptionMarginTransactionDefinition: equal-hash", CALL_BOBLM4_TRA_DEFINITION.equals(CALL_BOBLM4_TRA_DEFINITION));
    final BondFuturesOptionMarginTransactionDefinition duplicated = new BondFuturesOptionMarginTransactionDefinition(CALL_BOBLM4_SEC_DEFINITION, QUANTITY, TRADE_DATE, TRADE_PRICE);
    assertTrue("BondFuturesOptionMarginTransactionDefinition: equal-hash", CALL_BOBLM4_TRA_DEFINITION.equals(duplicated));
    assertTrue("BondFuturesOptionMarginTransactionDefinition: equal-hash", CALL_BOBLM4_TRA_DEFINITION.hashCode() == duplicated.hashCode());
    BondFuturesOptionMarginTransactionDefinition modified;
    final BondFuturesOptionMarginSecurityDefinition securityModified = new BondFuturesOptionMarginSecurityDefinition(BOBLM4_DEFINITION, LAST_TRADING_DATE_OPT, EXPIRY_DATE_OPT, STRIKE_125, !IS_CALL);
    modified = new BondFuturesOptionMarginTransactionDefinition(securityModified, QUANTITY, TRADE_DATE, TRADE_PRICE);
    assertFalse("BondFuturesOptionMarginTransactionDefinition: equal-hash", CALL_BOBLM4_TRA_DEFINITION.equals(modified));
    modified = new BondFuturesOptionMarginTransactionDefinition(CALL_BOBLM4_SEC_DEFINITION, QUANTITY + 1, TRADE_DATE, TRADE_PRICE);
    assertFalse("BondFuturesOptionMarginTransactionDefinition: equal-hash", CALL_BOBLM4_TRA_DEFINITION.equals(modified));
    modified = new BondFuturesOptionMarginTransactionDefinition(CALL_BOBLM4_SEC_DEFINITION, QUANTITY, TRADE_DATE.plusDays(1), TRADE_PRICE);
    assertFalse("BondFuturesOptionMarginTransactionDefinition: equal-hash", CALL_BOBLM4_TRA_DEFINITION.equals(modified));
    modified = new BondFuturesOptionMarginTransactionDefinition(CALL_BOBLM4_SEC_DEFINITION, QUANTITY, TRADE_DATE, TRADE_PRICE + 1);
    assertFalse("BondFuturesOptionMarginTransactionDefinition: equal-hash", CALL_BOBLM4_TRA_DEFINITION.equals(modified));
  }

  private static final double REFERENCE_PRICE = 0.01;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullToDerivativeDate() {
    CALL_BOBLM4_TRA_DEFINITION.toDerivative(null, REFERENCE_PRICE);
  }

  @Test
  /**
   * Tests the toDerivative method when the reference date is not the trade date
   */
  public void toDerivativeNotTradeDate() {
    final ZonedDateTime referenceDateNotTrade = TRADE_DATE.plusDays(1);
    final BondFuturesOptionMarginSecurity underlyingOption = CALL_BOBLM4_SEC_DEFINITION.toDerivative(referenceDateNotTrade);
    final BondFuturesOptionMarginTransaction optionExpected = new BondFuturesOptionMarginTransaction(underlyingOption, QUANTITY, REFERENCE_PRICE);
    final BondFuturesOptionMarginTransaction optionConverted = CALL_BOBLM4_TRA_DEFINITION.toDerivative(referenceDateNotTrade, REFERENCE_PRICE);
    assertEquals("BondFuturesOptionMarginTransactionDefinition: toDerivative", optionExpected, optionConverted);
  }

  @Test
  /**
   * Tests the toDerivative method when the reference date is the trade date
   */
  public void toDerivativeTradeDate() {
    final ZonedDateTime referenceDateTrade = TRADE_DATE;
    final BondFuturesOptionMarginSecurity underlyingOption = CALL_BOBLM4_SEC_DEFINITION.toDerivative(referenceDateTrade);
    final BondFuturesOptionMarginTransaction optionExpected = new BondFuturesOptionMarginTransaction(underlyingOption, QUANTITY, TRADE_PRICE);
    final BondFuturesOptionMarginTransaction optionConverted = CALL_BOBLM4_TRA_DEFINITION.toDerivative(referenceDateTrade, REFERENCE_PRICE);
    assertEquals("BondFuturesOptionMarginTransactionDefinition: toDerivative", optionExpected, optionConverted);
  }

}
