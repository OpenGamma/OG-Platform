/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.option;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.masterdb.security.hibernate.CurrencyBean;
import com.opengamma.masterdb.security.hibernate.ExpiryBean;
import com.opengamma.masterdb.security.hibernate.IdentifierBean;
import com.opengamma.masterdb.security.hibernate.SecurityBean;

/**
 * A bean representation of a {@link OptionSecurity}.
 */
public class SwaptionSecurityBean extends SecurityBean {

  private ExpiryBean _expiry;
  private IdentifierBean _underlying;
  private Boolean _cashSettled;
  private Boolean _long;
  private Boolean _payer;
  private CurrencyBean _currency;
  
  public SwaptionSecurityBean() {
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

  /**
   * @return the underlyingIdentifier
   */
  public IdentifierBean getUnderlying() {
    return _underlying;
  }

  /**
   * @param underlying the underlyingIdentifier to set
   */
  public void setUnderlying(final IdentifierBean underlying) {
    _underlying = underlying;
  }



  public Boolean isCashSettled() {
    return _cashSettled;
  }

  public void setCashSettled(final Boolean cashSettled) {
    _cashSettled = cashSettled;
  }
  
  public Boolean isLong() {
    return _long;
  }

  public void setLong(final Boolean aLong) {
    _long = aLong;
  }
  
  /**
   * Gets the payer.
   * @return the payer
   */
  public Boolean isPayer() {
    return _payer;
  }

  /**
   * Sets the payer.
   * @param payer  the payer
   */
  public void setPayer(Boolean payer) {
    _payer = payer;
  }
  
  /**
   * Gets the currency.
   * @return the currency
   */
  public CurrencyBean getCurrency() {
    return _currency;
  }

  /**
   * Sets the currency.
   * @param currency  the currency
   */
  public void setCurrency(CurrencyBean currency) {
    _currency = currency;
  }

  @Override
  public boolean equals(final Object other) {
    if (!(other instanceof SwaptionSecurityBean)) {
      return false;
    }
    final SwaptionSecurityBean option = (SwaptionSecurityBean) other;
    //    if (getId() != -1 && option.getId() != -1) {
    //      return getId().longValue() == option.getId().longValue();
    //    }
    return new EqualsBuilder()
      .append(getId(), option.getId())
      .append(getExpiry(), option.getExpiry())
      .append(getUnderlying(), option.getUnderlying())
      .append(isCashSettled(), option.isCashSettled())
      .append(isLong(), option.isLong())
      .append(isPayer(), option.isPayer())
      .append(getCurrency(), getCurrency())
      .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
      .append(getExpiry())
      .append(getUnderlying())
      .append(isCashSettled())
      .append(isLong())
      .append(isPayer())
      .append(getCurrency())
      .toHashCode();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}
