/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.util.LinkedHashMap;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.datasets.CalendarTarget;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsMulticurveEUR;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.provider.calculator.blackstirfutures.PresentValueBlackSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.blackstirfutures.PresentValueCurveSensitivityBlackSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.StandardDataSetsBlack;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesExpLogMoneynessProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class STIRFuturesOptionMarginTransactionBlackExpLogMoneynessMethodE2ETest {

  /** Data */
  private static final IborIndex[] INDEX_IBOR_LIST = StandardDataSetsMulticurveEUR.indexIborArrayEUROisE3();
  private static final IborIndex EUREURIBOR3M = INDEX_IBOR_LIST[0];
  private static final Calendar CALENDAR = StandardDataSetsMulticurveEUR.calendarArray()[0];
  private static final Currency EUR = EUREURIBOR3M.getCurrency();
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2014, 2, 18);
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_PAIR = StandardDataSetsMulticurveEUR.getCurvesUSDOisL3();
  private static final MulticurveProviderDiscount MULTICURVE = MULTICURVE_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK = MULTICURVE_PAIR.getSecond();
  final private static InterpolatedDoublesSurface BLACK_SURFACE_LOGMONEY = StandardDataSetsBlack.blackSurfaceExpiryLogMoneyness();
  final private static BlackSTIRFuturesExpLogMoneynessProviderDiscount MULTICURVE_BLACK =
      new BlackSTIRFuturesExpLogMoneynessProviderDiscount(MULTICURVE, BLACK_SURFACE_LOGMONEY, EUREURIBOR3M);

  /** Option on STIR futures */
  private static final ZonedDateTime LAST_TRADE_DATE = DateUtils.getUTCDate(2014, 12, 15);
  private static final double NOTIONAL = 1000000.0; // 1m
  private static final double FUTURE_FACTOR = 0.25;
  private static final String NAME = "ERZ4";
  private static final Calendar TARGET = new CalendarTarget("TARGET");
  private static final InterestRateFutureSecurityDefinition ERZ4_DEFINITION =
      new InterestRateFutureSecurityDefinition(LAST_TRADE_DATE, EUREURIBOR3M, NOTIONAL, FUTURE_FACTOR, NAME, TARGET);
  private static final ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2014, 11, 17);
  private static final double STRIKE_099 = 0.99;
  private static final boolean IS_CALL = true;
  private static final InterestRateFutureOptionMarginSecurityDefinition CALL_ERZ4_099_SEC_DEFINITION =
      new InterestRateFutureOptionMarginSecurityDefinition(ERZ4_DEFINITION, EXPIRY_DATE, STRIKE_099, IS_CALL);
  private static final InterestRateFutureOptionMarginSecurityDefinition PUT_ERZ4_099_SEC_DEFINITION =
      new InterestRateFutureOptionMarginSecurityDefinition(ERZ4_DEFINITION, EXPIRY_DATE, STRIKE_099, !IS_CALL);

  private static final int QUANTITY = 123;
  private static final ZonedDateTime TRADE_DATE_1 = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, -1, CALENDAR);
  private static final ZonedDateTime TRADE_DATE_2 = REFERENCE_DATE;
  private static final double TRADE_PRICE = 0.01;

  private static final InterestRateFutureOptionMarginTransactionDefinition CALL_ERZ4_099_TRA_1_DEFINITION =
      new InterestRateFutureOptionMarginTransactionDefinition(CALL_ERZ4_099_SEC_DEFINITION, QUANTITY, TRADE_DATE_1, TRADE_PRICE);
  private static final InterestRateFutureOptionMarginTransactionDefinition CALL_ERZ4_099_TRA_2_DEFINITION =
      new InterestRateFutureOptionMarginTransactionDefinition(CALL_ERZ4_099_SEC_DEFINITION, QUANTITY, TRADE_DATE_2, TRADE_PRICE);
  private static final InterestRateFutureOptionMarginTransactionDefinition PUT_ERZ4_099_TRA_1_DEFINITION =
      new InterestRateFutureOptionMarginTransactionDefinition(PUT_ERZ4_099_SEC_DEFINITION, QUANTITY, TRADE_DATE_2, TRADE_PRICE);
  private static final double REFERENCE_PRICE = 0.02;
  private static final InterestRateFutureOptionMarginTransaction CALL_ERZ4_099_TRA_1 = CALL_ERZ4_099_TRA_1_DEFINITION.toDerivative(REFERENCE_DATE, REFERENCE_PRICE);
  private static final InterestRateFutureOptionMarginTransaction CALL_ERZ4_099_TRA_2 = CALL_ERZ4_099_TRA_2_DEFINITION.toDerivative(REFERENCE_DATE, REFERENCE_PRICE);
  private static final InterestRateFutureOptionMarginTransaction PUT_ERZ4_099_TRA_1 = PUT_ERZ4_099_TRA_1_DEFINITION.toDerivative(REFERENCE_DATE, REFERENCE_PRICE);

  /** Methods and calculators */
  private static final PresentValueBlackSTIRFutureOptionCalculator PVBFOC = PresentValueBlackSTIRFutureOptionCalculator.getInstance();
  private static final PresentValueCurveSensitivityBlackSTIRFutureOptionCalculator PVCSBFOC =
      PresentValueCurveSensitivityBlackSTIRFutureOptionCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<BlackSTIRFuturesProviderInterface> PSSFC = new ParameterSensitivityParameterCalculator<>(PVCSBFOC);
  private static final MarketQuoteSensitivityBlockCalculator<BlackSTIRFuturesProviderInterface> MQSBC = new MarketQuoteSensitivityBlockCalculator<>(PSSFC);

  /** Tolerances */
  private static final double TOLERANCE_PV = 1.0E-4;
  private static final double TOLERANCE_PV_DELTA = 1.0E-2;
  private static final double BP1 = 1.0E-4;

  public void presentValue() {
    final MultipleCurrencyAmount pvComputed1 = CALL_ERZ4_099_TRA_1.accept(PVBFOC, MULTICURVE_BLACK);
    double pvExpected1 = -390258.6663437139;
    assertEquals("STIRFuturesOptionMarginTransactionBlackExpLogMoneynessMethodE2ETest",
        pvComputed1.getAmount(EUR), pvExpected1, TOLERANCE_PV);
    final MultipleCurrencyAmount pvComputed2 = CALL_ERZ4_099_TRA_2.accept(PVBFOC, MULTICURVE_BLACK);
    double pvExpected2 = -82758.66634371383;
    assertEquals("STIRFuturesOptionMarginTransactionBlackExpLogMoneynessMethodE2ETest",
        pvComputed2.getAmount(EUR), pvExpected2, TOLERANCE_PV);
    final MultipleCurrencyAmount pvComputed3 = PUT_ERZ4_099_TRA_1.accept(PVBFOC, MULTICURVE_BLACK);
    double pvExpected3 = -307492.22924060293;
    assertEquals("STIRFuturesOptionMarginTransactionBlackExpLogMoneynessMethodE2ETest",
        pvComputed3.getAmount(EUR), pvExpected3, TOLERANCE_PV);
  }

  @Test
  /**
   * Tests bucketed PV01 with a standard set of data against hard-coded values.
   */
  public void BucketedPV01() {
    final double[] deltaDsc = {-0.0003, -0.0003, 0.0000, 0.0000, -1.7044, -3.0201, -4.5627, 18.5730, 0.9670, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000 };
    final double[] deltaFwd = {2358.4356, 2438.3306, 2437.7080, -9265.1071, -897.3742, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000 };
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity = new LinkedHashMap<>();
    sensitivity.put(ObjectsPair.of(MULTICURVE.getName(EUR), EUR), new DoubleMatrix1D(deltaDsc));
    sensitivity.put(ObjectsPair.of(MULTICURVE.getName(EUREURIBOR3M), EUR), new DoubleMatrix1D(deltaFwd));
    final MultipleCurrencyParameterSensitivity pvpsExpected = new MultipleCurrencyParameterSensitivity(sensitivity);
    final MultipleCurrencyParameterSensitivity pvpsComputed = MQSBC.fromInstrument(CALL_ERZ4_099_TRA_1, MULTICURVE_BLACK, BLOCK).multipliedBy(BP1);
    AssertSensitivityObjects.assertEquals("STIRFuturesOptionMarginTransactionBlackExpLogMoneynessMethodE2ETest: bucketed delts from standard curves", pvpsExpected, pvpsComputed, TOLERANCE_PV_DELTA);
  }

}
