/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.derivative;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Description of an interest rate future security.
 */
public class InterestRateFutureSecurity extends FuturesSecurity {

  /**
   * Ibor index associated to the future.
   */
  private final IborIndex _iborIndex;
  /**
   * Fixing period of the reference Ibor starting time.
   */
  private final double _fixingPeriodStartTime;
  /**
   * Fixing period of the reference Ibor end time.
   */
  private final double _fixingPeriodEndTime;
  /**
   * Fixing period of the reference Ibor accrual factor.
   */
  private final double _fixingPeriodAccrualFactor;
  /**
   * Future notional.
   */
  private final double _notional;
  /**
   * Future payment accrual factor. Usually a standardized number of 0.25 for a 3M future.
   */
  private final double _paymentAccrualFactor;
  /**
   * Future name.
   */
  private final String _name;
  /**
   * The discounting curve name.
   * @deprecated Curve names should no longer be set in {@link InstrumentDefinition}s
   */
  @Deprecated
  private final String _discountingCurveName;
  /**
   * The name of the forward curve used in to estimate the fixing index.
   * @deprecated Curve names should no longer be set in {@link InstrumentDefinition}s
   */
  @Deprecated
  private final String _forwardCurveName;

  /**
   * Constructor from all the details.
   * @param lastTradingTime Future last trading time.
   * @param iborIndex Ibor index associated to the future.
   * @param fixingPeriodStartTime Fixing period of the reference Ibor starting time.
   * @param fixingPeriodEndTime Fixing period of the reference Ibor end time.
   * @param fixingPeriodAccrualFactor Fixing period of the reference Ibor accrual factor.
   * @param notional Future notional.
   * @param paymentAccrualFactor Future payment accrual factor.
   * @param name Future name.
   * @param discountingCurveName The discounting curve name.
   * @param forwardCurveName The forward curve name.
   * @deprecated Use the constructor that does not take curve names
   */
  @Deprecated
  public InterestRateFutureSecurity(final double lastTradingTime, final IborIndex iborIndex, final double fixingPeriodStartTime, final double fixingPeriodEndTime,
      final double fixingPeriodAccrualFactor, final double notional, final double paymentAccrualFactor, final String name, final String discountingCurveName,
      final String forwardCurveName) {
    super(lastTradingTime);
    ArgumentChecker.notNull(iborIndex, "Ibor index");
    ArgumentChecker.notNull(name, "Name");
    ArgumentChecker.notNull(discountingCurveName, "Discounting curve name");
    ArgumentChecker.notNull(forwardCurveName, "Forward curve name");
    _iborIndex = iborIndex;
    _fixingPeriodStartTime = fixingPeriodStartTime;
    _fixingPeriodEndTime = fixingPeriodEndTime;
    _fixingPeriodAccrualFactor = fixingPeriodAccrualFactor;
    _notional = notional;
    _paymentAccrualFactor = paymentAccrualFactor;
    _discountingCurveName = discountingCurveName;
    _forwardCurveName = forwardCurveName;
    _name = name;
  }

  /**
   * Constructor from all the details.
   * @param lastTradingTime Future last trading time.
   * @param iborIndex Ibor index associated to the future.
   * @param fixingPeriodStartTime Fixing period of the reference Ibor starting time.
   * @param fixingPeriodEndTime Fixing period of the reference Ibor end time.
   * @param fixingPeriodAccrualFactor Fixing period of the reference Ibor accrual factor.
   * @param notional Future notional.
   * @param paymentAccrualFactor Future payment accrual factor.
   * @param name Future name.
   */
  public InterestRateFutureSecurity(final double lastTradingTime, final IborIndex iborIndex, final double fixingPeriodStartTime, final double fixingPeriodEndTime,
      final double fixingPeriodAccrualFactor, final double notional, final double paymentAccrualFactor, final String name) {
    super(lastTradingTime);
    ArgumentChecker.notNull(iborIndex, "Ibor index");
    ArgumentChecker.notNull(name, "Name");
    _iborIndex = iborIndex;
    _fixingPeriodStartTime = fixingPeriodStartTime;
    _fixingPeriodEndTime = fixingPeriodEndTime;
    _fixingPeriodAccrualFactor = fixingPeriodAccrualFactor;
    _notional = notional;
    _paymentAccrualFactor = paymentAccrualFactor;
    _name = name;
    _discountingCurveName = null;
    _forwardCurveName = null;
  }

