/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
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
import com.opengamma.analytics.financial.interestrate.sensitivity.PresentValueSwaptionSurfaceSensitivity;
import com.opengamma.analytics.financial.interestrate.swap.provider.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.NormalPriceFunction;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalswaption.PresentValueCurveSensitivityNormalSwaptionCalculator;
import com.opengamma.analytics.financial.provider.calculator.normalswaption.PresentValueNormalSwaptionCalculator;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.NormalDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.NormalSwaptionExpiryTenorProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.NormalSwaptionProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.normalswaption.ParameterSensitivityNormalSwaptionExpiryTenorDiscountInterpolatedFDCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.util.AssertSensitivityObjects;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing of swaptions with a normal (Bachelier) model.
 */
public class SwaptionPhysicalFixedIborNormalExpiryTenorMethodTest {

  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2014, 7, 16);

  /** Conventions */
  private static final Calendar NYC = new CalendarUSD("NYC");
  private static final GeneratorSwapFixedIborMaster GENERATOR_IRS_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_IRS_MASTER.getGenerator("USD6MLIBOR3M", NYC);
  private static final IborIndex USDLIBOR3M = USD6MLIBOR3M.getIborIndex();
  private static final Currency USD = USDLIBOR3M.getCurrency();
  private static final AdjustedDateParameters ADJUSTED_DATE_LIBOR = 
      new AdjustedDateParameters(NYC, USD6MLIBOR3M.getBusinessDayConvention());
  private static final OffsetAdjustedDateParameters OFFSET_ADJ_LIBOR =
      new OffsetAdjustedDateParameters(-2, OffsetType.BUSINESS, NYC, USD6MLIBOR3M.getBusinessDayConvention());
  /** Data */
  private static final MulticurveProviderDiscount MULTICURVE = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final InterpolatedDoublesSurface NORMAL_SURFACE_SWAPTION_EXP_TENOR = 
      NormalDataSets.normalSurfaceSwaptionExpiryTenor();
  private static final NormalSwaptionExpiryTenorProvider MULTICURVE_NEG_NORMAL = 
      new NormalSwaptionExpiryTenorProvider(MULTICURVE, NORMAL_SURFACE_SWAPTION_EXP_TENOR, USD6MLIBOR3M);
  
  /** Swaption */
  private static final ZonedDateTime EXPIRY_1_DATE = DateUtils.getUTCDate(2016, 7, 14);
  private static final LocalDate SWAP_1_EFFECTIVE_DATE = LocalDate.of(2016, 7, 18);;
  private static final Period SWAP_1_TENOR_1 = Period.ofYears(5);
  private static final LocalDate SWAP_1_MATURITY_DATE = SWAP_1_EFFECTIVE_DATE.plus(SWAP_1_TENOR_1);
  private static final double RATE_1 = 0.0150;
  private static final double NOTIONAL_1 = 10_000_000;
  private static final boolean PAYER_1 = true;
  private static final boolean LONG_1 = true;
  private static final SwapFixedIborDefinition SWAP_1_P_DEFINITION = 
      swap(SWAP_1_EFFECTIVE_DATE, SWAP_1_MATURITY_DATE, RATE_1, NOTIONAL_1, PAYER_1);
  private static final SwapFixedIborDefinition SWAP_1_R_DEFINITION = 
      swap(SWAP_1_EFFECTIVE_DATE, SWAP_1_MATURITY_DATE, RATE_1, NOTIONAL_1, !PAYER_1);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_1_P_L_DEFINITION =
      SwaptionPhysicalFixedIborDefinition.from(EXPIRY_1_DATE, SWAP_1_P_DEFINITION, PAYER_1, LONG_1);
  private static final SwaptionPhysicalFixedIbor SWAPTION_1_P_L = SWAPTION_1_P_L_DEFINITION.toDerivative(VALUATION_DATE);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_1_P_S_DEFINITION =
      SwaptionPhysicalFixedIborDefinition.from(EXPIRY_1_DATE, SWAP_1_P_DEFINITION, PAYER_1, !LONG_1);
  private static final SwaptionPhysicalFixedIbor SWAPTION_1_P_S = SWAPTION_1_P_S_DEFINITION.toDerivative(VALUATION_DATE);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_1_R_S_DEFINITION =
      SwaptionPhysicalFixedIborDefinition.from(EXPIRY_1_DATE, SWAP_1_R_DEFINITION, !PAYER_1, !LONG_1);
  private static final SwaptionPhysicalFixedIbor SWAPTION_1_R_S = SWAPTION_1_R_S_DEFINITION.toDerivative(VALUATION_DATE);
  /** Swaption with negative strike */
  private static final ZonedDateTime EXPIRY_2_DATE = DateUtils.getUTCDate(2016, 7, 14);
  private static final LocalDate SWAP_2_EFFECTIVE_DATE = LocalDate.of(2016, 7, 18);;
  private static final Period SWAP_2_TENOR_1 = Period.ofYears(5);
  private static final LocalDate SWAP_2_MATURITY_DATE = SWAP_2_EFFECTIVE_DATE.plus(SWAP_2_TENOR_1);
  private static final double RATE_2 = -0.025;
  private static final double NOTIONAL_2 = 10_000_000;
  private static final boolean PAYER_2 = false;
  private static final boolean LONG_2 = true;
  private static final SwapFixedIborDefinition SWAP_2_DEFINITION = 
      swap(SWAP_2_EFFECTIVE_DATE, SWAP_2_MATURITY_DATE, RATE_2, NOTIONAL_2, PAYER_2);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_2_DEFINITION =
      SwaptionPhysicalFixedIborDefinition.from(EXPIRY_2_DATE, SWAP_2_DEFINITION, PAYER_2, LONG_2);
  private static final SwaptionPhysicalFixedIbor SWAPTION_2 = SWAPTION_2_DEFINITION.toDerivative(VALUATION_DATE);
  
  /** Calculators and methods */
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();
  private static final ParRateDiscountingCalculator PRDC = ParRateDiscountingCalculator.getInstance();
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueNormalSwaptionCalculator PVNSC = PresentValueNormalSwaptionCalculator.getInstance();
  private static final PresentValueCurveSensitivityNormalSwaptionCalculator PVCSNSC = 
      PresentValueCurveSensitivityNormalSwaptionCalculator.getInstance();
  private static final SwaptionPhysicalFixedIborNormalMethod METHOD_SWPT_NORMAL = 
      SwaptionPhysicalFixedIborNormalMethod.getInstance();
  private static final ParameterSensitivityParameterCalculator<NormalSwaptionProviderInterface> PS = 
      new ParameterSensitivityParameterCalculator<>(PVCSNSC);
  private static final double SHIFT_FD = 1.0E-6;
  private static final ParameterSensitivityNormalSwaptionExpiryTenorDiscountInterpolatedFDCalculator PS_FD =
      new ParameterSensitivityNormalSwaptionExpiryTenorDiscountInterpolatedFDCalculator(PVNSC, SHIFT_FD);
  
  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_PV_DELTA = 1.0E+2;
  private static final double TOLERANCE_VOL = 1.0E-8;
  
  @Test
  public void impliedVolatility() {
    double volatilityExpected = NORMAL_SURFACE_SWAPTION_EXP_TENOR.getZValue(SWAPTION_1_P_L.getTimeToExpiry(), 
        SWAPTION_1_P_L.getMaturityTime());
    double volatilityComputedLong = METHOD_SWPT_NORMAL.impliedVolatility(SWAPTION_1_P_L, MULTICURVE_NEG_NORMAL);
    assertEquals("SwaptionPhysicalFixedIborNormalExpiryTenorMethod: implied vol", 
        volatilityExpected, volatilityComputedLong, TOLERANCE_VOL);
    double volatilityComputedShort = METHOD_SWPT_NORMAL.impliedVolatility(SWAPTION_1_P_S, MULTICURVE_NEG_NORMAL);
    assertEquals("SwaptionPhysicalFixedIborNormalExpiryTenorMethod: implied vol", 
        volatilityExpected, volatilityComputedShort, TOLERANCE_VOL);
  }
  
  @Test
  public void presentValue() {
    MultipleCurrencyAmount pvComputed = METHOD_SWPT_NORMAL.presentValue(SWAPTION_1_P_L, MULTICURVE_NEG_NORMAL);
    final double pvbp = METHOD_SWAP.presentValueBasisPoint(SWAPTION_1_P_L.getUnderlyingSwap(), MULTICURVE);
    final double forward = PRDC.visitFixedCouponSwap(SWAPTION_1_P_L.getUnderlyingSwap(), MULTICURVE);
    double expiry = SWAPTION_1_P_L.getTimeToExpiry();
    double volatility = METHOD_SWPT_NORMAL.impliedVolatility(SWAPTION_1_P_L, MULTICURVE_NEG_NORMAL);
    NormalFunctionData normalData = new NormalFunctionData(forward, pvbp, volatility);
    EuropeanVanillaOption option = new EuropeanVanillaOption(RATE_1, expiry, SWAPTION_1_P_L.isCall());
    NormalPriceFunction normalFunction = new NormalPriceFunction();
    Function1D<NormalFunctionData, Double> func = normalFunction.getPriceFunction(option);
    double pvExpected = func.evaluate(normalData) * (SWAPTION_1_P_L.isLong() ? 1.0 : -1.0);
    assertEquals("SwaptionPhysicalFixedIborNormalExpiryTenorMethod: present value", 
        pvExpected, pvComputed.getAmount(USD), TOLERANCE_PV);
  }
  
  @Test
  public void presentValueNegativeStrike() {
    MultipleCurrencyAmount pvComputed = METHOD_SWPT_NORMAL.presentValue(SWAPTION_2, MULTICURVE_NEG_NORMAL);
    final double pvbp = METHOD_SWAP.presentValueBasisPoint(SWAPTION_2.getUnderlyingSwap(), MULTICURVE);
    final double forward = PRDC.visitFixedCouponSwap(SWAPTION_2.getUnderlyingSwap(), MULTICURVE);
    double expiry = SWAPTION_2.getTimeToExpiry();
    double volatility = METHOD_SWPT_NORMAL.impliedVolatility(SWAPTION_2, MULTICURVE_NEG_NORMAL);
    NormalFunctionData normalData = new NormalFunctionData(forward, pvbp, volatility);
    EuropeanVanillaOption option = new EuropeanVanillaOption(RATE_2, expiry, SWAPTION_2.isCall());
    NormalPriceFunction normalFunction = new NormalPriceFunction();
    Function1D<NormalFunctionData, Double> func = normalFunction.getPriceFunction(option);
    double pvExpected = func.evaluate(normalData) * (SWAPTION_2.isLong() ? 1.0 : -1.0);
    assertEquals("SwaptionPhysicalFixedIborNormalExpiryTenorMethod: present value", 
        pvExpected, pvComputed.getAmount(USD), TOLERANCE_PV);
  }
  
  @Test
  public void presentValueLongShortParity() {
    MultipleCurrencyAmount pvComputedLong = METHOD_SWPT_NORMAL.presentValue(SWAPTION_1_P_L, MULTICURVE_NEG_NORMAL);
    MultipleCurrencyAmount pvComputedShort = METHOD_SWPT_NORMAL.presentValue(SWAPTION_1_P_S, MULTICURVE_NEG_NORMAL);
    assertEquals("SwaptionPhysicalFixedIborNormalExpiryTenorMethod: present value", 
        pvComputedLong.getAmount(USD), -pvComputedShort.getAmount(USD), TOLERANCE_PV);
  }
  
  @Test
  public void presentValuePayerReceiverParity() {
    MultipleCurrencyAmount pvComputedPayerLong = METHOD_SWPT_NORMAL.presentValue(SWAPTION_1_P_L, MULTICURVE_NEG_NORMAL);
    MultipleCurrencyAmount pvComputedReceShort = METHOD_SWPT_NORMAL.presentValue(SWAPTION_1_R_S, MULTICURVE_NEG_NORMAL);
    MultipleCurrencyAmount pvSwapPayer = SWAPTION_1_P_L.getUnderlyingSwap().accept(PVDC, MULTICURVE);
    assertEquals("SwaptionPhysicalFixedIborNormalExpiryTenorMethod: present value", 
        pvComputedPayerLong.getAmount(USD) + pvComputedReceShort.getAmount(USD), 
        pvSwapPayer.getAmount(USD), TOLERANCE_PV);
  }
  
  @Test
  public void presentValueMethodVsCalculator() {
    MultipleCurrencyAmount pvMethod = METHOD_SWPT_NORMAL.presentValue(SWAPTION_1_P_L, MULTICURVE_NEG_NORMAL);
    MultipleCurrencyAmount pvMCalculator = SWAPTION_1_P_L.accept(PVNSC, MULTICURVE_NEG_NORMAL);
    assertEquals("SwaptionPhysicalFixedIborNormalExpiryTenorMethod: present value", 
        pvMethod.getAmount(USD), pvMCalculator.getAmount(USD), TOLERANCE_PV);    
  }

  @Test
  public void presentValueCurveSensitivity() {
    MultipleCurrencyParameterSensitivity pvcsAD = PS.calculateSensitivity(SWAPTION_1_P_L, MULTICURVE_NEG_NORMAL);
    MultipleCurrencyParameterSensitivity pvcsFD = PS_FD.calculateSensitivity(SWAPTION_1_P_L, MULTICURVE_NEG_NORMAL);
    AssertSensitivityObjects.assertEquals("SwaptionPhysicalFixedIborNormalExpiryTenorMethodTest: CurveSensitivity ", 
        pvcsAD, pvcsFD, TOLERANCE_PV_DELTA);
  }

  @Test
  public void presentValueVolatilitySensitivity() {
    double shiftVol = 1.0E-6;
    InterpolatedDoublesSurface volShifted = NormalDataSets.normalSurfaceSwaptionExpiryTenor(shiftVol);
    NormalSwaptionExpiryTenorProvider multicurveVolShifted =
        new NormalSwaptionExpiryTenorProvider(MULTICURVE, volShifted, USD6MLIBOR3M);
    MultipleCurrencyAmount pv0 = METHOD_SWPT_NORMAL.presentValue(SWAPTION_1_P_L, MULTICURVE_NEG_NORMAL);
    MultipleCurrencyAmount pvS = METHOD_SWPT_NORMAL.presentValue(SWAPTION_1_P_L, multicurveVolShifted);
    double pvvsExpected = (pvS.getAmount(USD) - pv0.getAmount(USD)) / shiftVol;
    PresentValueSwaptionSurfaceSensitivity pvvsSurface =
        METHOD_SWPT_NORMAL.presentValueVolatilitySensitivity(SWAPTION_1_P_L, MULTICURVE_NEG_NORMAL);
    double pvvsComputed = pvvsSurface.getSensitivity().toSingleValue();
    assertEquals("SwaptionPhysicalFixedIborNormalExpiryTenorMethod: present value",
        pvvsExpected, pvvsComputed, TOLERANCE_PV_DELTA);
  }
  
  @Test
  public void presentValueCurveSensitivityMethodVsCalculator() {
    MultipleCurrencyMulticurveSensitivity pvcsMethod = 
        METHOD_SWPT_NORMAL.presentValueCurveSensitivity(SWAPTION_1_P_L, MULTICURVE_NEG_NORMAL);
    MultipleCurrencyMulticurveSensitivity pvcsCalculator = SWAPTION_1_P_L.accept(PVCSNSC, MULTICURVE_NEG_NORMAL);
    AssertSensitivityObjects.assertEquals("SwaptionPhysicalFixedIborNormalExpiryTenorMethodTest: CurveSensitivity ", 
        pvcsMethod, pvcsCalculator, TOLERANCE_PV_DELTA);
  }
  
  static SwapFixedIborDefinition swap(final LocalDate effectiveDate, final LocalDate maturityDate, final double rate, 
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
