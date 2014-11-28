/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.tutorial.analysis.swaption;

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
import com.opengamma.analytics.financial.interestrate.PresentValueSABRSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.SwaptionSurfaceSensitivityNodeCalculator;
import com.opengamma.analytics.financial.interestrate.datasets.StandardDataSetsSABRSwaptionUSD;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.sensitivity.PresentValueSwaptionSurfaceSensitivity;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionPhysicalFixedIborNormalMethod;
import com.opengamma.analytics.financial.interestrate.swaption.provider.SwaptionPhysicalFixedIborSABRMethod;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalswaption.PresentValueCurveSensitivityNormalSwaptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalswaption.PresentValueNormalSwaptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueCurveSensitivitySABRSwaptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSensitivitySABRSwaptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.sabrswaption.PresentValueSABRSwaptionCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.NormalDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.NormalSwaptionExpiryTenorProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.NormalSwaptionProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.analytics.tutorial.datasets.RecentDataSetsMulticurveStandardUsd;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
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
  private static final SwaptionPhysicalFixedIborSABRMethod METHOD_SWPT_SABR = 
      SwaptionPhysicalFixedIborSABRMethod.getInstance();
  private static final SwaptionPhysicalFixedIborNormalMethod METHOD_SWPT_NORMAL = 
      SwaptionPhysicalFixedIborNormalMethod.getInstance();
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = 
      PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
  private static final PresentValueSABRSwaptionCalculator PVSSC = PresentValueSABRSwaptionCalculator.getInstance();
  private static final PresentValueCurveSensitivitySABRSwaptionCalculator PVCSSSC = 
      PresentValueCurveSensitivitySABRSwaptionCalculator.getInstance();
  private static final PresentValueNormalSwaptionCalculator PVNSC = PresentValueNormalSwaptionCalculator.getInstance();
  private static final PresentValueCurveSensitivityNormalSwaptionCalculator PVCSNSC = 
      PresentValueCurveSensitivityNormalSwaptionCalculator.getInstance();
  private static final ParameterSensitivityParameterCalculator<MulticurveProviderInterface> PSDC = 
      new ParameterSensitivityParameterCalculator<>(PVCSDC);
  private static final ParameterSensitivityParameterCalculator<NormalSwaptionProviderInterface> PSNSC = 
      new ParameterSensitivityParameterCalculator<>(PVCSNSC);
      private static final ParameterSensitivityParameterCalculator<SABRSwaptionProviderInterface> PSSSC = 
          new ParameterSensitivityParameterCalculator<>(PVCSSSC);
  private static final MarketQuoteSensitivityBlockCalculator<MulticurveProviderInterface> MQSBDC = 
      new MarketQuoteSensitivityBlockCalculator<>(PSDC);
  private static final MarketQuoteSensitivityBlockCalculator<NormalSwaptionProviderInterface> MQSBNSC = 
      new MarketQuoteSensitivityBlockCalculator<>(PSNSC);
  private static final MarketQuoteSensitivityBlockCalculator<SABRSwaptionProviderInterface> MQSBSSC =
      new MarketQuoteSensitivityBlockCalculator<>(PSSSC);
  private static final PresentValueSABRSensitivitySABRSwaptionCalculator PVSSSSC =
      PresentValueSABRSensitivitySABRSwaptionCalculator.getInstance();
  private static final SwaptionSurfaceSensitivityNodeCalculator SSSNC =
      new SwaptionSurfaceSensitivityNodeCalculator();

  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final GeneratorSwapFixedIborMaster GENERATOR_IRS_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_IRS_MASTER.getGenerator("USD6MLIBOR3M", NYC);
  private static final IborIndex USDLIBOR3M = USD6MLIBOR3M.getIborIndex();
  private static final AdjustedDateParameters ADJUSTED_DATE_LIBOR = new AdjustedDateParameters(NYC, USD6MLIBOR3M.getBusinessDayConvention());
  private static final OffsetAdjustedDateParameters OFFSET_ADJ_LIBOR =
      new OffsetAdjustedDateParameters(-2, OffsetType.BUSINESS, NYC, USD6MLIBOR3M.getBusinessDayConvention());


  private static final double NOTIONAL = 1_000_000;

  /** Curves and fixing */
  private static final SABRInterestRateParameters SABR_PARAMETER = StandardDataSetsSABRSwaptionUSD.createSABR1();
  private static final InterpolatedDoublesSurface NORMAL_SURFACE_SWAPTION_EXP_TENOR = 
      NormalDataSets.normalSurfaceSwaptionExpiryTenor();
  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_STD_PAIR =
      RecentDataSetsMulticurveStandardUsd.getCurvesUSDOisL1L3L6_20140728(VALUATION_DATE);
  private static final MulticurveProviderDiscount MULTICURVE_STD = MULTICURVE_STD_PAIR.getFirst();
  private static final CurveBuildingBlockBundle BLOCK_STD = MULTICURVE_STD_PAIR.getSecond();
  private static final SABRSwaptionProviderDiscount MULTICURVE_STD_SABR = 
      new SABRSwaptionProviderDiscount(MULTICURVE_STD, SABR_PARAMETER, USD6MLIBOR3M);  
  private static final NormalSwaptionExpiryTenorProvider MULTICURVE_STD_NORMAL = 
      new NormalSwaptionExpiryTenorProvider(MULTICURVE_STD, NORMAL_SURFACE_SWAPTION_EXP_TENOR, USD6MLIBOR3M);

  private static final Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> MULTICURVE_NEG_PAIR =
      RecentDataSetsMulticurveStandardUsd.getCurvesUSDOisL1L3L6_Negative(VALUATION_DATE);
  private static final MulticurveProviderDiscount MULTICURVE_NEG = MULTICURVE_NEG_PAIR.getFirst();
  private static final NormalSwaptionExpiryTenorProvider MULTICURVE_NEG_NORMAL = 
      new NormalSwaptionExpiryTenorProvider(MULTICURVE_NEG, NORMAL_SURFACE_SWAPTION_EXP_TENOR, USD6MLIBOR3M);
  
  private static final ZonedDateTimeDoubleTimeSeries TS_FIXED_IBOR_USD3M_WITHOUT_TODAY = 
      RecentDataSetsMulticurveStandardUsd.fixingUsdLibor3MWithoutLast();    
  private static final double BP1 = 1.0E-4;

  /** SWAPTION 1 : USD Fixed v USDLIBOR3M */
  private static final ZonedDateTime EXPIRATION_DATE_1 = DateUtils.getUTCDate(2016, 7, 14);
  private static final LocalDate EFFECTIVE_DATE_1 = LocalDate.of(2016, 7, 18);
  private static final LocalDate MATURITY_DATE_1 = LocalDate.of(2026, 7, 18);
  private static final double FIXED_RATE_1 = 0.0300;
  private static final boolean PAYER_1 = false;
  private static final boolean LONG_1 = true;
  private static final SwapFixedIborDefinition IRS_R_1_DEFINITION = 
      swap(EFFECTIVE_DATE_1, MATURITY_DATE_1, FIXED_RATE_1, NOTIONAL, PAYER_1);
  private static final SwaptionPhysicalFixedIborDefinition SWPT_R_L_1_DEFINITION =
      SwaptionPhysicalFixedIborDefinition.from(EXPIRATION_DATE_1, IRS_R_1_DEFINITION, PAYER_1, LONG_1); // Long Receiver
  

