/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.option;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.masterdb.security.hibernate.CurrencyBean;
import com.opengamma.masterdb.security.hibernate.ExpiryBean;
import com.opengamma.masterdb.security.hibernate.SecurityBean;
import com.opengamma.masterdb.security.hibernate.ZonedDateTimeBean;

/**
 * A bean representation of a {@link FXOptionSecurity}.
 */
public class FXOptionSecurityBean extends SecurityBean {
  
  private double _putAmount;
  private double _callAmount;
  private ExpiryBean _expiry;
  private CurrencyBean _putCurrency;
  private CurrencyBean _callCurrency;
  private ZonedDateTimeBean _settlementDate;
  private Boolean _isLong;

  public FXOptionSecurityBean() {
    super();
  }

  /**
   * @return the expiry
   */
  public ExpiryBean getExpiry() {
    return _expiry;
  }

  /**
   * @param expiry
   *          the expiry to set
   */
  public void setExpiry(ExpiryBean expiry) {
    _expiry = expiry;
  }

  public CurrencyBean getPutCurrency() {
    return _putCurrency;
  }

  public void setPutCurrency(final CurrencyBean currency) {
    _putCurrency = currency;
  }

  public CurrencyBean getCallCurrency() {
    return _callCurrency;
  }

  public void setCallCurrency(final CurrencyBean currency) {
    _callCurrency = currency;
  }

  public ZonedDateTimeBean getSettlementDate() {
    return _settlementDate;
  }
  
  public void setSettlementDate(ZonedDateTimeBean settlementDate) {
    _settlementDate = settlementDate;
  }
  
  /**
   * Gets the putAmount.
   * @return the putAmount
   */
  public double getPutAmount() {
    return _putAmount;
  }

  /**
   * Sets the putAmount.
   * @param putAmount  the putAmount
   */
  public void setPutAmount(double putAmount) {
    _putAmount = putAmount;
  }

  /**
   * Gets the callAmount.
   * @return the callAmount
   */
  public double getCallAmount() {
    return _callAmount;
  }

  /**
   * Sets the callAmount.
   * @param callAmount  the callAmount
   */
  public void setCallAmount(double callAmount) {
    _callAmount = callAmount;
  }
  
  /**
   * Gets the isLong.
   * @return the isLong
   */
  public Boolean getIsLong() {
    return _isLong;
  }

  /**
   * Sets the isLong.
   * @param isLong  the isLong
   */
  public void setIsLong(Boolean isLong) {
    _isLong = isLong;
  }

  @Override
  public boolean equals(final Object other) {
    if (!(other instanceof FXOptionSecurityBean)) {
      return false;
    }
    final FXOptionSecurityBean option = (FXOptionSecurityBean) other;

    return new EqualsBuilder()
      .append(getId(), option.getId())
      .append(getExpiry(), option.getExpiry())
      .append(getPutCurrency(), option.getPutCurrency())
      .append(getCallCurrency(), option.getCallCurrency())
      .append(getCallAmount(), option.getCallAmount())
      .append(getPutAmount(), option.getPutAmount())
      .append(getSettlementDate(), option.getSettlementDate())
      .append(getIsLong(), option.getIsLong())
      .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
      .append(getExpiry())
      .append(getPutCurrency())
      .append(getCallCurrency())
      .append(getSettlementDate())
      .append(getPutAmount())
      .append(getCallAmount())
      .append(getIsLong())
      .toHashCode();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}
