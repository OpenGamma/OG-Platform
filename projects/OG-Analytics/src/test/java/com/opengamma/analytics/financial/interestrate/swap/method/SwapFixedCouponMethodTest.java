package com.opengamma.analytics.financial.interestrate.swap.method;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class SwapFixedCouponMethodTest {

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
  //  Ibor leg: quarterly money
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  // Swaption construction:
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, ANNUITY_TENOR, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_PAYER = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_RECEIVER = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, !FIXED_IS_PAYER, CALENDAR);
  // to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 8, 18);
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final String[] CURVES_NAME = TestsDataSetsSABR.curves1Names();
  private static final SwapFixedCoupon<Coupon> SWAP_PAYER = SWAP_DEFINITION_PAYER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwapFixedCoupon<Coupon> SWAP_RECEIVER = SWAP_DEFINITION_RECEIVER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  // Yield curves

  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();
  private static final ParRateCalculator PRC = ParRateCalculator.getInstance();
  private static final double TOLERANCE_RATE = 1.0E-10;

  @Test
  public void testAnnuityCash() {
    final double forward = 0.04;
    final double cashAnnuity = METHOD_SWAP.getAnnuityCash(SWAP_PAYER, forward);
    final double expectedAnnuity = 1.903864349337 * NOTIONAL;
    assertEquals(expectedAnnuity, cashAnnuity, 1E-2); // one cent out of 100m
  }

  @Test
  public void testPVBP() {
    final YieldAndDiscountCurve curveFunding = CURVES.getCurve(CURVES_NAME[0]);
    double expectedPVBP = 0;
    for (int loopcpn = 0; loopcpn < SWAP_DEFINITION_PAYER.getFixedLeg().getPayments().length; loopcpn++) {
      expectedPVBP += Math.abs(SWAP_DEFINITION_PAYER.getFixedLeg().getNthPayment(loopcpn).getPaymentYearFraction())
          * curveFunding.getDiscountFactor(SWAP_PAYER.getFixedLeg().getNthPayment(loopcpn).getPaymentTime()) * NOTIONAL;
    }
    double pvbp = METHOD_SWAP.presentValueBasisPoint(SWAP_PAYER, CURVES);
    assertEquals(expectedPVBP, pvbp, 1E-2); // one cent out of 100m
    pvbp = METHOD_SWAP.presentValueBasisPoint(SWAP_PAYER, curveFunding);
    assertEquals(expectedPVBP, pvbp, 1E-2); // one cent out of 100m
  }

  @Test
  public void testPVBPSensitivity() {

    final double eps = 1e-8;
    final int nbPayDate = SWAP_PAYER.getFixedLeg().getPayments().length;
    final YieldAndDiscountCurve curveFunding = CURVES.getCurve(CURVES_NAME[0]);

    // 2. Funding curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    final String[] bumpedCurvesName = {bumpedCurveName, CURVES_NAME[1] };
    final SwapFixedCoupon<Coupon> swapBumpedFunding = SWAP_DEFINITION_PAYER.toDerivative(REFERENCE_DATE, bumpedCurvesName);
    final double[] yieldsFunding = new double[nbPayDate + 1];
    final double[] nodeTimes = new double[nbPayDate + 1];
    yieldsFunding[0] = curveFunding.getInterestRate(0.0);
    for (int i = 0; i < nbPayDate; i++) {
      nodeTimes[i + 1] = SWAP_PAYER.getFixedLeg().getNthPayment(i).getPaymentTime();
      yieldsFunding[i + 1] = curveFunding.getInterestRate(nodeTimes[i + 1]);
    }
    final YieldAndDiscountCurve tempCurveFunding = YieldCurve.from(InterpolatedDoublesCurve.fromSorted(nodeTimes, yieldsFunding, new LinearInterpolator1D()));
    final YieldCurveBundle curvesNotBumped = new YieldCurveBundle();
    curvesNotBumped.addAll(CURVES);
    curvesNotBumped.setCurve("Bumped Curve", tempCurveFunding);
    final double pvbp = METHOD_SWAP.presentValueBasisPoint(SWAP_PAYER, curvesNotBumped);
    final InterestRateCurveSensitivity pvbpDr = METHOD_SWAP.presentValueBasisPointCurveSensitivity(SWAP_PAYER, curvesNotBumped);

    final List<DoublesPair> tempFunding = pvbpDr.getSensitivities().get(CURVES_NAME[0]);
    for (int i = 0; i < nbPayDate; i++) {
      final YieldAndDiscountCurve bumpedCurve = tempCurveFunding.withSingleShift(nodeTimes[i + 1], eps);
      final YieldCurveBundle curvesBumped = new YieldCurveBundle();
      curvesBumped.addAll(CURVES);
      curvesBumped.setCurve("Bumped Curve", bumpedCurve);
      final double bumpedpvbp = METHOD_SWAP.presentValueBasisPoint(swapBumpedFunding, curvesBumped);
      final double res = (bumpedpvbp - pvbp) / eps;
      final DoublesPair pair = tempFunding.get(i);
      assertEquals("Node " + i, nodeTimes[i + 1], pair.getFirst(), 1E-8);
      assertEquals("Node " + i, res, pair.getSecond(), 5.0);
    }
  }

  @Test
  public void testCouponEquivalent() {
    final YieldAndDiscountCurve curveFunding = CURVES.getCurve(CURVES_NAME[0]);
    // Constant rate
    final double pvbp = METHOD_SWAP.presentValueBasisPoint(SWAP_PAYER, CURVES);
    double couponEquiv = METHOD_SWAP.couponEquivalent(SWAP_PAYER, pvbp, CURVES);
    assertEquals(RATE, couponEquiv, 1E-10);
    couponEquiv = METHOD_SWAP.couponEquivalent(SWAP_PAYER, CURVES);
    assertEquals(RATE, couponEquiv, 1E-10);
    // Non-constant rate
    final AnnuityCouponFixed annuity = SWAP_PAYER.getFixedLeg();
    final CouponFixed[] coupon = new CouponFixed[annuity.getNumberOfPayments()];
    for (int loopcpn = 0; loopcpn < annuity.getNumberOfPayments(); loopcpn++) {
      // Step-up by 10bps
      coupon[loopcpn] = new CouponFixed(CUR, annuity.getNthPayment(loopcpn).getPaymentTime(), CURVES_NAME[0], annuity.getNthPayment(loopcpn).getPaymentYearFraction(), NOTIONAL
          * (FIXED_IS_PAYER ? -1 : 1), RATE + loopcpn * 0.001);
    }
    final AnnuityCouponFixed annuityStepUp = new AnnuityCouponFixed(coupon);
    final SwapFixedCoupon<Coupon> swapStepup = new SwapFixedCoupon<>(annuityStepUp, SWAP_PAYER.getSecondLeg());
    couponEquiv = METHOD_SWAP.couponEquivalent(swapStepup, CURVES);
    double expectedCouponEquivalent = 0;
    for (int loopcpn = 0; loopcpn < annuity.getNumberOfPayments(); loopcpn++) {
      expectedCouponEquivalent += Math.abs(annuityStepUp.getNthPayment(loopcpn).getAmount()) * curveFunding.getDiscountFactor(SWAP_PAYER.getFixedLeg().getNthPayment(loopcpn).getPaymentTime());
    }
    expectedCouponEquivalent /= pvbp;
    assertEquals(expectedCouponEquivalent, couponEquiv, 1E-10);
  }

  @Test
  /**
   * Tests the par rate calculator for the swaps.
   */
  public void parRate() {
    final double ratePayer = SWAP_PAYER.accept(PRC, CURVES);
    final double rateReceiver = SWAP_RECEIVER.accept(PRC, CURVES);
    assertEquals("Par Rate swap", ratePayer, rateReceiver, TOLERANCE_RATE);
    final double ratePayer2 = PRC.visitFixedCouponSwap(SWAP_PAYER, FIXED_DAY_COUNT, CURVES);
    final double rateReceiver2 = PRC.visitFixedCouponSwap(SWAP_RECEIVER, FIXED_DAY_COUNT, CURVES);
    assertEquals("Par Rate swap", ratePayer2, rateReceiver2, TOLERANCE_RATE);
    assertEquals("Par Rate swap", ratePayer2, rateReceiver, TOLERANCE_RATE);
  }
}
