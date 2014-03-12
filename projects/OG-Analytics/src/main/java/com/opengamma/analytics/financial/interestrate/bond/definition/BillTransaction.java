/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.bond.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Describes a (Treasury) Bill transaction.
 */
public class BillTransaction implements InstrumentDerivative {

  /**
   * The bill underlying the transaction.
   * <P> The bill may not be suitable for standard price and yield calculation (incorrect settlement).
   */
  private final BillSecurity _billPurchased;
  /**
   * The bill quantity.
   */
  private final double _quantity;
  /**
   * The amount paid at settlement date for the bill transaction. The amount is negative for a purchase (_quantity>0) and positive for a sell (_quantity<0).
   */
  private final double _settlementAmount;

  /**
   * The bill with standard settlement date (time). Used for yield calculation.
   * <P> If the standard settlement date would be after the end date, the end date should be used for settlement.
   */
  private final BillSecurity _billStandard;

  /**
   * Constructor.
   * @param billPurchased The bill underlying the transaction.
   * @param quantity The bill quantity.
   * @param settlementAmount The amount paid at settlement date for the bill transaction. The amount is negative for a purchase (_quantity>0) and positive for a sell (_quantity<0).
   * @param billStandard The bill with standard settlement date (time).
   */
  public BillTransaction(final BillSecurity billPurchased, final double quantity, final double settlementAmount, final BillSecurity billStandard) {
    Validate.notNull(billPurchased, "Bill purchased");
    Validate.notNull(billStandard, "Bill standard");
    Validate.isTrue(quantity * settlementAmount <= 0, "Quantity and settlement amount should have opposite signs");
    _billPurchased = billPurchased;
    _quantity = quantity;
    _settlementAmount = settlementAmount;
    _billStandard = billStandard;
  }

  /**
   * Gets the bill underlying the transaction.
   * @return The bill.
   */
  public BillSecurity getBillPurchased() {
    return _billPurchased;
  }

  /**
   * Gets the bill quantity.
   * @return The quantity.
   */
  public double getQuantity() {
    return _quantity;
  }

  /**
   * Gets the amount paid at settlement date for the bill transaction.
   * @return The amount.
   */
  public double getSettlementAmount() {
    return _settlementAmount;
  }

  /**
   * Gets the bill with standard settlement date (time).
   * @return The bill.
   */
  public BillSecurity getBillStandard() {
    return _billStandard;
  }

  /**
   * Gets the currency.
   * @return The currency
   */
  public Currency getCurrency() {
    return _billStandard.getCurrency();
  }

  @Override
  public String toString() {
    return "Transaction: " + _quantity + " of " + _billPurchased.toString();
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBillTransaction(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBillTransaction(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _billPurchased.hashCode();
    result = prime * result + _billStandard.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_quantity);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_settlementAmount);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final BillTransaction other = (BillTransaction) obj;
    if (!ObjectUtils.equals(_billPurchased, other._billPurchased)) {
      return false;
    }
    if (!ObjectUtils.equals(_billStandard, other._billStandard)) {
      return false;
    }
    if (Double.doubleToLongBits(_quantity) != Double.doubleToLongBits(other._quantity)) {
      return false;
    }
    if (Double.doubleToLongBits(_settlementAmount) != Double.doubleToLongBits(other._settlementAmount)) {
      return false;
    }
    return true;
  }

}
