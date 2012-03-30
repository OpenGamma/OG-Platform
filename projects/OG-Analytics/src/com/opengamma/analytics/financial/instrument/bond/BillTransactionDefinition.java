/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.bond;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.analytics.financial.interestrate.bond.method.BillSecurityDiscountingMethod;

/**
 * Describes a (Treasury) Bill transaction.
 */
public class BillTransactionDefinition implements InstrumentDefinition<BillTransaction> {

  /**
   * The Bill security underlying the transaction.
   */
  private final BillSecurityDefinition _underlying;
  /**
   * The bill quantity.
   */
  private final double _quantity;
  /**
   * The date at which the bill transaction is settled.
   */
  private final ZonedDateTime _settlementDate;
  /**
   * The amount paid at settlement date for the bill transaction. The amount is negative for a purchase (_quantity>0) and positive for a sell (_quantity<0).
   */
  private final double _settlementAmount;
  /**
   * The method used to create 
   */
  private static final BillSecurityDiscountingMethod METHOD_BILL_SECURITY = BillSecurityDiscountingMethod.getInstance();

  /**
   * Constructor.
   * @param underlying The Bill security underlying the transaction.
   * @param quantity The bill quantity.
   * @param settlementDate The date at which the bill transaction is settled.
   * @param settlementAmount The amount paid at settlement date for the bill transaction. The amount is negative for a purchase (_quantity>0) and positive for a sell (_quantity<0).
   */
  public BillTransactionDefinition(final BillSecurityDefinition underlying, final double quantity, final ZonedDateTime settlementDate, final double settlementAmount) {
    Validate.notNull(underlying, "Underlying");
    Validate.notNull(settlementDate, "Settlement date");
    Validate.isTrue(quantity * settlementAmount <= 0, "Quantity and settlement amount should have opposite signs");
    _underlying = underlying;
    _quantity = quantity;
    _settlementDate = settlementDate;
    _settlementAmount = settlementAmount;
  }

  /**
   * Builder from the yield.
   * @param underlying The Bill security underlying the transaction.
   * @param quantity The bill quantity.
   * @param settlementDate The date at which the bill transaction is settled.
   * @param yield The transaction yield. The yield should be in the bill convention.
   * @return The bill transaction.
   */
  public static BillTransactionDefinition fromYield(final BillSecurityDefinition underlying, final double quantity, final ZonedDateTime settlementDate, final double yield) {
    Validate.notNull(underlying, "Underlying");
    Validate.notNull(settlementDate, "Settlement date");
    double accrualFactor = underlying.getDayCount().getDayCountFraction(settlementDate, underlying.getEndDate());
    double settlementAmount = -quantity * METHOD_BILL_SECURITY.priceFromYield(underlying.getYieldConvention(), yield, accrualFactor);
    return new BillTransactionDefinition(underlying, quantity, settlementDate, settlementAmount);
  }

  /**
   * Gets the Bill security underlying the transaction.
   * @return The bill.
   */
  public BillSecurityDefinition getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the bill quantity.
   * @return The quantity.
   */
  public double getQuantity() {
    return _quantity;
  }

  /**
   * Gets the date at which the bill transaction is settled.
   * @return The date.
   */
  public ZonedDateTime getSettlementDate() {
    return _settlementDate;
  }

  /**
   * Gets the amount paid at settlement date for the bill transaction.
   * @return The amount.
   */
  public double getSettlementAmount() {
    return _settlementAmount;
  }

  @Override
  public String toString() {
    return "Transaction: " + _quantity + " of " + _underlying.toString();
  }

  @Override
  public BillTransaction toDerivative(ZonedDateTime date, String... yieldCurveNames) {
    Validate.notNull(date, "Reference date");
    Validate.notNull(yieldCurveNames, "Yield curve names");
    BillSecurity purchased = _underlying.toDerivative(date, _settlementDate, yieldCurveNames);
    BillSecurity standard = _underlying.toDerivative(date, yieldCurveNames);
    double amount = (_settlementDate.isBefore(date)) ? 0.0 : _settlementAmount;
    return new BillTransaction(purchased, _quantity, amount, standard);
  }

  @Override
  public <U, V> V accept(InstrumentDefinitionVisitor<U, V> visitor, U data) {
    return visitor.visitBillTransactionDefinition(this, data);
  }

  @Override
  public <V> V accept(InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitBillTransactionDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_quantity);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_settlementAmount);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _settlementDate.hashCode();
    result = prime * result + _underlying.hashCode();
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
    BillTransactionDefinition other = (BillTransactionDefinition) obj;
    if (Double.doubleToLongBits(_quantity) != Double.doubleToLongBits(other._quantity)) {
      return false;
    }
    if (Double.doubleToLongBits(_settlementAmount) != Double.doubleToLongBits(other._settlementAmount)) {
      return false;
    }
    if (!ObjectUtils.equals(_settlementDate, other._settlementDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlying, other._underlying)) {
      return false;
    }
    return true;
  }

}
