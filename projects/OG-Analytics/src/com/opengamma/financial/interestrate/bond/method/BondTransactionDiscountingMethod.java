/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.PresentValueSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.definition.BondFixedTransaction;
import com.opengamma.financial.interestrate.bond.definition.BondSecurity;
import com.opengamma.financial.interestrate.bond.definition.BondTransaction;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.PaymentFixed;

/**
 * Class with methods related to bond transaction valued by discounting.
 */
public class BondTransactionDiscountingMethod {

  /**
   * The present value calculator (for the different parts of the bond transaction).
   */
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();

  /**
   * The present value calculator (for the different parts of the bond transaction).
   */
  private static final PresentValueSensitivityCalculator PVSC = PresentValueSensitivityCalculator.getInstance();

  /**
   * Compute the present value of a bond transaction.
   * @param bond The bond transaction.
   * @param curves The curve bundle.
   * @return The present value.
   */
  public double presentValue(BondTransaction<? extends BondSecurity<? extends Payment>> bond, YieldCurveBundle curves) {
    double pvNominal = PVC.visit(bond.getBondTransaction().getNominal(), curves);
    double pvCoupon = PVC.visit(bond.getBondTransaction().getCoupon(), curves);
    PaymentFixed settlement = new PaymentFixed(bond.getBondTransaction().getCurrency(), bond.getBondTransaction().getSettlementTime(), bond.getSettlementAmount(), bond.getBondTransaction()
        .getRepoCurveName());
    double pvSettlement = PVC.visit(settlement, curves);
    return (pvNominal + pvCoupon) * bond.getQuantity() + pvSettlement;
  }

  /**
   * Compute the present value of a bond transaction from its clean price.
   * @param bond The bond transaction.
   * @param curves The curve bundle.
   * @param cleanPrice The bond clean price.
   * @return The present value.
   */
  public double presentValueFromCleanPrice(BondTransaction<? extends BondSecurity<? extends Payment>> bond, YieldCurveBundle curves, double cleanPrice) {
    Validate.isTrue(bond instanceof BondFixedTransaction, "Present value from clean price only for fixed coupon bond");
    BondFixedTransaction bondFixed = (BondFixedTransaction) bond;
    double dfSettle = curves.getCurve(bondFixed.getBondStandard().getNominal().getDiscountCurve()).getDiscountFactor(bondFixed.getBondTransaction().getSettlementTime());
    double pvPriceStandard = (cleanPrice + bondFixed.getBondStandard().getAccruedInterest()) * bondFixed.getNotionalStandard() * dfSettle;
    double pvNominalStandard = PVC.visit(bond.getBondStandard().getNominal(), curves);
    double pvCouponStandard = PVC.visit(bond.getBondStandard().getCoupon(), curves);
    double pvDiscountingStandard = (pvNominalStandard + pvCouponStandard);
    double pvNominalTransaction = PVC.visit(bond.getBondTransaction().getNominal(), curves);
    double pvCouponTransaction = PVC.visit(bond.getBondTransaction().getCoupon(), curves);
    double pvDiscountingTransaction = (pvNominalTransaction + pvCouponTransaction);
    return (pvDiscountingTransaction - pvDiscountingStandard) * bond.getQuantity() + pvPriceStandard;
  }

  /**
   * Compute the present value sensitivity of a bond transaction.
   * @param bond The bond transaction.
   * @param curves The curve bundle.
   * @return The present value sensitivity.
   */
  public PresentValueSensitivity presentValueSensitivity(BondTransaction<? extends BondSecurity<? extends Payment>> bond, YieldCurveBundle curves) {
    PresentValueSensitivity pvsNominal = new PresentValueSensitivity(PVSC.visit(bond.getBondTransaction().getNominal(), curves));
    PresentValueSensitivity pvsCoupon = new PresentValueSensitivity(PVSC.visit(bond.getBondTransaction().getCoupon(), curves));
    PaymentFixed settlement = new PaymentFixed(bond.getBondTransaction().getCurrency(), bond.getBondTransaction().getSettlementTime(), bond.getSettlementAmount(), bond.getBondTransaction()
        .getRepoCurveName());
    PresentValueSensitivity pvsSettlement = new PresentValueSensitivity(PVSC.visit(settlement, curves));
    return pvsNominal.add(pvsCoupon).multiply(bond.getQuantity()).add(pvsSettlement);
  }
}
