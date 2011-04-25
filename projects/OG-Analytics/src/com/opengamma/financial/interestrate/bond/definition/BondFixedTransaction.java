/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.PaymentFixed;

/**
 * Describes a transaction on a fixed coupon bond transaction.
 */
public class BondFixedTransaction extends BondTransaction<CouponFixed> {

  /**
   * The accrued interest at the standard settlement date (spot).
   */
  private final double _accruedInterestAtSpot;

  /**
   * Fixed coupon bond transaction constructor from transaction details.
   * @param bondTransaction The bond underlying the transaction.
   * @param quantity The number of bonds purchased (can be negative or positive).
   * @param settlement Transaction settlement payment (time and amount).
   * @param bondStandard Description of the underlying bond with standard settlement date.
   * @param spotTime Description of the standard spot time.
   * @param accruedInterestAtSpot The accrued interest at the standard settlement date (spot).
   * @param notionalStandard The notional at the standard spot time.
   */
  public BondFixedTransaction(BondFixedDescription bondTransaction, double quantity, PaymentFixed settlement, BondFixedDescription bondStandard, double spotTime, double accruedInterestAtSpot,
      double notionalStandard) {
    super(bondTransaction, quantity, settlement, bondStandard, spotTime, notionalStandard);
    _accruedInterestAtSpot = accruedInterestAtSpot;
  }

  /**
   * Gets the accrued interest at the standard settlement date (spot).
   * @return The accrued interest at spot.
   */
  public double getAccruedInterestAtSpot() {
    return _accruedInterestAtSpot;
  }

}
