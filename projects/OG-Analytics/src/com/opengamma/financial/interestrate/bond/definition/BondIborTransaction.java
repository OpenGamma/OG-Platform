/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;

/**
 * Describes a transaction on a Ibor floating coupon bond (Floating Rate Note) issue.
 */
public class BondIborTransaction extends BondTransaction<BondIborSecurity> {

  /**
   * Ibor coupon bond transaction constructor from transaction details.
   * @param bondTransaction The bond underlying the transaction.
   * @param quantity The number of bonds purchased (can be negative or positive).
   * @param transactionPrice The transaction quoted price.
   * @param bondStandard Description of the underlying bond with standard settlement date.
   * @param notionalStandard The notional at the standard spot time.
   */
  public BondIborTransaction(BondIborSecurity bondTransaction, double quantity, double transactionPrice, BondIborSecurity bondStandard, double notionalStandard) {
    super(bondTransaction, quantity, transactionPrice, bondStandard, notionalStandard);
  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitBondIborTransaction(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitBondIborTransaction(this);
  }

}
