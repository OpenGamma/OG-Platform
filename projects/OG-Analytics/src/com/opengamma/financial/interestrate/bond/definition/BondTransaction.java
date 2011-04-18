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
   * The transaction settlement payment (time and amount). Will be time 0 and amount 0 if the settlement took place already.
   */
  private final PaymentFixed _settlement;
  /**
   * Description of the underlying bond with standard settlement date. Used for clean/dirty price calculation.
   */
  private final BondDescription<C> _bondStandard;
  /**
   * Description of the standard spot time. Used for clean/dirty price calculation.
   */
  private final double _spotTime;
  /**
   * The notional at the standard spot time.
   */
  private final double _notionalStandard;

  /**
   * Bond transaction constructor from the transaction details.
   * @param bondPurchased The bond underlying the transaction.
   * @param quantity The number of bonds purchased (can be negative or positive).
   * @param settlement Transaction settlement payment (time and amount). Can be null if the settlement took place already.
   * @param bondStandard Description of the underlying bond with standard settlement date.
   * @param spotTime Description of the standard spot time.
   * @param notionalStandard The notional at the standard spot time.
   */
  public BondTransaction(BondDescription<C> bondPurchased, double quantity, PaymentFixed settlement, BondDescription<C> bondStandard, double spotTime, double notionalStandard) {
    Validate.notNull(bondPurchased, "Bond underlying the transaction");
    Validate.notNull(settlement, "Settlement payment");
    Validate.notNull(bondStandard, "Bond underlying with standard settlement date");
    // TODO: Check coherence of bond with settlement.
    // TODO: Check coherence of quantity with settlement (sign).
    this._bondPurchased = bondPurchased;
    this._quantity = quantity;
    this._settlement = settlement;
    _bondStandard = bondStandard;
    _spotTime = spotTime;
    _notionalStandard = notionalStandard;
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

  /**
   * Gets the _bondStandard field.
   * @return the _bondStandard
   */
  public BondDescription<C> getBondStandard() {
    return _bondStandard;
  }

  /**
   * Gets the _spotTime field.
   * @return the _spotTime
   */
  public double getSpotTime() {
    return _spotTime;
  }

  /**
   * Gets the _notionalStandard field.
   * @return the _notionalStandard
   */
  public double getNotionalStandard() {
    return _notionalStandard;
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
    String result = "Bond Transaction: Quantity=" + _quantity + ", Spot time=" + _spotTime + ", Notional std=" + _notionalStandard + "\n";
    if (_settlement == null) {
      result += "Settlement in the past\n";
    } else {
      result += "Settlement: " + _settlement.toString() + "\n";
    }
    result += "Underlying: " + _bondPurchased.toString() + "\n";
    result += "Standard: " + _bondStandard.toString();
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _bondPurchased.hashCode();
    result = prime * result + _bondStandard.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_notionalStandard);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_quantity);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_settlement == null) ? 0 : _settlement.hashCode());
    temp = Double.doubleToLongBits(_spotTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    if (!ObjectUtils.equals(_bondPurchased, other._bondPurchased)) {
      return false;
    }
    if (!ObjectUtils.equals(_bondStandard, other._bondStandard)) {
      return false;
    }
    if (Double.doubleToLongBits(_notionalStandard) != Double.doubleToLongBits(other._notionalStandard)) {
      return false;
    }
    if (Double.doubleToLongBits(_quantity) != Double.doubleToLongBits(other._quantity)) {
      return false;
    }
    if (!ObjectUtils.equals(_settlement, other._settlement)) {
      return false;
    }
    if (Double.doubleToLongBits(_spotTime) != Double.doubleToLongBits(other._spotTime)) {
      return false;
    }
    return true;
  }

}
