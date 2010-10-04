/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.annuity.definition.ForwardLiborAnnuity;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.payments.FixedPayment;
import com.opengamma.financial.interestrate.payments.ForwardLiborPayment;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.FloatingRateNote;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.CompareUtils;

/**
 * Get the single fixed rate that makes the PV of the instrument zero. For  fixed-float swaps this is the swap rate, for FRAs it is the forward etc. For instruments that 
 * cannot PV to zero, e.g. bonds, a single payment of -1.0 is assumed at zero (i.e. the bond must PV to 1.0)
 */
public final class ParRateCalculator implements InterestRateDerivativeVisitor<YieldCurveBundle, Double> {

  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final ParRateCalculator s_instance = new ParRateCalculator();

  public static ParRateCalculator getInstance() {
    return s_instance;
  }

  private ParRateCalculator() {
  }

  @Override
  public Double getValue(final InterestRateDerivative derivative, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(derivative);
    return derivative.accept(this, curves);
  }

  @Override
  public Double visitCash(final Cash cash, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve curve = curves.getCurve(cash.getYieldCurveName());
    final double ta = cash.getTradeTime();
    final double tb = cash.getMaturity();
    final double yearFrac = cash.getYearFraction();
    // TODO need a getForwardRate method on YieldAndDiscountCurve
    if (yearFrac == 0.0) {
      if (!CompareUtils.closeEquals(ta, tb, 1e-16)) {
        throw new IllegalArgumentException("Year fraction is zero, but payment time greater than trade time");
      }
      final double eps = 1e-8;
      final double rate = curve.getInterestRate(ta);
      final double dRate = curve.getInterestRate(ta + eps);
      return rate + ta * (dRate - rate) / eps;
    }
    return (curve.getDiscountFactor(ta) / curve.getDiscountFactor(tb) - 1) / yearFrac;
  }

  @Override
  public Double visitForwardRateAgreement(final ForwardRateAgreement fra, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve curve = curves.getCurve(fra.getLiborCurveName());
    final double ta = fra.getFixingDate();
    final double tb = fra.getMaturity();
    final double yearFrac = fra.getForwardYearFraction();
    Validate.isTrue(yearFrac > 0, "tenor span must be greater than zero");
    final double pa = curve.getDiscountFactor(ta);
    final double pb = curve.getDiscountFactor(tb);
    return (pa / pb - 1) / yearFrac;
  }

  @Override
  public Double visitInterestRateFuture(final InterestRateFuture future, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve curve = curves.getCurve(future.getCurveName());
    final double ta = future.getFixingDate();
    final double tb = future.getMaturity();
    final double pa = curve.getDiscountFactor(ta);
    final double pb = curve.getDiscountFactor(tb);
    return (pa / pb - 1) / future.getIndexYearFraction();
  }

  /**
   * Generic swaps do not have a "swap rate". Do not use
   * @param swap 
   * @param curves 
   * @return nothing
   * @throws NotImplementedException
   */
  @Override
  public Double visitSwap(final Swap<?, ?> swap, final YieldCurveBundle curves) {
    throw new NotImplementedException();
  }

  /**
   * @param swap 
   * @param curves 
   *  @return The par swap rate. If the fixed leg has been set up with some fixed payments these are ignored for the purposes of finding the swap rate
   * 
   */
  @Override
  public Double visitFixedFloatSwap(final FixedFloatSwap swap, final YieldCurveBundle curves) {
    final double pvFloat = PVC.getValue(swap.getFloatingLeg(), curves);
    final double pvFixed = PVC.getValue(swap.getFixedLeg().withRate(1.0), curves);
    return pvFloat / pvFixed;
  }

