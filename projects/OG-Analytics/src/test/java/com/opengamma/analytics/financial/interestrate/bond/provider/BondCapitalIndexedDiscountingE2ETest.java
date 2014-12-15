/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.util.LinkedHashMap;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondCapitalIndexedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondDataSetsUsd;
import com.opengamma.analytics.financial.instrument.bond.BondFixedSecurityDefinition;
import com.opengamma.analytics.financial.instrument.bond.BondFixedTransactionDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedInflationMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedInflationZeroCoupon;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationGearingDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedInflationZeroCouponDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsGovtUsInflationUSD;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsInflationUSD;
import com.opengamma.analytics.financial.interestrate.datasets.StandardTimeSeriesInflationDataSets;
import com.opengamma.analytics.financial.provider.calculator.inflation.MarketQuoteInflationSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflation.PresentValueDiscountingInflationCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflationissuer.PresentValueCurveSensitivityInflationIssuerCalculator;
import com.opengamma.analytics.financial.provider.calculator.inflationissuer.PresentValueInflationIssuerDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueIssuerCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.ParameterSensitivityInflationParameterCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ParameterSensitivityMulticurveMatrixCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * End-to-end tests for (inflation) capital indexed bonds.
 */
@Test(groups = TestGroup.UNIT)
public class BondCapitalIndexedDiscountingE2ETest {

  private static final ZonedDateTime CALIBRATION_DATE = DateUtils.getUTCDate(2014, 12, 15);
  private static final Currency USD = Currency.USD;
  
  private static final GeneratorSwapFixedInflationZeroCoupon GENERATOR_ZCINFLATION_US = 
      GeneratorSwapFixedInflationMaster.getInstance().getGenerator("USCPI");
  private static final IndexPrice US_CPI = GENERATOR_ZCINFLATION_US.getIndexPrice();
  private static final ZonedDateTimeDoubleTimeSeries HTS_CPI = 
      StandardTimeSeriesInflationDataSets.timeSeriesUsCpi(CALIBRATION_DATE);
  private static final ZonedDateTimeDoubleTimeSeries  HTS_EMPTY = 
      ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(ZoneOffset.UTC);
  
  /** Calculators **/
  private static final BondCapitalIndexedSecurityDiscountingMethod METHOD_CAPIND_BOND_SEC =
      BondCapitalIndexedSecurityDiscountingMethod.getInstance();  
  private static final PresentValueIssuerCalculator PVIssuerC = 
      PresentValueIssuerCalculator.getInstance();
  private static final PresentValueDiscountingInflationCalculator PVInflC = 
      PresentValueDiscountingInflationCalculator.getInstance();
  private static final PresentValueInflationIssuerDiscountingCalculator PVInflIssuerC =
      PresentValueInflationIssuerDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityInflationIssuerCalculator PVCSInflIssuerC =
      PresentValueCurveSensitivityInflationIssuerCalculator.getInstance();

  private static final ParameterSensitivityInflationParameterCalculator<ParameterInflationIssuerProviderInterface> PSIC = 
      new ParameterSensitivityInflationParameterCalculator<>(PVCSInflIssuerC);
  private static final MarketQuoteInflationSensitivityBlockCalculator<ParameterInflationIssuerProviderInterface> MQISBC =
      new MarketQuoteInflationSensitivityBlockCalculator<>(PSIC);


  
  /** Bond fixed coupon 2024 */
  private static final BondFixedSecurityDefinition UST_SEC_DEFINITION = BondDataSetsUsd.bondUST_20241115(1.0);
  private static final double QUANTITY = 10000000; // 10m
  private static final ZonedDateTime SETTLE_DATE_FIXED = DateUtils.getUTCDate(2014, 12, 16);
  private static final double TRADE_PRICE_FIXED = 1.01+11.0/32.0/100.0;
  private static final BondFixedTransactionDefinition UST_TRA_DEFINITION = 
      new BondFixedTransactionDefinition(UST_SEC_DEFINITION, QUANTITY, SETTLE_DATE_FIXED, TRADE_PRICE_FIXED);
  private static final BondFixedTransaction UST_TRA = 
      UST_TRA_DEFINITION.toDerivative(CALIBRATION_DATE);


