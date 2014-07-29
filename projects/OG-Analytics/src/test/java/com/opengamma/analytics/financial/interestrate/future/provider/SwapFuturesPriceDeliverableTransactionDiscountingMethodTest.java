/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.SwapFuturesPriceDeliverableSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.SwapFuturesPriceDeliverableTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.future.calculator.FuturesPriceMulticurveCalculator;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableTransaction;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test related to methods on deliverable swap futures, price quoted.
 */
@Test(groups = TestGroup.UNIT)
public class SwapFuturesPriceDeliverableTransactionDiscountingMethodTest {

  private static final MulticurveProviderDiscount MULTICURVE = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex[] INDEX_LIST = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd();
  private static final IborIndex USDLIBOR3M = INDEX_LIST[2];
  private static final Currency USD = USDLIBOR3M.getCurrency();
  private static final Calendar NYC = MulticurveProviderDiscountDataSets.getUSDCalendar();
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("USD6MLIBOR3M", NYC);
  private static final ZonedDateTime EFFECTIVE_DATE = DateUtils.getUTCDate(2013, 6, 19);
  private static final ZonedDateTime LAST_TRADING_DATE = ScheduleCalculator.getAdjustedDate(EFFECTIVE_DATE, -USD6MLIBOR3M.getSpotLag(), NYC);
  private static final Period TENOR = Period.ofYears(10);
  private static final double NOTIONAL = 100000;
  private static final double RATE = 0.0175;
  private static final SwapFixedIborDefinition SWAP_DEFINITION = SwapFixedIborDefinition.from(EFFECTIVE_DATE, TENOR, USD6MLIBOR3M, 1.0, RATE, false);
  private static final SwapFuturesPriceDeliverableSecurityDefinition SWAP_FUTURES_SECURITY_DEFINITION = new SwapFuturesPriceDeliverableSecurityDefinition(LAST_TRADING_DATE, SWAP_DEFINITION, NOTIONAL);
  private static final ZonedDateTime TRAN_DATE = DateUtils.getUTCDate(2013, 3, 28);
  private static final double TRAN_PRICE = 0.98 + 31.0 / 32.0 / 100.0; // price quoted in 32nd of 1%
  private static final int QUANTITY = 1234;
  private static final SwapFuturesPriceDeliverableTransactionDefinition SWAP_FUTURES_TRANSACTION_DEFINITION =
      new SwapFuturesPriceDeliverableTransactionDefinition(SWAP_FUTURES_SECURITY_DEFINITION, QUANTITY, TRAN_DATE, TRAN_PRICE);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2013, 3, 28);
  private static final double LASTMARG_PRICE = 0.99 + 8.0 / 32.0 / 100.0; // price quoted in 32nd of 1%
  private static final SwapFuturesPriceDeliverableTransaction SWAP_FUTURES_TRANSACTION = SWAP_FUTURES_TRANSACTION_DEFINITION.toDerivative(REFERENCE_DATE, LASTMARG_PRICE);

  private static final FuturesPriceMulticurveCalculator FPMC = FuturesPriceMulticurveCalculator.getInstance();
  private static final FuturesTransactionMulticurveMethod FTMC = new FuturesTransactionMulticurveMethod();
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQDC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();

  private static final double SHIFT = 1.0E-7;
  private static final ParameterSensitivityParameterCalculator<MulticurveProviderInterface> PSC = new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator PSC_DSC_FD = new ParameterSensitivityMulticurveDiscountInterpolatedFDCalculator(PVDC, SHIFT);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2; // 0.01 currency unit by bp for 100m

  @Test
  public void presentValue() {
    final double price = SWAP_FUTURES_TRANSACTION.getUnderlyingSecurity().accept(FPMC, MULTICURVE);
    final MultipleCurrencyAmount pvComputed = FTMC.presentValue(SWAP_FUTURES_TRANSACTION, MULTICURVE);
    final MultipleCurrencyAmount pvExpected1 = FTMC.presentValueFromPrice(SWAP_FUTURES_TRANSACTION, price);
    assertEquals("SwapFuturesPriceDeliverableTransactionDiscountingMethod: present value", pvExpected1.getAmount(USD), pvComputed.getAmount(USD), TOLERANCE_PV);
    final double pvExpected2 = (price - SWAP_FUTURES_TRANSACTION.getReferencePrice()) * SWAP_FUTURES_SECURITY_DEFINITION.getNotional() * QUANTITY;
    assertEquals("SwapFuturesPriceDeliverableTransactionDiscountingMethod: present value", pvExpected2, pvComputed.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  public void presentValueMethodVsCalculator() {
    final MultipleCurrencyAmount pvMethod = FTMC.presentValue(SWAP_FUTURES_TRANSACTION, MULTICURVE);
    final MultipleCurrencyAmount pvCalculator = SWAP_FUTURES_TRANSACTION.accept(PVDC, MULTICURVE);
    assertEquals("SwapFuturesPriceDeliverableTransactionDiscountingMethod: present value", pvMethod.getAmount(USD), pvCalculator.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  public void parSpread() {
    double ps = SWAP_FUTURES_TRANSACTION.accept(PSMQDC, MULTICURVE);
    SwapFuturesPriceDeliverableTransactionDefinition fut0Definition =
        new SwapFuturesPriceDeliverableTransactionDefinition(SWAP_FUTURES_SECURITY_DEFINITION, QUANTITY, TRAN_DATE, TRAN_PRICE + ps);
    SwapFuturesPriceDeliverableTransaction fut0 = fut0Definition.toDerivative(REFERENCE_DATE, LASTMARG_PRICE);
    final MultipleCurrencyAmount pvComputed = FTMC.presentValue(fut0, MULTICURVE);
    assertEquals("SwapFuturesPriceDeliverableTransactionDiscountingMethod: present value", 0, pvComputed.getAmount(USD), TOLERANCE_PV);
  }

  @Test
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyParameterSensitivity pvpsDepositExact = PSC.calculateSensitivity(SWAP_FUTURES_TRANSACTION, MULTICURVE);
    final MultipleCurrencyParameterSensitivity pvpsDepositFD = PSC_DSC_FD.calculateSensitivity(SWAP_FUTURES_TRANSACTION, MULTICURVE);
    AssertSensitivityObjects.assertEquals("SwapFuturesPriceDeliverableTransactionDiscountingMethod: presentValueCurveSensitivity ", pvpsDepositExact, pvpsDepositFD, TOLERANCE_PV_DELTA);
  }

  @Test
  public void presentValueCurveSensitivityMethodVsCalculator() {
    final MultipleCurrencyMulticurveSensitivity pvcsMethod = FTMC.presentValueCurveSensitivity(SWAP_FUTURES_TRANSACTION, MULTICURVE);
    final MultipleCurrencyMulticurveSensitivity pvcsCalculator = SWAP_FUTURES_TRANSACTION.accept(PVCSDC, MULTICURVE);
    AssertSensitivityObjects.assertEquals("SwapFuturesPriceDeliverableTransactionDiscountingMethod: present value curve sensitivity",
        pvcsMethod, pvcsCalculator, TOLERANCE_PV_DELTA);
  }

}
