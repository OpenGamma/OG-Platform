/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.ParRateCalculator;
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
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.security.FinancialSecurity;

/**
 * 
 */
public class InterestRateInstrumentPresentValueFunction extends InterestRateInstrumentFunction {
  private static final PresentValueCalculator CALCULATOR = PresentValueCalculator.getInstance();
  private static final ParRateCalculator PR_CALCULATOR = ParRateCalculator.getInstance();

  public InterestRateInstrumentPresentValueFunction() {
    super(ValueRequirementNames.PRESENT_VALUE);
  }

  @Override
  public Set<ComputedValue> getComputedValues(final InstrumentDerivative derivative, final YieldCurveBundle bundle, final FinancialSecurity security, final ComputationTarget target,
      final String curveCalculationConfigName, final String currency) {
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
    temp += "fwdCurveKnots =  [0, ";
    for (int i = 0; i < x1.length; i++) {
      temp += x1[i] + (i == x1.length - 1 ? "];\n" : ", ");
    }
    temp += "fwdCurveValues =[ 0, ";
    for (int i = 0; i < x1.length; i++) {
      temp += y1[i] + (i == y1.length - 1 ? "];\n" : ", ");
    }
    curve = bundle.getCurve("OIS_" + currency);
    final double[] x2 = ((InterpolatedDoublesCurve) ((YieldCurve) curve).getCurve()).getXDataAsPrimitive();
    final double[] y2 = ((InterpolatedDoublesCurve) ((YieldCurve) curve).getCurve()).getYDataAsPrimitive();
    temp += "disCurveKnots = [0, ";
    for (int i = 0; i < x2.length; i++) {
      temp += x2[i] + (i == x2.length - 1 ? "];\n" : ", ");
    }
    temp += "disCurveValues = [0, ";
    for (int i = 0; i < x2.length; i++) {
      temp += y2[i] + (i == y2.length - 1 ? "];\n" : ", ");
    }

    // convert the curves to PCHIP
    Interpolator1D pchip = Interpolator1DFactory.MOD_PCHIP_INSTANCE;
    // int n = x1.length;
    // double[] x1Ex = new double[n+1];
    // double[] y1Ex = new double[n+1];
    // System.arraycopy(x1, 0, x1Ex, 1, n);
    // System.arraycopy(y1, 0, y1Ex, 1, n);
    InterpolatedDoublesCurve fwd_pchip = InterpolatedDoublesCurve.from(x1, y1, pchip);
    if (bundle.containsName("Forward3M_" + currency)) {
      bundle.replaceCurve("Forward3M_" + currency, new YieldCurve("", fwd_pchip));
    } else {
      bundle.replaceCurve("Forward6M_" + currency, new YieldCurve("", fwd_pchip));
    }
    // n = x2.length;
    // double[] x2Ex = new double[n+1];
    // double[] y2Ex = new double[n+1];
    // System.arraycopy(x2, 0, x2Ex, 1, n);
    // System.arraycopy(y2, 0, y2Ex, 1, n);
    InterpolatedDoublesCurve fund_pchip = InterpolatedDoublesCurve.from(x2, y2, pchip);
    bundle.replaceCurve("OIS_" + currency, new YieldCurve("", fund_pchip));

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
    final int nFixed = fixedLeg.getNumberOfPayments();
    final double[] tFixed = new double[nFixed];
    final double[] yfFixed = new double[nFixed];
    final double[] notionalFixed = new double[nFixed];
    final double[] rFixed = new double[nFixed];
    final int nFloat = floatLeg.getNumberOfPayments();
    final double[] tFloat = new double[nFloat];
    final double[] yfFloat = new double[nFloat];
    final double[] notionalFloat = new double[nFloat];
    final double[] fixingPeriodStartFloat = new double[nFloat];
    final double[] fixingPeriodEndFloat = new double[nFloat];
    final double[] fixingAccrualFactor = new double[nFloat];
    for (int i = 0; i < nFixed; i++) {
      final CouponFixed fixedCoupon = (CouponFixed) fixedLeg.getNthPayment(i);
      tFixed[i] = fixedCoupon.getPaymentTime();
      yfFixed[i] = fixedCoupon.getPaymentYearFraction();
      notionalFixed[i] = Math.abs(fixedCoupon.getNotional());
      rFixed[i] = fixedCoupon.getFixedRate();
    }
    boolean hasFixed = false;
    Double fixedIbor = null;
    for (int i = 0; i < nFloat; i++) {
      final Coupon coupon = floatLeg.getNthPayment(i);
      tFloat[i] = coupon.getPaymentTime();
      yfFloat[i] = coupon.getPaymentYearFraction();
      notionalFloat[i] = Math.abs(coupon.getNotional());
      if (coupon instanceof CouponFixed) {
        hasFixed = true;
        fixedIbor = ((CouponFixed) coupon).getFixedRate();
      } else {
        final CouponIbor iborCoupon = (CouponIbor) coupon;
        fixingPeriodStartFloat[i] = iborCoupon.getFixingPeriodStartTime();
        fixingPeriodEndFloat[i] = iborCoupon.getFixingPeriodEndTime();
        fixingAccrualFactor[i] = iborCoupon.getFixingAccrualFactor();
      }
    }
    temp += "fixedPaymentTimes = [";
    for (int i = 0; i < tFixed.length; i++) {
      temp += tFixed[i] + (i == tFixed.length - 1 ? "];\n" : ", ");
    }
    temp += "fixedPaymentYF = [";
    for (int i = 0; i < yfFixed.length; i++) {
      temp += yfFixed[i] + (i == yfFixed.length - 1 ? "];\n" : ", ");
    }
    temp += "fixedPaymentNotionals = [";
    for (int i = 0; i < notionalFixed.length; i++) {
      temp += notionalFixed[i] + (i == notionalFixed.length - 1 ? "];\n" : ", ");
    }
    temp += "fixedRates = [";
    for (int i = 0; i < rFixed.length; i++) {
      temp += rFixed[i] + (i == rFixed.length - 1 ? "];\n" : ", ");
    }
    temp += "floatingPaymentTimes = [";
    for (int i = 0; i < tFloat.length; i++) {
      temp += tFloat[i] + (i == tFloat.length - 1 ? "];\n" : ", ");
    }
    temp += "fixingStart = [";
    for (int i = 0; i < fixingPeriodStartFloat.length; i++) {
      temp += fixingPeriodStartFloat[i] + (i == fixingPeriodStartFloat.length - 1 ? "];\n" : ", ");
    }
    temp += "fixingEnd = [";
    for (int i = 0; i < fixingPeriodEndFloat.length; i++) {
      temp += fixingPeriodEndFloat[i] + (i == fixingPeriodEndFloat.length - 1 ? "];\n" : ", ");
    }
    temp += "accrualFactor = [";
    for (int i = 0; i < fixingAccrualFactor.length; i++) {
      temp += fixingAccrualFactor[i] + (i == fixingAccrualFactor.length - 1 ? "];\n" : ", ");
    }
    temp += "floatingPaymentYF = [";
    for (int i = 0; i < yfFloat.length; i++) {
      temp += yfFloat[i] + (i == yfFloat.length - 1 ? "];\n" : ", ");
    }
    temp += "floatingPaymentNotionals = [";
    for (int i = 0; i < notionalFloat.length; i++) {
      temp += notionalFloat[i] + (i == notionalFloat.length - 1 ? "];\n" : ", ");
    }
    if (hasFixed) {
      temp += "iborFixingRate = ";
      temp += fixedIbor + ";\n";
    }

    final Double presentValue = derivative.accept(CALCULATOR, bundle);
    temp += "PV = " + presentValue + ";\n";

    final double pr = derivative.accept(PR_CALCULATOR, bundle);
    temp += "parRate = " + pr + ";\n";

    System.out.println(temp);
    return Collections.singleton(new ComputedValue(getResultSpec(target, curveCalculationConfigName, currency), presentValue));
  }

}
