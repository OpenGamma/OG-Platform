/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.bond;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.interestrate.bond.calculator.PriceFromYieldCalculator;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BillTransaction;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

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
   * Constructor.
   * @param underlying The Bill security underlying the transaction.
   * @param quantity The bill quantity.
   * @param settlementDate The date at which the bill transaction is settled.
   * @param settlementAmount The amount paid at settlement date for the bill transaction. The amount is negative for a purchase (_quantity>0) and positive for a sell (_quantity<0).
   */
  public BillTransactionDefinition(final BillSecurityDefinition underlying, final double quantity, final ZonedDateTime settlementDate, final double settlementAmount) {
    ArgumentChecker.notNull(underlying, "Underlying");
    ArgumentChecker.notNull(settlementDate, "Settlement date");
    ArgumentChecker.isTrue(quantity * settlementAmount <= 0, "Quantity and settlement amount should have opposite signs");
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
   * @param calendar The holiday calendar
   * @return The bill transaction.
   */
  public static BillTransactionDefinition fromYield(final BillSecurityDefinition underlying, final double quantity, final ZonedDateTime settlementDate, final double yield,
      final Calendar calendar) {
    ArgumentChecker.notNull(underlying, "Underlying");
    ArgumentChecker.notNull(settlementDate, "Settlement date");
    final double accrualFactor = underlying.getDayCount().getDayCountFraction(settlementDate, underlying.getEndDate(), calendar);
    final double settlementAmount = -quantity * underlying.getNotional() * PriceFromYieldCalculator.priceFromYield(underlying.getYieldConvention(), yield, accrualFactor);
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

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public BillTransaction toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    ArgumentChecker.notNull(date, "Reference date");
    ArgumentChecker.notNull(yieldCurveNames, "Yield curve names");
    final BillSecurity purchased = _underlying.toDerivative(date, _settlementDate, yieldCurveNames);
    final BillSecurity standard = _underlying.toDerivative(date, yieldCurveNames);
    final double amount = (_settlementDate.isBefore(date)) ? 0.0 : _settlementAmount;
    return new BillTransaction(purchased, _quantity, amount, standard);
  }

  @Override
  public BillTransaction toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "Reference date");
    final BillSecurity purchased = _underlying.toDerivative(date, _settlementDate);
    final BillSecurity standard = _underlying.toDerivative(date);
    final double amount = (_settlementDate.isBefore(date)) ? 0.0 : _settlementAmount;
    return new BillTransaction(purchased, _quantity, amount, standard);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBillTransactionDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
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
    final BillTransactionDefinition other = (BillTransactionDefinition) obj;
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
