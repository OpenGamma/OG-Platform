/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.tutorial.analysis.swap;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.datasets.CalendarUSD;
import com.opengamma.analytics.financial.instrument.NotionalProvider;
import com.opengamma.analytics.financial.instrument.annuity.AdjustedDateParameters;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
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
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsSABRSwaptionUSD;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSwaptionCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderDiscount;
import com.opengamma.analytics.tutorial.datasets.RecentDataSetsMulticurveStandardUsd;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Examples of risk analysis for different swaptions in USD.
 * Those examples can be used for tutorials. 
 */
public class SwaptionUsdAnalysis {

  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2014, 7, 16);
  
  /** Calculators **/
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueSABRSwaptionCalculator PVSSC = PresentValueSABRSwaptionCalculator.getInstance();
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();

  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final GeneratorSwapFixedIborMaster GENERATOR_IRS_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_IRS_MASTER.getGenerator("USD6MLIBOR3M", NYC);
  private static final IborIndex USDLIBOR3M = USD6MLIBOR3M.getIborIndex();
  private static final Currency USD = USDLIBOR3M.getCurrency();
  private static final AdjustedDateParameters ADJUSTED_DATE_LIBOR = new AdjustedDateParameters(NYC, USD6MLIBOR3M.getBusinessDayConvention());
  private static final OffsetAdjustedDateParameters OFFSET_ADJ_LIBOR =
      new OffsetAdjustedDateParameters(-2, OffsetType.BUSINESS, NYC, USD6MLIBOR3M.getBusinessDayConvention());


  private static final double NOTIONAL = 1000000; // 1m
  private static final NotionalProvider NOTIONAL_PROV = new NotionalProvider() {
    @Override
    public double getAmount(final LocalDate date) {
      return NOTIONAL;
    }
  };

  /** Curves and fixing */
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_STD_PAIR =
      RecentDataSetsMulticurveStandardUsd.getCurvesUSDOisL1L3L6_20140728(VALUATION_DATE);
  private static final MulticurveProviderDiscount MULTICURVE_STD = MULTICURVE_STD_PAIR.getFirst();
//  private static final CurveBuildingBlockBundle BLOCK_STD = MULTICURVE_STD_PAIR.getSecond();
  private static final SABRInterestRateParameters SABR_PARAMETER = StandardDataSetsSABRSwaptionUSD.createSABR1();
  private static final SABRSwaptionProviderDiscount MULTICURVE_STD_SABR = 
      new SABRSwaptionProviderDiscount(MULTICURVE_STD, SABR_PARAMETER, USD6MLIBOR3M);  

  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_NEG_PAIR =
      RecentDataSetsMulticurveStandardUsd.getCurvesUSDOisL1L3L6_Negative(VALUATION_DATE);
  private static final MulticurveProviderDiscount MULTICURVE_NEG = MULTICURVE_NEG_PAIR.getFirst();
  
  
  private static final ZonedDateTimeDoubleTimeSeries TS_FIXED_IBOR_USD3M_WITHOUT_TODAY = 
      RecentDataSetsMulticurveStandardUsd.fixingUsdLibor3MWithoutLast();    

  /** SWAPTION 1 : USD Fixed v USDLIBOR3M */
  private static final ZonedDateTime EXPIRATION_DATE_1 = DateUtils.getUTCDate(2016, 7, 14);
  private static final LocalDate EFFECTIVE_DATE_1 = LocalDate.of(2016, 7, 18);
  private static final LocalDate MATURITY_DATE_1 = LocalDate.of(2026, 7, 18);
  private static final double FIXED_RATE_1 = 0.0300;
  private static final boolean PAYER_1 = false;
  private static final boolean LONG_1 = true;
  /** Fixed leg: Receiver */
  private static final AnnuityDefinition<?> FIXED_LEG_GEN_1_DEFINITION = new FixedAnnuityDefinitionBuilder().
      payer(PAYER_1).currency(USD6MLIBOR3M.getCurrency()).notional(NOTIONAL_PROV).startDate(EFFECTIVE_DATE_1).
      endDate(MATURITY_DATE_1).dayCount(USD6MLIBOR3M.getFixedLegDayCount()).
      accrualPeriodFrequency(USD6MLIBOR3M.getFixedLegPeriod()).rate(FIXED_RATE_1).
      accrualPeriodParameters(ADJUSTED_DATE_LIBOR).build();
  private static final AnnuityCouponFixedDefinition FIXED_LEG_1_DEFINITION = 
      new AnnuityCouponFixedDefinition((CouponFixedDefinition[])FIXED_LEG_GEN_1_DEFINITION.getPayments(), NYC);
  /** Ibor leg */
  private static final AnnuityDefinition<? extends CouponDefinition> IBOR_LEG_GEN_1_DEFINITION = 
      (AnnuityDefinition<? extends CouponDefinition>)
      new FloatingAnnuityDefinitionBuilder().payer(!PAYER_1).notional(NOTIONAL_PROV).startDate(EFFECTIVE_DATE_1).
          endDate(MATURITY_DATE_1).index(USDLIBOR3M).accrualPeriodFrequency(USDLIBOR3M.getTenor()).
          rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
          resetDateAdjustmentParameters(ADJUSTED_DATE_LIBOR).accrualPeriodParameters(ADJUSTED_DATE_LIBOR).
          dayCount(USDLIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_ADJ_LIBOR).
          currency(USDLIBOR3M.getCurrency()).build();
  private static final AnnuityCouponIborDefinition IBOR_LEG_1_DEFINITION =
      new AnnuityCouponIborDefinition((CouponIborDefinition[]) IBOR_LEG_GEN_1_DEFINITION.getPayments(), USDLIBOR3M, NYC);
  private static final SwapFixedIborDefinition IRS_1_DEFINITION = 
      new SwapFixedIborDefinition(FIXED_LEG_1_DEFINITION, IBOR_LEG_1_DEFINITION);
  private static final SwaptionPhysicalFixedIborDefinition SWPT_1_DEFINITION =
      SwaptionPhysicalFixedIborDefinition.from(EXPIRATION_DATE_1, IRS_1_DEFINITION, PAYER_1, LONG_1); // Long Receiver
  

