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
import com.opengamma.analytics.financial.instrument.future.BondFutureOptionPremiumSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.FutureInstrumentsDescriptionDataSet;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class DeprecatedBondFutureOptionPremiumTransactionTest {

  // 5-Year U.S. Treasury Note Futures: FVU1
  private static final BondFutureDefinition FVU1_DEFINITION = FutureInstrumentsDescriptionDataSet.createBondFutureSecurityDefinition();
  // Option security
  private static final double STRIKE = 1.20;
  private static final boolean IS_CALL = true;
  private static final ZonedDateTime EXPIRATION_DATE = DateUtils.getUTCDate(2011, 8, 26);
  private static final BondFutureOptionPremiumSecurityDefinition FVU1_C120_SEC_DEFINITION = new BondFutureOptionPremiumSecurityDefinition(FVU1_DEFINITION, EXPIRATION_DATE, STRIKE, IS_CALL);
  private static final String[] CURVE_NAMES = new String[] {"A", "B"};
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 6, 16);
  private static final BondFutureOptionPremiumSecurity FVU1_C120_SEC = FVU1_C120_SEC_DEFINITION.toDerivative(REFERENCE_DATE, CURVE_NAMES);
  // Option transaction
  private static final int QUANTITY = -123;
  private static final ZonedDateTime PREMIUM_DATE = DateUtils.getUTCDate(2011, 6, 17);
  private static final double TRANSACTION_PRICE = 62.5 / 64d; // Prices for options quoted in 1/64.
  private static final double PREMIUM_AMOUNT = TRANSACTION_PRICE * QUANTITY * FVU1_C120_SEC_DEFINITION.getNotional();
  private static final PaymentFixed PREMIUM = new PaymentFixed(FVU1_C120_SEC.getCurrency(), TimeCalculator.getTimeBetween(REFERENCE_DATE, PREMIUM_DATE), PREMIUM_AMOUNT,
      FVU1_C120_SEC.getDiscountingCurveName());
  private static final BondFutureOptionPremiumTransaction FVU1_C120_TRA = new BondFutureOptionPremiumTransaction(FVU1_C120_SEC, QUANTITY, PREMIUM);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullSecurity() {
    new BondFutureOptionPremiumTransaction(null, QUANTITY, PREMIUM);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullPremium() {
    new BondFutureOptionPremiumTransaction(FVU1_C120_SEC, QUANTITY, null);
  }

  @Test
  /**
   * Tests the getter methods.
   */
  public void getter() {
    assertEquals("Bond future option premium transaction", FVU1_C120_SEC, FVU1_C120_TRA.getUnderlyingOption());
    assertEquals("Bond future option premium transaction", QUANTITY, FVU1_C120_TRA.getQuantity());
    assertEquals("Bond future option premium transaction", PREMIUM, FVU1_C120_TRA.getPremium());
  }

  @Test
  /**
   * Tests the equal and hashCode methods.
   */
  public void equalHash() {
    assertTrue(FVU1_C120_TRA.equals(FVU1_C120_TRA));
    final BondFutureOptionPremiumTransaction other = new BondFutureOptionPremiumTransaction(FVU1_C120_SEC, QUANTITY, PREMIUM);
    assertTrue(FVU1_C120_TRA.equals(other));
    assertTrue(FVU1_C120_TRA.hashCode() == other.hashCode());
    BondFutureOptionPremiumTransaction modified;
    final BondFutureOptionPremiumSecurity modifiedSec = FVU1_C120_SEC_DEFINITION.toDerivative(REFERENCE_DATE.plusDays(1), CURVE_NAMES);
    modified = new BondFutureOptionPremiumTransaction(modifiedSec, QUANTITY, PREMIUM);
    assertFalse(FVU1_C120_TRA.equals(modified));
    modified = new BondFutureOptionPremiumTransaction(FVU1_C120_SEC, QUANTITY + 1, PREMIUM);
    assertFalse(FVU1_C120_TRA.equals(modified));
    modified = new BondFutureOptionPremiumTransaction(FVU1_C120_SEC, QUANTITY, new PaymentFixed(FVU1_C120_SEC.getCurrency(), TimeCalculator.getTimeBetween(REFERENCE_DATE, PREMIUM_DATE),
        PREMIUM_AMOUNT + 1.0, FVU1_C120_SEC.getDiscountingCurveName()));
    assertFalse(FVU1_C120_TRA.equals(modified));
    assertFalse(FVU1_C120_TRA.equals(EXPIRATION_DATE));
    assertFalse(FVU1_C120_TRA.equals(null));
  }
}
