/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */

package com.opengamma.masterdb.security.hibernate.swap;

import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.masterdb.security.hibernate.SecurityBean;
import com.opengamma.masterdb.security.hibernate.ZonedDateTimeBean;

/**
 * A bean representation of {@link SwapSecurity}.
 */
public class SwapSecurityBean extends SecurityBean {

  private SwapType _swapType;
  private ZonedDateTimeBean _tradeDate;
  private ZonedDateTimeBean _effectiveDate;
  private ZonedDateTimeBean _maturityDate;
  private ZonedDateTimeBean _forwardStartDate;
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
  public ZonedDateTimeBean getTradeDate() {
    return _tradeDate;
  }

  /**
   * Sets the tradeDate field.
   * @param tradeDate  the tradeDate
   */
  public void setTradeDate(ZonedDateTimeBean tradeDate) {
    _tradeDate = tradeDate;
  }

  /**
   * Gets the effectiveDate field.
   * @return the effectiveDate
   */
  public ZonedDateTimeBean getEffectiveDate() {
    return _effectiveDate;
  }

  /**
   * Sets the effectiveDate field.
   * @param effectiveDate  the effectiveDate
   */
  public void setEffectiveDate(ZonedDateTimeBean effectiveDate) {
    _effectiveDate = effectiveDate;
  }

  /**
   * Gets the maturityDate field.
   * @return the maturityDate
   */
  public ZonedDateTimeBean getMaturityDate() {
    return _maturityDate;
  }

  /**
   * Sets the maturityDate field.
   * @param maturityDate  the maturityDate
   */
  public void setMaturityDate(ZonedDateTimeBean maturityDate) {
    _maturityDate = maturityDate;
  }

  public ZonedDateTimeBean getForwardStartDate() {
    return _forwardStartDate;
  }

  public void setForwardStartDate(ZonedDateTimeBean forwardStartDate) {
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
