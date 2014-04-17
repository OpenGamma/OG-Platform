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

import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.index.IndexONMaster;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the construction of Federal Funds Futures transactions.
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class DeprecatedFederalFundsFutureTransactionTest {

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 1, 30);

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final IndexON INDEX_FEDFUND = IndexONMaster.getInstance().getIndex("FED FUND");

  private static final ZonedDateTime MARCH_1 = DateUtils.getUTCDate(2012, 3, 1);
  private static final double NOTIONAL = 5000000;
  private static final double PAYMENT_ACCURAL_FACTOR = 1.0 / 12.0;
  private static final String NAME = "FFH2";
  private static final double TRADE_PRICE = 0.99900;
  private static final int QUANTITY = 12;

  private static final String CURVE_NAME = "OIS";

  private static final FederalFundsFutureSecurityDefinition FUTURE_SECURITY_DEFINITION = FederalFundsFutureSecurityDefinition.from(MARCH_1, INDEX_FEDFUND, NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME, NYC);
  private static final FederalFundsFutureSecurity FUTURE_SECURITY_BEFOREFIXING = FUTURE_SECURITY_DEFINITION.toDerivative(REFERENCE_DATE, CURVE_NAME);

  private static final FederalFundsFutureTransaction FUTURE_TRANSACTION = new FederalFundsFutureTransaction(FUTURE_SECURITY_BEFOREFIXING, QUANTITY, TRADE_PRICE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullUnderlying() {
    new FederalFundsFutureTransaction(null, QUANTITY, TRADE_PRICE);
  }

  @Test
  /**
   * Tests the getter methods.
   */
  public void getter() {
    assertEquals("Fed fund future transaction: getter", FUTURE_SECURITY_BEFOREFIXING, FUTURE_TRANSACTION.getUnderlyingSecurity());
    assertEquals("Fed fund future transaction: getter", QUANTITY, FUTURE_TRANSACTION.getQuantity());
    assertEquals("Fed fund future transaction: getter", TRADE_PRICE, FUTURE_TRANSACTION.getReferencePrice());
  }

  @Test
  /**
   * Tests the equal and hashCode methods.
   */
  public void equalHash() {
    assertTrue(FUTURE_TRANSACTION.equals(FUTURE_TRANSACTION));
    final FederalFundsFutureTransaction other = new FederalFundsFutureTransaction(FUTURE_SECURITY_BEFOREFIXING, QUANTITY, TRADE_PRICE);
    assertTrue(FUTURE_TRANSACTION.equals(other));
    assertTrue(FUTURE_TRANSACTION.hashCode() == other.hashCode());
    FederalFundsFutureTransaction modifiedFuture;
    final FederalFundsFutureSecurity otherSecurity = FUTURE_SECURITY_DEFINITION.toDerivative(REFERENCE_DATE.minusDays(1), CURVE_NAME);
    modifiedFuture = new FederalFundsFutureTransaction(otherSecurity, QUANTITY, TRADE_PRICE);
    assertFalse(FUTURE_TRANSACTION.equals(modifiedFuture));
    modifiedFuture = new FederalFundsFutureTransaction(FUTURE_SECURITY_BEFOREFIXING, QUANTITY + 1, TRADE_PRICE);
    assertFalse(FUTURE_TRANSACTION.equals(modifiedFuture));
    modifiedFuture = new FederalFundsFutureTransaction(FUTURE_SECURITY_BEFOREFIXING, QUANTITY, TRADE_PRICE - 0.0001);
    assertFalse(FUTURE_TRANSACTION.equals(modifiedFuture));
    assertFalse(FUTURE_TRANSACTION.equals(REFERENCE_DATE));
    assertFalse(FUTURE_TRANSACTION.equals(null));
  }

}
