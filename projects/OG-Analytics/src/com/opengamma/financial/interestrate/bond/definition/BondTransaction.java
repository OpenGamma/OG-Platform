/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.payments.Payment;

/**
 * Describes a transaction on a generic single currency bond issue.
 * @param <B> The underlying bond type.
 */
public abstract class BondTransaction<B extends BondSecurity<? extends Payment, ? extends Payment>> implements InstrumentDerivative {
  /**
   * The bond underlying the transaction. All the nominal payment and coupon relevant to the transaction and only them are included in the bond. 
   * The bond may not be suitable for standard price and yield calculation (some coupon may be missing or added). In particular, the bond
   * may not have settlement meaningful spot time.
   */
  private final B _bondPurchased;
  /**
   * The transaction quoted price. The price meaning will depend on the bond type (Fixed coupon, FRN, Inflation bond).
   */
  private final double _transactionPrice;
  /**
   * The number of bonds purchased (can be negative or positive).
   */
  private final double _quantity;
  /**
   * Description of the underlying bond with standard settlement date. Used for clean/dirty price calculation.
   */
  private final B _bondStandard;
  /**
   * The notional at the standard spot time.
   */
  private final double _notionalStandard;

  /**
   * Bond transaction constructor from the transaction details.
   * @param bondPurchased The bond underlying the transaction.
   * @param quantity The number of bonds purchased (can be negative or positive).
   * @param transactionPrice The transaction quoted price.
   * @param bondStandard Description of the underlying bond with standard settlement date.
   * @param notionalStandard The notional at the standard spot time.
   */
  public BondTransaction(B bondPurchased, double quantity, double transactionPrice, B bondStandard, double notionalStandard) {
    Validate.notNull(bondPurchased, "Bond underlying the transaction");
    Validate.notNull(transactionPrice, "Price");
    Validate.notNull(bondStandard, "Bond underlying with standard settlement date");
    // TODO: Check coherence of bond with settlement.
    _bondPurchased = bondPurchased;
    _quantity = quantity;
    _transactionPrice = transactionPrice;
    _bondStandard = bondStandard;
    _notionalStandard = notionalStandard;
  }

  /**
   * Gets the bond underlying the transaction.
   * @return The bond underlying the transaction.
   */
  public B getBondTransaction() {
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
   * Gets Description of the underlying bond with standard settlement date. Used for clean/dirty price calculation.
   * @return The bond with standard settlement date.
   */
  public B getBondStandard() {
    return _bondStandard;
  }

  /**
   * Gets the notional at the standard spot time.
   * @return The notional standard.
   */
  public double getNotionalStandard() {
    return _notionalStandard;
  }

  /**
   * Gets the transaction price.
   * @return The price.
   */
  public double getTransactionPrice() {
    return _transactionPrice;
  }

  @Override
  public String toString() {
    String result = "Bond Transaction: Quantity=" + _quantity + ", Notional std=" + _notionalStandard + "\n";
    result += "Price: " + _transactionPrice + "\n";
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
    return true;
  }

}
