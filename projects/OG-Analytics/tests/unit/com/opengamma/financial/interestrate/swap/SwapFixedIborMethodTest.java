package com.opengamma.financial.interestrate.swap;

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
import com.opengamma.financial.instrument.index.CMSIndex;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

public class SwapFixedIborMethodTest {

  // Swap 2Y description
  private static final Currency CUR = Currency.USD;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Period ANNUITY_TENOR = Period.ofYears(2);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtil.getUTCDate(2011, 3, 30);
  private static final double NOTIONAL = 100000000; //100m
  //  Fixed leg: Semi-annual bond
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  //  Ibor leg: quarterly money
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  // Swaption construction: 
  private static final CMSIndex CMS_INDEX = new CMSIndex(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, ANNUITY_TENOR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_PAYER = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER);
  // to derivatives
  private static final LocalDate REFERENCE_DATE = LocalDate.of(2010, 8, 18);
  private static final String FUNDING_CURVE_NAME = " Funding";
  private static final String FORWARD_CURVE_NAME = " Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final FixedCouponSwap<Payment> SWAP_PAYER = SWAP_DEFINITION_PAYER.toDerivative(REFERENCE_DATE, CURVES_NAME);
  // Yield curves
  private static final YieldAndDiscountCurve CURVE_5 = new YieldCurve(ConstantDoublesCurve.from(0.05));
  private static final YieldAndDiscountCurve CURVE_4 = new YieldCurve(ConstantDoublesCurve.from(0.04));

  @Test
  public void testAnnuityCash() {
    double forward = 0.04;
    double cashAnnuity = SwapFixedIborMethod.getAnnuityCash(SWAP_PAYER, forward);
    double expectedAnnuity = 1.903864349337 * NOTIONAL;
    assertEquals(expectedAnnuity, cashAnnuity, 1E-2); // one cent out of 100m
  }

  @Test
  public void testPVBP() {
    // Yield curves
    YieldCurveBundle curves = new YieldCurveBundle();
    curves.setCurve(FUNDING_CURVE_NAME, CURVE_5);
    curves.setCurve(FORWARD_CURVE_NAME, CURVE_4);
    double expectedPVBP = 0;
    for (int loopcpn = 0; loopcpn < SWAP_DEFINITION_PAYER.getFixedLeg().getPayments().length; loopcpn++) {
      expectedPVBP += Math.abs(SWAP_DEFINITION_PAYER.getFixedLeg().getNthPayment(loopcpn).getPaymentYearFraction())
          * CURVE_5.getDiscountFactor(SWAP_PAYER.getFixedLeg().getNthPayment(loopcpn).getPaymentTime()) * NOTIONAL;
    }
    double pvbp = SwapFixedIborMethod.presentValueBasisPoint(SWAP_PAYER, curves);
    assertEquals(expectedPVBP, pvbp, 1E-2); // one cent out of 100m
    pvbp = SwapFixedIborMethod.presentValueBasisPoint(SWAP_PAYER, CURVE_5);
    assertEquals(expectedPVBP, pvbp, 1E-2); // one cent out of 100m
  }

  @Test
  public void testCouponEquivalent() {
    // Yield curves
    YieldCurveBundle curves = new YieldCurveBundle();
    curves.setCurve(FUNDING_CURVE_NAME, CURVE_5);
    curves.setCurve(FORWARD_CURVE_NAME, CURVE_4);
    // Constant rate
    double pvbp = SwapFixedIborMethod.presentValueBasisPoint(SWAP_PAYER, curves);
    double couponEquiv = SwapFixedIborMethod.couponEquivalent(SWAP_PAYER, pvbp, curves);
    assertEquals(RATE, couponEquiv, 1E-10);
    couponEquiv = SwapFixedIborMethod.couponEquivalent(SWAP_PAYER, curves);
    assertEquals(RATE, couponEquiv, 1E-10);
    // Non-constant rate
    AnnuityCouponFixed annuity = SWAP_PAYER.getFixedLeg();
    CouponFixed[] coupon = new CouponFixed[annuity.getNumberOfPayments()];
    for (int loopcpn = 0; loopcpn < annuity.getNumberOfPayments(); loopcpn++) {
      // Step-up by 10bps
      coupon[loopcpn] = new CouponFixed(CUR, annuity.getNthPayment(loopcpn).getPaymentTime(), FUNDING_CURVE_NAME, annuity.getNthPayment(loopcpn).getPaymentYearFraction(), NOTIONAL
          * (FIXED_IS_PAYER ? -1 : 1), RATE + loopcpn * 0.001);
    }
    AnnuityCouponFixed annuityStepUp = new AnnuityCouponFixed(coupon);
    FixedCouponSwap<Payment> swapStepup = new FixedCouponSwap<Payment>(annuityStepUp, SWAP_PAYER.getSecondLeg());
    couponEquiv = SwapFixedIborMethod.couponEquivalent(swapStepup, curves);
    double expectedCouponEquivalent = 0;
    for (int loopcpn = 0; loopcpn < annuity.getNumberOfPayments(); loopcpn++) {
      expectedCouponEquivalent += Math.abs(annuityStepUp.getNthPayment(loopcpn).getAmount()) * CURVE_5.getDiscountFactor(SWAP_PAYER.getFixedLeg().getNthPayment(loopcpn).getPaymentTime());
    }
    expectedCouponEquivalent /= pvbp;
    assertEquals(expectedCouponEquivalent, couponEquiv, 1E-10);
  }
}
