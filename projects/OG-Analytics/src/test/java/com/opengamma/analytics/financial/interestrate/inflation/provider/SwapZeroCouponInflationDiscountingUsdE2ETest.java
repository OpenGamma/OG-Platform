/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.inflation.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.util.LinkedHashMap;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedInflationMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedInflationZeroCoupon;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedInflationZeroCouponDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsInflationUSD;
import com.opengamma.analytics.financial.interestrate.datasets.StandardTimeSeriesInflationDataSets;
import com.opengamma.analytics.financial.provider.calculator.inflation.MarketQuoteInflationSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueCurveSensitivityDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.ParameterSensitivityInflationParameterCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * End-to-end tests for inflation curve calibration and pricing of inflation zero-coupon swaps.
 */
public class SwapZeroCouponInflationDiscountingUsdE2ETest {

  private static final ZonedDateTime CALIBRATION_DATE = DateUtils.getUTCDate(2014, 10, 9);
  private static final Currency USD = Currency.USD;

  /** Calculators **/
  private static final PresentValueDiscountingInflationCalculator PVDIC = 
      PresentValueDiscountingInflationCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingInflationCalculator PVCSDIC = 
      PresentValueCurveSensitivityDiscountingInflationCalculator.getInstance();
  private static final ParameterSensitivityInflationParameterCalculator<ParameterInflationProviderInterface> PSC = 
      new ParameterSensitivityInflationParameterCalculator<>(PVCSDIC);
  private static final MarketQuoteInflationSensitivityBlockCalculator<ParameterInflationProviderInterface> MQSBC = 
      new MarketQuoteInflationSensitivityBlockCalculator<>(PSC);
  
  /** Curves */
  private static final Pair<InflationProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_INFL_1_PAIR = 
      StandardDataSetsInflationUSD.getCurvesUsdOisUsCpi(CALIBRATION_DATE);
  private static final InflationProviderDiscount MULTICURVE_INFL_1 = MULTICURVE_INFL_1_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_INFL_1 = MULTICURVE_INFL_1_PAIR.getSecond();
  private static final Pair<InflationProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_INFL_2_PAIR = 
      StandardDataSetsInflationUSD.getCurvesUsdOisUsCpi2(CALIBRATION_DATE);
  private static final InflationProviderDiscount MULTICURVE_INFL_2 = MULTICURVE_INFL_2_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_INFL_2 = MULTICURVE_INFL_2_PAIR.getSecond();
  private static final Pair<InflationProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_INFL_3_PAIR = 
      StandardDataSetsInflationUSD.getCurvesUsdOisUsCpi3(CALIBRATION_DATE);
  private static final InflationProviderDiscount MULTICURVE_INFL_3 = MULTICURVE_INFL_3_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_INFL_3 = MULTICURVE_INFL_3_PAIR.getSecond();
  private static final Pair<InflationProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_INFL_4_PAIR = 
      StandardDataSetsInflationUSD.getCurvesUsdOisUsCpiSeasonality(CALIBRATION_DATE);
  private static final InflationProviderDiscount MULTICURVE_INFL_4 = MULTICURVE_INFL_4_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_INFL_4 = MULTICURVE_INFL_4_PAIR.getSecond();
  private static final Pair<InflationProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_INFL_5_PAIR = 
      StandardDataSetsInflationUSD.getCurvesUsdOisUsCpiCurrent(CALIBRATION_DATE);
  private static final InflationProviderDiscount MULTICURVE_INFL_5 = MULTICURVE_INFL_5_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_INFL_5 = MULTICURVE_INFL_5_PAIR.getSecond();
  private static final ZonedDateTimeDoubleTimeSeries HTS_CPI = 
      StandardTimeSeriesInflationDataSets.timeSeriesUsCpi(CALIBRATION_DATE);

  private static final GeneratorSwapFixedInflationZeroCoupon GENERATOR_ZCINFLATION_US = 
      GeneratorSwapFixedInflationMaster.getInstance().getGenerator("USCPI");
  private static final IndexPrice US_CPI = GENERATOR_ZCINFLATION_US.getIndexPrice();
  private static final double NOTIONAL = 10_000_000;
  