/** SWAPTION 2 : USD Fixed v USDLIBOR3M */
private static final ZonedDateTime EXPIRATION_DATE_2 = DateUtils.getUTCDate(2016, 1, 18);
private static final LocalDate EFFECTIVE_DATE_2 = LocalDate.of(2016, 1, 20);
private static final LocalDate MATURITY_DATE_2 = LocalDate.of(2019, 1, 20);
private static final double FIXED_RATE_2 = -0.0005;
private static final boolean PAYER_2 = false;
private static final boolean LONG_2 = true;
private static final SwapFixedIborDefinition IRS_R_2_DEFINITION = 
    swap(EFFECTIVE_DATE_2, MATURITY_DATE_2, FIXED_RATE_2, NOTIONAL, PAYER_2);
private static final SwaptionPhysicalFixedIborDefinition SWPT_R_L_2_DEFINITION =
    SwaptionPhysicalFixedIborDefinition.from(EXPIRATION_DATE_2, IRS_R_2_DEFINITION, PAYER_2, LONG_2); // Long Receiver

private static final Swap<? extends Payment, ? extends Payment> IRS_R_1 = IRS_R_1_DEFINITION.toDerivative(VALUATION_DATE,
    new ZonedDateTimeDoubleTimeSeries[] {TS_FIXED_IBOR_USD3M_WITHOUT_TODAY, TS_FIXED_IBOR_USD3M_WITHOUT_TODAY });
