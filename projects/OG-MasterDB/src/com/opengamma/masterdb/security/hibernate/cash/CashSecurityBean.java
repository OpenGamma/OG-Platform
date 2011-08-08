/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.masterdb.security.hibernate.cash;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.masterdb.security.hibernate.CurrencyBean;
import com.opengamma.masterdb.security.hibernate.ExternalIdBean;
import com.opengamma.masterdb.security.hibernate.SecurityBean;
import com.opengamma.masterdb.security.hibernate.ZonedDateTimeBean;

/**
 * A bean representation of {@link CashSecurity}.
 */
public class CashSecurityBean extends SecurityBean {
  private CurrencyBean _currency;
  private ExternalIdBean _region;
  private ZonedDateTimeBean _maturity;
  /**
   * Gets the rate field.
   * @return the rate
   */
  public double getRate() {
    return _rate;
  }
  /**
   * Sets the rate field.
   * @param rate  the rate
   */
  public void setRate(double rate) {
    _rate = rate;
  }
  /**
   * Gets the amount field.
   * @return the amount
   */
  public double getAmount() {
    return _amount;
  }
  /**
   * Sets the amount field.
   * @param amount  the amount
   */
  
  public void setAmount(double amount) {
    _amount = amount;
  }

  private double _rate;
  private double _amount;
  
  public CashSecurityBean() {
    super();
  }
  /**
   * @return the region Identifier
   */
  public ExternalIdBean getRegion() {
    return _region;
  }

  /**
   * @param region the region Identifier to set
   */
  public void setRegion(final ExternalIdBean region) {
    _region = region;
  }

  /**
   * @return the currency
   */
  public CurrencyBean getCurrency() {
    return _currency;
  }

  /**
   * @param currency the currency to set
   */
  public void setCurrency(final CurrencyBean currency) {
    _currency = currency;
  }
  
  /**
   * @return the maturity
   */
  public ZonedDateTimeBean getMaturity() {
    return _maturity;
  }
  
  /**
   * @param maturity the maturity to set 
   */
  public void setMaturity(ZonedDateTimeBean maturity) {
    _maturity = maturity;
  }
  
  
  
  @Override
  public boolean equals(final Object other) {
    if (!(other instanceof CashSecurityBean)) {
      return false;
    }
    CashSecurityBean cash = (CashSecurityBean) other;
    return new EqualsBuilder()
      .append(getId(), cash.getId())
      .append(getCurrency(), cash.getCurrency())
      .append(getRegion(), cash.getRegion())
      .append(getMaturity(), cash.getMaturity())
      .append(getRate(), cash.getRate())
      .append(getAmount(), cash.getAmount()).isEquals();
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder()
      .append(getCurrency())
      .append(getRegion())
      .append(getMaturity())
      .append(getRate())
      .append(getAmount())
      .toHashCode();
  }
}
