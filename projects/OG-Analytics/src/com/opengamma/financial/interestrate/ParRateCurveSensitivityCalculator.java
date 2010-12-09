/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.annuity.definition.FixedCouponAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.ForwardLiborAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.payments.ContinuouslyMonitoredAverageRatePayment;
import com.opengamma.financial.interestrate.payments.FixedCouponPayment;
import com.opengamma.financial.interestrate.payments.FixedPayment;
import com.opengamma.financial.interestrate.payments.ForwardLiborPayment;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swap.definition.FloatingRateNote;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.CompareUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public final class ParRateCurveSensitivityCalculator extends AbstractInterestRateDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> {
  private final PresentValueCalculator _pvCalculator = PresentValueCalculator.getInstance();
  private final PresentValueSensitivityCalculator _pvSenseCalculator = PresentValueSensitivityCalculator.getInstance();
  private static final ParRateCurveSensitivityCalculator s_instance = new ParRateCurveSensitivityCalculator();

  public static ParRateCurveSensitivityCalculator getInstance() {
    return s_instance;
  }

  private ParRateCurveSensitivityCalculator() {
  }

  @Override
  public Map<String, List<DoublesPair>> visit(final InterestRateDerivative instrument, final YieldCurveBundle curves) {
    Validate.notNull(instrument);
    Validate.notNull(curves);
    return instrument.accept(this, curves);
  }

  @Override
  public Map<String, List<DoublesPair>> visitCash(final Cash cash, final YieldCurveBundle curves) {
    final String curveName = cash.getYieldCurveName();
    final YieldAndDiscountCurve curve = curves.getCurve(curveName);
    final double ta = cash.getTradeTime();
    final double tb = cash.getMaturity();
    final double yearFrac = cash.getYearFraction();
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    final List<DoublesPair> temp = new ArrayList<DoublesPair>();
    if (yearFrac == 0.0) {
      if (!CompareUtils.closeEquals(ta, tb, 1e-16)) {
        throw new IllegalArgumentException("year fraction is zero, but payment time not equal the trade time");
      }
      temp.add(new DoublesPair(ta, 1.0));
    } else {
      final double ratio = curve.getDiscountFactor(ta) / curve.getDiscountFactor(tb) / yearFrac;
      temp.add(new DoublesPair(ta, -ta * ratio));
      temp.add(new DoublesPair(tb, tb * ratio));
    }
    result.put(curveName, temp);
    return result;
  }

  @Override
  public Map<String, List<DoublesPair>> visitForwardRateAgreement(final ForwardRateAgreement fra, final YieldCurveBundle curves) {
    final String curveName = fra.getLiborCurveName();
    final YieldAndDiscountCurve curve = curves.getCurve(curveName);
    final double ta = fra.getFixingDate();
    final double tb = fra.getMaturity();
    final double delta = fra.getForwardYearFraction();
    final double ratio = curve.getDiscountFactor(ta) / curve.getDiscountFactor(tb) / delta;
    final DoublesPair s1 = new DoublesPair(ta, -ta * ratio);
    final DoublesPair s2 = new DoublesPair(tb, tb * ratio);
    final List<DoublesPair> temp = new ArrayList<DoublesPair>();
    temp.add(s1);
    temp.add(s2);
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    result.put(curveName, temp);
    return result;
  }

  @Override
  public Map<String, List<DoublesPair>> visitInterestRateFuture(final InterestRateFuture future, final YieldCurveBundle curves) {
    final String curveName = future.getCurveName();
    final YieldAndDiscountCurve curve = curves.getCurve(curveName);
    final double ta = future.getFixingDate();
    final double tb = future.getMaturity();
    final double ratio = curve.getDiscountFactor(ta) / curve.getDiscountFactor(tb) / future.getIndexYearFraction();
    final DoublesPair s1 = new DoublesPair(ta, -ta * ratio);
    final DoublesPair s2 = new DoublesPair(tb, tb * ratio);
    final List<DoublesPair> temp = new ArrayList<DoublesPair>();
    temp.add(s1);
    temp.add(s2);
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    result.put(curveName, temp);
    return result;
  }

  @Override
  public Map<String, List<DoublesPair>> visitFixedCouponSwap(final FixedCouponSwap<?> swap, final YieldCurveBundle curves) {
    final FixedCouponAnnuity unitCouponAnnuity = swap.getFixedLeg().withRate(1.0);
    final GenericAnnuity<?> recieveAnnuity = swap.getReceiveLeg();
    final double a = _pvCalculator.visit(unitCouponAnnuity, curves);
    final double b = _pvCalculator.visit(recieveAnnuity, curves);
    final double bOveraSq = b / a / a;
    final Map<String, List<DoublesPair>> senseA = _pvSenseCalculator.visit(unitCouponAnnuity, curves);
    final Map<String, List<DoublesPair>> senseB = _pvSenseCalculator.visit(recieveAnnuity, curves);

    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    for (final String name : curves.getAllNames()) {
      boolean flag = false;
      final List<DoublesPair> temp = new ArrayList<DoublesPair>();
      if (senseA.containsKey(name)) {
        flag = true;
        for (final DoublesPair pair : senseA.get(name)) {
          final double t = pair.getFirst();
          final DoublesPair newPair = new DoublesPair(t, -bOveraSq * pair.getSecond());
          temp.add(newPair);
        }
      }
      if (senseB.containsKey(name)) {
        flag = true;
        for (final DoublesPair pair : senseB.get(name)) {
          final double t = pair.getFirst();
          final DoublesPair newPair = new DoublesPair(t, pair.getSecond() / a);
          temp.add(newPair);
        }
      }
      if (flag) {
        result.put(name, temp);
      }
    }
    return result;
  }

  /**
   * The assumption is that spread is received (i.e. the spread, if any, is on the received leg only)
   * If the spread is paid (i.e. on the pay leg), swap the legs around and take the negative of the returned value.
   * @param swap 
   * @param curves 
   * @return  The spread on the receive leg of a Tenor swap 
   */
  @Override
  public Map<String, List<DoublesPair>> visitTenorSwap(final TenorSwap swap, final YieldCurveBundle curves) {
    final ForwardLiborAnnuity payLeg = ((ForwardLiborAnnuity) swap.getPayLeg()).withZeroSpread();
    final ForwardLiborAnnuity receiveLeg = ((ForwardLiborAnnuity) swap.getReceiveLeg()).withZeroSpread();
    final FixedCouponAnnuity spreadLeg = receiveLeg.withUnitCoupons();

    final double a = _pvCalculator.visit(receiveLeg, curves);
    final double b = _pvCalculator.visit(payLeg, curves);
    final double c = _pvCalculator.visit(spreadLeg, curves);

    final Map<String, List<DoublesPair>> senseA = _pvSenseCalculator.visit(receiveLeg, curves);
    final Map<String, List<DoublesPair>> senseB = _pvSenseCalculator.visit(payLeg, curves);
    final Map<String, List<DoublesPair>> senseC = _pvSenseCalculator.visit(spreadLeg, curves);
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();

    final double factor = -(b - a) / c / c;

    for (final String name : curves.getAllNames()) {
      boolean flag = false;
      final List<DoublesPair> temp = new ArrayList<DoublesPair>();
      if (senseA.containsKey(name)) {
        flag = true;
        for (final DoublesPair pair : senseA.get(name)) {
          temp.add(new DoublesPair(pair.getFirst(), -pair.getSecond() / c));
        }
      }
      if (senseB.containsKey(name)) {
        flag = true;
        for (final DoublesPair pair : senseB.get(name)) {
          temp.add(new DoublesPair(pair.getFirst(), pair.getSecond() / c));
        }
      }
      if (senseC.containsKey(name)) {
        flag = true;
        for (final DoublesPair pair : senseC.get(name)) {
          temp.add(new DoublesPair(pair.getFirst(), factor * pair.getSecond()));
        }
      }
      if (flag) {
        result.put(name, temp);
      }
    }
    return result;
  }

  @Override
  public Map<String, List<DoublesPair>> visitFloatingRateNote(final FloatingRateNote frn, final YieldCurveBundle curves) {
    return visitSwap(frn, curves);
  }

  @Override
  public Map<String, List<DoublesPair>> visitBond(final Bond bond, final YieldCurveBundle curves) {
    final GenericAnnuity<FixedCouponPayment> coupons = bond.getUnitCouponAnnuity();
    final double a = _pvCalculator.visit(coupons, curves);
    final Map<String, List<DoublesPair>> senseA = _pvSenseCalculator.visit(coupons, curves);
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();

    final FixedPayment principlePaymemt = bond.getPrinciplePayment();
    final double df = _pvCalculator.visit(principlePaymemt, curves);
    final double factor = -(1 - df) / a / a;

    for (final String name : curves.getAllNames()) {
      if (senseA.containsKey(name)) {
        final List<DoublesPair> temp = new ArrayList<DoublesPair>();
        final List<DoublesPair> list = senseA.get(name);
        final int n = list.size();
        for (int i = 0; i < (n - 1); i++) {
          final DoublesPair pair = list.get(i);
          temp.add(new DoublesPair(pair.getFirst(), factor * pair.getSecond()));
        }
        final DoublesPair pair = list.get(n - 1);
        temp.add(new DoublesPair(pair.getFirst(), principlePaymemt.getPaymentTime() * df / a + factor * pair.getSecond()));
        result.put(name, temp);
      }
    }
    return result;
  }

  @Override
  public Map<String, List<DoublesPair>> visitForwardLiborPayment(final ForwardLiborPayment payment, final YieldCurveBundle data) {
    final String curveName = payment.getLiborCurveName();
    final YieldAndDiscountCurve curve = data.getCurve(curveName);
    final double ta = payment.getLiborFixingTime();
    final double tb = payment.getLiborMaturityTime();
    final double delta = payment.getForwardYearFraction();
    final double ratio = curve.getDiscountFactor(ta) / curve.getDiscountFactor(tb) / delta;
    final DoublesPair s1 = new DoublesPair(ta, -ta * ratio);
    final DoublesPair s2 = new DoublesPair(tb, tb * ratio);
    final List<DoublesPair> temp = new ArrayList<DoublesPair>();
    temp.add(s1);
    temp.add(s2);
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    result.put(curveName, temp);
    return result;
  }

  @Override
  public Map<String, List<DoublesPair>> visitContinuouslyMonitoredAverageRatePayment(final ContinuouslyMonitoredAverageRatePayment payment, final YieldCurveBundle data) {
    final double ta = payment.getStartTime();
    final double tb = payment.getEndTime();

    final DoublesPair s1 = new DoublesPair(ta, -ta / payment.getRateYearFraction());
    final DoublesPair s2 = new DoublesPair(tb, tb / payment.getRateYearFraction());
    final List<DoublesPair> temp = new ArrayList<DoublesPair>();
    temp.add(s1);
    temp.add(s2);
    final Map<String, List<DoublesPair>> result = new HashMap<String, List<DoublesPair>>();
    result.put(payment.getIndexCurveName(), temp);
    return result;
  }

}
