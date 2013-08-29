/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;

/**
 * Tests for the methods related to interest rate securities pricing without convexity adjustment.
 */
public class InterestRateFutureTransactionDiscountingMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex[] IBOR_INDEXES = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd();
  private static final IborIndex EURIBOR3M = IBOR_INDEXES[0];
  private static final Currency EUR = EURIBOR3M.getCurrency();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();

  // Future
  private static final ZonedDateTime SPOT_LAST_TRADING_DATE = DateUtils.getUTCDate(2012, 9, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(SPOT_LAST_TRADING_DATE, -EURIBOR3M.getSpotLag(), CALENDAR);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final String NAME = "ERU2";
  private static final int QUANTITY = 123;

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 5, 12);
  private static final ZonedDateTime TRADE_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, -1, CALENDAR);
  private static final double TRADE_PRICE = 0.99;
  private static final InterestRateFutureSecurityDefinition ERU2_SEC_DEFINITION = new InterestRateFutureSecurityDefinition(LAST_TRADING_DATE, EURIBOR3M, NOTIONAL, FUTURE_FACTOR, NAME, CALENDAR);
  private static final InterestRateFutureTransactionDefinition ERU2_TRA_DEFINITION = new InterestRateFutureTransactionDefinition(ERU2_SEC_DEFINITION, TRADE_DATE, TRADE_PRICE, QUANTITY);

  private static final double REFERENCE_PRICE = 0.98;
  private static final InterestRateFutureSecurity ERU2_SEC = ERU2_SEC_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final InterestRateFutureTransaction ERU2_TRA = ERU2_TRA_DEFINITION.toDerivative(REFERENCE_DATE, REFERENCE_PRICE);

  private static final InterestRateFutureTransactionDiscountingMethod METHOD_IRFUT_TRA_DSC = InterestRateFutureTransactionDiscountingMethod.getInstance();

  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQDC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();

  //  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PRICE = 1.0E-10;

  @Test
  /**
   * Test the par spread.
   */
  public void parSpread() {
    final double parSpread = ERU2_TRA.accept(PSMQDC, MULTICURVES);
    final InterestRateFutureTransaction futures0 = new InterestRateFutureTransaction(ERU2_SEC, REFERENCE_PRICE + parSpread, QUANTITY);
    //InterestRateFutureTransactionDefinition(ERU2_SEC_DEFINITION, TRADE_DATE, REFERENCE_PRICE + parSpread, QUANTITY);
    final MultipleCurrencyAmount pv0 = METHOD_IRFUT_TRA_DSC.presentValue(futures0, MULTICURVES);
    assertEquals("Future par spread", pv0.getAmount(EUR), 0, TOLERANCE_PRICE);
  }

}
