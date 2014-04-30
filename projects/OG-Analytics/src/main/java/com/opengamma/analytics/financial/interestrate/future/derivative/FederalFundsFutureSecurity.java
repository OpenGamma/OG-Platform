/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.derivative;

import java.util.Arrays;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Description of an Federal Funds Futures.
 */
public class FederalFundsFutureSecurity extends FuturesSecurity {

  /**
   * The OIS-like index on which the future fixes.
   */
  private final IndexON _index;
  /**
   * The accrual interest of the period already fixed. Interest (fixing rate * accrual fraction) for a notional of 1.
   * For a future without fixing, it will be 0.
   */
  private final double _accruedInterest;
  /**
   * The times of the fixing periods not yet fixed. There is one date more than period.
   */
  private final double[] _fixingPeriodTime;
  /**
   * The accrual factors (or year fractions) associated to the fixing periods not yet fixed in the Index day count convention.
   */
  private final double[] _fixingPeriodAccrualFactor;
  /**
   * The total accrual factor for all fixing periods (including the one that have fixed already).
   */
  private final double _fixingTotalAccrualFactor;
  /**
   * The future notional.
   */
  private final double _notional;
  /**
   * The future payment accrual factor. Usually a standardized number of 1/12 for a 30-day future.
   */
  private final double _paymentAccrualFactor;
  /**
   * The future name.
   */
  private final String _name;
  /**
   * The OIS curve name
   */
  private final String _oisCurveName;

  /**
   * Constructor from all the details.
   * @param index The OIS-like index on which the future fixes.
   * @param accruedInterest The accrual interest of the period already fixed. Interest (fixing rate * accrual fraction) for a notional of 1.
   * @param fixingPeriodTime The times of the fixing periods not yet fixed. There is one date more than period.
   * @param lastTradingTime The last trading time of the futures.
   * @param fixingPeriodAccrualFactor The accrual factors (or year fractions) associated to the fixing periods not yet fixed in the Index day count convention.
   * @param fixingTotalAccrualFactor The total accrual factor for all fixing periods (including the one that have fixed already).
   * @param notional The future notional.
   * @param paymentAccrualFactor The future payment accrual factor. Usually a standardized number of 1/12 for a 30-day future.
   * @param name The future name.
   * @param oisCurveName The OIS forward curve name.
   * @deprecated Use the constructor that does not take curve names
   */
  @Deprecated
  public FederalFundsFutureSecurity(final IndexON index, final double accruedInterest, final double[] fixingPeriodTime, final double lastTradingTime,
      final double[] fixingPeriodAccrualFactor, final double fixingTotalAccrualFactor, final double notional, final double paymentAccrualFactor, final String name,
      final String oisCurveName) {
    super(lastTradingTime);
    ArgumentChecker.notNull(index, "Index overnight");
    ArgumentChecker.notNull(fixingPeriodTime, "Fixing period time");
    ArgumentChecker.notNull(fixingPeriodAccrualFactor, "Fixing period accrual factors");
    ArgumentChecker.notNull(name, "Name");
    ArgumentChecker.isTrue(fixingPeriodTime.length == fixingPeriodAccrualFactor.length + 1, "Fixing dates length should be fixing accrual factors + 1.");
    _index = index;
    _accruedInterest = accruedInterest;
    _fixingPeriodTime = fixingPeriodTime;
    _fixingPeriodAccrualFactor = fixingPeriodAccrualFactor;
    _fixingTotalAccrualFactor = fixingTotalAccrualFactor;
    _notional = notional;
    _paymentAccrualFactor = paymentAccrualFactor;
    _name = name;
    _oisCurveName = oisCurveName;
  }

