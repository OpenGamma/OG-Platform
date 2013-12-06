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

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BondFutureOptionPremiumTransactionDefinitionTest {

  // 5-Year U.S. Treasury Note Futures: FVU1
  private static final BondFutureDefinition FVU1_DEFINITION = FutureInstrumentsDescriptionDataSet.createBondFutureSecurityDefinition();
  // Option security
  private static final double STRIKE = 1.20;
  private static final boolean IS_CALL = true;
  private static final ZonedDateTime EXPIRATION_DATE = DateUtils.getUTCDate(2011, 8, 26);
  private static final BondFutureOptionPremiumSecurityDefinition FVU1_C120_SEC_DEFINITION = new BondFutureOptionPremiumSecurityDefinition(FVU1_DEFINITION,
      EXPIRATION_DATE, STRIKE, IS_CALL);
  // Option transaction
  private static final ZonedDateTime PREMIUM_DATE = DateUtils.getUTCDate(2011, 6, 17);
  private static final double TRANSACTION_PRICE = -62.5 / 64d; // Prices for options quoted in 1/64.
  private static final int QUANTITY = -123;
  private static final double PREMIUM_AMOUNT = TRANSACTION_PRICE * QUANTITY * FVU1_C120_SEC_DEFINITION.getNotional();
  private static final BondFutureOptionPremiumTransactionDefinition FVU1_C120_TR_DEFINITION = new BondFutureOptionPremiumTransactionDefinition(FVU1_C120_SEC_DEFINITION,
      QUANTITY, PREMIUM_DATE, PREMIUM_AMOUNT);

  private static final String[] CURVE_NAMES = new String[] {"A", "B"};


  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullSecurity() {
    new BondFutureOptionPremiumTransactionDefinition(null, QUANTITY, PREMIUM_DATE, PREMIUM_AMOUNT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullDate() {
    new BondFutureOptionPremiumTransactionDefinition(FVU1_C120_SEC_DEFINITION, QUANTITY, null, PREMIUM_AMOUNT);
  }

  @Test
  public void fromTradePrice() {
    final BondFutureOptionPremiumTransactionDefinition from = BondFutureOptionPremiumTransactionDefinition.fromTradePrice(FVU1_C120_SEC_DEFINITION, QUANTITY,
        PREMIUM_DATE, TRANSACTION_PRICE);
    assertEquals("Bond future option premium transaction definition", from.getPremium().getReferenceAmount(), PREMIUM_AMOUNT);
    assertEquals("Bond future option premium transaction definition", from, FVU1_C120_TR_DEFINITION);
  }

  @Test
  /**
   * Tests the equal and hashCode methods.
   */
  public void equalHash() {
    assertTrue(FVU1_C120_TR_DEFINITION.equals(FVU1_C120_TR_DEFINITION));
    final BondFutureOptionPremiumTransactionDefinition other = new BondFutureOptionPremiumTransactionDefinition(FVU1_C120_SEC_DEFINITION, QUANTITY, PREMIUM_DATE,
        PREMIUM_AMOUNT);
    assertTrue(FVU1_C120_TR_DEFINITION.equals(other));
    assertTrue(FVU1_C120_TR_DEFINITION.hashCode() == other.hashCode());
    BondFutureOptionPremiumTransactionDefinition modified;
    final BondFutureOptionPremiumSecurityDefinition modifiedSec = new BondFutureOptionPremiumSecurityDefinition(FVU1_DEFINITION, EXPIRATION_DATE, STRIKE + 0.01, IS_CALL);
    modified = new BondFutureOptionPremiumTransactionDefinition(modifiedSec, QUANTITY, PREMIUM_DATE, PREMIUM_AMOUNT);
    assertFalse(FVU1_C120_TR_DEFINITION.equals(modified));
    modified = new BondFutureOptionPremiumTransactionDefinition(FVU1_C120_SEC_DEFINITION, QUANTITY + 1, PREMIUM_DATE, PREMIUM_AMOUNT);
    assertFalse(FVU1_C120_TR_DEFINITION.equals(modified));
    modified = new BondFutureOptionPremiumTransactionDefinition(FVU1_C120_SEC_DEFINITION, QUANTITY, PREMIUM_DATE.plusDays(1), PREMIUM_AMOUNT);
    assertFalse(FVU1_C120_TR_DEFINITION.equals(modified));
    modified = new BondFutureOptionPremiumTransactionDefinition(FVU1_C120_SEC_DEFINITION, QUANTITY, PREMIUM_DATE, PREMIUM_AMOUNT + 10.0);
    assertFalse(FVU1_C120_TR_DEFINITION.equals(modified));
    assertFalse(FVU1_C120_TR_DEFINITION.equals(EXPIRATION_DATE));
    assertFalse(FVU1_C120_TR_DEFINITION.equals(null));
  }

  /**
   * Tests the toDerivative method.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void toDerivativeBeforeSettleDeprecated() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 6, 16);
    final BondFutureOptionPremiumTransaction transactionConverted = FVU1_C120_TR_DEFINITION.toDerivative(referenceDate, CURVE_NAMES);
    final PaymentFixed premium = FVU1_C120_TR_DEFINITION.getPremium().toDerivative(referenceDate, CURVE_NAMES[1]);
    final BondFutureOptionPremiumTransaction transactionExpected = new BondFutureOptionPremiumTransaction(FVU1_C120_SEC_DEFINITION.toDerivative(referenceDate,
        CURVE_NAMES), QUANTITY, premium);
    assertEquals("Bond future option premium security definition: toDerivative", transactionExpected, transactionConverted);
  }

  /**
   * Tests the toDerivative method.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void toDerivativeOnSettleDeprecated() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 6, 17);
    final BondFutureOptionPremiumTransaction transactionConverted = FVU1_C120_TR_DEFINITION.toDerivative(referenceDate, CURVE_NAMES);
    final PaymentFixed premium = FVU1_C120_TR_DEFINITION.getPremium().toDerivative(referenceDate, CURVE_NAMES[1]);
    final BondFutureOptionPremiumTransaction transactionExpected = new BondFutureOptionPremiumTransaction(FVU1_C120_SEC_DEFINITION.toDerivative(referenceDate,
        CURVE_NAMES), QUANTITY, premium);
    assertEquals("Bond future option premium security definition: toDerivative", transactionExpected, transactionConverted);
  }

  /**
   * Tests the toDerivative method.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void toDerivativeAfterSettleDeprecated() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 6, 20);
    final BondFutureOptionPremiumTransaction transactionConverted = FVU1_C120_TR_DEFINITION.toDerivative(referenceDate, CURVE_NAMES);
    final PaymentFixed premium = new PaymentFixed(FVU1_C120_TR_DEFINITION.getCurrency(), 0.0, 0.0, CURVE_NAMES[1]);
    final BondFutureOptionPremiumTransaction transactionExpected = new BondFutureOptionPremiumTransaction(FVU1_C120_SEC_DEFINITION.toDerivative(referenceDate,
        CURVE_NAMES), QUANTITY, premium);
    assertEquals("Bond future option premium security definition: toDerivative", transactionExpected, transactionConverted);
  }

  /**
   * Tests the toDerivative method.
   */
  @Test
  public void toDerivativeBeforeSettle() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 6, 16);
    final BondFutureOptionPremiumTransaction transactionConverted = FVU1_C120_TR_DEFINITION.toDerivative(referenceDate);
    final PaymentFixed premium = FVU1_C120_TR_DEFINITION.getPremium().toDerivative(referenceDate);
    final BondFutureOptionPremiumTransaction transactionExpected = new BondFutureOptionPremiumTransaction(FVU1_C120_SEC_DEFINITION.toDerivative(referenceDate), QUANTITY, premium);
    assertEquals("Bond future option premium security definition: toDerivative", transactionExpected, transactionConverted);
  }

  /**
   * Tests the toDerivative method.
   */
  @Test
  public void toDerivativeOnSettle() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 6, 17);
    final BondFutureOptionPremiumTransaction transactionConverted = FVU1_C120_TR_DEFINITION.toDerivative(referenceDate);
    final PaymentFixed premium = FVU1_C120_TR_DEFINITION.getPremium().toDerivative(referenceDate);
    final BondFutureOptionPremiumTransaction transactionExpected = new BondFutureOptionPremiumTransaction(FVU1_C120_SEC_DEFINITION.toDerivative(referenceDate), QUANTITY, premium);
    assertEquals("Bond future option premium security definition: toDerivative", transactionExpected, transactionConverted);
  }

  /**
   * Tests the toDerivative method.
   */
  @Test
  public void toDerivativeAfterSettle() {
    final ZonedDateTime referenceDate = DateUtils.getUTCDate(2011, 6, 20);
    final BondFutureOptionPremiumTransaction transactionConverted = FVU1_C120_TR_DEFINITION.toDerivative(referenceDate);
    final PaymentFixed premium = new PaymentFixed(FVU1_C120_TR_DEFINITION.getCurrency(), 0.0, 0.0);
    final BondFutureOptionPremiumTransaction transactionExpected = new BondFutureOptionPremiumTransaction(FVU1_C120_SEC_DEFINITION.toDerivative(referenceDate), QUANTITY, premium);
    assertEquals("Bond future option premium security definition: toDerivative", transactionExpected, transactionConverted);
  }
}
