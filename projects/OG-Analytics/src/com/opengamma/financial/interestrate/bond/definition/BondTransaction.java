/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.PaymentFixed;

/**
 * Describes a transaction on a generic single currency bond issue.
 * @param <C> The coupon type.
 */
public abstract class BondTransaction<C extends Payment> implements InterestRateDerivative {
  /**
   * The bond underlying the transaction. All the nominal payment and coupon relevant to the transaction and only them are included in the bond. 
   * The bond may not be suitable for standard price and yield calculation (some coupon may be missing or added). In particular, the bond
   * may not have settlement meaningful spot time.
   */
  private final BondDescription<C> _bondPurchased;
  /**
   * The number of bonds purchased (can be negative or positive).
   */
  private final double _quantity;
  /**
   * The transaction settlement payment (time and amount). Can be null if the settlement took place already.
   */
  private final PaymentFixed _settlement;

  /**
   * Bond transaction constructor from the transaction details.
   * @param bondPurchased The bond underlying the transaction.
   * @param quantity The number of bonds purchased (can be negative or positive).
   * @param settlement Transaction settlement payment (time and amount). Can be null if the settlement took place already.
   */
  public BondTransaction(BondDescription<C> bondPurchased, double quantity, PaymentFixed settlement) {
    Validate.notNull(bondPurchased, "Bond underlying the transaction");
    Validate.notNull(settlement, "Settlement payment");
    // TODO: Check coherence of bond with settlement.
    this._bondPurchased = bondPurchased;
    this._quantity = quantity;
    this._settlement = settlement;
  }

  /**
   * Gets the bond underlying the transaction.
   * @return The bond underlying the transaction.
   */
  public BondDescription<C> getBondTransaction() {
    return _bondPurchased;
  }

  /**
   * Gets the number of bonds purchased (can be negative or positive).
   * @return The number of bonds purchased.
   */
  public double getQuantity() {
    return _quantity;
  }

  /**
   * Gets the transaction settlement payment (time and amount).
   * @return The transaction settlement payment.
   */
  public PaymentFixed getSettlement() {
    return _settlement;
  }

  @Override
  public <S, T> T accept(InterestRateDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitBondTransaction(this, data);
  }

  @Override
  public <T> T accept(InterestRateDerivativeVisitor<?, T> visitor) {
    return visitor.visitBondTransaction(this);
  }

  @Override
  public String toString() {
    String result = "Bond Transaction: Quantity=" + _quantity + "\n";
    if (_settlement == null) {
      result += "Settlement in the past\n";
    } else {
      result += "Settlement: " + _settlement.toString() + "\n";
    }
    result += "Underlying: " + _bondPurchased.toString();
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _bondPurchased.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_quantity);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_settlement == null) ? 0 : _settlement.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    BondTransaction<?> other = (BondTransaction<?>) obj;
    if (Double.doubleToLongBits(_quantity) != Double.doubleToLongBits(other._quantity)) {
      return false;
    }
    if (!ObjectUtils.equals(_bondPurchased, other._bondPurchased)) {
      return false;
    }
    if (!ObjectUtils.equals(_settlement, other._settlement)) {
      return false;
    }
    return true;
  }

}
