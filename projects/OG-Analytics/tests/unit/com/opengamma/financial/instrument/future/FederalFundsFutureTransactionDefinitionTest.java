/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.future;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.instrument.index.IndexON;
import com.opengamma.financial.instrument.index.indexon.FEDFUND;
import com.opengamma.financial.interestrate.future.derivative.FederalFundsFutureSecurity;
import com.opengamma.financial.interestrate.future.derivative.FederalFundsFutureTransaction;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

/**
 * Tests related to the construction of Federal Funds Futures transactions.
 */
public class FederalFundsFutureTransactionDefinitionTest {

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final IndexON INDEX_FEDFUND = new FEDFUND(NYC);

  private static final ZonedDateTime MARCH_1 = DateUtils.getUTCDate(2012, 3, 1);
  private static final double NOTIONAL = 5000000;
  private static final double PAYMENT_ACCURAL_FACTOR = 1.0 / 12.0;
  private static final String NAME = "FFH2";
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2012, 2, 1);
  private static final double TRADE_PRICE = 0.99900;
  private static final int QUANTITY = 12;

  private static final FederalFundsFutureSecurityDefinition FUTURE_SECURITY_DEFINITION = FederalFundsFutureSecurityDefinition.from(MARCH_1, INDEX_FEDFUND, NOTIONAL, PAYMENT_ACCURAL_FACTOR, NAME);
  private static final FederalFundsFutureTransactionDefinition FUTURE_TRANSACTION_DEFINITION = new FederalFundsFutureTransactionDefinition(FUTURE_SECURITY_DEFINITION, QUANTITY, TRADE_DATE,
      TRADE_PRICE);

  private static final String CURVE_NAME = "OIS";

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullUnderlying() {
    new FederalFundsFutureTransactionDefinition(null, QUANTITY, TRADE_DATE, TRADE_PRICE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullTradeDate() {
    new FederalFundsFutureTransactionDefinition(FUTURE_SECURITY_DEFINITION, QUANTITY, null, TRADE_PRICE);
  }

  @Test
  /**
   * Tests the getter methods.
   */
  public void getter() {
    assertEquals("Fed fund future transaction definition: getter", FUTURE_SECURITY_DEFINITION, FUTURE_TRANSACTION_DEFINITION.getUnderlyingFuture());
    assertEquals("Fed fund future transaction definition: getter", QUANTITY, FUTURE_TRANSACTION_DEFINITION.getQuantity());
    assertEquals("Fed fund future transaction definition: getter", TRADE_DATE, FUTURE_TRANSACTION_DEFINITION.getTradeDate());
    assertEquals("Fed fund future transaction definition: getter", TRADE_PRICE, FUTURE_TRANSACTION_DEFINITION.getTradePrice());
  }

  @Test
  /**
   * Tests the equal and hashCode methods.
   */
  public void equalHash() {
    assertTrue(FUTURE_TRANSACTION_DEFINITION.equals(FUTURE_TRANSACTION_DEFINITION));
    FederalFundsFutureTransactionDefinition other = new FederalFundsFutureTransactionDefinition(FUTURE_SECURITY_DEFINITION, QUANTITY, TRADE_DATE, TRADE_PRICE);
    assertTrue(FUTURE_TRANSACTION_DEFINITION.equals(other));
    assertTrue(FUTURE_TRANSACTION_DEFINITION.hashCode() == other.hashCode());
    FederalFundsFutureTransactionDefinition modifiedFuture;
    FederalFundsFutureSecurityDefinition otherSecurity = FederalFundsFutureSecurityDefinition.from(MARCH_1, INDEX_FEDFUND, NOTIONAL, PAYMENT_ACCURAL_FACTOR, "Other");
    modifiedFuture = new FederalFundsFutureTransactionDefinition(otherSecurity, QUANTITY, TRADE_DATE, TRADE_PRICE);
    assertFalse(FUTURE_TRANSACTION_DEFINITION.equals(modifiedFuture));
    modifiedFuture = new FederalFundsFutureTransactionDefinition(FUTURE_SECURITY_DEFINITION, QUANTITY + 1, TRADE_DATE, TRADE_PRICE);
    assertFalse(FUTURE_TRANSACTION_DEFINITION.equals(modifiedFuture));
    modifiedFuture = new FederalFundsFutureTransactionDefinition(FUTURE_SECURITY_DEFINITION, QUANTITY, TRADE_DATE.minusDays(1), TRADE_PRICE);
    assertFalse(FUTURE_TRANSACTION_DEFINITION.equals(modifiedFuture));
    modifiedFuture = new FederalFundsFutureTransactionDefinition(FUTURE_SECURITY_DEFINITION, QUANTITY, TRADE_DATE, TRADE_PRICE + 0.0001);
    assertFalse(FUTURE_TRANSACTION_DEFINITION.equals(modifiedFuture));
    assertFalse(FUTURE_TRANSACTION_DEFINITION.equals(TRADE_DATE));
    assertFalse(FUTURE_TRANSACTION_DEFINITION.equals(null));
  }

  @Test
  /**
   * Tests the toDerivative method before the security first fixing date - trade date.
   */
  public void toDerivativeNoFixingTradeDate() {
    ZonedDateTime referenceDate = TRADE_DATE;
    ZonedDateTime[] closingDate = new ZonedDateTime[] {TRADE_DATE.minusDays(2), TRADE_DATE.minusDays(1)};
    double[] closingPrice = new double[] {0.99895, 0.99905};
    ArrayZonedDateTimeDoubleTimeSeries closingTS = new ArrayZonedDateTimeDoubleTimeSeries(closingDate, closingPrice);
    ZonedDateTime[] fixingDate = new ZonedDateTime[] {TRADE_DATE.minusDays(2), TRADE_DATE.minusDays(1)};
    double[] fixingRate = new double[] {0.0010, 0.0011};
    ArrayZonedDateTimeDoubleTimeSeries fixingTS = new ArrayZonedDateTimeDoubleTimeSeries(fixingDate, fixingRate);
    ArrayZonedDateTimeDoubleTimeSeries[] data = new ArrayZonedDateTimeDoubleTimeSeries[] {fixingTS, closingTS};
    FederalFundsFutureSecurity securityConverted = FUTURE_SECURITY_DEFINITION.toDerivative(referenceDate, fixingTS, CURVE_NAME);
    FederalFundsFutureTransaction transactionExpected = new FederalFundsFutureTransaction(securityConverted, QUANTITY, TRADE_PRICE);
    FederalFundsFutureTransaction transactionConverted = FUTURE_TRANSACTION_DEFINITION.toDerivative(referenceDate, data, CURVE_NAME);
    assertEquals("Fed fund future transaction definition: toDerivative", transactionExpected, transactionConverted);
  }

  @Test
  /**
   * Tests the toDerivative method before the security first fixing date - after trade date.
   */
  public void toDerivativeNoFixingAfterTradeDate() {
    ZonedDateTime referenceDate = ScheduleCalculator.getAdjustedDate(TRADE_DATE, 1, NYC);
    ZonedDateTime[] closingDate = new ZonedDateTime[] {TRADE_DATE.minusDays(2), TRADE_DATE.minusDays(1), TRADE_DATE};
    double[] closingPrice = new double[] {0.99895, 0.99905, 0.99915};
    ArrayZonedDateTimeDoubleTimeSeries closingTS = new ArrayZonedDateTimeDoubleTimeSeries(closingDate, closingPrice);
    ZonedDateTime[] fixingDate = new ZonedDateTime[] {TRADE_DATE.minusDays(2), TRADE_DATE.minusDays(1), TRADE_DATE};
    double[] fixingRate = new double[] {0.0010, 0.0011, 0.0009};
    ArrayZonedDateTimeDoubleTimeSeries fixingTS = new ArrayZonedDateTimeDoubleTimeSeries(fixingDate, fixingRate);
    ArrayZonedDateTimeDoubleTimeSeries[] data = new ArrayZonedDateTimeDoubleTimeSeries[] {fixingTS, closingTS};
    FederalFundsFutureSecurity securityConverted = FUTURE_SECURITY_DEFINITION.toDerivative(referenceDate, fixingTS, CURVE_NAME);
    FederalFundsFutureTransaction transactionExpected = new FederalFundsFutureTransaction(securityConverted, QUANTITY, closingPrice[2]);
    FederalFundsFutureTransaction transactionConverted = FUTURE_TRANSACTION_DEFINITION.toDerivative(referenceDate, data, CURVE_NAME);
    assertEquals("Fed fund future transaction definition: toDerivative", transactionExpected, transactionConverted);
  }

  @Test
  /**
   * Tests the toDerivative method after the security first fixing date, fixing unknown - after trade date.
   */
  public void toDerivativeFixingStartedBeforePublicationAfterTradeDate() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 3, 7);
    ZonedDateTime[] closingDate = new ZonedDateTime[] {TRADE_DATE.minusDays(2), TRADE_DATE.minusDays(1), TRADE_DATE, DateUtils.getUTCDate(2012, 3, 6)};
    double[] closingPrice = new double[] {0.99895, 0.99905, 0.99915, 0.99925};
    ArrayZonedDateTimeDoubleTimeSeries closingTS = new ArrayZonedDateTimeDoubleTimeSeries(closingDate, closingPrice);
    ZonedDateTime[] fixingDate = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 3, 1), DateUtils.getUTCDate(2012, 3, 2), DateUtils.getUTCDate(2012, 3, 5), DateUtils.getUTCDate(2012, 3, 6)};
    double[] fixingRate = new double[] {0.0010, 0.0011, 0.0012, 0.0013};
    ArrayZonedDateTimeDoubleTimeSeries fixingTS = new ArrayZonedDateTimeDoubleTimeSeries(fixingDate, fixingRate);
    ArrayZonedDateTimeDoubleTimeSeries[] data = new ArrayZonedDateTimeDoubleTimeSeries[] {fixingTS, closingTS};
    FederalFundsFutureSecurity securityConverted = FUTURE_SECURITY_DEFINITION.toDerivative(referenceDate, fixingTS, CURVE_NAME);
    FederalFundsFutureTransaction transactionExpected = new FederalFundsFutureTransaction(securityConverted, QUANTITY, closingPrice[3]);
    FederalFundsFutureTransaction transactionConverted = FUTURE_TRANSACTION_DEFINITION.toDerivative(referenceDate, data, CURVE_NAME);
    assertEquals("Fed fund future transaction definition: toDerivative", transactionExpected, transactionConverted);
  }

  @Test
  /**
   * Tests the toDerivative method after the security first fixing date, fixing unknown - after trade date.
   */
  public void toDerivativeFixingStartedAfterPublicationAfterTradeDate() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 3, 7);
    ZonedDateTime[] closingDate = new ZonedDateTime[] {TRADE_DATE.minusDays(2), TRADE_DATE.minusDays(1), TRADE_DATE, DateUtils.getUTCDate(2012, 3, 6)};
    double[] closingPrice = new double[] {0.99895, 0.99905, 0.99915, 0.99925};
    ArrayZonedDateTimeDoubleTimeSeries closingTS = new ArrayZonedDateTimeDoubleTimeSeries(closingDate, closingPrice);
    ZonedDateTime[] fixingDate = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 3, 1), DateUtils.getUTCDate(2012, 3, 2), DateUtils.getUTCDate(2012, 3, 5), DateUtils.getUTCDate(2012, 3, 6),
        DateUtils.getUTCDate(2012, 3, 7)};
    double[] fixingRate = new double[] {0.0010, 0.0011, 0.0012, 0.0013, 0.0014};
    ArrayZonedDateTimeDoubleTimeSeries fixingTS = new ArrayZonedDateTimeDoubleTimeSeries(fixingDate, fixingRate);
    ArrayZonedDateTimeDoubleTimeSeries[] data = new ArrayZonedDateTimeDoubleTimeSeries[] {fixingTS, closingTS};
    FederalFundsFutureSecurity securityConverted = FUTURE_SECURITY_DEFINITION.toDerivative(referenceDate, fixingTS, CURVE_NAME);
    FederalFundsFutureTransaction transactionExpected = new FederalFundsFutureTransaction(securityConverted, QUANTITY, closingPrice[3]);
    FederalFundsFutureTransaction transactionConverted = FUTURE_TRANSACTION_DEFINITION.toDerivative(referenceDate, data, CURVE_NAME);
    assertEquals("Fed fund future transaction definition: toDerivative", transactionExpected, transactionConverted);
  }

  @Test
  /**
   * Tests the toDerivative method after the security first fixing date, fixing unknown - after trade date.
   */
  public void toDerivativeFixingStartedAfterPublicationTradeDate() {
    ZonedDateTime referenceDate = DateUtils.getUTCDate(2012, 3, 7);
    ZonedDateTime[] closingDate = new ZonedDateTime[] {TRADE_DATE.minusDays(2), TRADE_DATE.minusDays(1), TRADE_DATE, DateUtils.getUTCDate(2012, 3, 6)};
    double[] closingPrice = new double[] {0.99895, 0.99905, 0.99915, 0.99925};
    ArrayZonedDateTimeDoubleTimeSeries closingTS = new ArrayZonedDateTimeDoubleTimeSeries(closingDate, closingPrice);
    ZonedDateTime[] fixingDate = new ZonedDateTime[] {DateUtils.getUTCDate(2012, 3, 1), DateUtils.getUTCDate(2012, 3, 2), DateUtils.getUTCDate(2012, 3, 5), DateUtils.getUTCDate(2012, 3, 6),
        DateUtils.getUTCDate(2012, 3, 7)};
    double[] fixingRate = new double[] {0.0010, 0.0011, 0.0012, 0.0013, 0.0014};
    ArrayZonedDateTimeDoubleTimeSeries fixingTS = new ArrayZonedDateTimeDoubleTimeSeries(fixingDate, fixingRate);
    ArrayZonedDateTimeDoubleTimeSeries[] data = new ArrayZonedDateTimeDoubleTimeSeries[] {fixingTS, closingTS};
    FederalFundsFutureTransactionDefinition futureTransactionDefinition = new FederalFundsFutureTransactionDefinition(FUTURE_SECURITY_DEFINITION, QUANTITY, referenceDate, TRADE_PRICE);
    FederalFundsFutureSecurity securityConverted = FUTURE_SECURITY_DEFINITION.toDerivative(referenceDate, fixingTS, CURVE_NAME);
    FederalFundsFutureTransaction transactionExpected = new FederalFundsFutureTransaction(securityConverted, QUANTITY, TRADE_PRICE);
    FederalFundsFutureTransaction transactionConverted = futureTransactionDefinition.toDerivative(referenceDate, data, CURVE_NAME);
    assertEquals("Fed fund future transaction definition: toDerivative", transactionExpected, transactionConverted);
  }

}
