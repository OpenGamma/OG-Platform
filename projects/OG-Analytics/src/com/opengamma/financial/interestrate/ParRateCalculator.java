/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.financial.interestrate.cash.derivative.Cash;
import com.opengamma.financial.interestrate.fra.ForwardRateAgreement;
import com.opengamma.financial.interestrate.fra.method.ForwardRateAgreementDiscountingMethod;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.future.method.InterestRateFutureDiscountingMethod;
import com.opengamma.financial.interestrate.payments.CapFloorIbor;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.payments.CouponIborFixed;
import com.opengamma.financial.interestrate.payments.CouponIborGearing;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.derivative.CouponOIS;
import com.opengamma.financial.interestrate.payments.method.CouponOISDiscountingMethod;
import com.opengamma.financial.interestrate.swap.definition.CrossCurrencySwap;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.FloatingRateNote;
import com.opengamma.financial.interestrate.swap.definition.ForexForward;
import com.opengamma.financial.interestrate.swap.definition.OISSwap;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.CompareUtils;

/**
 * Get the single fixed rate that makes the PV of the instrument zero. For  fixed-float swaps this is the swap rate, for FRAs it is the forward etc. 
 * For instruments that cannot PV to zero, e.g. bonds, a single payment of -1.0 is assumed at zero (i.e. the bond must PV to 1.0)
 */