  private static final BondFixedSecurityDefinition UST_SEC_DEFINITION1 = BondDataSetsUsd.bondUST_20240815(1.0);
  private static final double QUANTITY1 = 10000000; // 10m
  private static final ZonedDateTime SETTLE_DATE_FIXED1 = DateUtils.getUTCDate(2014, 12, 16);
  private static final double TRADE_PRICE_FIXED1 = 1.01;
  private static final BondFixedTransactionDefinition UST_TRA_DEFINITION1 =
      new BondFixedTransactionDefinition(UST_SEC_DEFINITION1, QUANTITY1, SETTLE_DATE_FIXED1, TRADE_PRICE_FIXED1);
  private static final BondFixedTransaction UST_TRA1 =
      UST_TRA_DEFINITION1.toDerivative(CALIBRATION_DATE);
  
  /** Zero-coupon Inflation US (linear interpolation of Price Index). 5Y aged. */
  private static final double NOTIONAL = 10_000_000;
  private static final ZonedDateTime ACCRUAL_START_DATE_2 = DateUtils.getUTCDate(2014, 1, 8);
  private static final GeneratorAttributeIR ZCI_2_ATTR = new GeneratorAttributeIR(Period.ofYears(5));
  private static final double RATE_FIXED_2 = 0.0100;
  private static final SwapFixedInflationZeroCouponDefinition ZCI_2_DEFINITION = 
      GENERATOR_ZCINFLATION_US.generateInstrument(ACCRUAL_START_DATE_2, RATE_FIXED_2, NOTIONAL, ZCI_2_ATTR);
  private static final InstrumentDerivative ZCI_2 = ZCI_2_DEFINITION.toDerivative(ACCRUAL_START_DATE_2, 
      new ZonedDateTimeDoubleTimeSeries[] {HTS_EMPTY, HTS_CPI});
  
  /** Bond Inflation (TIPS) 2024 */
  private static final BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponInterpolationGearingDefinition> 
    TIPS_24_SEC_DEFINITION = BondDataSetsUsd.bondTIPS_20240715(1.0);
  private static final double QUANTITY_TIPS_1 = 10000000; // 10m
  private static final ZonedDateTime SETTLE_DATE_TIPS_1 = DateUtils.getUTCDate(2014, 12, 16);
  private static final double TRADE_PRICE_TIPS_1 = 0.98 + 23.5d / 100.0d / 32.0d;
  private static final BondCapitalIndexedTransactionDefinition<CouponInflationZeroCouponInterpolationGearingDefinition> 
    TIPS_24_TRA_DEFINITION = new BondCapitalIndexedTransactionDefinition<>(TIPS_24_SEC_DEFINITION, QUANTITY_TIPS_1,
        SETTLE_DATE_TIPS_1, TRADE_PRICE_TIPS_1);
  private static final BondCapitalIndexedTransaction<?> TIPS_24_1_TRA =
      TIPS_24_TRA_DEFINITION.toDerivative(CALIBRATION_DATE, HTS_CPI);

  private static final BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponInterpolationGearingDefinition>
      TIPS_43_SEC_DEFINITION = BondDataSetsUsd.bondTIPS_20160115(1.0);
//  private static final double QUANTITY_TIPS_1 = 10000000; // 10m
//  private static final ZonedDateTime SETTLE_DATE_TIPS_1 = DateUtils.getUTCDate(2014, 12, 15);
  private static final double TRADE_PRICE_TIPS_43 = 1.1211;
  private static final BondCapitalIndexedTransactionDefinition<CouponInflationZeroCouponInterpolationGearingDefinition>
      TIPS_43_TRA_DEFINITION = new BondCapitalIndexedTransactionDefinition<>(TIPS_43_SEC_DEFINITION, QUANTITY_TIPS_1,
                                                                             SETTLE_DATE_TIPS_1, TRADE_PRICE_TIPS_43);
  private static final BondCapitalIndexedTransaction<?> TIPS_43_1_TRA =
      TIPS_43_TRA_DEFINITION.toDerivative(CALIBRATION_DATE, HTS_CPI);
  
