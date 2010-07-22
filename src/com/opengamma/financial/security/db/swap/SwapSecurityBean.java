/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.security.db.swap;

import java.util.Date;

import com.opengamma.financial.security.db.SecurityBean;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 * A bean representation of {@link SwapSecurity}.
 */
public class SwapSecurityBean extends SecurityBean {

  private SwapType _swapType;
  private Date _tradeDate;
  private Date _effectiveDate;
  private Date _maturityDate;
  private Date _forwardStartDate;
  private String _counterparty;
  private SwapLegBean _payLeg;
  private SwapLegBean _receiveLeg;

  public SwapType getSwapType() {
    return _swapType;
  }

  public void setSwapType(final SwapType swapType) {
    _swapType = swapType;
  }

  /**
   * Gets the tradeDate field.
   * @return the tradeDate
   */
  public Date getTradeDate() {
    return _tradeDate;
  }

  /**
   * Sets the tradeDate field.
   * @param tradeDate  the tradeDate
   */
  public void setTradeDate(Date tradeDate) {
    _tradeDate = tradeDate;
  }

  /**
   * Gets the effectiveDate field.
   * @return the effectiveDate
   */
  public Date getEffectiveDate() {
    return _effectiveDate;
  }

  /**
   * Sets the effectiveDate field.
   * @param effectiveDate  the effectiveDate
   */
  public void setEffectiveDate(Date effectiveDate) {
    _effectiveDate = effectiveDate;
  }

  /**
   * Gets the maturityDate field.
   * @return the maturityDate
   */
  public Date getMaturityDate() {
    return _maturityDate;
  }

  /**
   * Sets the maturityDate field.
   * @param maturityDate  the maturityDate
   */
  public void setMaturityDate(Date maturityDate) {
    _maturityDate = maturityDate;
  }

  public Date getForwardStartDate() {
    return _forwardStartDate;
  }

  public void setForwardStartDate(Date forwardStartDate) {
    _forwardStartDate = forwardStartDate;
  }

  /**
   * Gets the counterparty field.
   * @return the counterparty
   */
  public String getCounterparty() {
    return _counterparty;
  }

  /**
   * Sets the counterparty field.
   * @param counterparty  the counterparty
   */
  public void setCounterparty(String counterparty) {
    _counterparty = counterparty;
  }

  /**
   * Gets the payLeg field.
   * @return the payLeg
   */
  public SwapLegBean getPayLeg() {
    return _payLeg;
  }

  /**
   * Sets the payLeg field.
   * @param payLeg  the payLeg
   */
  public void setPayLeg(SwapLegBean payLeg) {
    _payLeg = payLeg;
  }

  /**
   * Gets the receiveLeg field.
   * @return the receiveLeg
   */
  public SwapLegBean getReceiveLeg() {
    return _receiveLeg;
  }

  /**
   * Sets the receiveLeg field.
   * @param receiveLeg  the receiveLeg
   */
  public void setReceiveLeg(SwapLegBean receiveLeg) {
    _receiveLeg = receiveLeg;
  }
  
}