/** SWAPTION 2 : USD Fixed v USDLIBOR3M */
private static final ZonedDateTime EXPIRATION_DATE_2 = DateUtils.getUTCDate(2015, 7, 14);
private static final LocalDate EFFECTIVE_DATE_2 = LocalDate.of(2015, 7, 16);
private static final LocalDate MATURITY_DATE_2 = LocalDate.of(2017, 7, 16);
private static final double FIXED_RATE_2 = -0.0005;
private static final boolean PAYER_2 = false;
private static final boolean LONG_2 = true;
/** Fixed leg: Receiver */
private static final AnnuityDefinition<?> FIXED_LEG_GEN_2_DEFINITION = new FixedAnnuityDefinitionBuilder().
    payer(PAYER_2).currency(USD6MLIBOR3M.getCurrency()).notional(NOTIONAL_PROV).startDate(EFFECTIVE_DATE_2).
    endDate(MATURITY_DATE_2).dayCount(USD6MLIBOR3M.getFixedLegDayCount()).
    accrualPeriodFrequency(USD6MLIBOR3M.getFixedLegPeriod()).rate(FIXED_RATE_2).
    accrualPeriodParameters(ADJUSTED_DATE_LIBOR).build();
private static final AnnuityCouponFixedDefinition FIXED_LEG_2_DEFINITION = 
    new AnnuityCouponFixedDefinition((CouponFixedDefinition[])FIXED_LEG_GEN_2_DEFINITION.getPayments(), NYC);
/** Ibor leg */
private static final AnnuityDefinition<? extends CouponDefinition> IBOR_LEG_GEN_2_DEFINITION = 
    (AnnuityDefinition<? extends CouponDefinition>)
    new FloatingAnnuityDefinitionBuilder().payer(!PAYER_2).notional(NOTIONAL_PROV).startDate(EFFECTIVE_DATE_2).
        endDate(MATURITY_DATE_2).index(USDLIBOR3M).accrualPeriodFrequency(USDLIBOR3M.getTenor()).
        rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
        resetDateAdjustmentParameters(ADJUSTED_DATE_LIBOR).accrualPeriodParameters(ADJUSTED_DATE_LIBOR).
        dayCount(USDLIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_ADJ_LIBOR).
        currency(USDLIBOR3M.getCurrency()).build();
private static final AnnuityCouponIborDefinition IBOR_LEG_2_DEFINITION =
    new AnnuityCouponIborDefinition((CouponIborDefinition[]) IBOR_LEG_GEN_2_DEFINITION.getPayments(), USDLIBOR3M, NYC);
private static final SwapFixedIborDefinition IRS_2_DEFINITION = 
    new SwapFixedIborDefinition(FIXED_LEG_2_DEFINITION, IBOR_LEG_2_DEFINITION);
private static final SwaptionPhysicalFixedIborDefinition SWPT_2_DEFINITION =
    SwaptionPhysicalFixedIborDefinition.from(EXPIRATION_DATE_2, IRS_2_DEFINITION, PAYER_2, LONG_2); // Long Receiver

private static final Swap<? extends Payment, ? extends Payment> IRS_1 = IRS_1_DEFINITION.toDerivative(VALUATION_DATE,
    new ZonedDateTimeDoubleTimeSeries[] {TS_FIXED_IBOR_USD3M_WITHOUT_TODAY, TS_FIXED_IBOR_USD3M_WITHOUT_TODAY });
private static final SwaptionPhysicalFixedIbor SWPT_1 = SWPT_1_DEFINITION.toDerivative(VALUATION_DATE);

  private static final Swap<? extends Payment, ? extends Payment> IRS_2 = IRS_2_DEFINITION.toDerivative(VALUATION_DATE,
      new ZonedDateTimeDoubleTimeSeries[] {TS_FIXED_IBOR_USD3M_WITHOUT_TODAY, TS_FIXED_IBOR_USD3M_WITHOUT_TODAY });
  private static final SwaptionPhysicalFixedIbor SWPT_2 = SWPT_2_DEFINITION.toDerivative(VALUATION_DATE);
  
  @SuppressWarnings("unused")
  @Test
  public void swaptionStandard() {
    double prIrs1Std = IRS_1.accept(PRDC, MULTICURVE_STD);
    MultipleCurrencyAmount pvIrs1Std = IRS_1.accept(PVDC, MULTICURVE_STD);
    MultipleCurrencyAmount pvSwpt1Std = SWPT_1.accept(PVSSC, MULTICURVE_STD_SABR);
    int t = 0;
  }
  
  @SuppressWarnings("unused")
  @Test
  public void swaptionNegative() {
    double prIrs1Std = IRS_2.accept(PRDC, MULTICURVE_NEG);
    MultipleCurrencyAmount pvIrs1Std = IRS_2.accept(PVDC, MULTICURVE_NEG);
//    MultipleCurrencyAmount pvSwpt1Std = SWPT_1.accept(PVSSC, MULTICURVE_SABR);
    int t = 0;
  }
  
}
