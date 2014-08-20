/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.tutorial.analysis.swap;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.BeforeClass;
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
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.tutorial.datasets.ComputedDataSetsMulticurveImmUsd;
import com.opengamma.analytics.tutorial.datasets.GbpDatasetJuly16;
import com.opengamma.analytics.tutorial.datasets.RecentDataSetsMulticurveFFSUsd;
import com.opengamma.analytics.tutorial.datasets.RecentDataSetsMulticurveFutures3MUsd;
import com.opengamma.analytics.tutorial.datasets.RecentDataSetsMulticurveStandardUsd;
import com.opengamma.analytics.tutorial.datasets.UsdDatasetJuly16;
import com.opengamma.analytics.tutorial.utils.ExportUtils;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Examples of risk analysis for different swaps in USD.
 * Those examples can be used for tutorials. 
 */
@Test(groups = TestGroup.UNIT)
public class SwapRiskAnalysisJuly16Usd {

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
  private static final OffsetAdjustedDateParameters OFFSET_PAY_SONIA =
      new OffsetAdjustedDateParameters(GENERATOR_OIS_USD.getPaymentLag(), OffsetType.BUSINESS, NYC, BusinessDayConventionFactory.of("Following"));
  private static final OffsetAdjustedDateParameters OFFSET_FIX_SONIA =
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
  private static final LocalDate MATURITY_DATE_3 = LocalDate.of(2016, 7, 18);
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
  private static final CouponFixedDefinition[] CPN_FIXED_1_DEFINITION = 
      new CouponFixedDefinition[PAYMENT_LEG_1_DEFINITION.length];
  static {
    for (int loopcpn = 0; loopcpn < PAYMENT_LEG_1_DEFINITION.length; loopcpn++) {
      CPN_FIXED_1_DEFINITION[loopcpn] = (CouponFixedDefinition) PAYMENT_LEG_1_DEFINITION[loopcpn];
    }
  }
  private static final AnnuityCouponFixedDefinition FIXED_LEG_1_DEFINITION = 
      new AnnuityCouponFixedDefinition(CPN_FIXED_1_DEFINITION, NYC);
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
  private static final SwapCouponFixedCouponDefinition IRS_1_DEFINITION = 
      new SwapCouponFixedCouponDefinition(FIXED_LEG_1_DEFINITION, IBOR_LEG_1_DEFINITION);
  /** Swap LIBOR3M 2 **/
  private static final PaymentDefinition[] PAYMENT_LEG_2_DEFINITION = new FixedAnnuityDefinitionBuilder().
      payer(PAYER_2).currency(USD6MLIBOR3M.getCurrency()).notional(NOTIONAL_PROV_1).startDate(EFFECTIVE_DATE_2).
      endDate(MATURITY_DATE_2).dayCount(USD6MLIBOR3M.getFixedLegDayCount()).
      accrualPeriodFrequency(USD6MLIBOR3M.getFixedLegPeriod()).rate(FIXED_RATE_2).accrualPeriodParameters(ADJUSTED_DATE_LIBOR).
      build().getPayments();
  private static final CouponFixedDefinition[] CPN_FIXED_2_DEFINITION = 
      new CouponFixedDefinition[PAYMENT_LEG_2_DEFINITION.length];
  static {
    for (int loopcpn = 0; loopcpn < PAYMENT_LEG_2_DEFINITION.length; loopcpn++) {
      CPN_FIXED_2_DEFINITION[loopcpn] = (CouponFixedDefinition) PAYMENT_LEG_2_DEFINITION[loopcpn];
    }
  }
  private static final AnnuityCouponFixedDefinition FIXED_LEG_2_DEFINITION = 
      new AnnuityCouponFixedDefinition(CPN_FIXED_2_DEFINITION, NYC);
  @SuppressWarnings("unchecked")
  private static final AnnuityDefinition<? extends CouponDefinition> IBOR_LEG_2_DEFINITION = (AnnuityDefinition<? extends CouponDefinition>)
      new FloatingAnnuityDefinitionBuilder().payer(!PAYER_2).notional(NOTIONAL_PROV_1).startDate(EFFECTIVE_DATE_2).endDate(MATURITY_DATE_2).
          index(USDLIBOR3M).accrualPeriodFrequency(USDLIBOR3M.getTenor()).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
          resetDateAdjustmentParameters(ADJUSTED_DATE_LIBOR).accrualPeriodParameters(ADJUSTED_DATE_LIBOR).
          dayCount(USDLIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_ADJ_LIBOR).currency(USDLIBOR3M.getCurrency()).
          build();
  private static final SwapCouponFixedCouponDefinition IRS_2_DEFINITION = 
      new SwapCouponFixedCouponDefinition(FIXED_LEG_2_DEFINITION, IBOR_LEG_2_DEFINITION);
  /** Swap LIBOR3M 3 **/
  private static final PaymentDefinition[] PAYMENT_LEG_3_DEFINITION = new FixedAnnuityDefinitionBuilder().
      payer(PAYER_3).currency(USD6MLIBOR3M.getCurrency()).notional(NOTIONAL_PROV_1).startDate(EFFECTIVE_DATE_3).
      endDate(MATURITY_DATE_3).dayCount(USD6MLIBOR3M.getFixedLegDayCount()).
      accrualPeriodFrequency(USD6MLIBOR3M.getFixedLegPeriod()).rate(FIXED_RATE_3).accrualPeriodParameters(ADJUSTED_DATE_LIBOR).
      build().getPayments();
  private static final CouponFixedDefinition[] CPN_FIXED_3_DEFINITION = 
      new CouponFixedDefinition[PAYMENT_LEG_3_DEFINITION.length];
  static {
    for (int loopcpn = 0; loopcpn < PAYMENT_LEG_3_DEFINITION.length; loopcpn++) {
      CPN_FIXED_3_DEFINITION[loopcpn] = (CouponFixedDefinition) PAYMENT_LEG_3_DEFINITION[loopcpn];
    }
  }
  private static final AnnuityCouponFixedDefinition FIXED_LEG_3_DEFINITION = 
      new AnnuityCouponFixedDefinition(CPN_FIXED_3_DEFINITION, NYC);
  @SuppressWarnings("unchecked")
  private static final AnnuityDefinition<? extends CouponDefinition> IBOR_LEG_3_DEFINITION = 
  (AnnuityDefinition<? extends CouponDefinition>) new FloatingAnnuityDefinitionBuilder().
      payer(!PAYER_3).
      notional(NOTIONAL_PROV_1).
      startDate(EFFECTIVE_DATE_3).
      endDate(MATURITY_DATE_3).
      index(USDLIBOR3M).
      accrualPeriodFrequency(USDLIBOR3M.getTenor()).
      rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
      resetDateAdjustmentParameters(ADJUSTED_DATE_LIBOR).
      accrualPeriodParameters(ADJUSTED_DATE_LIBOR).
      dayCount(USDLIBOR3M.getDayCount()).
      fixingDateAdjustmentParameters(OFFSET_ADJ_LIBOR).
      currency(USDLIBOR3M.getCurrency()).
      build();
  private static final SwapCouponFixedCouponDefinition IRS_3_DEFINITION = 
      new SwapCouponFixedCouponDefinition(FIXED_LEG_3_DEFINITION, IBOR_LEG_3_DEFINITION);
  /** Swap LIBOR6M 1 **/
  //TODO
  /** Swap OIS 1 **/
  private static final PaymentDefinition[] PAYMENT_OIS_LEG_1_DEFINITION = new FixedAnnuityDefinitionBuilder().
      payer(PAYER_4).currency(USD).
      notional(NOTIONAL_PROV_1).
      startDate(EFFECTIVE_DATE_4).
      endDate(MATURITY_DATE_4).
      dayCount(GENERATOR_OIS_USD.getFixedLegDayCount()).
      accrualPeriodFrequency(GENERATOR_OIS_USD.getLegsPeriod()).
      rate(FIXED_RATE_4).
      accrualPeriodParameters(ADJUSTED_DATE_FEDFUND).
      paymentDateAdjustmentParameters(OFFSET_PAY_SONIA).
      build().getPayments();
  private static final CouponFixedDefinition[] CPN_FIXED_OIS_1_DEFINITION = 
      new CouponFixedDefinition[PAYMENT_OIS_LEG_1_DEFINITION.length];
  static {
    for (int loopcpn = 0; loopcpn < PAYMENT_OIS_LEG_1_DEFINITION.length; loopcpn++) {
      CPN_FIXED_OIS_1_DEFINITION[loopcpn] = (CouponFixedDefinition) PAYMENT_OIS_LEG_1_DEFINITION[loopcpn];
    }
  }
  private static final AnnuityCouponFixedDefinition FIXED_OIS_LEG_1_DEFINITION = 
      new AnnuityCouponFixedDefinition(CPN_FIXED_OIS_1_DEFINITION, NYC);
  /** ON leg */
  @SuppressWarnings("unchecked")
  private static final AnnuityDefinition<? extends CouponDefinition> ON_LEG_1_DEFINITION = 
  (AnnuityDefinition<? extends CouponDefinition>) new FloatingAnnuityDefinitionBuilder().
      payer(!PAYER_4).
      notional(NOTIONAL_PROV_1).
      startDate(EFFECTIVE_DATE_4).
      endDate(MATURITY_DATE_4).
      index(USDFEDFUND).
      accrualPeriodFrequency(GENERATOR_OIS_USD.getLegsPeriod()).
      rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
      resetDateAdjustmentParameters(ADJUSTED_DATE_FEDFUND).
      accrualPeriodParameters(ADJUSTED_DATE_FEDFUND).
      dayCount(USDFEDFUND.getDayCount()).
      fixingDateAdjustmentParameters(OFFSET_FIX_SONIA).
      currency(USD).
      compoundingMethod(CompoundingMethod.FLAT).
      build();
  private static final SwapCouponFixedCouponDefinition OIS_1_DEFINITION = 
      new SwapCouponFixedCouponDefinition(FIXED_OIS_LEG_1_DEFINITION, ON_LEG_1_DEFINITION);