  /** Zero-coupon Inflation US (linear interpolation of Price Index). 2Y node. */
  private static final ZonedDateTime ACCRUAL_START_DATE_1 = DateUtils.getUTCDate(2014, 10, 9);
  private static final GeneratorAttributeIR ZCI_1_ATTR = new GeneratorAttributeIR(Period.ofYears(2));
  private static final double RATE_FIXED_1 = 0.0200;
  private static final SwapFixedInflationZeroCouponDefinition ZCI_1_DEFINITION = 
      GENERATOR_ZCINFLATION_US.generateInstrument(ACCRUAL_START_DATE_1, RATE_FIXED_1, NOTIONAL, ZCI_1_ATTR);
  private static final InstrumentDerivative ZCI_1 = ZCI_1_DEFINITION.toDerivative(ACCRUAL_START_DATE_1, 
      new ZonedDateTimeDoubleTimeSeries[] {HTS_CPI, HTS_CPI});
  
  /** Zero-coupon Inflation US (linear interpolation of Price Index). 5Y aged. */
  private static final ZonedDateTime ACCRUAL_START_DATE_2 = DateUtils.getUTCDate(2014, 1, 8);
  private static final GeneratorAttributeIR ZCI_2_ATTR = new GeneratorAttributeIR(Period.ofYears(5));
  private static final double RATE_FIXED_2 = 0.0100;
  private static final SwapFixedInflationZeroCouponDefinition ZCI_2_DEFINITION = 
      GENERATOR_ZCINFLATION_US.generateInstrument(ACCRUAL_START_DATE_2, RATE_FIXED_2, NOTIONAL, ZCI_2_ATTR);
  private static final InstrumentDerivative ZCI_2 = ZCI_2_DEFINITION.toDerivative(ACCRUAL_START_DATE_2, 
      new ZonedDateTimeDoubleTimeSeries[] {HTS_CPI, HTS_CPI}); // TODO: empty hts
  
  private static final double TOLERANCE_PV = 1.0E-3;
  private static final double TOLERANCE_PV_DELTA = 1.0E-1;
  private static final double BP1 = 1.0E-4;
  
  @Test
  public void presentValueNode() {
    double pvExpectd = 0.0000;
    MultipleCurrencyAmount pv1 = ZCI_1.accept(PVDIC, MULTICURVE_INFL_1);
    assertEquals("SwapZeroCouponInflationUsdDiscountingE2ETest: present value", 
        pvExpectd, pv1.getAmount(USD), TOLERANCE_PV);
    MultipleCurrencyAmount pv2 = ZCI_1.accept(PVDIC, MULTICURVE_INFL_2);
    assertEquals("SwapZeroCouponInflationUsdDiscountingE2ETest: present value", 
        pv1.getAmount(USD), pv2.getAmount(USD), TOLERANCE_PV);
    MultipleCurrencyAmount pv3 = ZCI_1.accept(PVDIC, MULTICURVE_INFL_3);
    assertEquals("SwapZeroCouponInflationUsdDiscountingE2ETest: present value", 
        pv1.getAmount(USD), pv3.getAmount(USD), TOLERANCE_PV);
  }
  
  @Test
  public void presentValueAged() {
    double pvExpectd = 697518.0714;
    MultipleCurrencyAmount pv1 = ZCI_2.accept(PVDIC, MULTICURVE_INFL_1);
    assertEquals("SwapZeroCouponInflationUsdDiscountingE2ETest: present value", 
        pvExpectd, pv1.getAmount(USD), TOLERANCE_PV);
    MultipleCurrencyAmount pv2 = ZCI_2.accept(PVDIC, MULTICURVE_INFL_2);
    assertEquals("SwapZeroCouponInflationUsdDiscountingE2ETest: present value", 
        pv1.getAmount(USD), pv2.getAmount(USD), TOLERANCE_PV);
    MultipleCurrencyAmount pv3 = ZCI_2.accept(PVDIC, MULTICURVE_INFL_3);
    assertEquals("SwapZeroCouponInflationUsdDiscountingE2ETest: present value", 
        pv1.getAmount(USD), pv3.getAmount(USD), TOLERANCE_PV);
  }
  
  @Test
  public void presentValueAgedSeasonality() {
    double pvExpectd = 697821.1632;
    MultipleCurrencyAmount pv1 = ZCI_2.accept(PVDIC, MULTICURVE_INFL_4);
    assertEquals("SwapZeroCouponInflationUsdDiscountingE2ETest: present value", 
        pvExpectd, pv1.getAmount(USD), TOLERANCE_PV);
  }
  
  @Test
  public void presentValueAgedStart() {
    double pvExpectd = 697518.0690;
    MultipleCurrencyAmount pv1 = ZCI_2.accept(PVDIC, MULTICURVE_INFL_5);
    assertEquals("SwapZeroCouponInflationUsdDiscountingE2ETest: present value", 
        pvExpectd, pv1.getAmount(USD), TOLERANCE_PV);
  }
  