  /**
   * Constructor from all the details.
   * @param index The OIS-like index on which the future fixes.
   * @param accruedInterest The accrual interest of the period already fixed. Interest (fixing rate * accrual fraction) for a notional of 1.
   * @param fixingPeriodTime The times of the fixing periods not yet fixed. There is one date more than period.
   * @param lastTradingTime The last trading time of the futures.
   * @param fixingPeriodAccrualFactor The accrual factors (or year fractions) associated to the fixing periods not yet fixed in the Index day count convention.
   * @param fixingTotalAccrualFactor The total accrual factor for all fixing periods (including the one that have fixed already).
   * @param notional The future notional.
   * @param paymentAccrualFactor The future payment accrual factor. Usually a standardized number of 1/12 for a 30-day future.
   * @param name The future name.
   */
  public FederalFundsFutureSecurity(final IndexON index, final double accruedInterest, final double[] fixingPeriodTime, final double lastTradingTime,
      final double[] fixingPeriodAccrualFactor, final double fixingTotalAccrualFactor, final double notional, final double paymentAccrualFactor, final String name) {
    super(lastTradingTime);
    ArgumentChecker.notNull(index, "Index overnight");
    ArgumentChecker.notNull(fixingPeriodTime, "Fixing period time");
    ArgumentChecker.notNull(fixingPeriodAccrualFactor, "Fixing period accrual factors");
    ArgumentChecker.notNull(name, "Name");
    ArgumentChecker.isTrue(fixingPeriodTime.length == fixingPeriodAccrualFactor.length + 1, "Fixing dates length should be fixing accrual factors + 1.");
    _index = index;
    _accruedInterest = accruedInterest;
    _fixingPeriodTime = fixingPeriodTime;
    _fixingPeriodAccrualFactor = fixingPeriodAccrualFactor;
    _fixingTotalAccrualFactor = fixingTotalAccrualFactor;
    _notional = notional;
    _paymentAccrualFactor = paymentAccrualFactor;
    _name = name;
    _oisCurveName = null;
  }

  /**
   * Gets the OIS-like index on which the future fixes.
   * @return The index.
   */
  public IndexON getIndex() {
    return _index;
  }

  /**
   * Gets the accrual interest of the period already fixed. Interest (fixing rate * accrual fraction) for a notional of 1.
   * @return The accrual interest of the period already fixed.
   */
  public double getAccruedInterest() {
    return _accruedInterest;
  }

  /**
   * Gets the times of the fixing periods not yet fixed.
   * @return The times.
   */
  public double[] getFixingPeriodTime() {
    return _fixingPeriodTime;
  }

  /**
   * Gets the accrual factors (or year fractions) associated to the fixing periods not yet fixed in the Index day count convention.
   * @return The accrual factors.
   */
  public double[] getFixingPeriodAccrualFactor() {
    return _fixingPeriodAccrualFactor;
  }

  /**
   * Gets the total accrual factor for all fixing periods (including the one that have fixed already).
   * @return The accrual factor.
   */
  public double getFixingTotalAccrualFactor() {
    return _fixingTotalAccrualFactor;
  }

  /**
   * Gets the future notional.
   * @return The notional.
   */
  public double getNotional() {
    return _notional;
  }

  /**
   * Gets the future payment accrual factor. Usually a standardized number of 1/12 for a 30-day future.
   * @return The payment accrual factor.
   */
  public double getPaymentAccrualFactor() {
    return _paymentAccrualFactor;
  }

  /**
   * Gets the future name.
   * @return The future name.
   */
  public String getName() {
    return _name;
  }

  /**
   * Gets the OIS curve name.
   * @return The curve name.
   * @deprecated Curve names should no longer be set in {@link InstrumentDefinition}s
   */
  @Deprecated
  public String getOISCurveName() {
    if (_oisCurveName == null) {
      throw new IllegalStateException("OIS curve name not set");
    }
    return _oisCurveName;
  }

  /**
   * Gets the future currency.
   * @return The currency.
   */
  @Override
  public Currency getCurrency() {
    return _index.getCurrency();
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitFederalFundsFutureSecurity(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitFederalFundsFutureSecurity(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_accruedInterest);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + Arrays.hashCode(_fixingPeriodAccrualFactor);
    result = prime * result + Arrays.hashCode(_fixingPeriodTime);
    result = prime * result + _index.hashCode();
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
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final FederalFundsFutureSecurity other = (FederalFundsFutureSecurity) obj;
    if (Double.doubleToLongBits(_accruedInterest) != Double.doubleToLongBits(other._accruedInterest)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    if (Double.doubleToLongBits(_paymentAccrualFactor) != Double.doubleToLongBits(other._paymentAccrualFactor)) {
      return false;
    }
    if (!Arrays.equals(_fixingPeriodAccrualFactor, other._fixingPeriodAccrualFactor)) {
      return false;
    }
    if (!Arrays.equals(_fixingPeriodTime, other._fixingPeriodTime)) {
      return false;
    }
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    if (!ObjectUtils.equals(_name, other._name)) {
      return false;
    }
    return true;
  }

}
