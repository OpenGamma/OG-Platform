/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivityUtils.addSensitivity;
import static com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivityUtils.multiplySensitivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositZero;
import com.opengamma.analytics.financial.interestrate.cash.method.DepositZeroDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.fra.method.ForwardRateAgreementDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureSecurity;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.ForexForward;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponIborDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponONDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.method.SwapFixedCouponDiscountingMethod;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.CompareUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * For an instrument, this calculates the sensitivity of the par rate (the exact meaning of par rate depends on the instrument - for swaps it is the par swap rate) to points on the yield
 * curve(s) (i.e. dPar/dR at every point the instrument has sensitivity). The return format is a map with curve names (String) as keys and List of DoublesPair as the values; each list holds
 * set of time (corresponding to point of the yield curve) and sensitivity pairs (i.e. dPar/dR at that time).
 * <b>Note:</b> The length of the list is instrument dependent and may have repeated times (with the understanding the sensitivities should be summed).
 * @deprecated Use the calculators that reference {@link ParameterProviderInterface}
 */
@Deprecated
public final class ParRateCurveSensitivityCalculator extends InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Map<String, List<DoublesPair>>> {

  /**
   * The method unique instance.
   */
  private static final ParRateCurveSensitivityCalculator INSTANCE = new ParRateCurveSensitivityCalculator();

  /**
   * Return the unique instance of the class.
   * @return The instance.
   */
  public static ParRateCurveSensitivityCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  ParRateCurveSensitivityCalculator() {
  }

  /**
   * The methods and calculators.
   */
  private static final PresentValueCalculator PV_CALCULATOR = PresentValueCalculator.getInstance();
  private static final ParRateCalculator PRC_CALCULATOR = ParRateCalculator.getInstance();
  private static final PresentValueCurveSensitivityCalculator PV_SENSITIVITY_CALCULATOR = PresentValueCurveSensitivityCalculator.getInstance();
  private static final RateReplacingInterestRateDerivativeVisitor REPLACE_RATE = RateReplacingInterestRateDerivativeVisitor.getInstance();
  private static final CouponONDiscountingMethod METHOD_OIS = CouponONDiscountingMethod.getInstance();
  private static final CouponIborDiscountingMethod METHOD_IBOR = CouponIborDiscountingMethod.getInstance();
  private static final DepositZeroDiscountingMethod METHOD_DEPOSIT_ZERO = DepositZeroDiscountingMethod.getInstance();
  private static final InterestRateFutureSecurityDiscountingMethod METHOD_IRFUT_SECURITY = InterestRateFutureSecurityDiscountingMethod.getInstance();
  private static final SwapFixedCouponDiscountingMethod METHOD_SWAP = SwapFixedCouponDiscountingMethod.getInstance();

  @Override
  public Map<String, List<DoublesPair>> visitCash(final Cash cash, final YieldCurveBundle curves) {
    final String curveName = cash.getYieldCurveName();
    final YieldAndDiscountCurve curve = curves.getCurve(curveName);
    final double ta = cash.getStartTime();
    final double tb = cash.getEndTime();
    final double yearFrac = cash.getAccrualFactor();
    final Map<String, List<DoublesPair>> result = new HashMap<>();
    final List<DoublesPair> temp = new ArrayList<>();
    if (yearFrac == 0.0) {
      if (!CompareUtils.closeEquals(ta, tb, 1e-16)) {
        throw new IllegalArgumentException("year fraction is zero, but payment time not equal the trade time");
      }
      temp.add(DoublesPair.of(ta, 1.0));
    } else {
      final double ratio = curve.getDiscountFactor(ta) / curve.getDiscountFactor(tb) / yearFrac;
      temp.add(DoublesPair.of(ta, -ta * ratio));
      temp.add(DoublesPair.of(tb, tb * ratio));
    }
    result.put(curveName, temp);
    return result;
  }

  @Override
  public Map<String, List<DoublesPair>> visitDepositZero(final DepositZero deposit, final YieldCurveBundle curves) {
    return METHOD_DEPOSIT_ZERO.parRateCurveSensitivity(deposit, curves).getSensitivities();
  }

