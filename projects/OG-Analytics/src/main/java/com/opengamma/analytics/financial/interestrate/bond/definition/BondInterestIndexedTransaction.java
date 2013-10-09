/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.definition;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.util.ArgumentChecker;

/**
 * Describes a transaction on an interest indexed bond (inflation bond) issue.
 * @param <N> Type of PaymentFixed.
 * @param <C> Type of inflation coupon.
 */
public class BondInterestIndexedTransaction<N extends PaymentFixed, C extends Coupon> extends BondTransaction<BondInterestIndexedSecurity<N, C>> {

  /**
   * Interest indexed bond transaction constructor from transaction details.
   * @param bondPurchased The bond underlying the transaction.
   * @param quantity The number of bonds purchased (can be negative or positive).
   * @param transactionPrice The transaction clean real price.
   * @param bondStandard Description of the underlying bond with standard settlement date.
   * @param notionalStandard The notional at the standard spot time.
   */
  public BondInterestIndexedTransaction(final BondInterestIndexedSecurity<N, C> bondPurchased, final double quantity, final double transactionPrice,
      final BondInterestIndexedSecurity<N, C> bondStandard,
      final double notionalStandard) {
    super(bondPurchased, quantity, transactionPrice, bondStandard, notionalStandard);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBondInterestIndexedTransaction(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBondInterestIndexedTransaction(this);
  }
}
