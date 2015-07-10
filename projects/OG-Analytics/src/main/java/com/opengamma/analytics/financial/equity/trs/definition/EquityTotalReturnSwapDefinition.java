/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.trs.definition;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.equity.Equity;
import com.opengamma.analytics.financial.equity.EquityDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.swap.TotalReturnSwapDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Description of an equity total return swap.
 */
public class EquityTotalReturnSwapDefinition extends TotalReturnSwapDefinition {

  /** The notional amount */
  private final double _notionalAmount;
  /** The notional currency */
  private final Currency _notionalCurrency;
  /** The dividend percentage */
  private final double _dividendPercentage;

  /**
   * @param effectiveDate The effective date.
   * @param terminationDate The termination date.
   * @param fundingLeg The funding leg, not null
   * @param equity The equity, not null
   * @param notionalAmount The notional amount
   * @param notionalCurrency The notional currency, not null
   * @param dividendPercentage The dividend percentage received, >= 0 and <= 1
   */
  public EquityTotalReturnSwapDefinition(final ZonedDateTime effectiveDate, final ZonedDateTime terminationDate,
      final AnnuityDefinition<? extends PaymentDefinition> fundingLeg, final EquityDefinition equity,
      final double notionalAmount, final Currency notionalCurrency, final double dividendPercentage) {
    super(effectiveDate, terminationDate, fundingLeg, equity);
    ArgumentChecker.notNull(notionalCurrency, "notionalCurrency");
    ArgumentChecker.isTrue(ArgumentChecker.isInRangeInclusive(0, 1, dividendPercentage), "Dividend percentage must be >= 0 and <= 1 "
        + "have {}", dividendPercentage);
    _dividendPercentage = dividendPercentage;
    _notionalAmount = notionalAmount;
    _notionalCurrency = notionalCurrency;
  }

  /**
   * Gets the dividend percentage.
   * @return The dividend percentage
   */
  public double getDividendPercentage() {
    return _dividendPercentage;
  }

  /**
   * Gets the notional amount.
   * @return The notional amount
   */
  public double getNotionalAmount() {
    return _notionalAmount;
  }

  /**
   * Gets the notional currency.
   * @return The notional currency
   */
  public Currency getNotionalCurrency() {
    return _notionalCurrency;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitEquityTotalReturnSwapDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitEquityTotalReturnSwapDefinition(this);
  }

  @Override
  public EquityTotalReturnSwap toDerivative(final ZonedDateTime date, final ZonedDateTimeDoubleTimeSeries data) {
    final double effectiveTime = TimeCalculator.getTimeBetween(date, getEffectiveDate());
    final double terminationTime = TimeCalculator.getTimeBetween(date, getTerminationDate());
    final Annuity<? extends Payment> fundingLeg = getFundingLeg().toDerivative(date, data);
    final Equity equity = (Equity) getAsset().toDerivative(date);
    return new EquityTotalReturnSwap(effectiveTime, terminationTime, fundingLeg, equity, _notionalAmount, _notionalCurrency, _dividendPercentage);
  }

  @Override
  public EquityTotalReturnSwap toDerivative(final ZonedDateTime date) {
    final double effectiveTime = TimeCalculator.getTimeBetween(date, getEffectiveDate());
    final double terminationTime = TimeCalculator.getTimeBetween(date, getTerminationDate());
    final Annuity<? extends Payment> fundingLeg = getFundingLeg().toDerivative(date);
    final Equity equity = (Equity) getAsset().toDerivative(date);
    return new EquityTotalReturnSwap(effectiveTime, terminationTime, fundingLeg, equity, _notionalAmount, _notionalCurrency, _dividendPercentage);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_dividendPercentage);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_notionalAmount);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _notionalCurrency.hashCode();
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
    if (!(obj instanceof EquityTotalReturnSwapDefinition)) {
      return false;
    }
    final EquityTotalReturnSwapDefinition other = (EquityTotalReturnSwapDefinition) obj;
    if (Double.compare(_notionalAmount, other._notionalAmount) != 0) {
      return false;
    }
    if (!ObjectUtils.equals(_notionalCurrency, other._notionalCurrency)) {
      return false;
    }
    if (Double.compare(_dividendPercentage, other._dividendPercentage) != 0) {
      return false;
    }
    return true;
  }

}
