/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.tutorial.analysis.swap;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.datasets.CalendarUSD;
import com.opengamma.analytics.financial.instrument.NotionalProvider;
import com.opengamma.analytics.financial.instrument.annuity.AdjustedDateParameters;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.CompoundingMethod;
import com.opengamma.analytics.financial.instrument.annuity.FixedAnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.annuity.FloatingAnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.annuity.OffsetAdjustedDateParameters;
import com.opengamma.analytics.financial.instrument.annuity.OffsetType;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapCouponFixedCouponDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.tutorial.datasets.ComputedDataSetsMulticurveImmUsd;
import com.opengamma.analytics.tutorial.datasets.RecentDataSetsMulticurveFFSUsd;
import com.opengamma.analytics.tutorial.datasets.RecentDataSetsMulticurveFutures3MUsd;
import com.opengamma.analytics.tutorial.datasets.RecentDataSetsMulticurveStandardUsd;
import com.opengamma.analytics.util.export.ExportUtils;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Examples of risk analysis for different swaps in USD.
 * Those examples can be used for tutorials. 
 */
public class SwapUsdAnalysis {

  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2014, 7, 16);

  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final GeneratorSwapFixedONMaster GENERATOR_OIS_MASTER = GeneratorSwapFixedONMaster.getInstance();
  private static final GeneratorSwapFixedON GENERATOR_OIS_USD = GENERATOR_OIS_MASTER.getGenerator("USD1YFEDFUND", NYC);
  private static final IndexON USDFEDFUND = GENERATOR_OIS_USD.getIndex();
  private static final GeneratorSwapFixedIborMaster GENERATOR_IRS_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_IRS_MASTER.getGenerator("USD6MLIBOR3M", NYC);
  private static final IborIndex USDLIBOR3M = USD6MLIBOR3M.getIborIndex();
  private static final Currency USD = USDLIBOR3M.getCurrency();
  private static final AdjustedDateParameters ADJUSTED_DATE_LIBOR = new AdjustedDateParameters(NYC, USD6MLIBOR3M.getBusinessDayConvention());
  private static final OffsetAdjustedDateParameters OFFSET_ADJ_LIBOR =
      new OffsetAdjustedDateParameters(-2, OffsetType.BUSINESS, NYC, USD6MLIBOR3M.getBusinessDayConvention());
  private static final AdjustedDateParameters ADJUSTED_DATE_FEDFUND = new AdjustedDateParameters(NYC, GENERATOR_OIS_USD.getBusinessDayConvention());
  private static final OffsetAdjustedDateParameters OFFSET_PAY_FEDFUND =
      new OffsetAdjustedDateParameters(GENERATOR_OIS_USD.getPaymentLag(), OffsetType.BUSINESS, NYC, BusinessDayConventionFactory.of("Following"));
  private static final OffsetAdjustedDateParameters OFFSET_FIX_FEDFUND =
      new OffsetAdjustedDateParameters(0, OffsetType.BUSINESS, NYC, BusinessDayConventionFactory.of("Following"));

  /** USD Fixed v USDLIBOR3M */
  private static final LocalDate EFFECTIVE_DATE_1 = LocalDate.of(2016, 7, 18);
  private static final LocalDate MATURITY_DATE_1 = LocalDate.of(2026, 7, 18);
  private static final double FIXED_RATE_1 = 0.02655;
  private static final boolean PAYER_1 = false;
  private static final double NOTIONAL_1 = 1000000; // 1m
  private static final NotionalProvider NOTIONAL_PROV_1 = new NotionalProvider() {
    @Override
    public double getAmount(final LocalDate date) {
      return NOTIONAL_1;
    }
  };

  private static final LocalDate EFFECTIVE_DATE_2 = LocalDate.of(2019, 7, 3);
  private static final LocalDate MATURITY_DATE_2 = LocalDate.of(2024, 7, 3);
  private static final double FIXED_RATE_2 = 0.037125;
  private static final boolean PAYER_2 = true;

  private static final LocalDate EFFECTIVE_DATE_3 = LocalDate.of(2014, 7, 18);
  private static final LocalDate MATURITY_DATE_3 = LocalDate.of(2019, 7, 18);
  private static final double FIXED_RATE_3 = 0.0100;
  private static final boolean PAYER_3 = true;

  private static final LocalDate EFFECTIVE_DATE_4 = LocalDate.of(2014, 7, 18);
  private static final LocalDate MATURITY_DATE_4 = LocalDate.of(2017, 7, 18);
  private static final double FIXED_RATE_4 = 0.0100;
  private static final boolean PAYER_4 = true;

  /** Swap 1 **/
  /** Fixed leg */
  private static final PaymentDefinition[] PAYMENT_LEG_1_DEFINITION = new FixedAnnuityDefinitionBuilder().
      payer(PAYER_1).
      currency(USD6MLIBOR3M.getCurrency()).
      notional(NOTIONAL_PROV_1).
      startDate(EFFECTIVE_DATE_1).
      endDate(MATURITY_DATE_1).
      dayCount(USD6MLIBOR3M.getFixedLegDayCount()).
      accrualPeriodFrequency(USD6MLIBOR3M.getFixedLegPeriod()).
      rate(FIXED_RATE_1).
      accrualPeriodParameters(ADJUSTED_DATE_LIBOR).
      build().getPayments();
  private static final CouponFixedDefinition[] CPN_FIXED_1_DEFINITION = new CouponFixedDefinition[PAYMENT_LEG_1_DEFINITION.length];
  static {
    for (int loopcpn = 0; loopcpn < PAYMENT_LEG_1_DEFINITION.length; loopcpn++) {
      CPN_FIXED_1_DEFINITION[loopcpn] = (CouponFixedDefinition) PAYMENT_LEG_1_DEFINITION[loopcpn];
    }
  }
  private static final AnnuityCouponFixedDefinition FIXED_LEG_1_DEFINITION = new AnnuityCouponFixedDefinition(CPN_FIXED_1_DEFINITION, NYC);
  /** Ibor leg */
  @SuppressWarnings("unchecked")
  private static final AnnuityDefinition<? extends CouponDefinition> IBOR_LEG_1_DEFINITION = (AnnuityDefinition<? extends CouponDefinition>)
      new FloatingAnnuityDefinitionBuilder().
          payer(!PAYER_1).
          notional(NOTIONAL_PROV_1).
          startDate(EFFECTIVE_DATE_1).
          endDate(MATURITY_DATE_1).
          index(USDLIBOR3M).
          accrualPeriodFrequency(USDLIBOR3M.getTenor()).
          rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
          resetDateAdjustmentParameters(ADJUSTED_DATE_LIBOR).
          accrualPeriodParameters(ADJUSTED_DATE_LIBOR).
          dayCount(USDLIBOR3M.getDayCount()).
          fixingDateAdjustmentParameters(OFFSET_ADJ_LIBOR).
          currency(USDLIBOR3M.getCurrency()).
          build();
  private static final SwapCouponFixedCouponDefinition IRS_1_DEFINITION = new SwapCouponFixedCouponDefinition(FIXED_LEG_1_DEFINITION, IBOR_LEG_1_DEFINITION);
  /** Swap LIBOR3M 2 **/
  private static final PaymentDefinition[] PAYMENT_LEG_2_DEFINITION = new FixedAnnuityDefinitionBuilder().
      payer(PAYER_2).currency(USD6MLIBOR3M.getCurrency()).notional(NOTIONAL_PROV_1).startDate(EFFECTIVE_DATE_2).
      endDate(MATURITY_DATE_2).dayCount(USD6MLIBOR3M.getFixedLegDayCount()).
      accrualPeriodFrequency(USD6MLIBOR3M.getFixedLegPeriod()).rate(FIXED_RATE_2).accrualPeriodParameters(ADJUSTED_DATE_LIBOR).
      build().getPayments();
  private static final CouponFixedDefinition[] CPN_FIXED_2_DEFINITION = new CouponFixedDefinition[PAYMENT_LEG_2_DEFINITION.length];
  static {
    for (int loopcpn = 0; loopcpn < PAYMENT_LEG_2_DEFINITION.length; loopcpn++) {
      CPN_FIXED_2_DEFINITION[loopcpn] = (CouponFixedDefinition) PAYMENT_LEG_2_DEFINITION[loopcpn];
    }
  }
  private static final AnnuityCouponFixedDefinition FIXED_LEG_2_DEFINITION = new AnnuityCouponFixedDefinition(CPN_FIXED_2_DEFINITION, NYC);
  @SuppressWarnings("unchecked")
  private static final AnnuityDefinition<? extends CouponDefinition> IBOR_LEG_2_DEFINITION = (AnnuityDefinition<? extends CouponDefinition>)
      new FloatingAnnuityDefinitionBuilder().payer(!PAYER_2).notional(NOTIONAL_PROV_1).startDate(EFFECTIVE_DATE_2).endDate(MATURITY_DATE_2).
          index(USDLIBOR3M).accrualPeriodFrequency(USDLIBOR3M.getTenor()).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
          resetDateAdjustmentParameters(ADJUSTED_DATE_LIBOR).accrualPeriodParameters(ADJUSTED_DATE_LIBOR).
          dayCount(USDLIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_ADJ_LIBOR).currency(USDLIBOR3M.getCurrency()).
          build();
  private static final SwapCouponFixedCouponDefinition IRS_2_DEFINITION = new SwapCouponFixedCouponDefinition(FIXED_LEG_2_DEFINITION, IBOR_LEG_2_DEFINITION);
  /** Swap LIBOR3M 3 **/
  private static final PaymentDefinition[] PAYMENT_LEG_3_DEFINITION = new FixedAnnuityDefinitionBuilder().
      payer(PAYER_3).currency(USD6MLIBOR3M.getCurrency()).notional(NOTIONAL_PROV_1).startDate(EFFECTIVE_DATE_3).
      endDate(MATURITY_DATE_3).dayCount(USD6MLIBOR3M.getFixedLegDayCount()).
      accrualPeriodFrequency(USD6MLIBOR3M.getFixedLegPeriod()).rate(FIXED_RATE_3).accrualPeriodParameters(ADJUSTED_DATE_LIBOR).
      build().getPayments();
  private static final CouponFixedDefinition[] CPN_FIXED_3_DEFINITION = new CouponFixedDefinition[PAYMENT_LEG_3_DEFINITION.length];
  static {
    for (int loopcpn = 0; loopcpn < PAYMENT_LEG_3_DEFINITION.length; loopcpn++) {
      CPN_FIXED_3_DEFINITION[loopcpn] = (CouponFixedDefinition) PAYMENT_LEG_3_DEFINITION[loopcpn];
    }
  }
  private static final AnnuityCouponFixedDefinition FIXED_LEG_3_DEFINITION = new AnnuityCouponFixedDefinition(CPN_FIXED_3_DEFINITION, NYC);
  @SuppressWarnings("unchecked")
  private static final AnnuityDefinition<? extends CouponDefinition> IBOR_LEG_3_DEFINITION = (AnnuityDefinition<? extends CouponDefinition>)
      new FloatingAnnuityDefinitionBuilder().payer(!PAYER_3).notional(NOTIONAL_PROV_1).startDate(EFFECTIVE_DATE_3).endDate(MATURITY_DATE_3).
          index(USDLIBOR3M).accrualPeriodFrequency(USDLIBOR3M.getTenor()).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
          resetDateAdjustmentParameters(ADJUSTED_DATE_LIBOR).accrualPeriodParameters(ADJUSTED_DATE_LIBOR).
          dayCount(USDLIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_ADJ_LIBOR).currency(USDLIBOR3M.getCurrency()).
          build();
  private static final SwapCouponFixedCouponDefinition IRS_3_DEFINITION = new SwapCouponFixedCouponDefinition(FIXED_LEG_3_DEFINITION, IBOR_LEG_3_DEFINITION);
  /** Swap LIBOR6M 1 **/
  //TODO
  /** Swap OIS 1 **/
  private static final PaymentDefinition[] PAYMENT_OIS_LEG_1_DEFINITION = new FixedAnnuityDefinitionBuilder().
      payer(PAYER_4).currency(USD).notional(NOTIONAL_PROV_1).startDate(EFFECTIVE_DATE_4).endDate(MATURITY_DATE_4).
      dayCount(GENERATOR_OIS_USD.getFixedLegDayCount()).accrualPeriodFrequency(GENERATOR_OIS_USD.getLegsPeriod()).
      rate(FIXED_RATE_4).accrualPeriodParameters(ADJUSTED_DATE_FEDFUND).paymentDateAdjustmentParameters(OFFSET_PAY_FEDFUND).
      build().getPayments();
  private static final CouponFixedDefinition[] CPN_FIXED_OIS_1_DEFINITION = new CouponFixedDefinition[PAYMENT_OIS_LEG_1_DEFINITION.length];
  static {
    for (int loopcpn = 0; loopcpn < PAYMENT_OIS_LEG_1_DEFINITION.length; loopcpn++) {
      CPN_FIXED_OIS_1_DEFINITION[loopcpn] = (CouponFixedDefinition) PAYMENT_OIS_LEG_1_DEFINITION[loopcpn];
    }
  }
  private static final AnnuityCouponFixedDefinition FIXED_OIS_LEG_1_DEFINITION = new AnnuityCouponFixedDefinition(CPN_FIXED_OIS_1_DEFINITION, NYC);
  /** ON leg */
  @SuppressWarnings("unchecked")
  private static final AnnuityDefinition<? extends CouponDefinition> ON_LEG_1_DEFINITION = (AnnuityDefinition<? extends CouponDefinition>) new FloatingAnnuityDefinitionBuilder().
      payer(!PAYER_4).notional(NOTIONAL_PROV_1).startDate(EFFECTIVE_DATE_4).endDate(MATURITY_DATE_4).index(USDFEDFUND).
      accrualPeriodFrequency(GENERATOR_OIS_USD.getLegsPeriod()).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
      resetDateAdjustmentParameters(ADJUSTED_DATE_FEDFUND).accrualPeriodParameters(ADJUSTED_DATE_FEDFUND).
      dayCount(USDFEDFUND.getDayCount()).fixingDateAdjustmentParameters(OFFSET_FIX_FEDFUND).currency(USD).compoundingMethod(CompoundingMethod.FLAT).
      build();
  private static final SwapCouponFixedCouponDefinition OIS_1_DEFINITION = new SwapCouponFixedCouponDefinition(FIXED_OIS_LEG_1_DEFINITION, ON_LEG_1_DEFINITION);

  /** Curves and fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_FIXED_IBOR_USD3M_WITHOUT_TODAY = RecentDataSetsMulticurveStandardUsd.fixingUsdLibor3MWithoutLast();
  private static final ZonedDateTimeDoubleTimeSeries TS_FIXED_ON_USD_WITHOUT_TODAY = RecentDataSetsMulticurveStandardUsd.fixingUsdOnWithoutLast();

  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_FUT_PAIR =
      RecentDataSetsMulticurveFutures3MUsd.getCurvesUSDOisL1L3L6(VALUATION_DATE, false);
  private static final MulticurveProviderDiscount MULTICURVE_FUT = MULTICURVE_FUT_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_FUT = MULTICURVE_FUT_PAIR.getSecond();

  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_STD_PAIR =
      RecentDataSetsMulticurveStandardUsd.getCurvesUSDOisL1L3L6_20140728(VALUATION_DATE);
  private static final MulticurveProviderDiscount MULTICURVE_STD = MULTICURVE_STD_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_STD = MULTICURVE_STD_PAIR.getSecond();

  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_FFS_PAIR =
      RecentDataSetsMulticurveFFSUsd.getCurvesUSDOisL1L3L6(VALUATION_DATE);
  private static final MulticurveProviderDiscount MULTICURVE_FFS = MULTICURVE_FFS_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_FFS = MULTICURVE_FFS_PAIR.getSecond();

  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_IMM_PAIR =
      ComputedDataSetsMulticurveImmUsd.getCurvesUSDOisL3(VALUATION_DATE, 60, MULTICURVE_FUT);
  private static final MulticurveProviderDiscount MULTICURVE_IMM = MULTICURVE_IMM_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_IMM = MULTICURVE_IMM_PAIR.getSecond();

  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_STD_2_PAIR =
      RecentDataSetsMulticurveStandardUsd.getCurvesUSDOisL3(VALUATION_DATE, MULTICURVE_FUT);
  private static final MulticurveProviderDiscount MULTICURVE_STD_2 = MULTICURVE_STD_2_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_STD_2 = MULTICURVE_STD_2_PAIR.getSecond();

  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_FFS_2_PAIR =
      RecentDataSetsMulticurveFFSUsd.getCurvesUSDOisL3(VALUATION_DATE, MULTICURVE_FUT);
  private static final MulticurveProviderDiscount MULTICURVE_FFS_2 = MULTICURVE_FFS_2_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_FFS_2 = MULTICURVE_FFS_2_PAIR.getSecond();

  private static final Annuity<?> FIXED_LEG_1 = FIXED_LEG_1_DEFINITION.toDerivative(VALUATION_DATE);
  private static final Annuity<?> IBOR_LEG_1 = IBOR_LEG_1_DEFINITION.toDerivative(VALUATION_DATE, TS_FIXED_IBOR_USD3M_WITHOUT_TODAY);
  private static final Swap<? extends Payment, ? extends Payment> IRS_1 = IRS_1_DEFINITION.toDerivative(VALUATION_DATE,
      new ZonedDateTimeDoubleTimeSeries[] {TS_FIXED_IBOR_USD3M_WITHOUT_TODAY, TS_FIXED_IBOR_USD3M_WITHOUT_TODAY });
  private static final Swap<? extends Payment, ? extends Payment> IRS_2 = IRS_2_DEFINITION.toDerivative(VALUATION_DATE,
      new ZonedDateTimeDoubleTimeSeries[] {TS_FIXED_IBOR_USD3M_WITHOUT_TODAY, TS_FIXED_IBOR_USD3M_WITHOUT_TODAY });
  private static final Swap<? extends Payment, ? extends Payment> IRS_3 = IRS_3_DEFINITION.toDerivative(VALUATION_DATE,
      new ZonedDateTimeDoubleTimeSeries[] {TS_FIXED_IBOR_USD3M_WITHOUT_TODAY, TS_FIXED_IBOR_USD3M_WITHOUT_TODAY });
  private static final Swap<? extends Payment, ? extends Payment> OIS_1 = OIS_1_DEFINITION.toDerivative(VALUATION_DATE,
      new ZonedDateTimeDoubleTimeSeries[] {TS_FIXED_ON_USD_WITHOUT_TODAY, TS_FIXED_ON_USD_WITHOUT_TODAY });

  /** Calculators **/
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<ParameterProviderInterface> PSC = new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final MarketQuoteSensitivityBlockCalculator<ParameterProviderInterface> MQSBC = new MarketQuoteSensitivityBlockCalculator<>(PSC);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_2 = 1.0E+4;
  private static final double BP1 = 1.0E-4;

  @SuppressWarnings("unused")
  @Test
  public void presentValue() {
    MultipleCurrencyAmount pvFixed = FIXED_LEG_1.accept(PVDC, MULTICURVE_STD);
    MultipleCurrencyAmount pvIbor = IBOR_LEG_1.accept(PVDC, MULTICURVE_STD);
    MultipleCurrencyAmount pvSwap1Std = IRS_1.accept(PVDC, MULTICURVE_STD);
    assertTrue("SwapRiskUsdAnalysis: present value", pvFixed.getAmount(USD) * pvIbor.getAmount(USD) < 0);
    assertEquals("SwapRiskUsdAnalysis: present value", pvSwap1Std.getAmount(USD), pvFixed.getAmount(USD) + pvIbor.getAmount(USD), TOLERANCE_PV);
    MultipleCurrencyAmount pvSwap1Fut = IRS_1.accept(PVDC, MULTICURVE_FUT);
    MultipleCurrencyAmount pvSwap1Imm = IRS_1.accept(PVDC, MULTICURVE_IMM);
    assertEquals("SwapRiskUsdAnalysis: present value", pvSwap1Fut.getAmount(USD), pvSwap1Imm.getAmount(USD), TOLERANCE_PV_2);
    MultipleCurrencyAmount pvSwap1Std2 = IRS_1.accept(PVDC, MULTICURVE_STD_2);
    assertEquals("SwapRiskUsdAnalysis: present value", pvSwap1Fut.getAmount(USD), pvSwap1Std2.getAmount(USD), TOLERANCE_PV_2);
    MultipleCurrencyAmount pvSwap1Ffs2 = IRS_1.accept(PVDC, MULTICURVE_FFS_2);
    assertEquals("SwapRiskUsdAnalysis: present value", pvSwap1Fut.getAmount(USD), pvSwap1Ffs2.getAmount(USD), TOLERANCE_PV_2);
    MultipleCurrencyAmount pvSwap2Fut = IRS_2.accept(PVDC, MULTICURVE_FUT);
    MultipleCurrencyAmount pvOis1Ffs = OIS_1.accept(PVDC, MULTICURVE_FFS);
  
    System.out.println("--- PVs ---");
    System.out.println("SWAP1 PV swap," + String.valueOf(IRS_1.accept(PVDC, MULTICURVE_FFS_2).getAmount(USD)));
    System.out.println("SWAP2 PV swap," + String.valueOf(IRS_2.accept(PVDC, MULTICURVE_FFS_2).getAmount(USD)));
  }

  @SuppressWarnings("unused")
  @Test
  public void parRate() {
    double pr1Std = IRS_1.accept(PRDC, MULTICURVE_STD);
    double pr1Fut = IRS_1.accept(PRDC, MULTICURVE_FUT);
    double pr1Imm = IRS_1.accept(PRDC, MULTICURVE_IMM);
    double pr1Std2 = IRS_1.accept(PRDC, MULTICURVE_STD_2);
    double pr1Ffs2 = IRS_1.accept(PRDC, MULTICURVE_FFS_2);
    double pr2Std = IRS_2.accept(PRDC, MULTICURVE_STD);
    double pr2Fut = IRS_2.accept(PRDC, MULTICURVE_FUT);
    int t = 0;
    
    System.out.println("--- Break-even rate ---");
    System.out.println("SWAP1 Par rate," + String.valueOf(IRS_1.accept(PRDC, MULTICURVE_FFS_2)));
    System.out.println("SWAP2 Par rate," + String.valueOf(IRS_2.accept(PRDC, MULTICURVE_FFS_2)));


  }

  @Test(enabled = true)
  public void bucketedPv01() {
    System.out.println("--- IRS1 swap ---");
    MultipleCurrencyParameterSensitivity pvmqs1Std = MQSBC.fromInstrument(IRS_1, MULTICURVE_STD, BLOCK_STD).multipliedBy(BP1);
    MultipleCurrencyParameterSensitivity pvmqs1Ffs = MQSBC.fromInstrument(IRS_1, MULTICURVE_FFS, BLOCK_FFS).multipliedBy(BP1);
    MultipleCurrencyParameterSensitivity pvmqs1Fut = MQSBC.fromInstrument(IRS_1, MULTICURVE_FUT, BLOCK_FUT).multipliedBy(BP1);
    MultipleCurrencyParameterSensitivity pvmqs1Imm = MQSBC.fromInstrument(IRS_1, MULTICURVE_IMM, BLOCK_IMM).multipliedBy(BP1);
    MultipleCurrencyParameterSensitivity pvmqs1Std2 = MQSBC.fromInstrument(IRS_1, MULTICURVE_STD_2, BLOCK_STD_2).multipliedBy(BP1);
    MultipleCurrencyParameterSensitivity pvmqs2Std = MQSBC.fromInstrument(IRS_1, MULTICURVE_STD, BLOCK_STD).multipliedBy(BP1);
    ExportUtils.consolePrint(pvmqs1Std, MULTICURVE_STD);
    ExportUtils.consolePrint(pvmqs1Ffs, MULTICURVE_FFS);
    ExportUtils.consolePrint(pvmqs1Fut, MULTICURVE_FUT);
    ExportUtils.consolePrint(pvmqs1Imm, MULTICURVE_IMM);
    ExportUtils.consolePrint(pvmqs1Std2, MULTICURVE_STD);
    ExportUtils.consolePrint(pvmqs2Std, MULTICURVE_STD);

    System.out.println("--- IRS2 swap ---");
    MultipleCurrencyParameterSensitivity pvmqs1Ffs2 = MQSBC.fromInstrument(IRS_2, MULTICURVE_FFS_2, BLOCK_FFS_2).multipliedBy(BP1);
    MultipleCurrencyParameterSensitivity pvmqs2Imm = MQSBC.fromInstrument(IRS_2, MULTICURVE_IMM, BLOCK_IMM).multipliedBy(BP1);
    MultipleCurrencyParameterSensitivity pvmqs2Fut = MQSBC.fromInstrument(IRS_2, MULTICURVE_FUT, BLOCK_FUT).multipliedBy(BP1);
    ExportUtils.consolePrint(pvmqs1Ffs2, MULTICURVE_FFS_2);
    ExportUtils.consolePrint(pvmqs2Imm, MULTICURVE_IMM);
    ExportUtils.consolePrint(pvmqs2Fut, MULTICURVE_FUT);

    System.out.println("--- IRS3 swap ---");
    MultipleCurrencyParameterSensitivity pvmqs3Fut = MQSBC.fromInstrument(IRS_3, MULTICURVE_FUT, BLOCK_FUT).multipliedBy(BP1);
    ExportUtils.consolePrint(pvmqs3Fut, MULTICURVE_FUT);

    System.out.println("--- OIS swap ---");
    MultipleCurrencyParameterSensitivity pvmqs4Fut = MQSBC.fromInstrument(OIS_1, MULTICURVE_FUT, BLOCK_FUT).multipliedBy(BP1);
    MultipleCurrencyParameterSensitivity pvmqs4Imm = MQSBC.fromInstrument(OIS_1, MULTICURVE_IMM, BLOCK_IMM).multipliedBy(BP1);
    MultipleCurrencyParameterSensitivity pvmqs4Std2 = MQSBC.fromInstrument(OIS_1, MULTICURVE_STD_2, BLOCK_STD_2).multipliedBy(BP1);
    MultipleCurrencyParameterSensitivity pvmqs4Ffs2 = MQSBC.fromInstrument(OIS_1, MULTICURVE_FFS_2, BLOCK_FFS_2).multipliedBy(BP1);
    ExportUtils.consolePrint(pvmqs4Fut, MULTICURVE_FUT);
    ExportUtils.consolePrint(pvmqs4Imm, MULTICURVE_IMM);
    ExportUtils.consolePrint(pvmqs4Std2, MULTICURVE_STD_2);
    ExportUtils.consolePrint(pvmqs4Ffs2, MULTICURVE_FFS_2);

  }
  
  @Test(enabled = false)
  public void performanceCalibration() {
    long startTime, endTime;
    final int nbTest = 100;

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      RecentDataSetsMulticurveStandardUsd.getCurvesUSDOisL3(VALUATION_DATE);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " curve construction / 2 units: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 2 units: 22-Oct-2014: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 2500 ms for 100 sets.  
  }
  
  @Test(enabled = true)
  public void performanceBucketedPv01() {
    Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> pair = 
        RecentDataSetsMulticurveStandardUsd.getCurvesUSDOisL3(VALUATION_DATE);
    
    long startTime, endTime;
    final int nbTest = 1000;

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      @SuppressWarnings("unused")
      MultipleCurrencyParameterSensitivity pvmqs3Fut = MQSBC.fromInstrument(IRS_3, pair.getFirst(), pair.getSecond());
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " bucketed PV01 30Y: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction 2 units: 22-Oct-2014: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 400 ms for 1000 30Y swaps.
  }
  
}
