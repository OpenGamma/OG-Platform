/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedFxReset;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a fixed payment coupon with FX reset.
 * The currency is the currency of the payment. 
 * The notional is expressed in the reference currency, from which the FX reset will be computed.
 * The payment is (getNotional() * FX(at FX reset date) * _rate * getPaymentYearFraction())
 */
public class CouponFixedFxResetDefinition extends CouponDefinition 
    implements InstrumentDefinitionWithData<Payment, DoubleTimeSeries<ZonedDateTime>> {

  /** The fixed rate of the fixed coupon. */
  private final double _rate;
  /** The reference currency. */
  private final Currency _referenceCurrency;
  /** The FX fixing date. The notional used for the payment is the FX rate between the reference currency (RC) and the 
   *  payment currency (PC): 1 RC = X . PC. */
  private final ZonedDateTime _fxFixingDate;
  
  /**
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param paymentAccrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional in the reference currency.
   * @param rate Fixed rate.
   * @param referenceCurrency The reference currency for the FX reset.
   * @param fxFixingDate The FX fixing or reset date. The notional used for the payment is the FX rate between the 
   * reference currency (RC) and the payment currency (PC): 1 RC = X . PC.
   */
  public CouponFixedFxResetDefinition(Currency currency, ZonedDateTime paymentDate, ZonedDateTime accrualStartDate, 
      ZonedDateTime accrualEndDate, double paymentAccrualFactor, double notional,
      double rate, Currency referenceCurrency, ZonedDateTime fxFixingDate) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional);
    ArgumentChecker.notNull(referenceCurrency, "reference currency");
    ArgumentChecker.notNull(fxFixingDate, "FX fixing date");
    _rate = rate;
    _referenceCurrency = referenceCurrency;
    _fxFixingDate = fxFixingDate;
  }

  /**
   * Returns the fixed rate.
   * @return The rate.
   */
  public double getRate() {
    return _rate;
  }

  /**
   * Returns the reference currency.
   * @return The currency.
   */
  public Currency getReferenceCurrency() {
    return _referenceCurrency;
  }

  /**
   * Returns the FX fixing date.
   * @return The date.
   */
  public ZonedDateTime getFxFixingDate() {
    return _fxFixingDate;
  }

  /**
   * Returns the amount paid for a given FX reset rate.
   * The amount is "getNotional() * fxRate * _rate * getPaymentYearFraction()".
   * @param fxRate The exchange rate between the reference currency (RC) and the payment currency (PC): 1 RC = X . PC.
   * @return The amount.
   */
  public double paymentAmount(double fxRate) {
    return getNotional() * fxRate * _rate * getPaymentYearFraction();
  }

  @Override
  public CouponFixedFxReset toDerivative(ZonedDateTime dateTime) {
    ArgumentChecker.notNull(dateTime, "date");
    LocalDate conversionDate = dateTime.toLocalDate();
    LocalDate fixingDate = _fxFixingDate.toLocalDate();
    ArgumentChecker.isTrue(!conversionDate.isAfter(fixingDate), 
        "Do not have any fixing data but are asking for a derivative at {} which is after fixing date {}", 
        conversionDate, fixingDate);
    double paymentTime = TimeCalculator.getTimeBetween(dateTime, getPaymentDate());
    double fixingTime = TimeCalculator.getTimeBetween(dateTime, _fxFixingDate);
    return new CouponFixedFxReset(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), _rate, 
        _referenceCurrency, fixingTime);
  }

  /**
   * {@inheritDoc}
   * The data is the time series of FX fixing rates.
   */
  @Override
  public Payment toDerivative(ZonedDateTime dateTime, DoubleTimeSeries<ZonedDateTime> fxFixingHts) {
    ArgumentChecker.notNull(dateTime, "date");
    LocalDate conversionDate = dateTime.toLocalDate();
    LocalDate fixingDate = _fxFixingDate.toLocalDate();
    double paymentTime = TimeCalculator.getTimeBetween(dateTime, getPaymentDate());
    if (conversionDate.isAfter(fixingDate)) { // Fixing should be known
      ZonedDateTime rezonedFixingDate = _fxFixingDate.toLocalDate().atStartOfDay(ZoneOffset.UTC);
      Double fxRate = fxFixingHts.getValue(rezonedFixingDate);
      if (fxRate == null) {
        throw new OpenGammaRuntimeException("Could not get fixing value for date " + _fxFixingDate);
      }
      double notional = getNotional() * fxRate;
      return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), notional, _rate);
    }
    if (conversionDate.equals(fixingDate)) { // On fixing date: use fixing if present
      ZonedDateTime rezonedFixingDate = _fxFixingDate.toLocalDate().atStartOfDay(ZoneOffset.UTC);
      Double fxRate = fxFixingHts.getValue(rezonedFixingDate);
      if (fxRate != null) { // Fixed notional
        double notional = getNotional() * fxRate;
        return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), notional, _rate);        
      }
    } // Default: no fixing
    double fixingTime = TimeCalculator.getTimeBetween(dateTime, _fxFixingDate);
    return new CouponFixedFxReset(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), _rate, 
        _referenceCurrency, fixingTime);
  }

  @Override
  public <U, V> V accept(InstrumentDefinitionVisitor<U, V> visitor, U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponFixedFxResetDefinition(this, data);
  }

  @Override
  public <V> V accept(InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponFixedFxResetDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result +  _fxFixingDate.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_rate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result +  _referenceCurrency.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    CouponFixedFxResetDefinition other = (CouponFixedFxResetDefinition) obj;
    if (!ObjectUtils.equals(_fxFixingDate, other._fxFixingDate)) {
      return false;
    }
    if (Double.doubleToLongBits(_rate) != Double.doubleToLongBits(other._rate)) {
      return false;
    }
    if (!ObjectUtils.equals(_referenceCurrency, other._referenceCurrency)) {
      return false;
    }
    return true;
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public Payment toDerivative(ZonedDateTime date, String... yieldCurveNames) {
    throw new UnsupportedOperationException(
        "CouponFixedFXResetDefinition does not support toDerivative with yield curve name - deprecated method");
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public Payment toDerivative(ZonedDateTime date, DoubleTimeSeries<ZonedDateTime> data, String... yieldCurveNames) {
    throw new UnsupportedOperationException(
        "CouponFixedFXResetDefinition does not support toDerivative with yield curve name - deprecated method");
  }
  
  

}