  @Override
  public Map<String, List<DoublesPair>> visitForwardRateAgreement(final ForwardRateAgreement fra, final YieldCurveBundle curves) {
    final ForwardRateAgreementDiscountingMethod method = ForwardRateAgreementDiscountingMethod.getInstance();
    return method.parRateCurveSensitivity(fra, curves).getSensitivities();
  }

  @Override
  public Map<String, List<DoublesPair>> visitInterestRateFutureTransaction(final InterestRateFutureTransaction future, final YieldCurveBundle curves) {
    final String curveName = future.getUnderlyingFuture().getForwardCurveName();
    final YieldAndDiscountCurve curve = curves.getCurve(curveName);
    final double ta = future.getUnderlyingFuture().getFixingPeriodStartTime();
    final double tb = future.getUnderlyingFuture().getFixingPeriodEndTime();
    final double ratio = curve.getDiscountFactor(ta) / curve.getDiscountFactor(tb) / future.getUnderlyingFuture().getFixingPeriodAccrualFactor();
    final DoublesPair s1 = DoublesPair.of(ta, -ta * ratio);
    final DoublesPair s2 = DoublesPair.of(tb, tb * ratio);
    final List<DoublesPair> temp = new ArrayList<>();
    temp.add(s1);
    temp.add(s2);
    final Map<String, List<DoublesPair>> result = new HashMap<>();
    result.put(curveName, temp);
    return result;
  }

  @Override
  public Map<String, List<DoublesPair>> visitInterestRateFutureSecurity(final InterestRateFutureSecurity futures, final YieldCurveBundle curves) {
    return METHOD_IRFUT_SECURITY.parRateCurveSensitivity(futures, curves).getSensitivities();
  }

