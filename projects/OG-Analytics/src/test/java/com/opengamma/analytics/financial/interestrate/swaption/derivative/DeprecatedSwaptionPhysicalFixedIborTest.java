package com.opengamma.analytics.financial.interestrate.swaption.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueSABRCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.method.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRBerestyckiVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganAlternativeVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRJohnsonVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRPaulotVolatilityFunction;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class DeprecatedSwaptionPhysicalFixedIborTest {
  // Swaption description
  private static final ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2011, 3, 28);
  private static final boolean IS_LONG = true;
  // Swap 2Y description
  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Period ANNUITY_TENOR = Period.ofYears(2);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 3, 30);
  private static final double NOTIONAL = 100000000; //100m
  //  Fixed leg: Semi-annual bond
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final AnnuityCouponFixedDefinition FIXED_ANNUITY_PAYER = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, FIXED_PAYMENT_PERIOD, CALENDAR, FIXED_DAY_COUNT,
      BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, FIXED_IS_PAYER);
  private static final AnnuityCouponFixedDefinition FIXED_ANNUITY_RECEIVER = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, FIXED_PAYMENT_PERIOD, CALENDAR, FIXED_DAY_COUNT,
      BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, !FIXED_IS_PAYER);
  //  Ibor leg: quarterly money
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final IborIndex INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
  private static final AnnuityCouponIborDefinition IBOR_ANNUITY_RECEIVER = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, INDEX, !FIXED_IS_PAYER, CALENDAR);
  private static final AnnuityCouponIborDefinition IBOR_ANNUITY_PAYER = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, INDEX, FIXED_IS_PAYER, CALENDAR);
  // Swaption construction: All combinations
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, INDEX, ANNUITY_TENOR, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_PAYER = new SwapFixedIborDefinition(FIXED_ANNUITY_PAYER, IBOR_ANNUITY_RECEIVER);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_RECEIVER = new SwapFixedIborDefinition(FIXED_ANNUITY_RECEIVER, IBOR_ANNUITY_PAYER);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_DEFINITION_LONG_PAYER = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_PAYER, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_DEFINITION_LONG_RECEIVER = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_RECEIVER, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_DEFINITION_SHORT_PAYER = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_PAYER, !IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_DEFINITION_SHORT_RECEIVER = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_RECEIVER, !IS_LONG);
  // to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2008, 8, 18);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME };
  private static final SwapFixedCoupon<Coupon> SWAP_PAYER = SWAP_DEFINITION_PAYER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwapFixedCoupon<Coupon> SWAP_RECEIVER = SWAP_DEFINITION_RECEIVER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_LONG_PAYER = SWAPTION_DEFINITION_LONG_PAYER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_LONG_RECEIVER = SWAPTION_DEFINITION_LONG_RECEIVER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_SHORT_PAYER = SWAPTION_DEFINITION_SHORT_PAYER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_SHORT_RECEIVER = SWAPTION_DEFINITION_SHORT_RECEIVER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  // Yield curves
  private static final YieldAndDiscountCurve CURVE_5 = YieldCurve.from(ConstantDoublesCurve.from(0.05));
  private static final YieldAndDiscountCurve CURVE_4 = YieldCurve.from(ConstantDoublesCurve.from(0.04));
  // Calculators
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  private static final PresentValueSABRCalculator PVC = PresentValueSABRCalculator.getInstance();
  // Volatility and pricing functions
  private static final SABRHaganVolatilityFunction SABR_FUNCTION = new SABRHaganVolatilityFunction();
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();
  private static final DayCount DAY_COUNT_STANDARD = DayCounts.THIRTY_U_360;
  //Interpolation method
  private static final LinearInterpolator1D LINEAR = new LinearInterpolator1D();
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();

  @Test
  /**
   * Tests the equal and hashCode methods.
   */
  public void equalHash() {
    assertTrue(SWAPTION_LONG_PAYER.equals(SWAPTION_LONG_PAYER));
    final SwaptionPhysicalFixedIbor other = SWAPTION_DEFINITION_LONG_PAYER.toDerivative(REFERENCE_DATE, CURVES_NAME);
    assertTrue(SWAPTION_LONG_PAYER.equals(other));
    assertTrue(SWAPTION_LONG_PAYER.hashCode() == other.hashCode());
    assertEquals(SWAPTION_LONG_PAYER.toString(), other.toString());
    final SwaptionPhysicalFixedIbor otherS = SWAPTION_DEFINITION_SHORT_RECEIVER.toDerivative(REFERENCE_DATE, CURVES_NAME);
    assertTrue(SWAPTION_SHORT_RECEIVER.equals(otherS));
    assertTrue(SWAPTION_SHORT_RECEIVER.hashCode() == otherS.hashCode());
    SwaptionPhysicalFixedIbor modifiedSwaption;
    modifiedSwaption = SwaptionPhysicalFixedIbor.from(SWAPTION_LONG_PAYER.getTimeToExpiry() - 0.01, SWAP_PAYER, SWAPTION_LONG_PAYER.getSettlementTime(), IS_LONG);
    assertFalse(SWAPTION_LONG_PAYER.equals(modifiedSwaption));
    modifiedSwaption = SwaptionPhysicalFixedIbor.from(SWAPTION_LONG_PAYER.getTimeToExpiry(), SWAP_PAYER, SWAPTION_LONG_PAYER.getSettlementTime() - 0.01, IS_LONG);
    assertFalse(SWAPTION_LONG_PAYER.equals(modifiedSwaption));
    modifiedSwaption = SwaptionPhysicalFixedIbor.from(SWAPTION_LONG_PAYER.getTimeToExpiry(), SWAP_PAYER, SWAPTION_LONG_PAYER.getSettlementTime(), !IS_LONG);
    assertFalse(SWAPTION_LONG_PAYER.equals(modifiedSwaption));
    final SwapFixedIborDefinition otherSwapDefinition = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, 2 * NOTIONAL, RATE, FIXED_IS_PAYER, CALENDAR);
    final SwapFixedCoupon<Coupon> otherSwap = otherSwapDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    modifiedSwaption = SwaptionPhysicalFixedIbor.from(SWAPTION_LONG_PAYER.getTimeToExpiry(), otherSwap, SWAPTION_LONG_PAYER.getSettlementTime(), IS_LONG);
    assertFalse(SWAPTION_LONG_PAYER.equals(modifiedSwaption));
    assertFalse(SWAPTION_LONG_PAYER.equals(EXPIRY_DATE));
    assertFalse(SWAPTION_LONG_PAYER.equals(null));
  }

  @Test
  public void testPriceBlack() {
    // Black price with given volatility
    final YieldCurveBundle CURVES = new YieldCurveBundle();
    CURVES.setCurve(FUNDING_CURVE_NAME, CURVE_5);
    CURVES.setCurve(FORWARD_CURVE_NAME, CURVE_4);
    final double sigmaBlack = 0.20;
    final double forward = SWAP_PAYER.accept(PRC, CURVES);
    final double pvbp = METHOD_SWAP.presentValueBasisPoint(SWAP_PAYER, CURVE_5);
    final BlackFunctionData data = new BlackFunctionData(forward, pvbp, sigmaBlack);

    final Function1D<BlackFunctionData, Double> funcLongPayer = BLACK_FUNCTION.getPriceFunction(SWAPTION_LONG_PAYER);
    final double priceLongPayer = funcLongPayer.evaluate(data) * (SWAPTION_LONG_PAYER.isLong() ? 1.0 : -1.0);
    final Function1D<BlackFunctionData, Double> funcLongReceiver = BLACK_FUNCTION.getPriceFunction(SWAPTION_LONG_RECEIVER);
    final double priceLongReceiver = funcLongReceiver.evaluate(data) * (SWAPTION_LONG_RECEIVER.isLong() ? 1.0 : -1.0);
    final Function1D<BlackFunctionData, Double> funcShortPayer = BLACK_FUNCTION.getPriceFunction(SWAPTION_SHORT_PAYER);
    final double priceShortPayer = funcShortPayer.evaluate(data) * (SWAPTION_SHORT_PAYER.isLong() ? 1.0 : -1.0);
    final Function1D<BlackFunctionData, Double> funcShortReceiver = BLACK_FUNCTION.getPriceFunction(SWAPTION_SHORT_RECEIVER);
    final double priceShortReceiver = funcShortReceiver.evaluate(data) * (SWAPTION_SHORT_RECEIVER.isLong() ? 1.0 : -1.0);
    // Long/Short parity
    assertEquals(priceLongPayer, -priceShortPayer, 1E-2);
    // Payer/Receiver parity
    final double priceSwapPayer = SWAP_PAYER.accept(PVC, CURVES);
    final double priceSwapReceiver = SWAP_RECEIVER.accept(PVC, CURVES);
    assertEquals(priceSwapPayer, priceLongPayer + priceShortReceiver, 1E-2);
    assertEquals(priceSwapReceiver, priceLongReceiver + priceShortPayer, 1E-2);
  }

  @Test
  public void testPriceSABR() {
    final YieldCurveBundle CURVES = new YieldCurveBundle();
    CURVES.setCurve(FUNDING_CURVE_NAME, CURVE_5);
    CURVES.setCurve(FORWARD_CURVE_NAME, CURVE_4);

    final double alpha = 0.05;
    final double beta = 0.5;
    final double nu = 0.50;
    final double rho = -0.25;

    final double forward = SWAP_PAYER.accept(PRC, CURVES);
    final double pvbp = METHOD_SWAP.presentValueBasisPoint(SWAP_PAYER, CURVE_5);

    final SABRFormulaData data = new SABRFormulaData(alpha, beta, rho, nu);

    final Function1D<SABRFormulaData, Double> funcSabrLongPayer = SABR_FUNCTION.getVolatilityFunction(SWAPTION_LONG_PAYER, forward);
    final double volatilityLongPayer = funcSabrLongPayer.evaluate(data);
    final BlackFunctionData dataBlackLP = new BlackFunctionData(forward, pvbp, volatilityLongPayer);
    final Function1D<BlackFunctionData, Double> funcBlackLongPayer = BLACK_FUNCTION.getPriceFunction(SWAPTION_LONG_PAYER);
    final double priceLongPayer = funcBlackLongPayer.evaluate(dataBlackLP) * (SWAPTION_LONG_PAYER.isLong() ? 1.0 : -1.0);

    final Function1D<SABRFormulaData, Double> funcSabrShortPayer = SABR_FUNCTION.getVolatilityFunction(SWAPTION_SHORT_PAYER, forward);
    final double volatilityShortPayer = funcSabrShortPayer.evaluate(data);
    final BlackFunctionData dataBlackSP = new BlackFunctionData(forward, pvbp, volatilityShortPayer);
    final Function1D<BlackFunctionData, Double> funcBlackShortPayer = BLACK_FUNCTION.getPriceFunction(SWAPTION_SHORT_PAYER);
    final double priceShortPayer = funcBlackShortPayer.evaluate(dataBlackSP) * (SWAPTION_SHORT_PAYER.isLong() ? 1.0 : -1.0);

    final Function1D<SABRFormulaData, Double> funcSabrLongReceiver = SABR_FUNCTION.getVolatilityFunction(SWAPTION_LONG_RECEIVER, forward);
    final double volatilityLongReceiver = funcSabrLongReceiver.evaluate(data);
    final BlackFunctionData dataBlackLR = new BlackFunctionData(forward, pvbp, volatilityLongReceiver);
    final Function1D<BlackFunctionData, Double> funcBlackLongReceiver = BLACK_FUNCTION.getPriceFunction(SWAPTION_LONG_RECEIVER);
    final double priceLongReceiver = funcBlackLongReceiver.evaluate(dataBlackLR) * (SWAPTION_LONG_RECEIVER.isLong() ? 1.0 : -1.0);

    final Function1D<SABRFormulaData, Double> funcSabrShortReceiver = SABR_FUNCTION.getVolatilityFunction(SWAPTION_SHORT_RECEIVER, forward);
    final double volatilityShortReceiver = funcSabrShortReceiver.evaluate(data);
    final BlackFunctionData dataBlackSR = new BlackFunctionData(forward, pvbp, volatilityShortReceiver);
    final Function1D<BlackFunctionData, Double> funcBlackShortReceiver = BLACK_FUNCTION.getPriceFunction(SWAPTION_SHORT_RECEIVER);
    final double priceShortReceiver = funcBlackShortReceiver.evaluate(dataBlackSR) * (SWAPTION_SHORT_RECEIVER.isLong() ? 1.0 : -1.0);
    // Long/Short parity
    assertEquals(priceLongPayer, -priceShortPayer, 1E-2);
    // Payer/Receiver parity
    final double priceSwapPayer = SWAP_PAYER.accept(PVC, CURVES);
    final double priceSwapReceiver = SWAP_RECEIVER.accept(PVC, CURVES);
    assertEquals(priceSwapPayer, priceLongPayer + priceShortReceiver, 1E-2);
    assertEquals(priceSwapReceiver, priceLongReceiver + priceShortPayer, 1E-2);
  }

  @Test
  public void testPriceSABRSurface() {
    // Yield curves
    final YieldCurveBundle CURVES = new YieldCurveBundle();
    CURVES.setCurve(FUNDING_CURVE_NAME, CURVE_5);
    CURVES.setCurve(FORWARD_CURVE_NAME, CURVE_4);
    // Parameter surfaces are expiry - maturity - parameter
    final InterpolatedDoublesSurface alphaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 0.0, 0.5, 1, 2, 5 }, new double[] {1, 1, 1, 1, 1, 5, 5, 5, 5, 5 }, new double[] {
        0.05, 0.05, 0.05, 0.05, 0.05, 0.06, 0.06, 0.06, 0.06, 0.06 }, new GridInterpolator2D(LINEAR, LINEAR));
    //    final VolatilitySurface alphaVolatility = new VolatilitySurface(alphaSurface);
    final InterpolatedDoublesSurface betaSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 0.0, 0.5, 1, 2, 5 }, new double[] {1, 1, 1, 1, 1, 5, 5, 5, 5, 5 }, new double[] {
        0.5,
        0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5 }, new GridInterpolator2D(LINEAR, LINEAR));
    //    final VolatilitySurface betaVolatility = new VolatilitySurface(betaSurface);
    final InterpolatedDoublesSurface rhoSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 0.0, 0.5, 1, 2, 5 }, new double[] {1, 1, 1, 1, 1, 5, 5, 5, 5, 5 }, new double[] {
        -0.25, -0.25, -0.25, -0.25, -0.25, 0.00, 0.00, 0.00, 0.00, 0.00 }, new GridInterpolator2D(LINEAR, LINEAR));
    //    final VolatilitySurface rhoVolatility = new VolatilitySurface(rhoSurface);
    final InterpolatedDoublesSurface nuSurface = InterpolatedDoublesSurface.from(new double[] {0.0, 0.5, 1, 2, 5, 0.0, 0.5, 1, 2, 5 }, new double[] {1, 1, 1, 1, 1, 5, 5, 5, 5, 5 }, new double[] {
        0.50,
        0.50, 0.50, 0.50, 0.50, 0.30, 0.30, 0.30, 0.30, 0.30 }, new GridInterpolator2D(LINEAR, LINEAR));
    //    final VolatilitySurface nuVolatility = new VolatilitySurface(nuSurface);
    final SABRInterestRateParameters sabrParameter = new SABRInterestRateParameters(alphaSurface, betaSurface, rhoSurface, nuSurface, DAY_COUNT_STANDARD);
    final SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, CURVES);
    // Swaption pricing.
    final double priceLongPayer = SWAPTION_LONG_PAYER.accept(PVC, sabrBundle);
    final double priceShortPayer = SWAPTION_SHORT_PAYER.accept(PVC, sabrBundle);
    final double priceLongReceiver = SWAPTION_LONG_RECEIVER.accept(PVC, sabrBundle);
    final double priceShortReceiver = SWAPTION_SHORT_RECEIVER.accept(PVC, sabrBundle);
    // From previous run
    final double expectedPriceLongPayer = 1918849.475;
    assertEquals(expectedPriceLongPayer, priceLongPayer, 1E-2);
    // Long/Short parity
    assertEquals(priceLongPayer, -priceShortPayer, 1E-2);
    assertEquals(priceLongReceiver, -priceShortReceiver, 1E-2);
    // Payer/Receiver parity
    final double priceSwapPayer = SWAP_PAYER.accept(PVC, CURVES);
    final double priceSwapReceiver = SWAP_RECEIVER.accept(PVC, CURVES);
    assertEquals(priceSwapPayer, priceLongPayer + priceShortReceiver, 1E-2);
    assertEquals(priceSwapReceiver, priceLongReceiver + priceShortPayer, 1E-2);
    // Non-constant fixed rate/strike
    final AnnuityCouponFixed annuity = SWAP_PAYER.getFixedLeg();
    final CouponFixed[] coupon = new CouponFixed[annuity.getNumberOfPayments()];
    for (int loopcpn = 0; loopcpn < annuity.getNumberOfPayments(); loopcpn++) {
      // Step-up by 10bps
      coupon[loopcpn] = new CouponFixed(CUR, annuity.getNthPayment(loopcpn).getPaymentTime(), FUNDING_CURVE_NAME, annuity.getNthPayment(loopcpn).getPaymentYearFraction(), NOTIONAL
          * (FIXED_IS_PAYER ? -1 : 1), RATE + loopcpn * 0.001, annuity.getNthPayment(loopcpn).getAccrualStartDate(), annuity.getNthPayment(loopcpn).getAccrualEndDate());
    }
    final AnnuityCouponFixed annuityStepUp = new AnnuityCouponFixed(coupon);
    final SwapFixedCoupon<Coupon> swapStepup = new SwapFixedCoupon<>(annuityStepUp, SWAP_PAYER.getSecondLeg());
    final SwaptionPhysicalFixedIbor swaptionStepUp = SwaptionPhysicalFixedIbor.from(SWAPTION_LONG_PAYER.getTimeToExpiry(), swapStepup, SWAPTION_LONG_PAYER.getSettlementTime(), IS_LONG);
    final double priceLongPayerStepUp = swaptionStepUp.accept(PVC, sabrBundle);
    final double expectedPriceLongPayerSteUp = 1757950.752;
    assertEquals(expectedPriceLongPayerSteUp, priceLongPayerStepUp, 1E-2);

  }

  @Test
  public void testPriceChangeSABRFormula() {
    final YieldCurveBundle curves = TestsDataSetsSABR.createCurves1();
    // SABR Hagan volatility function
    final SABRInterestRateParameters sabrParameterHagan = TestsDataSetsSABR.createSABR1(new SABRHaganVolatilityFunction());
    final SABRInterestRateDataBundle sabrHaganBundle = new SABRInterestRateDataBundle(sabrParameterHagan, curves);
    final double priceHagan = SWAPTION_LONG_PAYER.accept(PVC, sabrHaganBundle);
    // From previous run
    assertEquals(1905857.579, priceHagan, 1E-2);
    // SABR Hagan alternative volatility function
    final SABRInterestRateParameters sabrParameterHaganAlt = TestsDataSetsSABR.createSABR1(new SABRHaganAlternativeVolatilityFunction());
    final SABRInterestRateDataBundle sabrHaganAltBundle = new SABRInterestRateDataBundle(sabrParameterHaganAlt, curves);
    final double priceHaganAlt = SWAPTION_LONG_PAYER.accept(PVC, sabrHaganAltBundle);
    assertEquals(priceHagan, priceHaganAlt, 5E+2);
    // SABR Berestycki volatility function
    final SABRInterestRateParameters sabrParameterBerestycki = TestsDataSetsSABR.createSABR1(new SABRBerestyckiVolatilityFunction());
    final SABRInterestRateDataBundle sabrBerestyckiBundle = new SABRInterestRateDataBundle(sabrParameterBerestycki, curves);
    final double priceBerestycki = SWAPTION_LONG_PAYER.accept(PVC, sabrBerestyckiBundle);
    assertEquals(priceHagan, priceBerestycki, 5E+2);
    // SABR Johnson volatility function
    final SABRInterestRateParameters sabrParameterJohnson = TestsDataSetsSABR.createSABR1(new SABRJohnsonVolatilityFunction());
    final SABRInterestRateDataBundle sabrJohnsonBundle = new SABRInterestRateDataBundle(sabrParameterJohnson, curves);
    final double priceJohnson = SWAPTION_LONG_PAYER.accept(PVC, sabrJohnsonBundle);
    assertEquals(priceHagan, priceJohnson, 1E+3);
    // SABR Paulot volatility function ! Does not work well !
    final SABRInterestRateParameters sabrParameterPaulot = TestsDataSetsSABR.createSABR1(new SABRPaulotVolatilityFunction());
    final SABRInterestRateDataBundle sabrPaulotBundle = new SABRInterestRateDataBundle(sabrParameterPaulot, curves);
    final double pricePaulot = SWAPTION_LONG_PAYER.accept(PVC, sabrPaulotBundle);
    assertEquals(priceHagan, pricePaulot, 1E+4);

  }
}
