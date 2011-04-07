/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.PaymentFixed;

/**
 * Describes a transaction on a fixed coupon bond issue.
 */
public class BondFixedTransaction extends BondTransaction<CouponFixed> {

  /**
   * Fixed coupon bond transaction constructor from transaction details.
   * @param bondTransaction The bond underlying the transaction.
   * @param quantity The number of bonds purchased (can be negative or positive).
   * @param settlement Transaction settlement payment (time and amount).
   */
  public BondFixedTransaction(BondFixedDescription bondTransaction, double quantity, PaymentFixed settlement) {
    super(bondTransaction, quantity, settlement);
  }

}