  /**
   *
   * If the spread is paid (i.e. on the pay leg), swap the legs around and take the negative of the returned value.
   *@param swap 
   * @param curves 
   *@return  The spread on the receive leg of a basis swap 
   */
  @Override
  public Double visitTenorSwap(final TenorSwap swap, final YieldCurveBundle curves) {
    ForwardLiborAnnuity pay = (ForwardLiborAnnuity) swap.getPayLeg();
    ForwardLiborAnnuity receive = (ForwardLiborAnnuity) swap.getReceiveLeg();
    final double pvPay = PVC.getValue(pay.withZeroSpread(), curves);
    final double pvReceive = PVC.getValue(receive.withZeroSpread(), curves);
    final double pvSpread = PVC.getValue(receive.withUnitCoupons(), curves);
    if (pvSpread == 0.0) {
      throw new IllegalArgumentException("Cannot calculate spread. Please check setup");
    }
    return (pvPay - pvReceive) / pvSpread;
  }

  @Override
  public Double visitFloatingRateNote(final FloatingRateNote frn, final YieldCurveBundle curves) {
    GenericAnnuity<FixedPayment> pay = frn.getPayLeg();
    ForwardLiborAnnuity receive = (ForwardLiborAnnuity) frn.getReceiveLeg();
    final double pvPay = PVC.getValue(pay, curves);
    final double pvReceive = PVC.getValue(receive.withZeroSpread(), curves);
    final double pvSpread = PVC.getValue(receive.withUnitCoupons(), curves);
    if (pvSpread == 0.0) {
      throw new IllegalArgumentException("Cannot calculate spread. Please check setup");
    }
    return (pvPay - pvReceive) / pvSpread;
  }

  /**
   * This gives you the bond coupon, for a given yield curve, that renders the bond par (present value of all cash flows equal to 1.0)
   * For a bonds yield use ??????????????? //TODO
   * @param bond the bond
   * @param curves the input curves
   * @return the par rate
   */
  @Override
  public Double visitBond(final Bond bond, final YieldCurveBundle curves) {

    final double pvann = PVC.getValue(bond.getUnitCouponAnnuity(), curves);
    final double matPV = PVC.getValue(bond.getPrinciplePayment(), curves);
    return (1 - matPV) / pvann;
  }

  @Override
  public Double visitFixedPayment(FixedPayment payment, YieldCurveBundle data) {
    throw new NotImplementedException();
  }

  @Override
  public Double visitForwardLiborPayment(ForwardLiborPayment payment, YieldCurveBundle data) {
    YieldAndDiscountCurve curve = data.getCurve(payment.getLiborCurveName());
    return (curve.getDiscountFactor(payment.getLiborFixingTime()) / curve.getDiscountFactor(payment.getLiborMaturityTime()) - 1.0) / payment.getForwardYearFraction();
  }

  /**
  * Generic annuity do not have a "par rate". Do not use
  * @param annuity 
  * @param data 
  * @return nothing
  * @throws NotImplementedException
  */
  @Override
  public Double visitGenericAnnuity(GenericAnnuity<? extends Payment> annuity, YieldCurveBundle data) {
    throw new NotImplementedException();
  }

  // /**
  // * Returns the fixed coupon paid on the same dates (and with the same year fraction) as the floating payments, that gives the
  // * same PV (for the given yield curves) as that expected from the floating payments. This is essentially a swap rate
  // * @param annuity the variable annuity
  // * @param curves the input curves
  // * @return the par rate
  // */
  // @Override
  // public Double visitVariableAnnuity(final ForwardLiborAnnuity annuity, final YieldCurveBundle curves) {
  // final double pvFloat = PVC.getValue(annuity, curves);
  // final double pvFixed = PVC.getValue(annuity.withUnitCoupons(), curves);
  // return pvFloat / pvFixed;
  // }
  //
  // /**
  // * For non-constant fixed payments (i.e. payments are known at the outset), returns the fixed coupon paid on the same dates (and with the same year fraction)
  // * that gives the same PV for a given funding curve
  // * @param annuity the fixed annuity
  // * @param curves the input curves
  // * @return the par rate
  // */
  // @Override
  // public Double visitFixedAnnuity(final FixedAnnuity annuity, final YieldCurveBundle curves) {
  // final double pvFixed = PVC.getValue(annuity, curves);
  // return pvFixed / PVC.getValue(annuity.withUnitCoupons(), curves);
  // }
  //
  // @Override
  // public Double visitConstantCouponAnnuity(final FixedCouponAnnuity annuity, final YieldCurveBundle curves) {
  // return annuity.getCouponRate();
  // }

}
