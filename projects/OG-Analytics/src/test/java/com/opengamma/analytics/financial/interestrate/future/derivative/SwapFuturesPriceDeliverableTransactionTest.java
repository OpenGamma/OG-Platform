/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.SwapFuturesPriceDeliverableSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the description of Deliverable Interest Rate Swap Futures as traded on CME.
 */
@Test(groups = TestGroup.UNIT)
public class SwapFuturesPriceDeliverableTransactionTest {

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

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2013, 3, 28);

  private static final SwapFuturesPriceDeliverableSecurity SWAP_FUTURES_SECURITY = SWAP_FUTURES_SECURITY_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final double REF_PRICE = 0.98 + 31.0 / 32.0 / 100.0; // price quoted in 32nd of 1%.
  private static final int QUANTITY = 1234;

  private static final SwapFuturesPriceDeliverableTransaction SWAP_FUTURES_TRANSACTION = new SwapFuturesPriceDeliverableTransaction(SWAP_FUTURES_SECURITY, REF_PRICE, QUANTITY);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullSwap() {
    new SwapFuturesPriceDeliverableTransaction(null, REF_PRICE, QUANTITY);
  }

  @Test
  /**
   * Tests the getter methods.
   */
  public void getter() {
    assertEquals("DeliverableSwapFuturesTransaction: getter", SWAP_FUTURES_SECURITY, SWAP_FUTURES_TRANSACTION.getUnderlyingSecurity());
    assertEquals("DeliverableSwapFuturesTransaction: getter", REF_PRICE, SWAP_FUTURES_TRANSACTION.getReferencePrice());
    assertEquals("DeliverableSwapFuturesTransaction: getter", QUANTITY, SWAP_FUTURES_TRANSACTION.getQuantity());
    assertEquals("DeliverableSwapFuturesTransaction: getter", USD6MLIBOR3M.getCurrency(), SWAP_FUTURES_TRANSACTION.getCurrency());
  }

  @Test
  public void testHashCodeEquals() {
    SwapFuturesPriceDeliverableTransaction other = new SwapFuturesPriceDeliverableTransaction(SWAP_FUTURES_SECURITY, REF_PRICE, QUANTITY);
    assertEquals(SWAP_FUTURES_TRANSACTION, other);
    assertEquals(SWAP_FUTURES_TRANSACTION.hashCode(), other.hashCode());
    other = new SwapFuturesPriceDeliverableTransaction(SWAP_FUTURES_SECURITY_DEFINITION.toDerivative(REFERENCE_DATE.plusDays(1)), REF_PRICE, QUANTITY);
    assertFalse(other.equals(SWAP_FUTURES_TRANSACTION));
    other = new SwapFuturesPriceDeliverableTransaction(SWAP_FUTURES_SECURITY, REF_PRICE + 1, QUANTITY);
    assertFalse(other.equals(SWAP_FUTURES_TRANSACTION));
    other = new SwapFuturesPriceDeliverableTransaction(SWAP_FUTURES_SECURITY, REF_PRICE, QUANTITY + 1);
    assertFalse(other.equals(SWAP_FUTURES_TRANSACTION));
  }

}