  /** Curves **/
  private static final Pair<IssuerProviderDiscount, CurveBuildingBlockBundle> ISSUER_GOVT_PAIR = 
      StandardDataSetsGovtUsInflationUSD.getCurvesUsdOisUsGovt(CALIBRATION_DATE);
  private static final IssuerProviderDiscount ISSUER_GOVT = ISSUER_GOVT_PAIR.getFirst();
//  private static final CurveBuildingBlockBundle BLOCK_GOVT = ISSUER_GOVT_PAIR.getSecond();
  private static final Pair<InflationProviderDiscount, CurveBuildingBlockBundle> INFL_PAIR =
      StandardDataSetsInflationUSD.getCurvesUsdOisUsCpi(CALIBRATION_DATE);
  private static final InflationProviderDiscount INFL = INFL_PAIR.getFirst();
//  private static final CurveBuildingBlockBundle INFL_BLOCK = INFL_PAIR.getSecond();
  private static final Pair<InflationIssuerProviderDiscount, CurveBuildingBlockBundle> INFL_ISSUER_GOVT_PAIR =
      StandardDataSetsGovtUsInflationUSD.getCurvesUsdOisUsGovtUsCpi(CALIBRATION_DATE);
  private static final InflationIssuerProviderDiscount INFL_ISSUER_GOVT_1 = INFL_ISSUER_GOVT_PAIR.getFirst();
  private static final CurveBuildingBlockBundle INFL_ISSUER_GOVT_BLOCK_1 = INFL_ISSUER_GOVT_PAIR.getSecond();
  private static final Pair<InflationIssuerProviderDiscount, CurveBuildingBlockBundle> INFL_ISSUER_GOVT_2_PAIR =
      StandardDataSetsGovtUsInflationUSD.getCurvesUsdOisUsGovtUsCpiCurrentSeasonality(CALIBRATION_DATE);
  private static final InflationIssuerProviderDiscount INFL_ISSUER_GOVT_2 = INFL_ISSUER_GOVT_2_PAIR.getFirst();
  private static final CurveBuildingBlockBundle INFL_ISSUER_GOVT_2_BLOCK = INFL_ISSUER_GOVT_2_PAIR.getSecond();
  private static final Pair<InflationIssuerProviderDiscount, CurveBuildingBlockBundle> INFL_ISSUER_GOVT_3_PAIR =
      StandardDataSetsGovtUsInflationUSD.getCurvesUsdOisUsGovtUsTips(CALIBRATION_DATE);
  private static final InflationIssuerProviderDiscount INFL_ISSUER_GOVT_3 = INFL_ISSUER_GOVT_3_PAIR.getFirst();
  private static final CurveBuildingBlockBundle INFL_ISSUER_GOVT_3_BLOCK = INFL_ISSUER_GOVT_3_PAIR.getSecond();
  
  private static final double TOLERANCE_PV = 1.0E-3;
  private static final double TOLERANCE_PV_DELTA = 1.0E-1;
  private static final double TOLERANCE_PRICE = 1.0E-8;
  private static final double BP1 = 1.0E-4;
  
