/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.derivative;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Description of an interest rate future security.
 */
public class InterestRateFutureTransaction implements InstrumentDerivative {

  /**
   * The underlying STIR futures security.
   */
  private final InterestRateFutureSecurity _underlying;
  /**
   * The reference price is used to express present value with respect to some level, for example, the transaction price on the transaction date or the last close price afterward.
   * The price is in relative number and not in percent. A standard price will be 0.985 and not 98.5.
   */
  private final double _referencePrice;
  /**
   * The quantity/number of contract.
   */
  private final int _quantity;

  /**
   * Constructor from tthe underlying and transaction details.
   * @param underlying The underlying futures security.
   * @param referencePrice The reference price (trading price or last margining price).
   * @param quantity The number of contracts.
   */
  public InterestRateFutureTransaction(final InterestRateFutureSecurity underlying, final double referencePrice, final int quantity) {
    ArgumentChecker.notNull(underlying, "Underlying futures");
    ArgumentChecker.notNull(referencePrice, "The reference price");
    ArgumentChecker.notNull(quantity, "Quantity");
    _underlying = underlying;
    _referencePrice = referencePrice;
    _quantity = quantity;
  }

  /**
   * Constructor from all the details.
   * @param lastTradingTime Future last trading time.
   * @param iborIndex Ibor index associated to the future.
   * @param fixingPeriodStartTime Fixing period of the reference Ibor starting time.
   * @param fixingPeriodEndTime Fixing period of the reference Ibor end time.
   * @param fixingPeriodAccrualFactor Fixing period of the reference Ibor accrual factor.
   * @param referencePrice The reference price.
   * @param notional Future notional.
   * @param paymentAccrualFactor Future payment accrual factor.
   * @param quantity The quantity.
   * @param name Future name.
   * @param discountingCurveName The discounting curve name.
   * @param forwardCurveName The forward curve name.
   * @deprecated Deprecated since 2.2.M17. Use the constructor that does not that curve names
   */
  @Deprecated
  public InterestRateFutureTransaction(final double lastTradingTime, final IborIndex iborIndex, final double fixingPeriodStartTime, final double fixingPeriodEndTime,
      final double fixingPeriodAccrualFactor, final double referencePrice, final double notional, final double paymentAccrualFactor, final int quantity, final String name,
      final String discountingCurveName, final String forwardCurveName) {
    _underlying = new InterestRateFutureSecurity(lastTradingTime, iborIndex, fixingPeriodStartTime, fixingPeriodEndTime, fixingPeriodAccrualFactor, notional, paymentAccrualFactor, name,
        discountingCurveName, forwardCurveName);
    _quantity = quantity;
    _referencePrice = referencePrice;
  }

  /**
   * Constructor from all the details.
   * @param lastTradingTime Future last trading time.
   * @param iborIndex Ibor index associated to the future.
   * @param fixingPeriodStartTime Fixing period of the reference Ibor starting time.
   * @param fixingPeriodEndTime Fixing period of the reference Ibor end time.
   * @param fixingPeriodAccrualFactor Fixing period of the reference Ibor accrual factor.
   * @param referencePrice The reference price.
   * @param notional Future notional.
   * @param paymentAccrualFactor Future payment accrual factor.
   * @param quantity The quantity.
   * @param name Future name.
   * @deprecated Deprecated since 2.2.M17. Use the constructor from the security.
   */
  @Deprecated
  public InterestRateFutureTransaction(final double lastTradingTime, final IborIndex iborIndex, final double fixingPeriodStartTime, final double fixingPeriodEndTime,
      final double fixingPeriodAccrualFactor, final double referencePrice, final double notional, final double paymentAccrualFactor, final int quantity, final String name) {
    _underlying = new InterestRateFutureSecurity(lastTradingTime, iborIndex, fixingPeriodStartTime, fixingPeriodEndTime, fixingPeriodAccrualFactor, notional, paymentAccrualFactor, name);
    _quantity = quantity;
    _referencePrice = referencePrice;
  }

  /**
   * Gets the future last trading time.
   * @return The future last trading time.
   */
  public InterestRateFutureSecurity getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the future last trading time.
   * @return The future last trading time.
   */
  public double getLastTradingTime() {
    return _underlying.getTradingLastTime();
  }

  /**
   * Gets the Ibor index associated to the future.
   * @return The Ibor index.
   */
  public IborIndex getIborIndex() {
    return _underlying.getIborIndex();
  }

  /**
   * Gets the fixing period of the reference Ibor starting time.
   * @return The fixing period starting time.
   */
  public double getFixingPeriodStartTime() {
    return _underlying.getFixingPeriodStartTime();
  }

  /**
   * Gets the fixing period of the reference Ibor end time.
   * @return The fixing period end time.
   */
  public double getFixingPeriodEndTime() {
    return _underlying.getFixingPeriodEndTime();
  }

  /**
   * Gets the fixing period of the reference Ibor accrual factor.
   * @return The fixing period accrual factor.
   */
  public double getFixingPeriodAccrualFactor() {
    return _underlying.getFixingPeriodAccrualFactor();
  }

  /**
   * Gets the future notional.
   * @return The notional.
   */
  public double getNotional() {
    return _underlying.getNotional();
  }

  /**
   * Gets the future payment accrual factor.
   * @return The future payment accrual factor.
   */
  public double getPaymentAccrualFactor() {
    return _underlying.getPaymentAccrualFactor();
  }

  /**
   * Gets the referencePrice.
   * @return the referencePrice
   */
  public double getReferencePrice() {
    return _referencePrice;
  }

  /**
   * Gets the discounting curve name.
   * @return The discounting curve name.
   * @deprecated Curve names should not be set in derivatives
   */
  @Deprecated
  public String getDiscountingCurveName() {
    if (_underlying.getDiscountingCurveName() == null) {
      throw new IllegalStateException("Curve names should not be set in derivatives");
    }
    return _underlying.getDiscountingCurveName();
  }

  /**
   * Gets the forward curve name.
   * @return The forward curve name.
   * @deprecated Curve names should not be set in derivatives
   */
  @Deprecated
  public String getForwardCurveName() {
    if (_underlying.getForwardCurveName() == null) {
      throw new IllegalStateException("Curve names should not be set in derivatives");
    }
    return _underlying.getForwardCurveName();
  }

  /**
   * Gets the future name.
   * @return The name.
   */
  public String getName() {
    return _underlying.getName();
  }

  /**
   * The future currency.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _underlying.getCurrency();
  }

  /**
   * Gets the quantity/number of contract.
   * @return The quantity.
   */
  public int getQuantity() {
    return _quantity;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitInterestRateFutureTransaction(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitInterestRateFutureTransaction(this);
  }

  @Override
  public String toString() {
    final String result = "Quantity: " + _quantity + " of " + _underlying.toString();
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _quantity;
    long temp;
    temp = Double.doubleToLongBits(_referencePrice);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    final InterestRateFutureTransaction other = (InterestRateFutureTransaction) obj;
    if (_quantity != other._quantity) {
      return false;
    }
    if (Double.doubleToLongBits(_referencePrice) != Double.doubleToLongBits(other._referencePrice)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlying, other._underlying)) {
      return false;
    }
    return true;
  }

}