private static final SwaptionPhysicalFixedIbor SWPT_R_L_1 = SWPT_R_L_1_DEFINITION.toDerivative(VALUATION_DATE);

  private static final Swap<? extends Payment, ? extends Payment> IRS_R_2 = IRS_R_2_DEFINITION.toDerivative(VALUATION_DATE,
      new ZonedDateTimeDoubleTimeSeries[] {TS_FIXED_IBOR_USD3M_WITHOUT_TODAY, TS_FIXED_IBOR_USD3M_WITHOUT_TODAY });
  private static final SwaptionPhysicalFixedIbor SWPT_R_L_2 = SWPT_R_L_2_DEFINITION.toDerivative(VALUATION_DATE);
  
  @SuppressWarnings("unused")
  @Test
  public void swaptionStandard() {
    // Forward rate
    double prIrs1Std = IRS_R_1.accept(PRDC, MULTICURVE_STD);
    // Present value
    MultipleCurrencyAmount pvIrs1Std = IRS_R_1.accept(PVDC, MULTICURVE_STD);
    MultipleCurrencyAmount pvSwpt1StdSabr = SWPT_R_L_1.accept(PVSSC, MULTICURVE_STD_SABR);
    MultipleCurrencyAmount pvSwpt1StdNorm = SWPT_R_L_1.accept(PVNSC, MULTICURVE_STD_NORMAL);
    // Bucketed PV01
    MultipleCurrencyParameterSensitivity pvcsIrs1Std = 
        MQSBDC.fromInstrument(IRS_R_1, MULTICURVE_STD, BLOCK_STD).multipliedBy(BP1);
    MultipleCurrencyParameterSensitivity pvcsSwpt1StdSabr = 
        MQSBSSC.fromInstrument(SWPT_R_L_1, MULTICURVE_STD_SABR, BLOCK_STD).multipliedBy(BP1);
    MultipleCurrencyParameterSensitivity pvcsSwpt1StdNorm = 
        MQSBNSC.fromInstrument(SWPT_R_L_1, MULTICURVE_STD_NORMAL, BLOCK_STD).multipliedBy(BP1);
    // Implied volatility: Black vol or normal vol
    double ivSwpt1StdSabr = METHOD_SWPT_SABR.impliedVolatility(SWPT_R_L_1, MULTICURVE_STD_SABR);
    double ivSwpt1StdNorm = METHOD_SWPT_NORMAL.impliedVolatility(SWPT_R_L_1, MULTICURVE_STD_NORMAL);
    // Vega: sensitivity to SABR parameters or normal volatility
    PresentValueSABRSensitivityDataBundle pvvsSwpt1StdSabr = SWPT_R_L_1.accept(PVSSSSC, MULTICURVE_STD_SABR);
    PresentValueSwaptionSurfaceSensitivity pvvsSwpt1StdNorm = 
        METHOD_SWPT_NORMAL.presentValueVolatilitySensitivity(SWPT_R_L_2, MULTICURVE_STD_NORMAL);
  }
  
  @SuppressWarnings("unused")
  @Test
  public void swaptionNegativeStrike() {
    // Forward rate
    double prIrs2Neg = IRS_R_2.accept(PRDC, MULTICURVE_NEG);
    // Present value
    MultipleCurrencyAmount pvIrs2Neg = IRS_R_2.accept(PVDC, MULTICURVE_NEG);
    MultipleCurrencyAmount pvSwpt2NegNorm = SWPT_R_L_2.accept(PVNSC, MULTICURVE_NEG_NORMAL);
    // Bucketed PV01
    MultipleCurrencyParameterSensitivity pvcsIrs2Neg = 
        MQSBDC.fromInstrument(IRS_R_2, MULTICURVE_NEG, BLOCK_STD).multipliedBy(BP1);
    MultipleCurrencyParameterSensitivity pvcsSwpt2NegNorm = 
        MQSBNSC.fromInstrument(SWPT_R_L_2, MULTICURVE_NEG_NORMAL, BLOCK_STD).multipliedBy(BP1);
    // Vega (sensitivity to normal vol)
    PresentValueSwaptionSurfaceSensitivity pvvs2NegNorm = 
        METHOD_SWPT_NORMAL.presentValueVolatilitySensitivity(SWPT_R_L_2, MULTICURVE_NEG_NORMAL);
    PresentValueSwaptionSurfaceSensitivity pvnns = SSSNC.calculateNodeSensitivities(pvvs2NegNorm, MULTICURVE_NEG_NORMAL);
  }
  
  private static SwapFixedIborDefinition swap(final LocalDate effectiveDate, final LocalDate maturityDate, final double rate, 
      final double notional, final boolean payer) {
    NotionalProvider notionalProvider = new NotionalProvider() {
      @Override
      public double getAmount(final LocalDate date) {
        return notional;
      }
    };
    AnnuityDefinition<?> fixedGeneric = new FixedAnnuityDefinitionBuilder().
        payer(payer).currency(USD6MLIBOR3M.getCurrency()).notional(notionalProvider).startDate(effectiveDate).
        endDate(maturityDate).dayCount(USD6MLIBOR3M.getFixedLegDayCount()).
        accrualPeriodFrequency(USD6MLIBOR3M.getFixedLegPeriod()).rate(rate).
        accrualPeriodParameters(ADJUSTED_DATE_LIBOR).build();
    AnnuityCouponFixedDefinition fixegLeg = 
        new AnnuityCouponFixedDefinition((CouponFixedDefinition[])fixedGeneric.getPayments(), NYC);
    /** Ibor leg */
    AnnuityDefinition<? extends CouponDefinition> iborGeneric = 
        (AnnuityDefinition<? extends CouponDefinition>)
        new FloatingAnnuityDefinitionBuilder().payer(!payer).notional(notionalProvider).startDate(effectiveDate).
            endDate(maturityDate).index(USDLIBOR3M).accrualPeriodFrequency(USDLIBOR3M.getTenor()).
            rollDateAdjuster(RollConvention.NONE.getRollDateAdjuster(0)).
            resetDateAdjustmentParameters(ADJUSTED_DATE_LIBOR).accrualPeriodParameters(ADJUSTED_DATE_LIBOR).
            dayCount(USDLIBOR3M.getDayCount()).fixingDateAdjustmentParameters(OFFSET_ADJ_LIBOR).
            currency(USDLIBOR3M.getCurrency()).build();
    AnnuityCouponIborDefinition iborLeg =
        new AnnuityCouponIborDefinition((CouponIborDefinition[]) iborGeneric.getPayments(), USDLIBOR3M, NYC);
    SwapFixedIborDefinition irs = new SwapFixedIborDefinition(fixegLeg, iborLeg);
    return irs;
  }
  
}
