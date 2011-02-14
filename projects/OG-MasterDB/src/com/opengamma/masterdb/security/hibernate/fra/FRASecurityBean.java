/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.masterdb.security.hibernate.fra;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.masterdb.security.hibernate.CurrencyBean;
import com.opengamma.masterdb.security.hibernate.IdentifierBean;
import com.opengamma.masterdb.security.hibernate.SecurityBean;
import com.opengamma.masterdb.security.hibernate.ZonedDateTimeBean;

/**
 * A bean representation of {@link FRASecurity}.
 */
public class FRASecurityBean extends SecurityBean {
  private CurrencyBean _currency;
  private IdentifierBean _region;
  private ZonedDateTimeBean _startDate;
  private ZonedDateTimeBean _endDate;
  private double _rate;
  private double _amount;
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

  
  /**
   * Gets the currency field.
   * @return the currency
   */
  public CurrencyBean getCurrency() {
    return _currency;
  }

  /**
   * Sets the currency field.
   * @param currency  the currency
   */
  public void setCurrency(CurrencyBean currency) {
    _currency = currency;
  }

  /**
   * Gets the region field.
   * @return the region
   */
  public IdentifierBean getRegion() {
    return _region;
  }

  /**
   * Sets the region field.
   * @param region  the region
   */
  public void setRegion(IdentifierBean region) {
    _region = region;
  }

  /**
   * Gets the startDate field.
   * @return the startDate
   */
  public ZonedDateTimeBean getStartDate() {
    return _startDate;
  }

  /**
   * Sets the startDate field.
   * @param startDate  the startDate
   */
  public void setStartDate(ZonedDateTimeBean startDate) {
    _startDate = startDate;
  }

  /**
   * Gets the endDate field.
   * @return the endDate
   */
  public ZonedDateTimeBean getEndDate() {
    return _endDate;
  }

  /**
   * Sets the endDate field.
   * @param endDate  the endDate
   */
  public void setEndDate(ZonedDateTimeBean endDate) {
    _endDate = endDate;
  }

  @Override
  public boolean equals(final Object other) {
    if (!(other instanceof FRASecurityBean)) {
      return false;
    }
    FRASecurityBean fra = (FRASecurityBean) other;
    return new EqualsBuilder()
      .append(getId(), fra.getId())
      .append(getCurrency(), fra.getCurrency())
      .append(getRegion(), fra.getRegion())
      .append(getStartDate(), fra.getStartDate())
      .append(getEndDate(), fra.getEndDate())
      .append(getRate(), fra.getRate())
      .append(getAmount(), fra.getAmount()).isEquals();
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder()
      .append(getCurrency())
      .append(getRegion())
      .append(getStartDate())
      .append(getStartDate())
      .append(getRate())
      .append(getAmount())
      .toHashCode();
  }


}