  @Override
  public Map<String, List<DoublesPair>> visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final YieldCurveBundle curves) {
    final AnnuityCouponFixed unitCouponAnnuity = REPLACE_RATE.visitFixedCouponAnnuity(swap.getFixedLeg(), 1.0);
    final Annuity<?> floatingAnnuity = swap.getSecondLeg();
    final double a = unitCouponAnnuity.accept(PV_CALCULATOR, curves);
    final double b = floatingAnnuity.accept(PV_CALCULATOR, curves);
    final double bOveraSq = b / a / a;
    final Map<String, List<DoublesPair>> senseA = unitCouponAnnuity.accept(PV_SENSITIVITY_CALCULATOR, curves);
    final Map<String, List<DoublesPair>> senseB = floatingAnnuity.accept(PV_SENSITIVITY_CALCULATOR, curves);

    return addSensitivity(multiplySensitivity(senseA, bOveraSq), multiplySensitivity(senseB, -1 / a));
  }

  /**
   * Computes the sensitivity to the curve of swap convention-modified par rate for a fixed coupon swap with a PVBP externally provided.
   * <P>Reference: Swaption pricing - v 1.3, OpenGamma Quantitative Research, June 2012.
   * @param swap The swap.
   * @param dayCount The day count convention to modify the swap rate.
   * @param curves The curves.
   * @return The modified rate.
   */
  public Map<String, List<DoublesPair>> visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final DayCount dayCount, final YieldCurveBundle curves) {
    final double pvSecond = swap.getSecondLeg().accept(PV_CALCULATOR, curves) * Math.signum(swap.getSecondLeg().getNthPayment(0).getNotional());
    final double pvbp = METHOD_SWAP.presentValueBasisPoint(swap, dayCount, curves);
    final InterestRateCurveSensitivity pvbpDr = METHOD_SWAP.presentValueBasisPointCurveSensitivity(swap, dayCount, curves);
    final InterestRateCurveSensitivity pvSecondDr = new InterestRateCurveSensitivity(swap.getSecondLeg().accept(PV_SENSITIVITY_CALCULATOR, curves)).multipliedBy(Math
        .signum(swap.getSecondLeg().getNthPayment(0).getNotional()));
    final InterestRateCurveSensitivity result = pvSecondDr.multipliedBy(1.0 / pvbp).plus(pvbpDr.multipliedBy(-pvSecond / (pvbp * pvbp)));
    return result.getSensitivities();
  }

  @Override
  public Map<String, List<DoublesPair>> visitForexForward(final ForexForward fx, final YieldCurveBundle curves) {

    final double fwdFX = fx.accept(PRC_CALCULATOR, curves);
    final double t = fx.getPaymentTime();
    List<DoublesPair> temp = new ArrayList<>();
    temp.add(DoublesPair.of(t, t * fwdFX));
    final Map<String, List<DoublesPair>> senseD = new HashMap<>();
    senseD.put(fx.getPaymentCurrency1().getFundingCurveName(), temp);
    temp = new ArrayList<>();
    temp.add(DoublesPair.of(t, -t * fwdFX));
    final Map<String, List<DoublesPair>> senseF = new HashMap<>();
    senseF.put(fx.getPaymentCurrency2().getFundingCurveName(), temp);

    return addSensitivity(senseD, senseF);
  }

  @Override
  public Map<String, List<DoublesPair>> visitCouponIbor(final CouponIbor payment, final YieldCurveBundle data) {
    return METHOD_IBOR.parRateCurveSensitivity(payment, data).getSensitivities();
  }

  @Override
  public Map<String, List<DoublesPair>> visitCouponIborSpread(final CouponIborSpread payment, final YieldCurveBundle data) {
    final String curveName = payment.getForwardCurveName();
    final YieldAndDiscountCurve curve = data.getCurve(curveName);
    //    final double ta = payment.getFixingTime();
    final double ta = payment.getFixingPeriodStartTime();
    final double tb = payment.getFixingPeriodEndTime();
    final double delta = payment.getFixingAccrualFactor();
    final double ratio = curve.getDiscountFactor(ta) / curve.getDiscountFactor(tb) / delta;
    final DoublesPair s1 = DoublesPair.of(ta, -ta * ratio);
    final DoublesPair s2 = DoublesPair.of(tb, tb * ratio);
    final List<DoublesPair> temp = new ArrayList<>();
    temp.add(s1);
    temp.add(s2);
    final Map<String, List<DoublesPair>> result = new HashMap<>();
    result.put(curveName, temp);
    return result;
  }

  @Override
  public Map<String, List<DoublesPair>> visitCouponOIS(final CouponON payment, final YieldCurveBundle data) {
    return METHOD_OIS.parRateCurveSensitivity(payment, data).getSensitivities();
  }

  @Override
  public Map<String, List<DoublesPair>> visitCapFloorIbor(final CapFloorIbor payment, final YieldCurveBundle data) {
    return visitCouponIborSpread(payment.toCoupon(), data);
  }

  @Override
  public Map<String, List<DoublesPair>> visitBondFixedSecurity(final BondFixedSecurity bond, final YieldCurveBundle curves) {
    final Annuity<CouponFixed> coupons = bond.getCoupon();
    final int n = coupons.getNumberOfPayments();
    final CouponFixed[] unitCoupons = new CouponFixed[n];
    for (int i = 0; i < n; i++) {
      unitCoupons[i] = coupons.getNthPayment(i).withUnitCoupon();
    }
    final Annuity<CouponFixed> unitCouponAnnuity = new Annuity<>(unitCoupons);
    final double a = unitCouponAnnuity.accept(PV_CALCULATOR, curves);
    final Map<String, List<DoublesPair>> senseA = unitCouponAnnuity.accept(PV_SENSITIVITY_CALCULATOR, curves);
    final Map<String, List<DoublesPair>> result = new HashMap<>();
    final PaymentFixed principlePayment = bond.getNominal().getNthPayment(0);
    final double df = principlePayment.accept(PV_CALCULATOR, curves);
    final double factor = -(1 - df) / a / a;
    for (final String name : curves.getAllNames()) {
      if (senseA.containsKey(name)) {
        final List<DoublesPair> temp = new ArrayList<>();
        final List<DoublesPair> list = senseA.get(name);
        final int m = list.size();
        for (int i = 0; i < (m - 1); i++) {
          final DoublesPair pair = list.get(i);
          temp.add(DoublesPair.of(pair.getFirstDouble(), factor * pair.getSecondDouble()));
        }
        final DoublesPair pair = list.get(m - 1);
        temp.add(DoublesPair.of(pair.getFirstDouble(), principlePayment.getPaymentTime() * df / a + factor * pair.getSecondDouble()));
        result.put(name, temp);
      }
    }
    return result;
  }
}