  /**
   * Gets the Ibor index associated to the future.
   * @return The Ibor index.
   */
  public IborIndex getIborIndex() {
    return _iborIndex;
  }

  /**
   * Gets the fixing period of the reference Ibor starting time.
   * @return The fixing period starting time.
   */
  public double getFixingPeriodStartTime() {
    return _fixingPeriodStartTime;
  }

  /**
   * Gets the fixing period of the reference Ibor end time.
   * @return The fixing period end time.
   */
  public double getFixingPeriodEndTime() {
    return _fixingPeriodEndTime;
  }

  /**
   * Gets the fixing period of the reference Ibor accrual factor.
   * @return The fixing period accrual factor.
   */
  public double getFixingPeriodAccrualFactor() {
    return _fixingPeriodAccrualFactor;
  }

  /**
   * Gets the future notional.
   * @return The notional.
   */
  public double getNotional() {
    return _notional;
  }

  /**
   * Gets the future payment accrual factor.
   * @return The future payment accrual factor.
   */
  public double getPaymentAccrualFactor() {
    return _paymentAccrualFactor;
  }

  /**
   * Gets the unit Amount. This represents the PNL of a single long contract if its price increases by 1.0. Also known as the 'Point Value'.
   * @return the point value
   */
  public double getUnitAmount() {
    return _notional * _paymentAccrualFactor;
  }

  /**
   * Gets the discounting curve name.
   * @return The name.
   * @deprecated Curve names should no longer be set in {@link InstrumentDefinition}s
   */
  @Deprecated
  public String getDiscountingCurveName() {
    if (_discountingCurveName == null) {
      throw new IllegalStateException("Discounting curve name was not set");
    }
    return _discountingCurveName;
  }

  /**
   * Gets the forward curve name.
   * @return The name.
   * @deprecated Curve names should no longer be set in {@link InstrumentDefinition}s
   */
  @Deprecated
  public String getForwardCurveName() {
    if (_forwardCurveName == null) {
      throw new IllegalStateException("Forward curve name was not set");
    }
    return _forwardCurveName;
  }

  /**
   * Gets the future name.
   * @return The name.
   */
  public String getName() {
    return _name;
  }

  /**
   * The future currency.
   * @return The currency.
   */
  @Override
  public Currency getCurrency() {
    return _iborIndex.getCurrency();
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitInterestRateFutureSecurity(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitInterestRateFutureSecurity(this);
  }

  @Override
  public String toString() {
    String result = "IRFuture Security: " + _name;
    result += " - Index: " + _iborIndex.getName();
    result += " - Start fixing: " + _fixingPeriodStartTime;
    result += " - End fixing: " + _fixingPeriodEndTime;
    result += " - Notional: " + _notional;
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_fixingPeriodAccrualFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_fixingPeriodEndTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_fixingPeriodStartTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + (_forwardCurveName == null ? 0 : _forwardCurveName.hashCode());
    result = prime * result + (_discountingCurveName == null ? 0 : _discountingCurveName.hashCode());
    result = prime * result + _iborIndex.hashCode();
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _name.hashCode();
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_paymentAccrualFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final InterestRateFutureSecurity other = (InterestRateFutureSecurity) obj;
    if (Double.doubleToLongBits(_fixingPeriodAccrualFactor) != Double.doubleToLongBits(other._fixingPeriodAccrualFactor)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingPeriodEndTime) != Double.doubleToLongBits(other._fixingPeriodEndTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingPeriodStartTime) != Double.doubleToLongBits(other._fixingPeriodStartTime)) {
      return false;
    }
    if (!ObjectUtils.equals(_forwardCurveName, other._forwardCurveName)) {
      return false;
    }
    if (!ObjectUtils.equals(_discountingCurveName, other._discountingCurveName)) {
      return false;
    }
    if (!ObjectUtils.equals(_iborIndex, other._iborIndex)) {
      return false;
    }
    if (!ObjectUtils.equals(_name, other._name)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    if (Double.doubleToLongBits(_paymentAccrualFactor) != Double.doubleToLongBits(other._paymentAccrualFactor)) {
      return false;
    }
    return true;
  }

}
