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

import com.opengamma.analytics.financial.datasets.CalendarGBP;
import com.opengamma.analytics.financial.instrument.NotionalProvider;
import com.opengamma.analytics.financial.instrument.annuity.AdjustedDateParameters;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.annuity.CompoundingMethod;
import com.opengamma.analytics.financial.instrument.annuity.FixedAnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.annuity.FloatingAnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.annuity.OffsetAdjustedDateParameters;
import com.opengamma.analytics.financial.instrument.annuity.OffsetType;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
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
import com.opengamma.analytics.tutorial.datasets.RecentDataSetsMulticurveOisMeetingDatesGbp;
import com.opengamma.analytics.tutorial.datasets.RecentDataSetsMulticurveStandardGbp;
import com.opengamma.analytics.util.export.ExportUtils;
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
 * Examples of risk analysis for different swaps in GBP.
 * Those examples can be used for tutorials. 
 */
@Test(groups = TestGroup.UNIT)
public class SwapRiskGbpAnalysis {

  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2014, 7, 16);

  private static final Calendar LON = new CalendarGBP("LON");
  private static final GeneratorSwapFixedONMaster GENERATOR_OIS_MASTER = GeneratorSwapFixedONMaster.getInstance();
  private static final GeneratorSwapFixedON GENERATOR_OIS_GBP = GENERATOR_OIS_MASTER.getGenerator("GBP1YSONIA", LON);
  private static final IndexON GBPSONIA = GENERATOR_OIS_GBP.getIndex();
  private static final Currency GBP = Currency.GBP;
  private static final AdjustedDateParameters ADJUSTED_DATE_SONIA = new AdjustedDateParameters(LON, GENERATOR_OIS_GBP.getBusinessDayConvention());
  private static final OffsetAdjustedDateParameters OFFSET_PAY_SONIA =
      new OffsetAdjustedDateParameters(GENERATOR_OIS_GBP.getPaymentLag(), OffsetType.BUSINESS, LON, BusinessDayConventionFactory.of("Following"));
  private static final OffsetAdjustedDateParameters OFFSET_FIX_SONIA =
      new OffsetAdjustedDateParameters(0, OffsetType.BUSINESS, LON, BusinessDayConventionFactory.of("Following"));

  /** GBP Fixed v SINOA */
  private static final LocalDate EFFECTIVE_DATE_1 = LocalDate.of(2014, 11, 6);
  private static final LocalDate MATURITY_DATE_1 = LocalDate.of(2014, 12, 4);
  private static final double FIXED_RATE_1 = 0.0051875;
  private static final boolean PAYER_1 = false;
  private static final double NOTIONAL_1 = 1000000; // 1m
  private static final NotionalProvider NOTIONAL_PROV_1 = new NotionalProvider() {
    @Override
    public double getAmount(final LocalDate date) {
      return NOTIONAL_1;
    }
  };

  /** OIS GBP 1 **/
  /** Fixed leg */
  private static final PaymentDefinition[] PAYMENT_LEG_1_DEFINITION = new FixedAnnuityDefinitionBuilder().
      payer(PAYER_1).
      currency(GBPSONIA.getCurrency()).
      notional(NOTIONAL_PROV_1).
      startDate(EFFECTIVE_DATE_1).
      endDate(MATURITY_DATE_1).
      dayCount(GENERATOR_OIS_GBP.getFixedLegDayCount()).
      accrualPeriodFrequency(GENERATOR_OIS_GBP.getLegsPeriod()).
      rate(FIXED_RATE_1).
      accrualPeriodParameters(ADJUSTED_DATE_SONIA).
      paymentDateAdjustmentParameters(OFFSET_PAY_SONIA).
      build().getPayments();
  private static final CouponFixedDefinition[] CPN_FIXED_1_DEFINITION = new CouponFixedDefinition[PAYMENT_LEG_1_DEFINITION.length];
  static {
    for (int loopcpn = 0; loopcpn < PAYMENT_LEG_1_DEFINITION.length; loopcpn++) {
      CPN_FIXED_1_DEFINITION[loopcpn] = (CouponFixedDefinition) PAYMENT_LEG_1_DEFINITION[loopcpn];
    }
  }
  private static final AnnuityCouponFixedDefinition FIXED_LEG_1_DEFINITION = new AnnuityCouponFixedDefinition(CPN_FIXED_1_DEFINITION, LON);
  /** ON leg */
  @SuppressWarnings("unchecked")
  private static final AnnuityDefinition<? extends CouponDefinition> ON_LEG_1_DEFINITION = (AnnuityDefinition<? extends CouponDefinition>) new FloatingAnnuityDefinitionBuilder().
      payer(!PAYER_1).
      notional(NOTIONAL_PROV_1).
      startDate(EFFECTIVE_DATE_1).
      endDate(MATURITY_DATE_1).
      index(GBPSONIA).
      accrualPeriodFrequency(GENERATOR_OIS_GBP.getLegsPeriod()).
      rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
      resetDateAdjustmentParameters(ADJUSTED_DATE_SONIA).
      accrualPeriodParameters(ADJUSTED_DATE_SONIA).
      dayCount(GBPSONIA.getDayCount()).
      fixingDateAdjustmentParameters(OFFSET_FIX_SONIA).
      currency(GBP).
      compoundingMethod(CompoundingMethod.FLAT).
      build();
  private static final SwapCouponFixedCouponDefinition SWAP_1_DEFINITION = new SwapCouponFixedCouponDefinition(FIXED_LEG_1_DEFINITION, ON_LEG_1_DEFINITION);

  /** Curves and fixing */
  private static final ZonedDateTimeDoubleTimeSeries TS_FIXED_SONIA_WITHOUT_TODAY = RecentDataSetsMulticurveStandardGbp.fixingGbpSoniaWithoutLast();

  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_STD_PAIR =
      RecentDataSetsMulticurveStandardGbp.getCurvesGbpOisL6(VALUATION_DATE);
  private static final MulticurveProviderDiscount MULTICURVE_STD = MULTICURVE_STD_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_STD = MULTICURVE_STD_PAIR.getSecond();

  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_BOE_PAIR =
      RecentDataSetsMulticurveOisMeetingDatesGbp.getCurvesGbpOis(VALUATION_DATE);
  private static final MulticurveProviderDiscount MULTICURVE_BOE = MULTICURVE_BOE_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_BOE = MULTICURVE_BOE_PAIR.getSecond();

  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_STD_BOE_PAIR =
      RecentDataSetsMulticurveOisMeetingDatesGbp.getCurvesGbpOisWithStdInstruments(VALUATION_DATE);
  private static final MulticurveProviderDiscount MULTICURVE_STD_BOE = MULTICURVE_BOE_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_STD_BOE = MULTICURVE_BOE_PAIR.getSecond();

  private static final Annuity<?> FIXED_LEG_1 = FIXED_LEG_1_DEFINITION.toDerivative(VALUATION_DATE);
  private static final Annuity<?> ON_LEG_1 = ON_LEG_1_DEFINITION.toDerivative(VALUATION_DATE, TS_FIXED_SONIA_WITHOUT_TODAY);
  private static final Swap<? extends Payment, ? extends Payment> SWAP_1 = SWAP_1_DEFINITION.toDerivative(VALUATION_DATE,
      new ZonedDateTimeDoubleTimeSeries[] {TS_FIXED_SONIA_WITHOUT_TODAY, TS_FIXED_SONIA_WITHOUT_TODAY });

  /** Calculators **/
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<MulticurveProviderInterface> PSC = new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final MarketQuoteSensitivityBlockCalculator<MulticurveProviderInterface> MQSBC = new MarketQuoteSensitivityBlockCalculator<>(PSC);

  private static final double TOLERANCE_PV = 1.0E-2;
  //  private static final double TOLERANCE_PV_2 = 1.0E+4;
  private static final double BP1 = 1.0E-4;

  @SuppressWarnings("unused")
  @Test
  public void presentValueStdCurve() {
    MultipleCurrencyAmount pvFixed = FIXED_LEG_1.accept(PVDC, MULTICURVE_STD);
    MultipleCurrencyAmount pvIbor = ON_LEG_1.accept(PVDC, MULTICURVE_STD);
    MultipleCurrencyAmount pvSwap1Std = SWAP_1.accept(PVDC, MULTICURVE_STD);
    assertTrue("SwapRiskUsdAnalysis: present value", pvFixed.getAmount(GBP) * pvIbor.getAmount(GBP) < 0);
    assertEquals("SwapRiskUsdAnalysis: present value", pvSwap1Std.getAmount(GBP), pvFixed.getAmount(GBP) + pvIbor.getAmount(GBP), TOLERANCE_PV);
    int t = 0;
  }

  @SuppressWarnings("unused")
  @Test
  public void parRateStdCurve() {
    double pr1 = SWAP_1.accept(PRDC, MULTICURVE_STD);
    int t = 0;
  }

  @SuppressWarnings("unused")
  @Test
  public void bucketedPv01StdCurve() {
    MultipleCurrencyParameterSensitivity pvmqs1Std = MQSBC.fromInstrument(SWAP_1, MULTICURVE_STD, BLOCK_STD).multipliedBy(BP1);
    int t = 0;
  }

  @Test
  public void presentValueBoeCurve() {
    MultipleCurrencyAmount pvFixed = FIXED_LEG_1.accept(PVDC, MULTICURVE_BOE);
    MultipleCurrencyAmount pvIbor = ON_LEG_1.accept(PVDC, MULTICURVE_BOE);
    MultipleCurrencyAmount pvSwap1Std = SWAP_1.accept(PVDC, MULTICURVE_BOE);
    assertTrue("SwapRiskUsdAnalysis: present value", pvFixed.getAmount(GBP) * pvIbor.getAmount(GBP) < 0);
    assertEquals("SwapRiskUsdAnalysis: present value", pvSwap1Std.getAmount(GBP), 
        pvFixed.getAmount(GBP) + pvIbor.getAmount(GBP), TOLERANCE_PV);
    System.out.println("--- BOE PVs ---");
    System.out.println("PV fixed-rate leg," + String.valueOf(pvFixed.getAmount(GBP)));
    System.out.println("PV floating-rate leg," + String.valueOf(pvIbor.getAmount(GBP)));
    System.out.println("PV swap," + String.valueOf(pvSwap1Std.getAmount(GBP)));
  }

  @Test
  public void parRateBoeCurve() {
    double pr1 = SWAP_1.accept(PRDC, MULTICURVE_BOE);
    System.out.println("--- BOE Break-even rate ---");
    System.out.println("Par rate," + String.valueOf(pr1));
  }
  
  @Test
  public void bucketedPv0BoeCurve() {
    MultipleCurrencyParameterSensitivity pvmqs1Boe = 
        MQSBC.fromInstrument(SWAP_1, MULTICURVE_BOE, BLOCK_BOE).multipliedBy(BP1);
    ExportUtils.consolePrint(pvmqs1Boe, MULTICURVE_BOE);
  }

}
