/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.datasets.CalendarTarget;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.provider.calculator.blackstirfutures.PresentValueBlackSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.blackstirfutures.PresentValueCurveSensitivityBlackSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.StandardDataSetsBlack;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesExpLogMoneynessProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesExpLogMoneynessProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.blackstirfutures.ParameterSensitivityBlackSTIRFuturesExpLogMoneynessDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.util.AssertSensivityObjects;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;

public class STIRFuturesOptionMarginTransactionBlackExpLogMoneynessMethodTest {

  /** Option on STIR futures */
  private static final IndexIborMaster MASTER_IBOR_INDEX = IndexIborMaster.getInstance();
  private static final IborIndex EURIBOR3M = MASTER_IBOR_INDEX.getIndex("EURIBOR3M");
  private static final Currency EUR = EURIBOR3M.getCurrency();
  private static final ZonedDateTime LAST_TRADE_DATE = DateUtils.getUTCDate(2014, 12, 15);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final String NAME = "ERZ4";
  private static final Calendar TARGET = new CalendarTarget("TARGET");
  private static final InterestRateFutureSecurityDefinition ERZ4_DEFINITION =
      new InterestRateFutureSecurityDefinition(LAST_TRADE_DATE, EURIBOR3M, NOTIONAL, FUTURE_FACTOR, NAME, TARGET);
  private static final ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2014, 11, 17);
  private static final double STRIKE_099 = 0.99;
  private static final boolean IS_CALL = true;
  private static final InterestRateFutureOptionMarginSecurityDefinition CALL_ERZ4_099_SEC_DEFINITION =
      new InterestRateFutureOptionMarginSecurityDefinition(ERZ4_DEFINITION, EXPIRY_DATE, STRIKE_099, IS_CALL);
  private static final InterestRateFutureOptionMarginSecurityDefinition PUT_ERZ4_099_SEC_DEFINITION =
      new InterestRateFutureOptionMarginSecurityDefinition(ERZ4_DEFINITION, EXPIRY_DATE, STRIKE_099, !IS_CALL);

  private static final int QUANTITY = 123;
  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2014, 3, 31);
  private static final double TRADE_PRICE = 0.01;

  private static final InterestRateFutureOptionMarginTransactionDefinition CALL_ERZ4_099_TRA_1_DEFINITION =
      new InterestRateFutureOptionMarginTransactionDefinition(CALL_ERZ4_099_SEC_DEFINITION, QUANTITY, TRADE_DATE, TRADE_PRICE);
  private static final InterestRateFutureOptionMarginTransactionDefinition CALL_ERZ4_099_TRA_2_DEFINITION =
      new InterestRateFutureOptionMarginTransactionDefinition(CALL_ERZ4_099_SEC_DEFINITION, QUANTITY, TRADE_DATE.minusDays(1), TRADE_PRICE);
  private static final InterestRateFutureOptionMarginTransactionDefinition PUT_ERZ4_099_TRA_1_DEFINITION =
      new InterestRateFutureOptionMarginTransactionDefinition(PUT_ERZ4_099_SEC_DEFINITION, QUANTITY, TRADE_DATE, TRADE_PRICE);
  private static final InterestRateFutureTransactionDefinition ERZ4_TRA_STRIKE_DEFINITION =
      new InterestRateFutureTransactionDefinition(ERZ4_DEFINITION, QUANTITY, TRADE_DATE, STRIKE_099);
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2014, 3, 31);
  private static final double REFERENCE_PRICE = 0.02;
  private static final InterestRateFutureOptionMarginTransaction CALL_ERZ4_099_TRA_1 = CALL_ERZ4_099_TRA_1_DEFINITION.toDerivative(REFERENCE_DATE, REFERENCE_PRICE);
  private static final InterestRateFutureOptionMarginTransaction CALL_ERZ4_099_TRA_2 = CALL_ERZ4_099_TRA_2_DEFINITION.toDerivative(REFERENCE_DATE, REFERENCE_PRICE);
  private static final InterestRateFutureOptionMarginTransaction PUT_ERZ4_099_TRA_1 = PUT_ERZ4_099_TRA_1_DEFINITION.toDerivative(REFERENCE_DATE, REFERENCE_PRICE);
  private static final InterestRateFutureTransaction ERZ4_TRA_STRIKE = ERZ4_TRA_STRIKE_DEFINITION.toDerivative(REFERENCE_DATE, 0.0);
  /** Black surface expiry/log-moneyness */
  final private static InterpolatedDoublesSurface BLACK_SURFACE_LOGMONEY = StandardDataSetsBlack.blackSurfaceExpiryLogMoneyness();
  /** EUR curves */
  final private static MulticurveProviderDiscount MULTICURVE = MulticurveProviderDiscountDataSets.createMulticurveEUR();
  final private static BlackSTIRFuturesExpLogMoneynessProviderDiscount MULTICURVE_BLACK =
      new BlackSTIRFuturesExpLogMoneynessProviderDiscount(MULTICURVE, BLACK_SURFACE_LOGMONEY, EURIBOR3M);

  /** Methods and calculators */
  private static final InterestRateFutureOptionMarginSecurityBlackSTIRFuturesMethod METHOD_OPT_SEC =
      InterestRateFutureOptionMarginSecurityBlackSTIRFuturesMethod.getInstance();
  private static final FuturesTransactionBlackSTIRFuturesMethod METHOD_OPT_TRA = new FuturesTransactionBlackSTIRFuturesMethod();
  private static final FuturesTransactionMulticurveMethod METHOD_FUT_TRA = new FuturesTransactionMulticurveMethod();
  private static final PresentValueBlackSTIRFutureOptionCalculator PVBFOC = PresentValueBlackSTIRFutureOptionCalculator.getInstance();
  private static final PresentValueCurveSensitivityBlackSTIRFutureOptionCalculator PVCSBFOC =
      PresentValueCurveSensitivityBlackSTIRFutureOptionCalculator.getInstance();
  private static final double SHIFT_FD = 1.0E-6;
  private static final ParameterSensitivityParameterCalculator<BlackSTIRFuturesProviderInterface> PSSFC = new ParameterSensitivityParameterCalculator<>(PVCSBFOC);
  private static final ParameterSensitivityBlackSTIRFuturesExpLogMoneynessDiscountInterpolatedFDCalculator PSSFC_FD =
      new ParameterSensitivityBlackSTIRFuturesExpLogMoneynessDiscountInterpolatedFDCalculator(PVBFOC, SHIFT_FD);

  /** Tolerances */
  private static final double TOLERANCE_PV = 1.0E-3;
  private static final double TOLERANCE_PV_DELTA = 1.0E-1;

  @Test
  public void presentValue() {
    final MultipleCurrencyAmount pvComputed1 = METHOD_OPT_TRA.presentValue(CALL_ERZ4_099_TRA_1, MULTICURVE_BLACK);
    final double priceOpt1 = METHOD_OPT_SEC.price(CALL_ERZ4_099_TRA_1.getUnderlyingSecurity(), MULTICURVE_BLACK);
    final double pvExpected1 = (priceOpt1 - TRADE_PRICE) * ERZ4_DEFINITION.getNotional() * ERZ4_DEFINITION.getPaymentAccrualFactor() * QUANTITY;
    assertEquals("BondFuturesOptionMarginTransactionBlackFlatMethod: present value", pvExpected1,
        pvComputed1.getAmount(ERZ4_DEFINITION.getCurrency()), TOLERANCE_PV);
    assertTrue("BondFuturesOptionMarginTransactionBlackFlatMethod: present value", pvComputed1.size() == 1);
    final MultipleCurrencyAmount pvCalculator = CALL_ERZ4_099_TRA_1.accept(PVBFOC, MULTICURVE_BLACK);
    assertEquals("BondFuturesOptionMarginTransactionBlackFlatMethod: present value", pvExpected1, pvCalculator.getAmount(EUR), TOLERANCE_PV);
    final MultipleCurrencyAmount pvComputed2 = METHOD_OPT_TRA.presentValue(CALL_ERZ4_099_TRA_2, MULTICURVE_BLACK);
    final double priceOpt2 = METHOD_OPT_SEC.price(CALL_ERZ4_099_TRA_2.getUnderlyingSecurity(), MULTICURVE_BLACK);
    final double pvExpected2 = (priceOpt2 - REFERENCE_PRICE) * ERZ4_DEFINITION.getNotional() * ERZ4_DEFINITION.getPaymentAccrualFactor() * QUANTITY;
    assertEquals("STIRFuturesOptionMarginTransactionBlackExpLogMoneynessMethod: present value", pvExpected2,
        pvComputed2.getAmount(ERZ4_DEFINITION.getCurrency()), TOLERANCE_PV);
  }

  @Test
  public void presentValuePuCallParity() {
    final MultipleCurrencyAmount priceFutures = METHOD_FUT_TRA.presentValue(ERZ4_TRA_STRIKE, MULTICURVE);
    final MultipleCurrencyAmount pvCallComputed = METHOD_OPT_TRA.presentValue(CALL_ERZ4_099_TRA_1, MULTICURVE_BLACK);
    final MultipleCurrencyAmount pvPutComputed = METHOD_OPT_TRA.presentValue(PUT_ERZ4_099_TRA_1, MULTICURVE_BLACK);
    assertEquals("STIRFuturesOptionMarginTransactionBlackExpLogMoneynessMethod: put call parity present value",
        pvCallComputed.getAmount(EURIBOR3M.getCurrency()) - pvPutComputed.getAmount(EURIBOR3M.getCurrency()),
        priceFutures.getAmount(EURIBOR3M.getCurrency()), TOLERANCE_PV);
  }

  @Test
  /**
   * Test using the flat smile. This is required for a finite difference comparison, as the model is the Black sensitivity and a full bump and re-price with 
   * volatility interpolation would change the volatility (and the risk).
   */
  public void presentValueCurveSensitivity() {
    final MultipleCurrencyParameterSensitivity pvpsComputed = PSSFC.calculateSensitivity(CALL_ERZ4_099_TRA_1, MULTICURVE_BLACK);
    final MultipleCurrencyParameterSensitivity pvpsFD = PSSFC_FD.calculateSensitivity(CALL_ERZ4_099_TRA_1, MULTICURVE_BLACK);
    AssertSensivityObjects.assertEquals("BondFuturesOptionMarginTransactionBlackFlatMethod: presentValueCurveSensitivity", pvpsFD, pvpsComputed, TOLERANCE_PV_DELTA);
  }

  @Test
  /**
   * Compare the computed Black sensitivity (vega) with a finite difference approximation.
   */
  public void presentValueBlackSensitivity() {
    final double shiftVega = 1.0E-6;
    final InterpolatedDoublesSurface shiftedSurfacePlus = StandardDataSetsBlack.blackSurfaceExpiryLogMoneyness(shiftVega);
    final InterpolatedDoublesSurface shiftedSurfaceMinus = StandardDataSetsBlack.blackSurfaceExpiryLogMoneyness(-shiftVega);
    final BlackSTIRFuturesExpLogMoneynessProvider blackShiftedPlus =
        new BlackSTIRFuturesExpLogMoneynessProvider(MULTICURVE, shiftedSurfacePlus, EURIBOR3M);
    final BlackSTIRFuturesExpLogMoneynessProvider blackShiftedMinus =
        new BlackSTIRFuturesExpLogMoneynessProvider(MULTICURVE, shiftedSurfaceMinus, EURIBOR3M);
    final MultipleCurrencyAmount pvP = METHOD_OPT_TRA.presentValue(CALL_ERZ4_099_TRA_1, blackShiftedPlus);
    final MultipleCurrencyAmount pvM = METHOD_OPT_TRA.presentValue(CALL_ERZ4_099_TRA_1, blackShiftedMinus);
    final double vegaExpected = (pvP.getAmount(CALL_ERZ4_099_TRA_1.getCurrency()) - pvM.getAmount(CALL_ERZ4_099_TRA_1.getCurrency())) / (2 * shiftVega);
    final double vegaComputed = METHOD_OPT_TRA.presentValueBlackSensitivity(CALL_ERZ4_099_TRA_1, MULTICURVE_BLACK).getSensitivity().toSingleValue();
    assertEquals("STIRFuturesOptionMarginTransactionBlackExpLogMoneynessMethod: present value Black sensitivity", vegaExpected, vegaComputed, TOLERANCE_PV_DELTA);
  }

}
