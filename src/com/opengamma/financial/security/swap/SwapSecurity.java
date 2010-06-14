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
  private ZonedDateTime _effectiveDate;
  private ZonedDateTime _maturityDate;
  private String _counterparty;
  private SwapLeg _payLeg;
  private SwapLeg _receiveLeg;

  /**
   * @param effectiveDate the 'effective' or 'value' date
   * @param maturityDate the 'maturity' or 'termination' date
   * @param counterparty the counterparty
   * @param payLeg the pay leg
   * @param receiveLeg the receive leg
   */
  public SwapSecurity(ZonedDateTime effectiveDate, ZonedDateTime maturityDate,
                      String counterparty, SwapLeg payLeg, SwapLeg receiveLeg) {
    super(SECURITY_TYPE);
    
    Validate.notNull(effectiveDate);
    Validate.notNull(maturityDate);
    Validate.notNull(counterparty);
    Validate.notNull(payLeg);
    Validate.notNull(receiveLeg);
    
    if (effectiveDate.isAfter(maturityDate)) {
      throw new IllegalArgumentException("Effective date cannot be after maturity date");
    }
    _effectiveDate = effectiveDate;
    _maturityDate = maturityDate;
    _counterparty = counterparty;
    _payLeg = payLeg;
    _receiveLeg = receiveLeg;
  }
  
  public SwapSecurity() {
    super(SECURITY_TYPE);
  }
  
  public ZonedDateTime getEffectiveDate() {
    return _effectiveDate;
  }
  
  public void setEffectiveDate(ZonedDateTime effectiveDate) {
    _effectiveDate = effectiveDate;
  }
  
  public ZonedDateTime getMaturityDate() {
    return _maturityDate;
  }
  
  public void setMaturityDate(ZonedDateTime maturityDate) {
    _maturityDate = maturityDate;
  }
  
  public String getCounterparty() {
    return _counterparty;
  }
  
  public void setCounterparty(String counterparty) {
    _counterparty = counterparty;
  }
  
  public SwapLeg getPayLeg() {
    return _payLeg; 
  }
    
  public SwapLeg getReceiveLeg() {
    return _receiveLeg;
  }

  @Override
  public <T> T accept(FinancialSecurityVisitor<T> visitor) {
    return null;
  }
}
