/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionPremiumSecurity;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BondFuturesOptionPremiumSecurityDefinitionTest {

  // 5-Year U.S. Treasury Note Futures: FVU1
  private static final BondFuturesSecurityDefinition FVU1_DEFINITION = BondFuturesDataSets.FVU1Definition();
  // Option
  private static final double STRIKE = 1.25;
  private static final boolean IS_CALL = true;
  private static final ZonedDateTime EXPIRATION_DATE = DateUtils.getUTCDate(2011, 8, 26);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 17);

  private static final BondFuturesOptionPremiumSecurityDefinition FVU1_C100_DEFINITION = 
      new BondFuturesOptionPremiumSecurityDefinition(FVU1_DEFINITION, EXPIRATION_DATE, STRIKE, IS_CALL);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullFuture() {
    new BondFuturesOptionPremiumSecurityDefinition(null, EXPIRATION_DATE, STRIKE, IS_CALL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullExpiry() {
    new BondFuturesOptionPremiumSecurityDefinition(FVU1_DEFINITION, null, STRIKE, IS_CALL);
  }

  @Test
  /**
   * Tests the getter methods.
   */
  public void getter() {
    assertEquals("Bond future option premium security definition", FVU1_DEFINITION,
        FVU1_C100_DEFINITION.getUnderlyingFuture());
    assertEquals("Bond future option premium security definition", EXPIRATION_DATE,
        FVU1_C100_DEFINITION.getExpirationDate());
    assertEquals("Bond future option premium security definition", STRIKE, FVU1_C100_DEFINITION.getStrike());
    assertEquals("Bond future option premium security definition", IS_CALL, FVU1_C100_DEFINITION.isCall());
  }

  @Test
  /**
   * Tests the equal and hashCode methods.
   */
  public void equalHash() {
    assertTrue(FVU1_C100_DEFINITION.equals(FVU1_C100_DEFINITION));
    final BondFuturesOptionPremiumSecurityDefinition other = new BondFuturesOptionPremiumSecurityDefinition(
        FVU1_DEFINITION, EXPIRATION_DATE, STRIKE, IS_CALL);
    assertTrue(FVU1_C100_DEFINITION.equals(other));
    assertTrue(FVU1_C100_DEFINITION.hashCode() == other.hashCode());
    BondFuturesOptionPremiumSecurityDefinition modified;
    final BondFuturesSecurityDefinition modifiedFuture =
        new BondFuturesSecurityDefinition(FVU1_DEFINITION.getLastTradingDate().plusDays(1),
            FVU1_DEFINITION.getNoticeFirstDate(), FVU1_DEFINITION.getNoticeLastDate(),
            FVU1_DEFINITION.getNotional(), FVU1_DEFINITION.getDeliveryBasket(), FVU1_DEFINITION.getConversionFactor());
    modified = new BondFuturesOptionPremiumSecurityDefinition(modifiedFuture, EXPIRATION_DATE, STRIKE, IS_CALL);
    assertFalse(FVU1_C100_DEFINITION.equals(modified));
    modified = new BondFuturesOptionPremiumSecurityDefinition(FVU1_DEFINITION, EXPIRATION_DATE.plusDays(1), STRIKE,
        IS_CALL);
    assertFalse(FVU1_C100_DEFINITION.equals(modified));
    modified = new BondFuturesOptionPremiumSecurityDefinition(FVU1_DEFINITION, EXPIRATION_DATE, STRIKE + 0.1, IS_CALL);
    assertFalse(FVU1_C100_DEFINITION.equals(modified));
    modified = new BondFuturesOptionPremiumSecurityDefinition(FVU1_DEFINITION, EXPIRATION_DATE, STRIKE, !IS_CALL);
    assertFalse(FVU1_C100_DEFINITION.equals(modified));
    assertFalse(FVU1_C100_DEFINITION.equals(EXPIRATION_DATE));
    assertFalse(FVU1_C100_DEFINITION.equals(null));
  }

  /**
   * Tests the toDerivative method.
   */
  @Test
  public void toDerivative() {
    final BondFuturesOptionPremiumSecurity optionConverted = FVU1_C100_DEFINITION.toDerivative(REFERENCE_DATE);
    final BondFuturesOptionPremiumSecurity optionExpected =
        new BondFuturesOptionPremiumSecurity(FVU1_DEFINITION.toDerivative(REFERENCE_DATE),
            TimeCalculator.getTimeBetween(REFERENCE_DATE, EXPIRATION_DATE), STRIKE, IS_CALL);
    assertEquals("Bond future option premium security definition: toDerivative", optionExpected, optionConverted);
  }
  
}
