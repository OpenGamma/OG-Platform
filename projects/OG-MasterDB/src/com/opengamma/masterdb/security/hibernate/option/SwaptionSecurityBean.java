/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.option;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.financial.security.option.OptionSecurity;
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



  public Boolean iscashSettled() {
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
      .append(iscashSettled(), option.iscashSettled())
      .append(isLong(), option.isLong())
      .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
      .append(getExpiry())
      .append(getUnderlying())
      .append(iscashSettled())
      .append(isLong())
      .toHashCode();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}
