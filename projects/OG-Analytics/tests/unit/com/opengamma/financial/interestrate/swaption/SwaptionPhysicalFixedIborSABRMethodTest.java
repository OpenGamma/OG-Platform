package com.opengamma.financial.interestrate.swaption;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.SwapFixedIborMethod;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.financial.model.option.definition.SABRInterestRateParameter;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

public class SwaptionPhysicalFixedIborSABRMethodTest {

  // Swaption description
  private static final ZonedDateTime EXPIRY_DATE = DateUtil.getUTCDate(2014, 3, 18);
  private static final boolean IS_LONG = true;
  private static final int SETTLEMENT_DAYS = 2;
  // Swap 5Y description
  private static final Currency CUR = Currency.USD;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final int ANNUITY_TENOR_YEAR = 5;
  private static final Period ANNUITY_TENOR = Period.ofYears(ANNUITY_TENOR_YEAR);
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, BUSINESS_DAY, CALENDAR, SETTLEMENT_DAYS);
  private static final double NOTIONAL = 100000000; //100m
  //  Fixed leg: Semi-annual bond
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final AnnuityCouponFixedDefinition FIXED_ANNUITY_PAYER = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, FIXED_PAYMENT_PERIOD, CALENDAR, FIXED_DAY_COUNT,
      BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, FIXED_IS_PAYER);
  private static final AnnuityCouponFixedDefinition FIXED_ANNUITY_RECEIVER = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, FIXED_PAYMENT_PERIOD, CALENDAR, FIXED_DAY_COUNT,
      BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, !FIXED_IS_PAYER);
  //  Ibor leg: quarterly money
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final IborIndex INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  private static final AnnuityCouponIborDefinition IBOR_ANNUITY_RECEIVER = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, INDEX, !FIXED_IS_PAYER);
  private static final AnnuityCouponIborDefinition IBOR_ANNUITY_PAYER = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, INDEX, FIXED_IS_PAYER);
  // Swaption construction: All combinations
  private static final SwapFixedIborDefinition SWAP_DEFINITION_PAYER = new SwapFixedIborDefinition(FIXED_ANNUITY_PAYER, IBOR_ANNUITY_RECEIVER);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_RECEIVER = new SwapFixedIborDefinition(FIXED_ANNUITY_RECEIVER, IBOR_ANNUITY_PAYER);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_DEFINITION_LONG_PAYER = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_PAYER, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_DEFINITION_LONG_RECEIVER = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_RECEIVER, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_DEFINITION_SHORT_PAYER = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_PAYER, !IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_DEFINITION_SHORT_RECEIVER = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_RECEIVER, !IS_LONG);
  // to derivatives
  private static final LocalDate REFERENCE_DATE = LocalDate.of(2008, 8, 18);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final FixedCouponSwap<Payment> SWAP_PAYER = SWAP_DEFINITION_PAYER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final FixedCouponSwap<Payment> SWAP_RECEIVER = SWAP_DEFINITION_RECEIVER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_LONG_PAYER = SWAPTION_DEFINITION_LONG_PAYER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_LONG_RECEIVER = SWAPTION_DEFINITION_LONG_RECEIVER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_SHORT_PAYER = SWAPTION_DEFINITION_SHORT_PAYER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_SHORT_RECEIVER = SWAPTION_DEFINITION_SHORT_RECEIVER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  // Calculators
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  // Pricing functions
  private static final BlackPriceFunction BLACK_FUNCTION = new BlackPriceFunction();

  @Test
  public void testPresentValueSABRParameters() {
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    SABRInterestRateParameter sabrParameter = TestsDataSets.createSABR1();
    SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    // Swaption pricing.
    double priceLongPayer = PVC.visit(SWAPTION_LONG_PAYER, sabrBundle);
    double priceShortPayer = PVC.visit(SWAPTION_SHORT_PAYER, sabrBundle);
    double priceLongReceiver = PVC.visit(SWAPTION_LONG_RECEIVER, sabrBundle);
    double priceShortReceiver = PVC.visit(SWAPTION_SHORT_RECEIVER, sabrBundle);
    // From previous run
    double expectedPriceLongPayer = 5328188.209;
    assertEquals(expectedPriceLongPayer, priceLongPayer, 1E-2);
    double pvbp = SwapFixedIborMethod.presentValueBasisPoint(SWAP_PAYER, curves.getCurve(FUNDING_CURVE_NAME));
    double forward = PRC.visit(SWAP_PAYER, curves);
    double maturity = SWAP_PAYER.getFirstLeg().getNthPayment(SWAP_PAYER.getFirstLeg().getNumberOfPayments() - 1).getPaymentTime() - SWAPTION_LONG_PAYER.getSettlementTime();
    assertEquals(maturity, ANNUITY_TENOR_YEAR, 1E-2);
    double volatility = sabrParameter.getVolatility(SWAPTION_LONG_PAYER.getTimeToExpiry(), maturity, RATE, forward);
    BlackFunctionData data = new BlackFunctionData(forward, pvbp, volatility);
    Function1D<BlackFunctionData, Double> func = BLACK_FUNCTION.getPriceFunction(SWAPTION_LONG_PAYER);
    double expectedPrice = func.evaluate(data);
    assertEquals(expectedPrice, priceLongPayer, 1E-2);
    // Long/Short parity
    assertEquals(priceLongPayer, -priceShortPayer, 1E-2);
    assertEquals(priceLongReceiver, -priceShortReceiver, 1E-2);
    // Payer/Receiver parity
    double priceSwapPayer = PVC.visit(SWAP_PAYER, curves);
    double priceSwapReceiver = PVC.visit(SWAP_RECEIVER, curves);
    assertEquals(priceSwapPayer, priceLongPayer + priceShortReceiver, 1E-2);
    assertEquals(priceSwapReceiver, priceLongReceiver + priceShortPayer, 1E-2);
  }

  @Test
  public void testPresentValueSensitivitySABRParameters() {
    YieldCurveBundle curves = TestsDataSets.createCurves1();
    SABRInterestRateParameter sabrParameter = TestsDataSets.createSABR1();
    SABRInterestRateDataBundle sabrBundle = new SABRInterestRateDataBundle(sabrParameter, curves);
    SwaptionPhysicalFixedIborSABRMethod method = new SwaptionPhysicalFixedIborSABRMethod();
    // Swaption sensitivity
    PresentValueSensitivity pvsLongPayer = method.presentValueSensitivity(SWAPTION_LONG_PAYER, sabrBundle);
    PresentValueSensitivity pvsShortPayer = method.presentValueSensitivity(SWAPTION_SHORT_PAYER, sabrBundle);
    // Long/short parity
    PresentValueSensitivity pvsShortPayer_1 = pvsShortPayer.multiply(-1);
    assertEquals(pvsLongPayer.getSensitivity(), pvsShortPayer_1.getSensitivity());

    PresentValueSensitivity test = pvsLongPayer.clean();
  }
}
