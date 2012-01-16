/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.money.Currency;

/**
 * Describes a (Treasury) Bill with settlement date.
 */
public class BillSecurity implements InstrumentDerivative {

  /**
   * The bill currency.
   */
  private final Currency _currency;
  /**
   * The bill time to settlement.
   */
  private final double _settlementTime;
  /**
   * The bill end or maturity time.
   */
  private final double _endTime;
  /**
   * The bill nominal.
   */
  private final double _notional;
  /**
   * The yield (to maturity) computation convention.
   */
  private final YieldConvention _yieldConvention;
  /**
   * The yield day count convention.
   */
  private final DayCount _dayCount;
  /**
   * The bill issuer name.
   */
  private final String _issuer;
  /**
   * The name of the curve used for settlement amount discounting.
   */
  private final String _discountingCurveName;

  /**
   * Constructor from all details.
   * @param currency The bill currency.
   * @param settlementTime The bill time to settlement.
   * @param endTime The bill end or maturity time.
   * @param notional The bill nominal.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param dayCount The yield day count convention.
   * @param issuer The bill issuer name.
   * @param discountingCurveName The name of the curve used for settlement amount discounting.
   */
  public BillSecurity(final Currency currency, final double settlementTime, final double endTime, double notional, final YieldConvention yieldConvention, final DayCount dayCount,
      final String issuer, final String discountingCurveName) {
    Validate.notNull(currency, "Currency");
    Validate.notNull(yieldConvention, "Yield convention");
    Validate.notNull(dayCount, "Day count");
    Validate.notNull(issuer, "Issuer");
    Validate.notNull(discountingCurveName, "Discounting curve");
    Validate.isTrue(notional > 0.0, "Notional should be positive");
    Validate.isTrue(endTime >= settlementTime, "End time should be after settlement time");
    Validate.isTrue(settlementTime >= 0, "Settlement time should be positive");
    _currency = currency;
    _endTime = endTime;
    _settlementTime = settlementTime;
    this._notional = notional;
    this._yieldConvention = yieldConvention;
    this._dayCount = dayCount;
    this._issuer = issuer;
    this._discountingCurveName = discountingCurveName;
  }

  /**
   * Get the bill currency.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Gets the bill time to settlement.
   * @return The time.
   */
  public double getSettlementTime() {
    return _settlementTime;
  }

  /**
   * Gets the bill end or maturity time.
   * @return The time.
   */
  public double getEndTime() {
    return _endTime;
  }

  /**
   * Gets the bill notional.
   * @return The notional.
   */
  public double getNotional() {
    return _notional;
  }

  /**
   * Gets the yield (to maturity) computation convention.
   * @return The convention.
   */
  public YieldConvention getYieldConvention() {
    return _yieldConvention;
  }

  /**
   * Gets the yield day count convention.
   * @return The convention.
   */
  public DayCount getDayCount() {
    return _dayCount;
  }

  /**
   * Gets the bill issuer name.
   * @return The name.
   */
  public String getIssuer() {
    return _issuer;
  }

  /**
   * Gets the name of the curve used for settlement amount discounting.
   * @return The name.
   */
  public String getDiscountingCurveName() {
    return _discountingCurveName;
  }

  @Override
  public String toString() {
    return "Bill " + _issuer + " " + _currency + ": settle" + _settlementTime + " - maturity " + _endTime + " - notional " + _notional;
  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitBillSecurity(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitBillSecurity(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currency.hashCode();
    result = prime * result + _dayCount.hashCode();
    result = prime * result + _discountingCurveName.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_endTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_issuer == null) ? 0 : _issuer.hashCode());
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_settlementTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _yieldConvention.hashCode();
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
    BillSecurity other = (BillSecurity) obj;
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    if (!ObjectUtils.equals(_dayCount, other._dayCount)) {
      return false;
    }
    if (!ObjectUtils.equals(_discountingCurveName, other._discountingCurveName)) {
      return false;
    }
    if (Double.doubleToLongBits(_endTime) != Double.doubleToLongBits(other._endTime)) {
      return false;
    }
    if (!ObjectUtils.equals(_issuer, other._issuer)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    if (Double.doubleToLongBits(_settlementTime) != Double.doubleToLongBits(other._settlementTime)) {
      return false;
    }
    if (!ObjectUtils.equals(_yieldConvention, other._yieldConvention)) {
      return false;
    }
    return true;
  }

}