public final class ParRateCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final ParRateCalculator INSTANCE = new ParRateCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ParRateCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private ParRateCalculator() {
  }

  /**
   * The methods and calculators.
   */
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final CouponOISDiscountingMethod METHOD_OIS = new CouponOISDiscountingMethod();
  private static final ForwardRateAgreementDiscountingMethod METHOD_FRA = ForwardRateAgreementDiscountingMethod.getInstance();
  private static final InterestRateFutureDiscountingMethod METHOD_IRFUT = InterestRateFutureDiscountingMethod.getInstance();

  @Override
  public Double visit(final InstrumentDerivative derivative, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(derivative);
    return derivative.accept(this, curves);
  }

  // TODO: review
  @Override
  public Double visitCash(final Cash cash, final YieldCurveBundle curves) {
    final YieldAndDiscountCurve curve = curves.getCurve(cash.getYieldCurveName());
    final double ta = cash.getStartTime();
    final double tb = cash.getEndTime();
    final double yearFrac = cash.getAccrualFactor();
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
    return METHOD_FRA.parRate(fra, curves);
  }

  @Override
  /**
   * Compute the future rate (1-price) without convexity adjustment.
   */
  public Double visitInterestRateFuture(final InterestRateFuture future, final YieldCurveBundle curves) {
    return METHOD_IRFUT.parRate(future, curves);
  }

  /**
   * Computes the par rate of a swap with one fixed leg.
   * @param swap The Fixed coupon swap.
   * @param curves The curves.
   * @return The par swap rate. If the fixed leg has been set up with some fixed payments these are ignored for the purposes of finding the swap rate
   */
  @Override
  public Double visitFixedCouponSwap(final FixedCouponSwap<?> swap, final YieldCurveBundle curves) {
    final double pvSecond = PVC.visit(swap.getSecondLeg(), curves);
    final double pvbp = PVC.visit(swap.getFixedLeg().withUnitCoupon(), curves);
    return -pvSecond / pvbp;
  }

  @Override
  public Double visitOISSwap(final OISSwap swap, final YieldCurveBundle curves) {
    return visitFixedCouponSwap(swap, curves);
  }

  /**
   * The assumption is that spread is on the second leg.
   * @param swap The tenor swap.
   * @param curves The valuation curves.
   * @return The spread on the second leg of a basis swap 
   */
  @Override
  public Double visitTenorSwap(final TenorSwap<? extends Payment> swap, final YieldCurveBundle curves) {
    final AnnuityCouponIbor firstLeg = (AnnuityCouponIbor) swap.getFirstLeg();
    final AnnuityCouponIbor secondLeg = (AnnuityCouponIbor) swap.getSecondLeg();
    final double pvFirst = PVC.visit(firstLeg.withZeroSpread(), curves);
    final double pvSecond = PVC.visit(secondLeg.withZeroSpread(), curves);
    final double pvSpread = PVC.visit(secondLeg.withUnitCoupons(), curves);
    if (pvSpread == 0.0) {
      throw new IllegalArgumentException("Cannot calculate spread. Please check setup");
    }
    return (-pvFirst - pvSecond) / pvSpread;
  }

  /**
   * This assumes that the spread is pay on the foreign leg (i.e. foreign ibor + spread against domestic ibor)
   * @param ccs The Cross Currency Swap.
   * @param curves The valuation curves.
   * @return The spread on the foreign leg of a CCS
   */
  @Override
  public Double visitCrossCurrencySwap(final CrossCurrencySwap ccs, final YieldCurveBundle curves) {
    FloatingRateNote dFRN = ccs.getDomesticLeg();
    FloatingRateNote fFRN = ccs.getForeignLeg();
    AnnuityCouponIbor dFloatLeg = dFRN.getFloatingLeg().withZeroSpread();
    AnnuityCouponIbor fFloatLeg = fFRN.getFloatingLeg().withZeroSpread();
    AnnuityCouponFixed fAnnuity = fFRN.getFloatingLeg().withUnitCoupons();

    double dPV = PVC.visit(dFloatLeg, curves) + PVC.visit(dFRN.getFirstLeg(), curves); //this is in domestic currency
    double fPV = PVC.visit(fFloatLeg, curves) + PVC.visit(fFRN.getFirstLeg(), curves); //this is in foreign currency
    double fAnnuityPV = PVC.visit(fAnnuity, curves); //this is in foreign currency

    double fx = ccs.getSpotFX(); //TODO remove having CCS holding spot FX rate 

    return (dPV - fx * fPV) / fx / fAnnuityPV;
  }

  // TODO: review
  @Override
  public Double visitForexForward(final ForexForward fx, final YieldCurveBundle curves) {
    //TODO this is not a par rate, it is a forward FX rate
    final YieldAndDiscountCurve curve1 = curves.getCurve(fx.getPaymentCurrency1().getFundingCurveName());
    final YieldAndDiscountCurve curve2 = curves.getCurve(fx.getPaymentCurrency2().getFundingCurveName());
    final double t = fx.getPaymentTime();
    return fx.getSpotForexRate() * curve2.getDiscountFactor(t) / curve1.getDiscountFactor(t);
  }

  // TODO: review
  /**
   * This gives you the bond coupon, for a given yield curve, that renders the bond par (present value of all cash flows equal to 1.0)
   * For a bonds yield use ??????????????? //TODO
   * @param bond the bond
   * @param curves the input curves
   * @return the par rate
   */
  @Override
  public Double visitBondFixedSecurity(final BondFixedSecurity bond, final YieldCurveBundle curves) {
    final GenericAnnuity<CouponFixed> coupons = bond.getCoupon();
    final int n = coupons.getNumberOfPayments();
    final CouponFixed[] unitCoupons = new CouponFixed[n];
    for (int i = 0; i < n; i++) {
      unitCoupons[i] = coupons.getNthPayment(i).withUnitCoupon();
    }
    final GenericAnnuity<CouponFixed> unitCouponAnnuity = new GenericAnnuity<CouponFixed>(unitCoupons);
    final double pvann = PVC.visit(unitCouponAnnuity, curves);
    final double matPV = PVC.visit(bond.getNominal(), curves);
    return (1 - matPV) / pvann;
  }

  @Override
  public Double visitCouponIbor(final CouponIbor payment, final YieldCurveBundle data) {
    final YieldAndDiscountCurve curve = data.getCurve(payment.getForwardCurveName());
    return (curve.getDiscountFactor(payment.getFixingPeriodStartTime()) / curve.getDiscountFactor(payment.getFixingPeriodEndTime()) - 1.0) / payment.getFixingYearFraction();
  }

  @Override
  public Double visitCouponIborGearing(final CouponIborGearing payment, final YieldCurveBundle data) {
    final YieldAndDiscountCurve curve = data.getCurve(payment.getForwardCurveName());
    return (curve.getDiscountFactor(payment.getFixingPeriodStartTime()) / curve.getDiscountFactor(payment.getFixingPeriodEndTime()) - 1.0) / payment.getFixingAccrualFactor();
  }

  @Override
  public Double visitCouponOIS(final CouponOIS payment, final YieldCurveBundle data) {
    return METHOD_OIS.parRate(payment, data);
  }

  @Override
  public Double visitCapFloorIbor(final CapFloorIbor payment, final YieldCurveBundle data) {
    return visitCouponIbor(payment, data);
  }

  @Override
  public Double visitFixedFloatSwap(final FixedFloatSwap swap, final YieldCurveBundle data) {
    return visitFixedCouponSwap(swap, data);
  }

  @Override
  public Double visitCouponIborFixed(final CouponIborFixed payment, final YieldCurveBundle data) {
    return visitCouponIbor(payment.toCouponIbor(), data);
  }

}
