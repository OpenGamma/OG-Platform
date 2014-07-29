/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableTransaction;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the description of Deliverable Interest Rate Swap Futures as traded on CME.
 */
@Test(groups = TestGroup.UNIT)
public class SwapFuturesPriceDeliverableTransactionDefinitionTest {

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("USD6MLIBOR3M", NYC);
  private static final ZonedDateTime EFFECTIVE_DATE = DateUtils.getUTCDate(2013, 6, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(EFFECTIVE_DATE, -USD6MLIBOR3M.getSpotLag(), NYC);
  private static final Period TENOR = Period.ofYears(10);
  private static final double NOTIONAL = 100000;
  private static final double RATE = 0.0200;
  private static final SwapFixedIborDefinition SWAP_DEFINITION = SwapFixedIborDefinition.from(EFFECTIVE_DATE, TENOR, USD6MLIBOR3M, 1.0, RATE, false);
  private static final SwapFuturesPriceDeliverableSecurityDefinition SWAP_FUTURES_SECURITY_DEFINITION =
      new SwapFuturesPriceDeliverableSecurityDefinition(LAST_TRADING_DATE, SWAP_DEFINITION, NOTIONAL);

  private static final ZonedDateTime TRAN_DATE = DateUtils.getUTCDate(2013, 3, 28);
  private static final double TRAN_PRICE = 0.98 + 31.0 / 32.0 / 100.0; // price quoted in 32nd of 1%
  private static final int QUANTITY = 1234;
  private static final SwapFuturesPriceDeliverableTransactionDefinition SWAP_FUTURES_TRANSACTION_DEFINITION =
      new SwapFuturesPriceDeliverableTransactionDefinition(SWAP_FUTURES_SECURITY_DEFINITION, QUANTITY, TRAN_DATE, TRAN_PRICE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullUnderlying() {
    new SwapFuturesPriceDeliverableTransactionDefinition(null, QUANTITY, TRAN_DATE, TRAN_PRICE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullTrDate() {
    new SwapFuturesPriceDeliverableTransactionDefinition(SWAP_FUTURES_SECURITY_DEFINITION, QUANTITY, null, TRAN_PRICE);
  }

  @Test
  /**
   * Tests the getter methods.
   */
  public void getter() {
    assertEquals("DeliverableSwapFuturesTransactionDefinition: getter", SWAP_FUTURES_SECURITY_DEFINITION, SWAP_FUTURES_TRANSACTION_DEFINITION.getUnderlyingSecurity());
    assertEquals("DeliverableSwapFuturesTransactionDefinition: getter", TRAN_DATE, SWAP_FUTURES_TRANSACTION_DEFINITION.getTradeDate());
    assertEquals("DeliverableSwapFuturesTransactionDefinition: getter", TRAN_PRICE, SWAP_FUTURES_TRANSACTION_DEFINITION.getTradePrice());
    assertEquals("DeliverableSwapFuturesTransactionDefinition: getter", QUANTITY, SWAP_FUTURES_TRANSACTION_DEFINITION.getQuantity());
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeOnTradeDate() {
    final ZonedDateTime referenceDate = TRAN_DATE;
    final double lastMargin = 0.99 + 1.0 / 32.0 / 100.0;
    final SwapFuturesPriceDeliverableSecurity underlying = SWAP_FUTURES_SECURITY_DEFINITION.toDerivative(referenceDate);
    final SwapFuturesPriceDeliverableTransaction derivativeExpected = new SwapFuturesPriceDeliverableTransaction(underlying, TRAN_PRICE, QUANTITY);
    final SwapFuturesPriceDeliverableTransaction derivativeConverted = SWAP_FUTURES_TRANSACTION_DEFINITION.toDerivative(referenceDate, lastMargin);
    assertEquals("DeliverableSwapFuturesTransactionDefinition: toDerivative", derivativeExpected, derivativeConverted);
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivativeAfterTradeDate() {
    final ZonedDateTime referenceDate = ScheduleCalculator.getAdjustedDate(TRAN_DATE, 1, NYC);
    final double lastMargin = 0.99080;
    final SwapFuturesPriceDeliverableSecurity underlying = SWAP_FUTURES_SECURITY_DEFINITION.toDerivative(referenceDate);
    final SwapFuturesPriceDeliverableTransaction derivativeExpected = new SwapFuturesPriceDeliverableTransaction(underlying, lastMargin, QUANTITY);
    final SwapFuturesPriceDeliverableTransaction derivativeConverted = SWAP_FUTURES_TRANSACTION_DEFINITION.toDerivative(referenceDate, lastMargin);
    assertEquals("DeliverableSwapFuturesTransactionDefinition: toDerivative", derivativeExpected, derivativeConverted);
  }

}
