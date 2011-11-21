/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;

/**
 * Describes a transaction on a fixed coupon bond transaction.
 */
public class BondFixedTransaction extends BondTransaction<BondFixedSecurity> {

  /**
   * Fixed coupon bond transaction constructor from transaction details.
   * @param bondTransaction The bond underlying the transaction.
   * @param quantity The number of bonds purchased (can be negative or positive).
   * @param transactionPrice The transaction clean price.
   * @param bondStandard Description of the underlying bond with standard settlement date.
   * @param notionalStandard The notional at the standard spot time.
   */
  public BondFixedTransaction(BondFixedSecurity bondTransaction, double quantity, double transactionPrice, BondFixedSecurity bondStandard, double notionalStandard) {
    super(bondTransaction, quantity, transactionPrice, bondStandard, notionalStandard);
  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitBondFixedTransaction(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitBondFixedTransaction(this);
  }

}