  @Test
  public void presentValueBondFixed() {
    double pvExpected = 0;
    MultipleCurrencyAmount pvComputedIs = UST_TRA.accept(PVIssuerC, ISSUER_GOVT);
    MultipleCurrencyAmount pvComputedIs1 = UST_TRA1.accept(PVIssuerC, ISSUER_GOVT);
//    assertEquals("BondCapitalIndexedDiscountingE2E: present value bond fixed",
//        pvExpected, pvComputedIs.getAmount(USD), TOLERANCE_PV);
//    MultipleCurrencyAmount pvComputedInIs = UST_TRA.accept(PVInflIssuerC, INFL_ISSUER_GOVT_1);
//    assertEquals("BondCapitalIndexedDiscountingE2E: present value bond fixed",
//        pvExpected, pvComputedInIs.getAmount(USD), TOLERANCE_PV);

//    MultipleCurrencyParameterSensitivity
      System.out.println("OTR PV Computed: " + pvComputedIs.getAmount(USD));
      System.out.println("OffTR PV Computed: " + pvComputedIs1.getAmount(USD));
  }
  
  @Test
  public void bucketeTipsToZCITIPSWithCurrent() {
    final double[] deltaDsc =
      {0.0831, 15.4536,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,
      0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000};
    final double[] deltaGovt =
      {-0.7353, -4.8187, -784.6413, -416.7951,0.0000,0.0000};
    final double[] deltaCpi =
      {881.5432, 0.0000,0.0000};
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity = new LinkedHashMap<>();
    sensitivity.put(ObjectsPair.of(INFL_ISSUER_GOVT_2.getName(USD), USD), new DoubleMatrix1D(deltaDsc));
    sensitivity.put(ObjectsPair.of(INFL_ISSUER_GOVT_2.getIssuerProvider().getName(TIPS_24_SEC_DEFINITION.getIssuerEntity()), USD),
        new DoubleMatrix1D(deltaGovt));
    sensitivity.put(ObjectsPair.of(INFL_ISSUER_GOVT_2.getName(US_CPI), USD), new DoubleMatrix1D(deltaCpi));
    MultipleCurrencyParameterSensitivity pvpsExpected = new MultipleCurrencyParameterSensitivity(sensitivity);
    MultipleCurrencyParameterSensitivity pvpsComputed =
        MQISBC.fromInstrument(TIPS_24_1_TRA, INFL_ISSUER_GOVT_2, INFL_ISSUER_GOVT_2_BLOCK).multipliedBy(BP1);


    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity1 = new LinkedHashMap<>();
    sensitivity.put(ObjectsPair.of(INFL_ISSUER_GOVT_3.getName(USD), USD), new DoubleMatrix1D(deltaDsc));
    sensitivity.put(ObjectsPair.of(INFL_ISSUER_GOVT_3.getIssuerProvider().getName(TIPS_24_SEC_DEFINITION.getIssuerEntity()), USD),
                    new DoubleMatrix1D(deltaGovt));
    sensitivity.put(ObjectsPair.of(INFL_ISSUER_GOVT_3.getName(US_CPI), USD), new DoubleMatrix1D(deltaCpi));
//    MultipleCurrencyParameterSensitivity pvpsExpected = new MultipleCurrencyParameterSensitivity(sensitivity1);
    MultipleCurrencyParameterSensitivity pvpsComputed1 =
        MQISBC.fromInstrument(TIPS_24_1_TRA, INFL_ISSUER_GOVT_3, INFL_ISSUER_GOVT_3_BLOCK).multipliedBy(BP1);



    System.out.println("Sensitivities Gov2: " + pvpsComputed);
    System.out.println("Sensitivities Gov3: " + pvpsComputed1);
//    AssertSensitivityObjects.assertEquals("BondCapitalIndexedDiscountingE2E",
//        pvpsExpected, pvpsComputed, TOLERANCE_PV_DELTA);
  }
//
//  @Test
//  public void cleanRealPriceTips() {
//    double cleanRealPriceExpected = 1.0258835457;
//    double cleanRealPrice = METHOD_CAPIND_BOND_SEC.cleanRealPriceFromCurves(TIPS_16_1_TRA.getBondStandard(), INFL_ISSUER_GOVT_2);
//    assertEquals("BondCapitalIndexedDiscountingE2E: clean real price TIPS",
//        cleanRealPriceExpected, cleanRealPrice, TOLERANCE_PRICE);
//  }
//
//  @Test
//  public void yieldRealTips() {
//    double yieldRealExpected = -0.012113884588;
//    double yieldReal = METHOD_CAPIND_BOND_SEC.yieldRealFromCurves(TIPS_16_1_TRA.getBondStandard(), INFL_ISSUER_GOVT_2);
//    assertEquals("BondCapitalIndexedDiscountingE2E: real yield TIPS", yieldRealExpected, yieldReal, TOLERANCE_PRICE);
//  }
//
//  @Test
//  public void consistencyPricePvTips() {
//    double cleanRealPrice = METHOD_CAPIND_BOND_SEC.cleanRealPriceFromCurves(TIPS_16_1_TRA.getBondStandard(),
//        INFL_ISSUER_GOVT_2);
//    ZonedDateTime settleDateStandard = ScheduleCalculator.getAdjustedDate(CALIBRATION_DATE,
//        TIPS_16_SEC_DEFINITION.getSettlementDays(), TIPS_16_SEC_DEFINITION.getCalendar());
//    BondCapitalIndexedTransactionDefinition<CouponInflationZeroCouponInterpolationGearingDefinition>
//        tipsAtmDefinition = new BondCapitalIndexedTransactionDefinition<>(TIPS_16_SEC_DEFINITION, QUANTITY_TIPS_1,
//            settleDateStandard, cleanRealPrice);
//    BondCapitalIndexedTransaction<?> tipsAtm = tipsAtmDefinition.toDerivative(CALIBRATION_DATE, HTS_CPI);
//    MultipleCurrencyAmount pv1 = tipsAtm.accept(PVInflIssuerC, INFL_ISSUER_GOVT_2);
//    assertEquals("BondCapitalIndexedDiscountingE2E: present value TIPS",
//        0.0, pv1.getAmount(USD), TOLERANCE_PV);
//  }
//
//  @Test
//  public void bucketeTipsToZCITreasuryWithSeasonalityCurrent() {
//    final double[] deltaDsc =
//        {-0.0123,15.3801,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,
//            0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000};
//    final double[] deltaGovt =
//        {-2.6934,-1.1980,-1056.9639,262.7236,0.0000,0.0000};
//    final double[] deltaCpi =
//        {994.7588,522.2729,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000};
//    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity = new LinkedHashMap<>();
//    sensitivity.put(ObjectsPair.of(INFL_ISSUER_GOVT_2.getName(USD), USD), new DoubleMatrix1D(deltaDsc));
//    sensitivity.put(ObjectsPair.of(INFL_ISSUER_GOVT_2.getIssuerProvider().getName(TIPS_16_SEC_DEFINITION.getIssuerEntity()), USD),
//                    new DoubleMatrix1D(deltaGovt));
//    sensitivity.put(ObjectsPair.of(INFL_ISSUER_GOVT_2.getName(US_CPI), USD), new DoubleMatrix1D(deltaCpi));
//    MultipleCurrencyParameterSensitivity pvpsExpected = new MultipleCurrencyParameterSensitivity(sensitivity);
//    MultipleCurrencyParameterSensitivity pvpsComputed =
//        MQISBC.fromInstrument(TIPS_16_1_TRA, INFL_ISSUER_GOVT_2, INFL_ISSUER_GOVT_2_BLOCK).multipliedBy(BP1);
//    @SuppressWarnings("unused")
//    MultipleCurrencyInflationSensitivity pvcsPoint = TIPS_16_1_TRA.accept(PVCSInflIssuerC, INFL_ISSUER_GOVT_2);
//    @SuppressWarnings("unused")
//    MultipleCurrencyParameterSensitivity pvcsParam = PSIC.calculateSensitivity(TIPS_16_1_TRA, INFL_ISSUER_GOVT_2);
//    AssertSensitivityObjects.assertEquals("BondCapitalIndexedDiscountingE2E",
//                                          pvpsExpected, pvpsComputed, TOLERANCE_PV_DELTA);
//  }
//
//  @Test(enabled = false)
//  public void performanceCalibration() {
//    long startTime, endTime;
//    final int nbTest = 100;
//
//    startTime = System.currentTimeMillis();
//    for (int looptest = 0; looptest < nbTest; looptest++) {
//      StandardDataSetsGovtUsInflationUSD.getCurvesUsdOisUsGovtUsCpiCurrentSeasonality(CALIBRATION_DATE);
//    }
//    endTime = System.currentTimeMillis();
//    System.out.println(nbTest + " curve calibration (OIS, GOVT, ZCCPI): " + (endTime - startTime) + " ms");
//    // Performance note: Curve construction 2 units: 22-Oct-2014: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 1800 ms for 100 sets.
//
//    startTime = System.currentTimeMillis();
//    for (int looptest = 0; looptest < nbTest; looptest++) {
//      StandardDataSetsGovtUsInflationUSD.getCurvesUsdOisUsGovtUsTips(CALIBRATION_DATE);
//    }
//    endTime = System.currentTimeMillis();
//    System.out.println(nbTest + " curve calibration (OIS, GOVT, TIPS): " + (endTime - startTime) + " ms");
//    // Performance note: Curve construction 2 units: 22-Oct-2014: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 800 ms for 100 sets.
//  }
//
//  @Test(enabled = false)
//  public void performancePvBucketedPv01() {
//
//    long startTime, endTime;
//    final int nbTest = 1000;
//
//    startTime = System.currentTimeMillis();
//    for (int looptest = 0; looptest < nbTest; looptest++) {
//      @SuppressWarnings("unused")
//      MultipleCurrencyAmount pvIn = ZCI_2.accept(PVInflC, INFL);
//    }
//    endTime = System.currentTimeMillis();
//    System.out.println(nbTest + " PV ZC Swap: " + (endTime - startTime) + " ms");
//    // Performance note: Present Value: 22-Oct-2014: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 5 ms for 1000 ZC swaps.
//
//    startTime = System.currentTimeMillis();
//    for (int looptest = 0; looptest < nbTest; looptest++) {
//      @SuppressWarnings("unused")
//      MultipleCurrencyAmount pv1 = TIPS_16_1_TRA.accept(PVInflIssuerC, INFL_ISSUER_GOVT_3);
//    }
//    endTime = System.currentTimeMillis();
//    System.out.println(nbTest + " PV TIPS: " + (endTime - startTime) + " ms");
//    // Performance note: Present Value: 22-Oct-2014: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 30 ms for 1000 TIPS.
//
//    startTime = System.currentTimeMillis();
//    for (int looptest = 0; looptest < nbTest; looptest++) {
//      @SuppressWarnings("unused")
//      MultipleCurrencyParameterSensitivity pvpsComputed =
//        MQISBC.fromInstrument(ZCI_2, INFL_ISSUER_GOVT_2, INFL_ISSUER_GOVT_2_BLOCK).multipliedBy(BP1);
//    }
//    endTime = System.currentTimeMillis();
//    System.out.println(nbTest + " bucketed PV01 ZC Swap: " + (endTime - startTime) + " ms");
//    // Performance note: BucketedPV01: 22-Oct-2014: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 165 ms for 1000 ZC swaps.
//
//    startTime = System.currentTimeMillis();
//    for (int looptest = 0; looptest < nbTest; looptest++) {
//      @SuppressWarnings("unused")
//      MultipleCurrencyParameterSensitivity pvpsComputed =
//        MQISBC.fromInstrument(TIPS_16_1_TRA, INFL_ISSUER_GOVT_2, INFL_ISSUER_GOVT_2_BLOCK).multipliedBy(BP1);
//    }
//    endTime = System.currentTimeMillis();
//    System.out.println(nbTest + " bucketed PV01 TIPS: " + (endTime - startTime) + " ms");
//    // Performance note: BucketedPV01: 22-Oct-2014: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 550 ms for 1000 TIPS.
//  }
//
//  @Test
//  public void presentValueSwap() {
//    double pvExpected = 697518.0714;
//    MultipleCurrencyAmount pvIn = ZCI_2.accept(PVInflC, INFL);
//    assertEquals("BondCapitalIndexedDiscountingE2E: present value ZC",
//                 pvExpected, pvIn.getAmount(USD), TOLERANCE_PV);
//    MultipleCurrencyAmount pvInIs = ZCI_2.accept(PVInflIssuerC, INFL_ISSUER_GOVT_1);
//    assertEquals("BondCapitalIndexedDiscountingE2E: present value ZC",
//                 pvExpected, pvInIs.getAmount(USD), TOLERANCE_PV);
//  }
//
//  @Test
//  public void presentValueTips() {
//    double pvExpected = 474588.4388;
//    MultipleCurrencyAmount pv1 = TIPS_16_1_TRA.accept(PVInflIssuerC, INFL_ISSUER_GOVT_1);
//    assertEquals("BondCapitalIndexedDiscountingE2E: present value TIPS",
//                 pvExpected, pv1.getAmount(USD), TOLERANCE_PV);
//  }
//
//  @Test
//  public void presentValueTipsSeasonalityCurrent() {
//    double pvExpected = 434008.6291;
//    MultipleCurrencyAmount pv1 = TIPS_16_1_TRA.accept(PVInflIssuerC, INFL_ISSUER_GOVT_2);
//    assertEquals("BondCapitalIndexedDiscountingE2E: present value TIPS",
//                 pvExpected, pv1.getAmount(USD), TOLERANCE_PV);
//  }
//
  @Test
  public void presentValueTipsFromTips() {
    double pvExpected = 937819.3845;
    MultipleCurrencyAmount pv1 = TIPS_24_1_TRA.accept(PVInflIssuerC, INFL_ISSUER_GOVT_3);
    MultipleCurrencyAmount pv43 = TIPS_43_1_TRA.accept(PVInflIssuerC, INFL_ISSUER_GOVT_3);
//    assertEquals("BondCapitalIndexedDiscountingE2E: present value TIPS",
//                 pvExpected, pv1.getAmount(USD), TOLERANCE_PV);
    System.out.println("TIPS from TIPS PV: " + pv1.getAmount(USD));
    System.out.println("TIPS from TIPS PV43: " + pv43.getAmount(USD));
  }
//
//  @Test
//  public void bucketePV01TipsToZCITreasury() {
//    final double[] deltaDsc =
//        {-0.0250,15.3801,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,
//            0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000};
//    final double[] deltaGovt =
//        {-2.7315,-1.1653,-1060.3815,263.5878,0.0000,0.0000};
//    final double[] deltaCpi =
//        {954.1522,524.2191,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000,0.0000};
//    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity = new LinkedHashMap<>();
//    sensitivity.put(ObjectsPair.of(INFL_ISSUER_GOVT_1.getName(USD), USD), new DoubleMatrix1D(deltaDsc));
//    sensitivity.put(ObjectsPair.of(INFL_ISSUER_GOVT_1.getIssuerProvider().getName(TIPS_16_SEC_DEFINITION.getIssuerEntity()), USD),
//                    new DoubleMatrix1D(deltaGovt));
//    sensitivity.put(ObjectsPair.of(INFL_ISSUER_GOVT_1.getName(US_CPI), USD), new DoubleMatrix1D(deltaCpi));
//    MultipleCurrencyParameterSensitivity pvpsExpected = new MultipleCurrencyParameterSensitivity(sensitivity);
//    MultipleCurrencyParameterSensitivity pvpsComputed =
//        MQISBC.fromInstrument(TIPS_16_1_TRA, INFL_ISSUER_GOVT_1, INFL_ISSUER_GOVT_BLOCK_1).multipliedBy(BP1);
//    AssertSensitivityObjects.assertEquals("BondCapitalIndexedDiscountingE2E",
//                                          pvpsExpected, pvpsComputed, TOLERANCE_PV_DELTA);
//  }

}
