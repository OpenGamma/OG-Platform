/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.BondFutureDefinition;
import com.opengamma.analytics.financial.instrument.future.FutureInstrumentsDescriptionDataSet;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BondFutureOptionPremiumSecurityTest {

  // 5-Year U.S. Treasury Note Futures: FVU1
  private static final BondFutureDefinition FVU1_DEFINITION = FutureInstrumentsDescriptionDataSet.createBondFutureSecurityDefinition();
  // Option
  private static final double STRIKE = 1.25;
  private static final boolean IS_CALL = true;
  private static final ZonedDateTime EXPIRATION_DATE = DateUtils.getUTCDate(2011, 8, 26);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 17);

  private static final BondFuture FVU1 = FVU1_DEFINITION.toDerivative(REFERENCE_DATE, 0.0);
  private static final double EXPIRATION_TIME = TimeCalculator.getTimeBetween(REFERENCE_DATE, EXPIRATION_DATE);
  private static final BondFutureOptionPremiumSecurity FVU1_C125 = new BondFutureOptionPremiumSecurity(FVU1, EXPIRATION_TIME, STRIKE, IS_CALL);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullFuture() {
    new BondFutureOptionPremiumSecurity(null, EXPIRATION_TIME, STRIKE, IS_CALL);
  }

  @Test
  /**
   * Tests the getter methods.
   */
  public void getter() {
    assertEquals("Bond future option premium security", FVU1, FVU1_C125.getUnderlyingFuture());
    assertEquals("Bond future option premium security", EXPIRATION_TIME, FVU1_C125.getExpirationTime());
    assertEquals("Bond future option premium security", STRIKE, FVU1_C125.getStrike());
    assertEquals("Bond future option premium security", IS_CALL, FVU1_C125.isCall());
  }

  @Test
  /**
   * Tests the equal and hashCode methods.
   */
  public void equalHash() {
    assertTrue(FVU1_C125.equals(FVU1_C125));
    final BondFutureOptionPremiumSecurity other = new BondFutureOptionPremiumSecurity(FVU1, EXPIRATION_TIME, STRIKE, IS_CALL);
    assertTrue(FVU1_C125.equals(other));
    assertTrue(FVU1_C125.hashCode() == other.hashCode());
    BondFutureOptionPremiumSecurity modified;
    final BondFuture modifiedFuture = FVU1_DEFINITION.toDerivative(REFERENCE_DATE.plusDays(1), 0.0);
    modified = new BondFutureOptionPremiumSecurity(modifiedFuture, EXPIRATION_TIME, STRIKE, IS_CALL);
    assertFalse(FVU1_C125.equals(modified));
    modified = new BondFutureOptionPremiumSecurity(FVU1, EXPIRATION_TIME + 0.01, STRIKE, IS_CALL);
    assertFalse(FVU1_C125.equals(modified));
    modified = new BondFutureOptionPremiumSecurity(FVU1, EXPIRATION_TIME, STRIKE + 0.1, IS_CALL);
    assertFalse(FVU1_C125.equals(modified));
    modified = new BondFutureOptionPremiumSecurity(FVU1, EXPIRATION_TIME, STRIKE, !IS_CALL);
    assertFalse(FVU1_C125.equals(modified));
    assertFalse(FVU1_C125.equals(EXPIRATION_DATE));
    assertFalse(FVU1_C125.equals(null));
  }

}
