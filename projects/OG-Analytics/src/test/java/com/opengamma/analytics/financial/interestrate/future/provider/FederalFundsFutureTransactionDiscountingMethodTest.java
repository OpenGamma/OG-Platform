/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureTransaction;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test related to Fed Funds futures pricing.
 */
@Test(groups = TestGroup.UNIT)
public class FederalFundsFutureTransactionDiscountingMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IndexON INDEX_FEDFUND = MulticurveProviderDiscountDataSets.getIndexesON()[0];
  private static final Currency USD = INDEX_FEDFUND.getCurrency();
  private static final Calendar NYC = MulticurveProviderDiscountDataSets.getUSDCalendar();

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2012, 1, 30);

  private static final ZonedDateTime MARCH_1 = DateUtils.getUTCDate(2012, 3, 1);
  private static final double TRADE_PRICE = 0.99900;
  private static final int QUANTITY = 123;

  private static final FederalFundsFutureSecurityDefinition FUTURE_SECURITY_DEFINITION = FederalFundsFutureSecurityDefinition.fromFedFund(MARCH_1, INDEX_FEDFUND, NYC);
  private static final FederalFundsFutureTransactionDefinition FUTURE_TRANSACTION_DEFINITION = new FederalFundsFutureTransactionDefinition(FUTURE_SECURITY_DEFINITION, QUANTITY, REFERENCE_DATE,
      TRADE_PRICE);

  private static final ZonedDateTime[] CLOSING_DATE = new ZonedDateTime[] {REFERENCE_DATE.minusDays(2), REFERENCE_DATE.minusDays(1), REFERENCE_DATE };
  private static final double[] CLOSING_PRICE = new double[] {0.99895, 0.99905, 0.99915 };
  private static final ZonedDateTimeDoubleTimeSeries CLOSING_TS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(CLOSING_DATE, CLOSING_PRICE);
  private static final ZonedDateTime[] FIXING_DATE = new ZonedDateTime[] {REFERENCE_DATE.minusDays(2), REFERENCE_DATE.minusDays(1), REFERENCE_DATE };
  private static final double[] FIXING_RATE = new double[] {0.0010, 0.0011, 0.0009 };
  private static final ZonedDateTimeDoubleTimeSeries FIXING_TS = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(FIXING_DATE, FIXING_RATE);
  private static final ZonedDateTimeDoubleTimeSeries[] DATA = new ZonedDateTimeDoubleTimeSeries[] {FIXING_TS, CLOSING_TS };

  private static final FederalFundsFutureSecurity FUTURE_SECURITY = FUTURE_SECURITY_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final FederalFundsFutureTransaction FUTURE_TRANSACTION = FUTURE_TRANSACTION_DEFINITION.toDerivative(REFERENCE_DATE, DATA);

  private static final FederalFundsFutureSecurityDiscountingMethod METHOD_SECURITY = FederalFundsFutureSecurityDiscountingMethod.getInstance();
  private static final FederalFundsFutureTransactionDiscountingMethod METHOD_TRANSACTION = FederalFundsFutureTransactionDiscountingMethod.getInstance();

  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();

  private static final double SHIFT = 1.0E-6;

  private static final ParameterSensitivityParameterCalculator<ParameterProviderInterface> PSC = new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator PSC_DSC_FD = new ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator(PVDC, SHIFT);

  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQDC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadRateDiscountingCalculator PSRDC = ParSpreadRateDiscountingCalculator.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;
  private static final double TOLERANCE_RATE = 1.0E-8;

  @Test
  public void presentValueFromPrice() {
    final double price = 0.99895;
    final MultipleCurrencyAmount pv = METHOD_TRANSACTION.presentValueFromPrice(FUTURE_TRANSACTION, price);
    final double pvExpected = (price - FUTURE_TRANSACTION.getReferencePrice()) * FUTURE_SECURITY_DEFINITION.getNotional() * FUTURE_SECURITY_DEFINITION.getMarginAccrualFactor() * QUANTITY;
    assertEquals("Federal Funds Future transaction: present value", pvExpected, pv.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  public void presentValue() {
    final MultipleCurrencyAmount pv = METHOD_TRANSACTION.presentValue(FUTURE_TRANSACTION, MULTICURVES);
    final double price = METHOD_SECURITY.price(FUTURE_SECURITY, MULTICURVES);
    final MultipleCurrencyAmount pvExpected = METHOD_TRANSACTION.presentValueFromPrice(FUTURE_TRANSACTION, price);
    assertEquals("Federal Funds Future transaction: present value", pvExpected.getAmount(USD), pv.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyParameterSensitivity pvpsDepositExact = PSC.calculateSensitivity(FUTURE_TRANSACTION, MULTICURVES, MULTICURVES.getAllNames());
    final MultipleCurrencyParameterSensitivity pvpsDepositFD = PSC_DSC_FD.calculateSensitivity(FUTURE_TRANSACTION, MULTICURVES);
    AssertSensitivityObjects.assertEquals("FederalFundsFutureTransactionDiscountingMethod: presentValueCurveSensitivity ", pvpsDepositExact, pvpsDepositFD, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Test the par spread to market quotes.
   */
  public void parSpreadMarketQuote() {
    final double parSpreadMQ = FUTURE_TRANSACTION.accept(PSMQDC, MULTICURVES);
    final FederalFundsFutureTransaction futures0 = new FederalFundsFutureTransaction(FUTURE_SECURITY, QUANTITY, FUTURE_TRANSACTION.getReferencePrice() + parSpreadMQ);
    final MultipleCurrencyAmount pv0 = METHOD_TRANSACTION.presentValue(futures0, MULTICURVES);
    assertEquals("FederalFundsFutureTransactionDiscountingMethod: par spread market quote", pv0.getAmount(USD), 0, TOLERANCE_PV);
  }

  @Test
  /**
   * Test the par spread to rate.
   */
  public void parSpreadRate() {
    final double parSpreadRate = FUTURE_TRANSACTION.accept(PSRDC, MULTICURVES);
    final double parSpreadMQ = FUTURE_TRANSACTION.accept(PSMQDC, MULTICURVES);
    assertEquals("FederalFundsFutureTransactionDiscountingMethod: par spread market quote", -parSpreadMQ, parSpreadRate, TOLERANCE_RATE);
  }

}
