/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.provider;

import static org.testng.AssertJUnit.assertEquals;

import java.util.LinkedHashMap;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.datasets.CalendarGBP;
import com.opengamma.analytics.financial.datasets.CalendarUSD;
import com.opengamma.analytics.financial.instrument.NotionalProvider;
import com.opengamma.analytics.financial.instrument.annuity.AdjustedDateParameters;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.FixedAnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.annuity.FloatingAnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.annuity.OffsetAdjustedDateParameters;
import com.opengamma.analytics.financial.instrument.annuity.OffsetType;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapCouponFixedCouponDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.interestrate.E2EUtils;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsMulticurveUSDGBP;
import com.opengamma.analytics.financial.interestrate.datasets.StandardTimeSeriesOnIborDataSets;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Tests the Swap discounting method with standard data for cross-currency instruments.
 * Demo test - worked-out example on how to use OG-Analytics library for compute standard measure to simple instruments. 
 * The data is hard-coded. It is also available in some integration unit test and in snapshots.
 */
@Test(groups = TestGroup.UNIT)
public class SwapCrossCurrencyUsdGbpE2ETest {  

  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2014, 1, 22);

  private static final Calendar LON = new CalendarGBP("LON");
  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final IndexIborMaster MASTER_IBOR = IndexIborMaster.getInstance();
  private static final GeneratorSwapFixedIborMaster GENERATOR_IRS_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_IRS_MASTER.getGenerator("USD6MLIBOR3M", NYC);
  private static final IborIndex USDLIBOR3M = MASTER_IBOR.getIndex(IndexIborMaster.USDLIBOR3M);
  private static final IborIndex GBPLIBOR3M = MASTER_IBOR.getIndex(IndexIborMaster.GBPLIBOR3M);
  private static final Currency USD = USDLIBOR3M.getCurrency();
  private static final Currency GBP = Currency.GBP;
  private static final AdjustedDateParameters ADJUSTED_DATE_LIBOR_NYC = 
      new AdjustedDateParameters(NYC, USD6MLIBOR3M.getBusinessDayConvention());
  private static final AdjustedDateParameters ADJUSTED_DATE_LIBOR_LONNYC = 
      new AdjustedDateParameters(NYC, USD6MLIBOR3M.getBusinessDayConvention()); // Calendar should be LON+NYC
  private static final OffsetAdjustedDateParameters OFFSET_ADJ_LIBOR_LON_2 =
      new OffsetAdjustedDateParameters(-2, OffsetType.BUSINESS, LON, USD6MLIBOR3M.getBusinessDayConvention());
  private static final OffsetAdjustedDateParameters OFFSET_ADJ_LIBOR_LON_0 =
      new OffsetAdjustedDateParameters(0, OffsetType.BUSINESS, LON, USD6MLIBOR3M.getBusinessDayConvention());

  /** Curve providers */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_FFCOL_PAIR = 
      StandardDataSetsMulticurveUSDGBP.getCurvesUsdDscL1L3L6GbpDscL3(VALUATION_DATE);
  private static final MulticurveProviderDiscount MULTICURVE_FFCOL = MULTICURVE_FFCOL_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_FFCOL = MULTICURVE_FFCOL_PAIR.getSecond();
  
  /** Calculators */
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQDC = 
      ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = 
      PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<ParameterProviderInterface> PSC = 
      new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final MarketQuoteSensitivityBlockCalculator<ParameterProviderInterface> MQSBC = 
      new MarketQuoteSensitivityBlockCalculator<>(PSC);
  
  private static final double TOLERANCE_RATE = 1.0E-8;
  private static final double TOLERANCE_PV_DELTA = 1.0E-4;
  private static final double BP1 = 1.0E-4;
  
  /** Fixing time series */
  private static final ZonedDateTimeDoubleTimeSeries HTS_GBPLIBOR3M_INCL =
      StandardTimeSeriesOnIborDataSets.timeSeriesGbpIbor3M2014Jan(VALUATION_DATE.plusDays(1));
  private static final ZonedDateTimeDoubleTimeSeries HTS_USDLIBOR3M_INCL =
      StandardTimeSeriesOnIborDataSets.timeSeriesUsdIbor3M2014Jan(VALUATION_DATE.plusDays(1));
  
  /** USD Fixed v USDLIBOR3M */
  private static final LocalDate EFFECTIVE_DATE_1 = LocalDate.of(2016, 7, 18);
  private static final LocalDate MATURITY_DATE_1 = LocalDate.of(2021, 7, 18);
  private static final double FIXED_RATE_1 = 0.02655;
  private static final boolean PAYER_1 = false;
  private static final double NOTIONAL_1 = 1000000; // 1m
  private static final SwapCouponFixedCouponDefinition IRS_USD_1_DEFINITION =
      irsUsd(EFFECTIVE_DATE_1, MATURITY_DATE_1, FIXED_RATE_1, PAYER_1, NOTIONAL_1);
  private static final SwapFixedCoupon<Coupon> IRS_USD_1 = 
      IRS_USD_1_DEFINITION.toDerivative(VALUATION_DATE);
  /** GBPLIBOR3M + spread v USDLIBOR3M */
  private static final LocalDate EFFECTIVE_DATE_2 = LocalDate.of(2014, 1, 24);
  private static final LocalDate MATURITY_DATE_2 = LocalDate.of(2021, 1, 24);
  private static final double SPREAD_2 = 0.0091;
  private static final boolean PAYER_2 = true;
  private static final double NOTIONAL_GBP_2 = 61_600_000; // GBP
  private static final double NOTIONAL_USD_2 = 100_000_000; // USD
  private static final SwapDefinition XCCY_GBP_USD_NOT_2_DEFINITION =
      xccyGbpL3SUsdL3(EFFECTIVE_DATE_2, MATURITY_DATE_2, SPREAD_2, PAYER_2, NOTIONAL_GBP_2, NOTIONAL_USD_2, true);
  private static final SwapDefinition XCCY_GBP_USD_NONOT_2_DEFINITION =
      xccyGbpL3SUsdL3(EFFECTIVE_DATE_2, MATURITY_DATE_2, SPREAD_2, PAYER_2, NOTIONAL_GBP_2, NOTIONAL_USD_2, false);
  private static final Swap<? extends Payment, ? extends Payment> XCCY_GBP_USD_NOT_2 = 
      XCCY_GBP_USD_NOT_2_DEFINITION.toDerivative(VALUATION_DATE);
  private static final Swap<? extends Payment, ? extends Payment> XCCY_GBP_USD_NONOT_2 = 
      XCCY_GBP_USD_NONOT_2_DEFINITION.toDerivative(VALUATION_DATE);
  /** GBPLIBOR3M v USDLIBOR3M + spread */
  private static final LocalDate EFFECTIVE_DATE_3 = LocalDate.of(2014, 1, 24);
  private static final LocalDate MATURITY_DATE_3 = LocalDate.of(2021, 1, 24);
  private static final double SPREAD_3 = 0.0091;
  private static final boolean PAYER_3 = true;
  private static final double NOTIONAL_GBP_3 = 61_600_000; // 100m GBP
  private static final double NOTIONAL_USD_3 = 100_000_000; // GBP
  private static final SwapDefinition XCCY_GBP_USD_NONOT_3_DEFINITION =
      xccyGbpL3UsdL3S(EFFECTIVE_DATE_3, MATURITY_DATE_3, SPREAD_3, PAYER_3, NOTIONAL_GBP_3, NOTIONAL_USD_3, false);
  private static final Swap<? extends Payment, ? extends Payment> XCCY_GBP_USD_NONOT_3 = 
      XCCY_GBP_USD_NONOT_3_DEFINITION.toDerivative(VALUATION_DATE, 
          new ZonedDateTimeDoubleTimeSeries[] {HTS_GBPLIBOR3M_INCL, HTS_USDLIBOR3M_INCL});
  private static final SwapDefinition XCCY_GBP_USD_NOT_3_DEFINITION =
      xccyGbpL3UsdL3S(EFFECTIVE_DATE_3, MATURITY_DATE_3, SPREAD_3, PAYER_3, NOTIONAL_GBP_3, NOTIONAL_USD_3, true);
  private static final Swap<? extends Payment, ? extends Payment> XCCY_GBP_USD_NOT_3 = 
      XCCY_GBP_USD_NOT_3_DEFINITION.toDerivative(VALUATION_DATE, 
          new ZonedDateTimeDoubleTimeSeries[] {HTS_GBPLIBOR3M_INCL, HTS_USDLIBOR3M_INCL});
  /** USD Fixed v GBPLIBOR3M */
  private static final LocalDate EFFECTIVE_DATE_4 = LocalDate.of(2014, 1, 24);
  private static final LocalDate MATURITY_DATE_4 = LocalDate.of(2021, 1, 24);
  private static final double FIXED_RATE_4 = 0.0300;
  private static final boolean PAYER_4 = true; // USD
  private static final double NOTIONAL_GBP_4 = 61_600_000; // 100m GBP
  private static final double NOTIONAL_USD_4 = 100_000_000; // GBP
  private static final SwapDefinition XCCY_GBP_USD_NONOT_4_DEFINITION =
      xccyUsdFGbpL3(EFFECTIVE_DATE_4, MATURITY_DATE_4, FIXED_RATE_4, PAYER_4, NOTIONAL_USD_4, NOTIONAL_GBP_4, false);
  private static final Swap<? extends Payment, ? extends Payment> XCCY_GBP_USD_NONOT_4 = 
      XCCY_GBP_USD_NONOT_4_DEFINITION.toDerivative(VALUATION_DATE);
  private static final SwapDefinition XCCY_GBP_USD_NOT_4_DEFINITION =
      xccyUsdFGbpL3(EFFECTIVE_DATE_4, MATURITY_DATE_4, FIXED_RATE_4, PAYER_4, NOTIONAL_USD_4, NOTIONAL_GBP_4, true);
  private static final Swap<? extends Payment, ? extends Payment> XCCY_GBP_USD_NOT_4 = 
      XCCY_GBP_USD_NOT_4_DEFINITION.toDerivative(VALUATION_DATE);

  @Test
  public void presentValueUsd() {
    E2EUtils.presentValueTest(IRS_USD_1, MULTICURVE_FFCOL, USD, -11662.2515, "IRS USD: present value");
  }
  
  @Test
  /** GBPLIBOR3M + spread v USDLIBOR3M */
  public void presentValueXccyGbpUsd2() {
    E2EUtils.presentValueTest(XCCY_GBP_USD_NONOT_2, MULTICURVE_FFCOL, USD, -5700420.7686, 
        "XCcy GBP L3 + S / USD L3: present value");
    E2EUtils.presentValueTest(XCCY_GBP_USD_NOT_2, MULTICURVE_FFCOL, USD, -6407557.7364, 
        "XCcy GBP L3 + S / USD L3: present value");
  }
  
  @Test
  /** GBPLIBOR3M v USDLIBOR3M + spread */
  public void presentValueXccyGbpUsd3() {
    E2EUtils.presentValueTest(XCCY_GBP_USD_NONOT_3, MULTICURVE_FFCOL, USD, 6616702.4015, 
        "XCcy GBP L3 / USD L3 + S: present value");
    E2EUtils.presentValueTest(XCCY_GBP_USD_NOT_3, MULTICURVE_FFCOL, USD, 5909565.4338, 
        "XCcy GBP L3 / USD L3 + S: present value");
  }
  
  @Test
  /** USD Fixed v GBPLIBOR3M */
  public void presentValueXccyGbpUsd4() {
    E2EUtils.presentValueTest(XCCY_GBP_USD_NONOT_4, MULTICURVE_FFCOL, USD, -6148390.0570, 
        "XCcy USD Fixed / GBP L3: present value");
    E2EUtils.presentValueTest(XCCY_GBP_USD_NOT_4, MULTICURVE_FFCOL, USD, -5441253.0892, 
        "XCcy USD Fixed / GBP L3: present value");
  }

  @Test
  public void parRateUsd() {
    double prIrsUsd1Expected = 0.0290919093;
    E2EUtils.parRateTest(IRS_USD_1, MULTICURVE_FFCOL, prIrsUsd1Expected, "IRS USD: par rate");
    SwapCouponFixedCouponDefinition irsAtmDefinition = irsUsd(EFFECTIVE_DATE_1, MATURITY_DATE_1, 
        prIrsUsd1Expected, PAYER_1, NOTIONAL_1);
    SwapFixedCoupon<Coupon> irsAtm = irsAtmDefinition.toDerivative(VALUATION_DATE);
    E2EUtils.presentValueTest(irsAtm, MULTICURVE_FFCOL, USD, 0.0, "IRS USD: par rate");
  }

  @Test
  public void parSpreaMarketQuoteUsd() {
    double psIrsUsd1Expected = 0.002541909269;
    double psIrsUsd1 = IRS_USD_1.accept(PSMQDC, MULTICURVE_FFCOL);
    assertEquals("SwapCrossCurrencyE2ETest", psIrsUsd1Expected, psIrsUsd1, TOLERANCE_RATE);
    SwapCouponFixedCouponDefinition irsAtmDefinition = irsUsd(EFFECTIVE_DATE_1, MATURITY_DATE_1,
        FIXED_RATE_1 + psIrsUsd1Expected, PAYER_1, NOTIONAL_1);
    SwapFixedCoupon<Coupon> irsAtm = irsAtmDefinition.toDerivative(VALUATION_DATE);
    E2EUtils.presentValueTest(irsAtm, MULTICURVE_FFCOL, USD, 0.0, "IRS USD: par spread market quote");
  }

  @Test
  /** Test Bucketed PV01 for a swap Fixed v LIBOR3M. */
  public void BucketedPV01Irs1Usd() {
    final double[] deltaDsc = 
      {0.0098,-0.0237,0.0745,-0.1060,0.5216,-0.5733,1.5341,-2.8175,0.1023,1.6374,
      1.8413,4.5090,0.4741,0.9589,1.1476,-0.3028,-0.2125,0.0698,-0.0170,0.0055,-0.0011 };
    final double[] deltaFwd3 = 
      {12.4970,10.4205,17.1440,-67.2599,156.0905,161.2391,-111.7588,165.5459,-726.1585,-119.0022,
      41.1031,-6.0092,0.9211,-0.2429,0.0423 };
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity = new LinkedHashMap<>();
    sensitivity.put(ObjectsPair.of(MULTICURVE_FFCOL.getName(USD), USD), new DoubleMatrix1D(deltaDsc));
    sensitivity.put(ObjectsPair.of(MULTICURVE_FFCOL.getName(USDLIBOR3M), USD), new DoubleMatrix1D(deltaFwd3));
    final MultipleCurrencyParameterSensitivity pvpsExpected = new MultipleCurrencyParameterSensitivity(sensitivity);
    final MultipleCurrencyParameterSensitivity pvpsComputed = 
        MQSBC.fromInstrument(IRS_USD_1, MULTICURVE_FFCOL, BLOCK_FFCOL).multipliedBy(BP1);
    AssertSensitivityObjects.assertEquals("IRS USD: bucketed deltas", 
        pvpsExpected, pvpsComputed, TOLERANCE_PV_DELTA);
  }

  @Test
  /** Test Bucketed PV01 for a XCcy swap GBPLIBOR3M + S v USDLIBOR3M. */
  public void BucketedPV01XCcyGbpUsd() {
    final double[] deltaDscUsdUsd = 
      {-0.7522,-0.1780,0.4435,-1.8528,-0.2675,-3.9998,5.1698,-5.7528,16.9166,62.6627,
      76.6267,110.2680,68112.7639,-8.3781,2.0839,-0.4449,0.0654,-0.0130,0.0025,-0.0008,0.0001 };
    final double[] deltaFwd3UsdUsd = 
      {0.3042,0.2535,0.4171,-1.6364,8.5562,-27.0588,-22.6155,-167.2095,-634.1891,10.3537,
      -3.6670,0.5361,-0.0822,0.0217,-0.0038};
    final double[] deltaDscUsdGbp = 
      {0.6747,1.6600,1.7811,1.0587,13.9470,-12.7788,54.2353,-104.1401,-173.2242,-260.4330,
      -316.7749,-427.8305,-42198.6873,29.4411,-7.6645,1.6545,-0.1758,0.0276,-0.0045,0.0012,-0.0002 };
    final double[] deltaFwd3UsdGbp = 
      {-0.1965,-0.1638,-0.2695,-55.8330,-5.1828,18.2156,15.2236,108.5490,394.1477,-6.6834,
      2.3672,-0.3461,0.0531,-0.0140,0.0024 };
    final double[] deltaDscGbpGbp = 
      {-10.7500,-7.6405,0.3396,-16.5170,12.2845,56.3878,104.4582,156.3313,211.8309,257.5053,
      344.3316,40422.0222,-23.2651,6.1226,-1.3261,0.1299,-0.0188,0.0028,-0.0007,0.0001 };
    final double[] deltaFwd3GbpGbp = 
      {-1.8241,0.1527,0.2476,54.8081,106.2315,159.0364,215.2629,261.2691,349.9073,236.4169,
      -22.2440,5.8396,-1.2612,0.1223,-0.0174,0.0026,-0.0007,0.0001};
    LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> sensitivity = new LinkedHashMap<>();
    sensitivity.put(ObjectsPair.of(MULTICURVE_FFCOL.getName(USD), USD), new DoubleMatrix1D(deltaDscUsdUsd));
    sensitivity.put(ObjectsPair.of(MULTICURVE_FFCOL.getName(USDLIBOR3M), USD), new DoubleMatrix1D(deltaFwd3UsdUsd));
    sensitivity.put(ObjectsPair.of(MULTICURVE_FFCOL.getName(USD), GBP), new DoubleMatrix1D(deltaDscUsdGbp));
    sensitivity.put(ObjectsPair.of(MULTICURVE_FFCOL.getName(USDLIBOR3M), GBP), new DoubleMatrix1D(deltaFwd3UsdGbp));
    sensitivity.put(ObjectsPair.of(MULTICURVE_FFCOL.getName(GBP), GBP), new DoubleMatrix1D(deltaDscGbpGbp));
    sensitivity.put(ObjectsPair.of(MULTICURVE_FFCOL.getName(GBPLIBOR3M), GBP), new DoubleMatrix1D(deltaFwd3GbpGbp));
    MultipleCurrencyParameterSensitivity pvpsExpected = new MultipleCurrencyParameterSensitivity(sensitivity);
    MultipleCurrencyParameterSensitivity pvpsComputed = 
        MQSBC.fromInstrument(XCCY_GBP_USD_NOT_2, MULTICURVE_FFCOL, BLOCK_FFCOL).multipliedBy(BP1);
    AssertSensitivityObjects.assertEquals("XCcy GBP / USD: bucketed deltas", 
        pvpsExpected, pvpsComputed, TOLERANCE_PV_DELTA);
  }

  private static SwapCouponFixedCouponDefinition irsUsd(final LocalDate effectiveDate, final LocalDate maturityDate, 
      final double fixedRate, boolean payer, final double notional) {
    NotionalProvider notionalProvider = new NotionalProvider() {
      @Override
      public double getAmount(final LocalDate date) {
        return notional;
      }
    };
    // Fixed leg
    AnnuityDefinition<?> legFixedGenDefinition = new FixedAnnuityDefinitionBuilder().
        payer(payer).currency(USD6MLIBOR3M.getCurrency()).notional(notionalProvider).startDate(effectiveDate).
        endDate(maturityDate).dayCount(USD6MLIBOR3M.getFixedLegDayCount()).
        accrualPeriodFrequency(USD6MLIBOR3M.getFixedLegPeriod()).rate(fixedRate).
        accrualPeriodParameters(ADJUSTED_DATE_LIBOR_NYC).build();
    AnnuityCouponFixedDefinition legFixedDefinition =
        new AnnuityCouponFixedDefinition((CouponFixedDefinition[]) legFixedGenDefinition.getPayments(), NYC);
    AnnuityDefinition<? extends CouponDefinition> legIborDefinition = 
        (AnnuityDefinition<? extends CouponDefinition>) new FloatingAnnuityDefinitionBuilder().payer(!payer).
            notional(notionalProvider).startDate(effectiveDate).endDate(maturityDate).index(USDLIBOR3M).
            accrualPeriodFrequency(USDLIBOR3M.getTenor()).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
            resetDateAdjustmentParameters(ADJUSTED_DATE_LIBOR_NYC).accrualPeriodParameters(ADJUSTED_DATE_LIBOR_NYC).
            dayCount(USDLIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_ADJ_LIBOR_LON_2).
            currency(USDLIBOR3M.getCurrency()).build();
    return new SwapCouponFixedCouponDefinition(legFixedDefinition, legIborDefinition);
  }

  private static SwapDefinition xccyGbpL3SUsdL3(final LocalDate effectiveDate, final LocalDate maturityDate, 
      final double spreadGbp, boolean payerGbp, final double notionalGbp, final double notionalUsd, 
      boolean exchangeNotional) {
    NotionalProvider notionalGbpProvider = new NotionalProvider() {
      @Override
      public double getAmount(final LocalDate date) {
        return notionalGbp;
      }
    };
    NotionalProvider notionalUsdProvider = new NotionalProvider() {
      @Override
      public double getAmount(final LocalDate date) {
        return notionalUsd;
      }
    };
    FloatingAnnuityDefinitionBuilder iborGbpBuilder = 
        new FloatingAnnuityDefinitionBuilder().payer(payerGbp).
        notional(notionalGbpProvider).startDate(effectiveDate).endDate(maturityDate).index(GBPLIBOR3M).
        accrualPeriodFrequency(GBPLIBOR3M.getTenor()).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
        resetDateAdjustmentParameters(ADJUSTED_DATE_LIBOR_LONNYC).accrualPeriodParameters(ADJUSTED_DATE_LIBOR_LONNYC).
        dayCount(GBPLIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_ADJ_LIBOR_LON_0).
        currency(GBPLIBOR3M.getCurrency()).spread(spreadGbp);
    if (exchangeNotional) {
      iborGbpBuilder = iborGbpBuilder.
          exchangeInitialNotional(true).startDateAdjustmentParameters(ADJUSTED_DATE_LIBOR_LONNYC).
          exchangeFinalNotional(true).endDateAdjustmentParameters(ADJUSTED_DATE_LIBOR_LONNYC);
    }
    FloatingAnnuityDefinitionBuilder iborUsdBuilder =
        new FloatingAnnuityDefinitionBuilder().payer(!payerGbp).
        notional(notionalUsdProvider).startDate(effectiveDate).endDate(maturityDate).index(USDLIBOR3M).
        accrualPeriodFrequency(USDLIBOR3M.getTenor()).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
        resetDateAdjustmentParameters(ADJUSTED_DATE_LIBOR_LONNYC).accrualPeriodParameters(ADJUSTED_DATE_LIBOR_LONNYC).
        dayCount(USDLIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_ADJ_LIBOR_LON_2).
        currency(USDLIBOR3M.getCurrency());
    if (exchangeNotional) {
      iborUsdBuilder = iborUsdBuilder.
          exchangeInitialNotional(true).startDateAdjustmentParameters(ADJUSTED_DATE_LIBOR_LONNYC).
          exchangeFinalNotional(true).endDateAdjustmentParameters(ADJUSTED_DATE_LIBOR_LONNYC);
    }
    AnnuityDefinition<?> legIborGbpDefinition = iborGbpBuilder.build();
    AnnuityDefinition<?> legIborUsdDefinition = iborUsdBuilder.build();
    return new SwapDefinition(legIborGbpDefinition, legIborUsdDefinition);
  }

  private static SwapDefinition xccyGbpL3UsdL3S(final LocalDate effectiveDate, final LocalDate maturityDate, 
      final double spreadUsd, boolean payerGbp, final double notionalGbp, final double notionalUsd,
      boolean exchangeNotional) {
    NotionalProvider notionalGbpProvider = new NotionalProvider() {
      @Override
      public double getAmount(final LocalDate date) {
        return notionalGbp;
      }
    };
    NotionalProvider notionalUsdProvider = new NotionalProvider() {
      @Override
      public double getAmount(final LocalDate date) {
        return notionalUsd;
      }
    };
    FloatingAnnuityDefinitionBuilder legIborGbpBuilder = new FloatingAnnuityDefinitionBuilder().payer(payerGbp).
        notional(notionalGbpProvider).startDate(effectiveDate).endDate(maturityDate).index(GBPLIBOR3M).
        accrualPeriodFrequency(GBPLIBOR3M.getTenor()).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
        resetDateAdjustmentParameters(ADJUSTED_DATE_LIBOR_LONNYC).accrualPeriodParameters(ADJUSTED_DATE_LIBOR_LONNYC).
        dayCount(GBPLIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_ADJ_LIBOR_LON_0).
        currency(GBPLIBOR3M.getCurrency());
    if (exchangeNotional) {
      legIborGbpBuilder = legIborGbpBuilder.
          exchangeInitialNotional(true).startDateAdjustmentParameters(ADJUSTED_DATE_LIBOR_LONNYC).
          exchangeFinalNotional(true).endDateAdjustmentParameters(ADJUSTED_DATE_LIBOR_LONNYC);
    }
    AnnuityDefinition<?> legIborGbpDefinition = legIborGbpBuilder.build();
    FloatingAnnuityDefinitionBuilder legIborUsdBuilder = new FloatingAnnuityDefinitionBuilder().payer(!payerGbp).
        notional(notionalUsdProvider).startDate(effectiveDate).endDate(maturityDate).index(USDLIBOR3M).
        accrualPeriodFrequency(USDLIBOR3M.getTenor()).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
        resetDateAdjustmentParameters(ADJUSTED_DATE_LIBOR_LONNYC).accrualPeriodParameters(ADJUSTED_DATE_LIBOR_LONNYC).
        dayCount(USDLIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_ADJ_LIBOR_LON_2).
        currency(USDLIBOR3M.getCurrency()).spread(spreadUsd);
    if (exchangeNotional) {
      legIborUsdBuilder = legIborUsdBuilder.
          exchangeInitialNotional(true).startDateAdjustmentParameters(ADJUSTED_DATE_LIBOR_LONNYC).
          exchangeFinalNotional(true).endDateAdjustmentParameters(ADJUSTED_DATE_LIBOR_LONNYC);
    }
    AnnuityDefinition<?> legIborUsdDefinition = legIborUsdBuilder.build();
    return new SwapDefinition(legIborGbpDefinition, legIborUsdDefinition);
  }

  private static SwapDefinition xccyUsdFGbpL3(final LocalDate effectiveDate, final LocalDate maturityDate, 
      final double fixedRate, boolean payerUsd, final double notionalUsd, final double notionalGbp,
      boolean exchangeNotional) {
    NotionalProvider notionalGbpProvider = new NotionalProvider() {
      @Override
      public double getAmount(final LocalDate date) {
        return notionalGbp;
      }
    };
    NotionalProvider notionalUsdProvider = new NotionalProvider() {
      @Override
      public double getAmount(final LocalDate date) {
        return notionalUsd;
      }
    };
    FixedAnnuityDefinitionBuilder fixedUsdBuilder = new FixedAnnuityDefinitionBuilder().
    payer(payerUsd).currency(USD6MLIBOR3M.getCurrency()).notional(notionalUsdProvider).startDate(effectiveDate).
    endDate(maturityDate).dayCount(USD6MLIBOR3M.getFixedLegDayCount()).
    accrualPeriodFrequency(USD6MLIBOR3M.getFixedLegPeriod()).rate(fixedRate).
    accrualPeriodParameters(ADJUSTED_DATE_LIBOR_NYC);
    if (exchangeNotional) {
      fixedUsdBuilder = fixedUsdBuilder.
          exchangeInitialNotional(true).startDateAdjustmentParameters(ADJUSTED_DATE_LIBOR_LONNYC).
          exchangeFinalNotional(true).endDateAdjustmentParameters(ADJUSTED_DATE_LIBOR_LONNYC);
    }
    FloatingAnnuityDefinitionBuilder iborGbpBuilder = new FloatingAnnuityDefinitionBuilder().payer(!payerUsd).
    notional(notionalGbpProvider).startDate(effectiveDate).endDate(maturityDate).index(GBPLIBOR3M).
    accrualPeriodFrequency(GBPLIBOR3M.getTenor()).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
    resetDateAdjustmentParameters(ADJUSTED_DATE_LIBOR_LONNYC).accrualPeriodParameters(ADJUSTED_DATE_LIBOR_LONNYC).
    dayCount(GBPLIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_ADJ_LIBOR_LON_0).
    currency(GBPLIBOR3M.getCurrency());
    if (exchangeNotional) {
      iborGbpBuilder = iborGbpBuilder.
          exchangeInitialNotional(true).startDateAdjustmentParameters(ADJUSTED_DATE_LIBOR_LONNYC).
          exchangeFinalNotional(true).endDateAdjustmentParameters(ADJUSTED_DATE_LIBOR_LONNYC);
    }
    AnnuityDefinition<?> legFixedGenDefinition = fixedUsdBuilder.build();
    AnnuityCouponFixedDefinition legFixedUsdDefinition =
        new AnnuityCouponFixedDefinition((CouponFixedDefinition[]) legFixedGenDefinition.getPayments(), NYC);
    AnnuityDefinition<? extends CouponDefinition> legIborGbpDefinition = 
        (AnnuityDefinition<? extends CouponDefinition>) iborGbpBuilder.build();
    return new SwapCouponFixedCouponDefinition(legFixedUsdDefinition, legIborGbpDefinition);
  }
  
}