  /** Curves and fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_FIXED_IBOR_USD3M_WITHOUT_TODAY = 
      RecentDataSetsMulticurveStandardUsd.fixingUsdLibor3MWithoutLast();
  private static final ZonedDateTimeDoubleTimeSeries TS_FIXED_ON_USD_WITHOUT_TODAY = 
      RecentDataSetsMulticurveStandardUsd.fixingUsdOnWithoutLast();

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
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = 
      PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<MulticurveProviderInterface> PSC = 
      new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final MarketQuoteSensitivityBlockCalculator<MulticurveProviderInterface> MQSBC = 
      new MarketQuoteSensitivityBlockCalculator<>(PSC);

  private static final double BP1 = 1.0E-4;

  final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> m_stdCurveBundle; 
  final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> m_cashImmHedgeCurveBundle;
  final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> m_cashOisFedFundHedgeCurveBundle; 
  final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> m_cashOisHedgeCurveBundle;

  public SwapRiskAnalysisJuly16Usd() {
    m_stdCurveBundle = UsdDatasetJuly16.getStandardCurveBundle(VALUATION_DATE, true, 
        UsdDatasetJuly16.INTERPOLATOR_LINEAR);

    m_cashImmHedgeCurveBundle = UsdDatasetJuly16.getHedgeCurveBundle(VALUATION_DATE, m_stdCurveBundle.getFirst(), 
        UsdDatasetJuly16.INTERPOLATOR_LINEAR, 0, 48, 0, 0);

    m_cashOisFedFundHedgeCurveBundle = UsdDatasetJuly16.getHedgeCurveBundle(VALUATION_DATE, m_stdCurveBundle.getFirst(), 
        UsdDatasetJuly16.INTERPOLATOR_LINEAR, 1, 0, 14, 17);

    m_cashOisHedgeCurveBundle = UsdDatasetJuly16.getHedgeCurveBundle(VALUATION_DATE, m_stdCurveBundle.getFirst(), 
        UsdDatasetJuly16.INTERPOLATOR_LINEAR, 1, 0, 31, 0);
  }
  
  @Test
  public void compareStdVsImmCurves() {

    System.out.println("Swap name,PV STD,PV IMM,PV OISFF,PV OIS,PAR STD,PAR IMM,PAR OISFF,PAR OIS");
    System.out.println("IRS_1," 
        + String.valueOf(IRS_1.accept(PVDC, m_stdCurveBundle.getFirst()).getAmount(Currency.USD)) + ","
        + String.valueOf(IRS_1.accept(PVDC, m_cashImmHedgeCurveBundle.getFirst()).getAmount(Currency.USD)) + ","
        + String.valueOf(IRS_1.accept(PVDC, m_cashOisFedFundHedgeCurveBundle.getFirst()).getAmount(Currency.USD)) + ","
        + String.valueOf(IRS_1.accept(PVDC, m_cashOisHedgeCurveBundle.getFirst()).getAmount(Currency.USD)) + ","
        + String.valueOf(IRS_1.accept(PRDC, m_stdCurveBundle.getFirst())) + ","
        + String.valueOf(IRS_1.accept(PRDC, m_cashImmHedgeCurveBundle.getFirst())) + ","
        + String.valueOf(IRS_1.accept(PRDC, m_cashOisFedFundHedgeCurveBundle.getFirst())) + ","
        + String.valueOf(IRS_1.accept(PRDC, m_cashOisHedgeCurveBundle.getFirst())) 
        );
    System.out.println("IRS_2," 
        + String.valueOf(IRS_2.accept(PVDC, m_stdCurveBundle.getFirst()).getAmount(Currency.USD)) + ","
        + String.valueOf(IRS_2.accept(PVDC, m_cashImmHedgeCurveBundle.getFirst()).getAmount(Currency.USD)) + ","
        + String.valueOf(IRS_2.accept(PVDC, m_cashOisFedFundHedgeCurveBundle.getFirst()).getAmount(Currency.USD)) + ","
        + String.valueOf(IRS_2.accept(PVDC, m_cashOisHedgeCurveBundle.getFirst()).getAmount(Currency.USD)) + ","
        + String.valueOf(IRS_2.accept(PRDC, m_stdCurveBundle.getFirst())) + ","
        + String.valueOf(IRS_2.accept(PRDC, m_cashImmHedgeCurveBundle.getFirst())) + ","
        + String.valueOf(IRS_2.accept(PRDC, m_cashOisFedFundHedgeCurveBundle.getFirst())) + ","
        + String.valueOf(IRS_2.accept(PRDC, m_cashOisHedgeCurveBundle.getFirst())) 
        );
    System.out.println("IRS_3," 
        + String.valueOf(IRS_3.accept(PVDC, m_stdCurveBundle.getFirst()).getAmount(Currency.USD)) + ","
        + String.valueOf(IRS_3.accept(PVDC, m_cashImmHedgeCurveBundle.getFirst()).getAmount(Currency.USD)) + ","
        + String.valueOf(IRS_3.accept(PVDC, m_cashOisFedFundHedgeCurveBundle.getFirst()).getAmount(Currency.USD)) + ","
        + String.valueOf(IRS_3.accept(PVDC, m_cashOisHedgeCurveBundle.getFirst()).getAmount(Currency.USD)) + ","
        + String.valueOf(IRS_3.accept(PRDC, m_stdCurveBundle.getFirst())) + ","
        + String.valueOf(IRS_3.accept(PRDC, m_cashImmHedgeCurveBundle.getFirst())) + ","
        + String.valueOf(IRS_3.accept(PRDC, m_cashOisFedFundHedgeCurveBundle.getFirst())) + ","
        + String.valueOf(IRS_3.accept(PRDC, m_cashOisHedgeCurveBundle.getFirst())) 
        );
    System.out.println("OIS_1," 
        + String.valueOf(OIS_1.accept(PVDC, m_stdCurveBundle.getFirst()).getAmount(Currency.USD)) + ","
        + String.valueOf(OIS_1.accept(PVDC, m_cashImmHedgeCurveBundle.getFirst()).getAmount(Currency.USD)) + ","
        + String.valueOf(OIS_1.accept(PVDC, m_cashOisFedFundHedgeCurveBundle.getFirst()).getAmount(Currency.USD)) + ","
        + String.valueOf(OIS_1.accept(PVDC, m_cashOisHedgeCurveBundle.getFirst()).getAmount(Currency.USD)) + ","
        + String.valueOf(OIS_1.accept(PRDC, m_stdCurveBundle.getFirst())) + ","
        + String.valueOf(OIS_1.accept(PRDC, m_cashImmHedgeCurveBundle.getFirst())) + ","
        + String.valueOf(OIS_1.accept(PRDC, m_cashOisFedFundHedgeCurveBundle.getFirst())) + ","
        + String.valueOf(OIS_1.accept(PRDC, m_cashOisHedgeCurveBundle.getFirst())) 
        );
    
  }

  @Test
  public void compareHedgeCurves() {
    System.out.println("--- STD curve ---");
    MultipleCurrencyParameterSensitivity stdSensitivities = 
        MQSBC.fromInstrument(IRS_1, m_stdCurveBundle.getFirst(), m_stdCurveBundle.getSecond()).multipliedBy(BP1);
    ExportUtils.consolePrint(stdSensitivities, m_stdCurveBundle.getFirst());
    
    System.out.println("--- 48 IMM curve ---");
    MultipleCurrencyParameterSensitivity hedgeSensitivities = MQSBC.fromInstrument(IRS_1, 
        m_cashImmHedgeCurveBundle.getFirst(), m_cashImmHedgeCurveBundle.getSecond()).multipliedBy(BP1);
    ExportUtils.consolePrint(hedgeSensitivities, m_cashImmHedgeCurveBundle.getFirst());

    System.out.println("--- 1 Cash + 14 OIS + 17 FF curve ---");
    hedgeSensitivities = MQSBC.fromInstrument(IRS_1, m_cashOisFedFundHedgeCurveBundle.getFirst(), 
        m_cashOisFedFundHedgeCurveBundle.getSecond()).multipliedBy(BP1);
    ExportUtils.consolePrint(hedgeSensitivities, m_cashOisFedFundHedgeCurveBundle.getFirst());
 

    System.out.println("--- 1 Cash + 31 OIS curve ---");
    hedgeSensitivities = MQSBC.fromInstrument(IRS_1, m_cashOisHedgeCurveBundle.getFirst(), 
        m_cashOisHedgeCurveBundle.getSecond()).multipliedBy(BP1);
    ExportUtils.consolePrint(hedgeSensitivities, m_cashOisHedgeCurveBundle.getFirst());
 

  }

  @Test(enabled = false)
  public static void main(String[] cmdLineArgs) {
    SwapRiskAnalysisJuly16Usd runner = new SwapRiskAnalysisJuly16Usd();
    runner.compareStdVsImmCurves();
    runner.compareHedgeCurves();
  }

}
