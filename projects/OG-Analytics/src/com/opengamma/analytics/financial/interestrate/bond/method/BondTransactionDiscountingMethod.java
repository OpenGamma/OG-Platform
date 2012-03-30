/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.method;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondIborTransaction;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondTransaction;
import com.opengamma.analytics.financial.interestrate.payments.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.Payment;
import com.opengamma.analytics.financial.interestrate.payments.PaymentFixed;

/**
 * Class with methods related to bond transaction valued by discounting.
 */
public final class BondTransactionDiscountingMethod {

  private static final Logger LOGGER = LoggerFactory.getLogger(BondTransactionDiscountingMethod.class);
  /**
   * The unique instance of the class.
   */
  private static final BondTransactionDiscountingMethod INSTANCE = new BondTransactionDiscountingMethod();

  /**
   * Return the class instance.
   * @return The instance.
   */
  public static BondTransactionDiscountingMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor
   */
  private BondTransactionDiscountingMethod() {
  }

  /**
   * The present value calculator (for the different parts of the bond transaction).
   */
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();

  /**
   * The present value calculator (for the different parts of the bond transaction).
   */
  private static final PresentValueCurveSensitivityCalculator PVSC = PresentValueCurveSensitivityCalculator.getInstance();

  /**
   * Compute the present value of a fixed coupon bond transaction.
   * @param bond The bond transaction.
   * @param curves The curve bundle.
   * @return The present value.
   */
  public double presentValue(final BondFixedTransaction bond, final YieldCurveBundle curves) {
    final double pvNominal = PVC.visit(bond.getBondTransaction().getNominal(), curves);
    final double pvCoupon = PVC.visit(bond.getBondTransaction().getCoupon(), curves);
    final double settlementAmount = -(bond.getTransactionPrice() * bond.getBondTransaction().getCoupon().getNthPayment(0).getNotional() + bond.getBondTransaction().getAccruedInterest())
        * bond.getQuantity();
    final PaymentFixed settlement = new PaymentFixed(bond.getBondTransaction().getCurrency(), bond.getBondTransaction().getSettlementTime(), settlementAmount, bond.getBondTransaction()
        .getRepoCurveName());
    final double pvSettlement = PVC.visit(settlement, curves);
    return (pvNominal + pvCoupon) * bond.getQuantity() + pvSettlement;
  }

  /**
   * Compute the present value of a Ibor coupon bond (FRN) transaction.
   * @param bond The bond transaction.
   * @param curves The curve bundle.
   * @return The present value.
   */
  public double presentValue(final BondIborTransaction bond, final YieldCurveBundle curves) {
    final double pvNominal = PVC.visit(bond.getBondTransaction().getNominal(), curves);
    final double pvCoupon = PVC.visit(bond.getBondTransaction().getCoupon(), curves);
    final double settlementAmount = bond.getTransactionPrice() * bond.getBondTransaction().getCoupon().getNthPayment(0).getNotional(); //FIXME: add accrued.
    LOGGER.error("The FRN settlement amount does not include the accrued interests.");
    final PaymentFixed settlement = new PaymentFixed(bond.getBondTransaction().getCurrency(), bond.getBondTransaction().getSettlementTime(), settlementAmount, bond.getBondTransaction()
        .getRepoCurveName());
    final double pvSettlement = PVC.visit(settlement, curves);
    return (pvNominal + pvCoupon) * bond.getQuantity() + pvSettlement;
  }

  /**
   * Compute the present value of a bond transaction from its clean price.
   * @param bond The bond transaction.
   * @param curves The curve bundle.
   * @param cleanPrice The bond clean price.
   * @return The present value.
   */
  public double presentValueFromCleanPrice(final BondTransaction<? extends BondSecurity<? extends Payment, ? extends Coupon>> bond, final YieldCurveBundle curves, final double cleanPrice) {
    Validate.isTrue(bond instanceof BondFixedTransaction, "Present value from clean price only for fixed coupon bond");
    final BondFixedTransaction bondFixed = (BondFixedTransaction) bond;
    final double dfSettle = curves.getCurve(bondFixed.getBondStandard().getRepoCurveName()).getDiscountFactor(bondFixed.getBondTransaction().getSettlementTime());
    final double pvPriceStandard = (cleanPrice * bondFixed.getNotionalStandard() + bondFixed.getBondStandard().getAccruedInterest()) * dfSettle;
    final double pvNominalStandard = PVC.visit(bond.getBondStandard().getNominal(), curves);
    final double pvCouponStandard = PVC.visit(bond.getBondStandard().getCoupon(), curves);
    final double pvDiscountingStandard = (pvNominalStandard + pvCouponStandard);
    final double pvNominalTransaction = PVC.visit(bond.getBondTransaction().getNominal(), curves);
    final double pvCouponTransaction = PVC.visit(bond.getBondTransaction().getCoupon(), curves);
    final double pvDiscountingTransaction = (pvNominalTransaction + pvCouponTransaction);
    return (pvDiscountingTransaction - pvDiscountingStandard) * bond.getQuantity() + pvPriceStandard;
  }

  /**
   * Compute the present value sensitivity of a bond transaction.
   * @param bond The bond transaction.
   * @param curves The curve bundle.
   * @return The present value sensitivity.
   */
  public InterestRateCurveSensitivity presentValueSensitivity(final BondFixedTransaction bond, final YieldCurveBundle curves) {
    final InterestRateCurveSensitivity pvsNominal = new InterestRateCurveSensitivity(PVSC.visit(bond.getBondTransaction().getNominal(), curves));
    final InterestRateCurveSensitivity pvsCoupon = new InterestRateCurveSensitivity(PVSC.visit(bond.getBondTransaction().getCoupon(), curves));
    final double settlementAmount = -(bond.getTransactionPrice() * bond.getBondTransaction().getCoupon().getNthPayment(0).getNotional() + bond.getBondTransaction().getAccruedInterest())
        * bond.getQuantity();
    final PaymentFixed settlement = new PaymentFixed(bond.getBondTransaction().getCurrency(), bond.getBondTransaction().getSettlementTime(), settlementAmount, bond.getBondTransaction()
        .getRepoCurveName());
    final InterestRateCurveSensitivity pvsSettlement = new InterestRateCurveSensitivity(PVSC.visit(settlement, curves));
    return pvsNominal.plus(pvsCoupon).multiply(bond.getQuantity()).plus(pvsSettlement);
  }

  public InterestRateCurveSensitivity presentValueSensitivity(final BondIborTransaction bond, final YieldCurveBundle curves) {
    final InterestRateCurveSensitivity pvsNominal = new InterestRateCurveSensitivity(PVSC.visit(bond.getBondTransaction().getNominal(), curves));
    final InterestRateCurveSensitivity pvsCoupon = new InterestRateCurveSensitivity(PVSC.visit(bond.getBondTransaction().getCoupon(), curves));
    final double settlementAmount = bond.getTransactionPrice() * bond.getBondTransaction().getCoupon().getNthPayment(0).getNotional(); //FIXME: add accrued.
    LOGGER.error("The FRN settlement amount does not include the accrued interests.");
    final PaymentFixed settlement = new PaymentFixed(bond.getBondTransaction().getCurrency(), bond.getBondTransaction().getSettlementTime(), settlementAmount, bond.getBondTransaction()
        .getRepoCurveName());
    final InterestRateCurveSensitivity pvsSettlement = new InterestRateCurveSensitivity(PVSC.visit(settlement, curves));
    return pvsNominal.plus(pvsCoupon).multiply(bond.getQuantity()).plus(pvsSettlement);
  }
}
