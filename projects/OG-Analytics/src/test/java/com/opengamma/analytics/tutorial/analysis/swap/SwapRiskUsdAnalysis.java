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
import com.opengamma.analytics.financial.instrument.annuity.FixedAnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.annuity.FloatingAnnuityDefinitionBuilder;
import com.opengamma.analytics.financial.instrument.annuity.OffsetAdjustedDateParameters;
import com.opengamma.analytics.financial.instrument.annuity.OffsetType;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapCouponFixedCouponDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.datasets.ComputedDataSetsMulticurveImmUsd;
import com.opengamma.analytics.financial.interestrate.datasets.RecentDataSetsMulticurveStandardUsd;
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
public class SwapRiskUsdAnalysis {

  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2014, 7, 28);

  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final GeneratorSwapFixedIborMaster GENERATOR_IRS_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_IRS_MASTER.getGenerator("USD6MLIBOR3M", NYC);
  private static final IborIndex USDLIBOR3M = USD6MLIBOR3M.getIborIndex();
  private static final Currency USD = USDLIBOR3M.getCurrency();
  private static final AdjustedDateParameters ADJUSTED_DATE_LIBOR = new AdjustedDateParameters(NYC, USD6MLIBOR3M.getBusinessDayConvention());
  private static final OffsetAdjustedDateParameters OFFSET_ADJ_LIBOR =
      new OffsetAdjustedDateParameters(-2, OffsetType.BUSINESS, NYC, USD6MLIBOR3M.getBusinessDayConvention());

  /** USD Fixed v USDLIBOR3M */
  private static final LocalDate EFFECTIVE_DATE_1 = LocalDate.of(2014, 7, 18);
  private static final LocalDate MATURITY_DATE_1 = LocalDate.of(2024, 7, 18);
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
  private static final SwapCouponFixedCouponDefinition SWAP_1_DEFINITION = new SwapCouponFixedCouponDefinition(FIXED_LEG_1_DEFINITION, IBOR_LEG_1_DEFINITION);
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
  private static final AnnuityDefinition<? extends CouponDefinition> IBOR_LEG_2_DEFINITION = (AnnuityDefinition<? extends CouponDefinition>)
      new FloatingAnnuityDefinitionBuilder().payer(!PAYER_2).notional(NOTIONAL_PROV_1).startDate(EFFECTIVE_DATE_2).endDate(MATURITY_DATE_2).
          index(USDLIBOR3M).accrualPeriodFrequency(USDLIBOR3M.getTenor()).rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
          resetDateAdjustmentParameters(ADJUSTED_DATE_LIBOR).accrualPeriodParameters(ADJUSTED_DATE_LIBOR).
          dayCount(USDLIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_ADJ_LIBOR).currency(USDLIBOR3M.getCurrency()).
          build();
  private static final SwapCouponFixedCouponDefinition SWAP_2_DEFINITION = new SwapCouponFixedCouponDefinition(FIXED_LEG_2_DEFINITION, IBOR_LEG_2_DEFINITION);
  /** Swap LIBOR6M 1 **/
  /** Swap OIS 1 **/

  /** Curves and fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_PAIR =
      RecentDataSetsMulticurveStandardUsd.getCurvesUSDOisL1L3L6(VALUATION_DATE);
  private static final MulticurveProviderDiscount MULTICURVE = MULTICURVE_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK = MULTICURVE_PAIR.getSecond();
  private static final ZonedDateTimeDoubleTimeSeries TS_FIXED_IBOR_USD3M_WITHOUT_TODAY = RecentDataSetsMulticurveStandardUsd.fixingUsdLibor3MWithoutLast();
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVEIMM_PAIR =
      ComputedDataSetsMulticurveImmUsd.getCurvesUSDOisL3(VALUATION_DATE, 40, MULTICURVE);
  private static final MulticurveProviderDiscount MULTICURVEIMM = MULTICURVEIMM_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCKIMM = MULTICURVEIMM_PAIR.getSecond();

  private static final Annuity<?> FIXED_LEG_1 = FIXED_LEG_1_DEFINITION.toDerivative(VALUATION_DATE);
  private static final Annuity<?> IBOR_LEG_1 = IBOR_LEG_1_DEFINITION.toDerivative(VALUATION_DATE, TS_FIXED_IBOR_USD3M_WITHOUT_TODAY);
  private static final Swap<? extends Payment, ? extends Payment> SWAP_1 = SWAP_1_DEFINITION.toDerivative(VALUATION_DATE,
      new ZonedDateTimeDoubleTimeSeries[] {TS_FIXED_IBOR_USD3M_WITHOUT_TODAY, TS_FIXED_IBOR_USD3M_WITHOUT_TODAY });
  private static final Swap<? extends Payment, ? extends Payment> SWAP_2 = SWAP_2_DEFINITION.toDerivative(VALUATION_DATE,
      new ZonedDateTimeDoubleTimeSeries[] {TS_FIXED_IBOR_USD3M_WITHOUT_TODAY, TS_FIXED_IBOR_USD3M_WITHOUT_TODAY });

  /** Calculators **/
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<MulticurveProviderInterface> PSC = new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final MarketQuoteSensitivityBlockCalculator<MulticurveProviderInterface> MQSBC = new MarketQuoteSensitivityBlockCalculator<>(PSC);

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_2 = 1.0E+4;
  private static final double BP1 = 1.0E-4;

  @Test
  public void presentValue() {
    MultipleCurrencyAmount pvFixed = FIXED_LEG_1.accept(PVDC, MULTICURVE);
    MultipleCurrencyAmount pvIbor = IBOR_LEG_1.accept(PVDC, MULTICURVE);
    MultipleCurrencyAmount pvSwap = SWAP_1.accept(PVDC, MULTICURVE);
    assertTrue("SwapRiskUsdAnalysis: present value", pvFixed.getAmount(USD) * pvIbor.getAmount(USD) < 0);
    assertEquals("SwapRiskUsdAnalysis: present value", pvSwap.getAmount(USD), pvFixed.getAmount(USD) + pvIbor.getAmount(USD), TOLERANCE_PV);
    MultipleCurrencyAmount pvSwap2 = SWAP_1.accept(PVDC, MULTICURVEIMM);
    assertEquals("SwapRiskUsdAnalysis: present value", pvSwap.getAmount(USD), pvSwap2.getAmount(USD), TOLERANCE_PV_2);
  }

  @SuppressWarnings("unused")
  @Test
  public void parRate() {
    double pr = SWAP_2.accept(PRDC, MULTICURVE);
    int t = 0;
  }

  @SuppressWarnings("unused")
  @Test
  public void bucketedPv01() {
    MultipleCurrencyParameterSensitivity pvmqsStd = MQSBC.fromInstrument(SWAP_1, MULTICURVE, BLOCK).multipliedBy(BP1);
    MultipleCurrencyParameterSensitivity pvmqsImm = MQSBC.fromInstrument(SWAP_1, MULTICURVEIMM, BLOCKIMM).multipliedBy(BP1);
    int t = 0;
  }

}