  @Test
  public void bucketedPv01Node() {
    final double[] deltaDsc = 
      {0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,
        0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000};
    final double[] deltaCpi = 
      {0.0000,2026.9265,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,
        0.0000,0.0000,0.0000,0.0000,0.0000};
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity = new LinkedHashMap<>();
    sensitivity.put(ObjectsPair.of(MULTICURVE_INFL_1.getName(USD), USD), new DoubleMatrix1D(deltaDsc));
    sensitivity.put(ObjectsPair.of(MULTICURVE_INFL_1.getName(US_CPI), USD), new DoubleMatrix1D(deltaCpi));
    final MultipleCurrencyParameterSensitivity pvpsExpected = new MultipleCurrencyParameterSensitivity(sensitivity);
    final MultipleCurrencyParameterSensitivity pvpsComputed1 = 
        MQSBC.fromInstrument(ZCI_1, MULTICURVE_INFL_1, BLOCK_INFL_1).multipliedBy(BP1);
    AssertSensitivityObjects.assertEquals("Zero-coupon Inflation swap: Bucketed PV01", 
        pvpsExpected, pvpsComputed1, TOLERANCE_PV_DELTA);
    final MultipleCurrencyParameterSensitivity pvpsComputed2 = 
        MQSBC.fromInstrument(ZCI_1, MULTICURVE_INFL_2, BLOCK_INFL_2).multipliedBy(BP1);
    AssertSensitivityObjects.assertEquals("Zero-coupon Inflation swap: Bucketed PV01", 
        pvpsComputed1, pvpsComputed2, TOLERANCE_PV_DELTA);
    final MultipleCurrencyParameterSensitivity pvpsComputed3 = 
        MQSBC.fromInstrument(ZCI_1, MULTICURVE_INFL_3, BLOCK_INFL_3).multipliedBy(BP1);
    AssertSensitivityObjects.assertEquals("Zero-coupon Inflation swap: Bucketed PV01", 
        pvpsComputed1, pvpsComputed3, TOLERANCE_PV_DELTA);
  }
  
  @Test
  public void bucketedPv01Aged() {
    final double[] deltaDsc = 
      {-0.1561,-0.6246,0.0000,0.0000,0.0000,0.0003,-0.0144,0.7254,1.4424,3.1991,
        -186.7859,-58.1989,0.0000,0.0000,0.0000,0.0000,0.0000};
    final double[] deltaCpi = 
      {-0.1751,6.2301,-156.0361,3475.9913,1061.5416,0.0000,0.0000,0.0000,0.0000,0.0000,
        0.0000,0.0000,0.0000,0.0000,0.0000};
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity = new LinkedHashMap<>();
    sensitivity.put(ObjectsPair.of(MULTICURVE_INFL_1.getName(USD), USD), new DoubleMatrix1D(deltaDsc));
    sensitivity.put(ObjectsPair.of(MULTICURVE_INFL_1.getName(US_CPI), USD), new DoubleMatrix1D(deltaCpi));
    MultipleCurrencyParameterSensitivity pvpsExpected = new MultipleCurrencyParameterSensitivity(sensitivity);
    MultipleCurrencyParameterSensitivity pvpsComputed1 = 
        MQSBC.fromInstrument(ZCI_2, MULTICURVE_INFL_1, BLOCK_INFL_1).multipliedBy(BP1);
    AssertSensitivityObjects.assertEquals("Zero-coupon Inflation swap: Bucketed PV01", 
        pvpsExpected, pvpsComputed1, TOLERANCE_PV_DELTA);
    final MultipleCurrencyParameterSensitivity pvpsComputed2 = 
        MQSBC.fromInstrument(ZCI_2, MULTICURVE_INFL_2, BLOCK_INFL_2).multipliedBy(BP1);
    AssertSensitivityObjects.assertEquals("Zero-coupon Inflation swap: Bucketed PV01", 
        pvpsComputed1, pvpsComputed2, TOLERANCE_PV_DELTA);
    final MultipleCurrencyParameterSensitivity pvpsComputed3 = 
        MQSBC.fromInstrument(ZCI_2, MULTICURVE_INFL_3, BLOCK_INFL_3).multipliedBy(BP1);
    AssertSensitivityObjects.assertEquals("Zero-coupon Inflation swap: Bucketed PV01", 
        pvpsComputed1, pvpsComputed3, TOLERANCE_PV_DELTA);
  }
  
}
