package com.opengamma.financial.security.swap;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;

/**
 * Base class for swaps.
 */
public class SwapSecurity extends FinancialSecurity {
  private static final String SECURITY_TYPE = "SWAP";
  private ZonedDateTime _tradeDate;
  private ZonedDateTime _effectiveDate;
  private ZonedDateTime _maturityDate;
  private String _counterparty;
  private SwapLeg _payLeg;
  private SwapLeg _receiveLeg;

  /**
   * @param tradeDate the date the trade begins
   * @param effectiveDate the 'effective' or 'value' date
   * @param maturityDate the 'maturity' or 'termination' date
   * @param counterparty the counterparty
   * @param payLeg the pay leg
   * @param receiveLeg the receive leg
   */
  public SwapSecurity(final ZonedDateTime tradeDate, final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate, final String counterparty, final SwapLeg payLeg, final SwapLeg receiveLeg) {
    super(SECURITY_TYPE);
    Validate.notNull(tradeDate);
    Validate.notNull(effectiveDate);
    Validate.notNull(maturityDate);
    Validate.notNull(counterparty);
    Validate.notNull(payLeg);
    Validate.notNull(receiveLeg);
    if (tradeDate.isAfter(maturityDate)) {
      throw new IllegalArgumentException("Trade date cannot be after maturity date");
    }
    if (effectiveDate.isAfter(maturityDate)) {
      throw new IllegalArgumentException("Effective date cannot be after maturity date");
    }
    _tradeDate = tradeDate;
    _effectiveDate = effectiveDate;
    _maturityDate = maturityDate;
    _counterparty = counterparty;
    _payLeg = payLeg;
    _receiveLeg = receiveLeg;
  }

  public SwapSecurity() {
    super(SECURITY_TYPE);
  }

  public ZonedDateTime getTradeDate() {
    return _tradeDate;
  }

  public void setTradeDate(final ZonedDateTime tradeDate) {
    Validate.notNull(tradeDate);
    if (tradeDate.isAfter(_maturityDate)) {
      throw new IllegalArgumentException("Trade date cannot be after maturity date");
    }
    _tradeDate = tradeDate;
  }

  public ZonedDateTime getEffectiveDate() {
    return _effectiveDate;
  }

  public void setEffectiveDate(final ZonedDateTime effectiveDate) {
    Validate.notNull(effectiveDate);
    if (effectiveDate.isAfter(_maturityDate)) {
      throw new IllegalArgumentException("Effective date cannot be after maturity date");
    }
    _effectiveDate = effectiveDate;
  }

  public ZonedDateTime getMaturityDate() {
    return _maturityDate;
  }

  public void setMaturityDate(final ZonedDateTime maturityDate) {
    Validate.notNull(maturityDate);
    _maturityDate = maturityDate;
  }

  public String getCounterparty() {
    return _counterparty;
  }

  public void setCounterparty(final String counterparty) {
    Validate.notNull(counterparty);
    _counterparty = counterparty;
  }

  public SwapLeg getPayLeg() {
    return _payLeg;
  }

  public void setPayLeg(final SwapLeg payLeg) {
    Validate.notNull(payLeg);
    _payLeg = payLeg;
  }

  public SwapLeg getReceiveLeg() {
    return _receiveLeg;
  }

  public void setReceiveLeg(final SwapLeg receiveLeg) {
    Validate.notNull(receiveLeg);
    _receiveLeg = receiveLeg;
  }

  @Override
  public <T> T accept(final FinancialSecurityVisitor<T> visitor) {
    return null;
  }
}
