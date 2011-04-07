/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.PresentValueSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.payments.Payment;

/**
 * 
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
  public double presentValue(BondTransaction<? extends Payment> bond, YieldCurveBundle curves) {
    double pvNominal = PVC.visit(bond.getBondTransaction().getNominal(), curves);
    double pvCoupon = PVC.visit(bond.getBondTransaction().getCoupon(), curves);
    double pvSettlement = PVC.visit(bond.getSettlement(), curves);
    return (pvNominal + pvCoupon) * bond.getQuantity() + pvSettlement;
  }

  /**
   * Compute the present value sensitivity of a bond transaction.
   * @param bond The bond transaction.
   * @param curves The curve bundle.
   * @return The present value sensitivity.
   */
  public PresentValueSensitivity presentValueSensitivity(BondTransaction<? extends Payment> bond, YieldCurveBundle curves) {
    PresentValueSensitivity pvsNominal = new PresentValueSensitivity(PVSC.visit(bond.getBondTransaction().getNominal(), curves));
    PresentValueSensitivity pvsCoupon = new PresentValueSensitivity(PVSC.visit(bond.getBondTransaction().getCoupon(), curves));
    PresentValueSensitivity pvsSettlement = new PresentValueSensitivity(PVSC.visit(bond.getSettlement(), curves));
    return pvsNominal.add(pvsCoupon).multiply(bond.getQuantity()).add(pvsSettlement);
  }
}
