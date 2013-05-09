/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.security.FinancialSecurity;

/**
 * 
 */
public class InterestRateInstrumentPresentValueFunction extends InterestRateInstrumentFunction {
  private static final PresentValueCalculator CALCULATOR = PresentValueCalculator.getInstance();

  public InterestRateInstrumentPresentValueFunction() {
    super(ValueRequirementNames.PRESENT_VALUE);
  }

  @Override
  public Set<ComputedValue> getComputedValues(final InstrumentDerivative derivative, final YieldCurveBundle bundle, final FinancialSecurity security,
      final ComputationTarget target, final String curveCalculationConfigName, final String currency) {
    String temp = "";
    temp += "-------------------------------------------------------------\n";
    temp += currency + "\n";
    YieldAndDiscountCurve curve;
    if (bundle.containsName("Forward3M_" + currency)) {
      curve = bundle.getCurve("Forward3M_" + currency);
    } else {
      curve = bundle.getCurve("Forward6M_" + currency);
    }
    final double[] x1 = ((InterpolatedDoublesCurve) ((YieldCurve) curve).getCurve()).getXDataAsPrimitive();
    final double[] y1 = ((InterpolatedDoublesCurve) ((YieldCurve) curve).getCurve()).getYDataAsPrimitive();
    temp += "Forward curve x:\t0, ";
    for (int i = 0; i < x1.length; i++) {
      temp += x1[i] + (i == x1.length - 1 ? "\n" : ", ");
    }
    temp += "Forward curve y:\t0, ";
    for (int i = 0; i < x1.length; i++) {
      temp += x1[i] * y1[i] + (i == x1.length - 1 ? "\n" : ", ");
    }
    curve = bundle.getCurve("Discounting" + currency);
    final double[] x2 = ((InterpolatedDoublesCurve) ((YieldCurve) curve).getCurve()).getXDataAsPrimitive();
    final double[] y2 = ((InterpolatedDoublesCurve) ((YieldCurve) curve).getCurve()).getYDataAsPrimitive();
    temp += "Discounting curve x:\t0, ";
    for (int i = 0; i < x2.length; i++) {
      temp += x2[i] + (i == x2.length - 1 ? "\n" : ", ");
    }
    temp += "Discounting curve y:\t0, ";
    for (int i = 0; i < x2.length; i++) {
      temp += x2[i] * y2[i] + (i == x2.length - 1 ? "\n" : ", ");
    }
    final SwapFixedCoupon<Coupon> swap = (SwapFixedCoupon<Coupon>) derivative;
    final Annuity<? extends Coupon> firstLeg = swap.getFirstLeg();
    final Annuity<? extends Coupon> secondLeg = swap.getSecondLeg();
    Annuity<? extends Coupon> fixedLeg;
    Annuity<? extends Coupon> floatLeg;
    if (firstLeg.getNthPayment(firstLeg.getNumberOfPayments() - 1) instanceof CouponFixed) {
      fixedLeg = firstLeg;
      floatLeg = secondLeg;
    } else {
      fixedLeg = secondLeg;
      floatLeg = firstLeg;
    }
    final double[] tFixed = new double[fixedLeg.getNumberOfPayments() + 1];
    tFixed[0] = 0;
    final double[] pFixed = new double[fixedLeg.getNumberOfPayments() + 1];
    pFixed[0] = 0;
    final double[] tFloat = new double[floatLeg.getNumberOfPayments() + 1];
    tFloat[0] = 0;
    final double[] pFloat = new double[floatLeg.getNumberOfPayments() + 1];
    pFloat[0] = 0;
    for (int i = 0; i < fixedLeg.getNumberOfPayments(); i++) {
      final CouponFixed fixedCoupon = (CouponFixed) fixedLeg.getNthPayment(i);
      tFixed[i + 1] = fixedCoupon.getPaymentTime();
      pFixed[i + 1] = fixedCoupon.getNotional() * fixedCoupon.getFixedRate();
    }
    for (int i = 0; i < floatLeg.getNumberOfPayments(); i++) {
      final Coupon coupon = floatLeg.getNthPayment(i);
      tFloat[i] = coupon.getPaymentTime();
      if (coupon instanceof CouponFixed) {
        final CouponFixed fixedCoupon = (CouponFixed) coupon;
        pFloat[i + 1] = fixedCoupon.getNotional() * fixedCoupon.getFixedRate();
      } else {
        final CouponIbor iborCoupon = (CouponIbor) coupon;
        pFloat[i + 1] = iborCoupon.getNotional();
      }
    }
    temp += "Fixed payment times:\t";
    for (int i = 0; i < tFixed.length; i++) {
      temp += tFixed[i] + (i == tFixed.length - 1 ? "\n" : ", ");
    }
    temp += "Fixed payment amounts:\t";
    for (int i = 0; i < pFixed.length; i++) {
      temp += pFixed[i] + (i == pFixed.length - 1 ? "\n" : ", ");
    }
    temp += "Floating payment times:\t";
    for (int i = 0; i < tFloat.length; i++) {
      temp += tFloat[i] + (i == tFloat.length - 1 ? "\n" : ", ");
    }
    temp += "Floating payment amounts:\t";
    for (int i = 0; i < pFloat.length; i++) {
      temp += pFloat[i] + (i == pFloat.length - 1 ? "\n" : ", ");
    }
    temp += "Present value:\t";
    final Double presentValue = derivative.accept(CALCULATOR, bundle);
    temp += presentValue;
    System.out.println(temp);
    return Collections.singleton(new ComputedValue(getResultSpec(target, curveCalculationConfigName, currency), presentValue));
  }

}
