/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */

package com.opengamma.masterdb.security.hibernate.cash;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.masterdb.security.hibernate.CurrencyBean;
import com.opengamma.masterdb.security.hibernate.IdentifierBean;
import com.opengamma.masterdb.security.hibernate.SecurityBean;
import com.opengamma.masterdb.security.hibernate.ZonedDateTimeBean;

/**
 * A bean representation of {@link CashSecurity}.
 */
public class CashSecurityBean extends SecurityBean {
  private CurrencyBean _currency;
  private IdentifierBean _region;
  private ZonedDateTimeBean _maturity;
  
  public CashSecurityBean() {
    super();
  }
  /**
   * @return the region Identifier
   */
  public IdentifierBean getRegion() {
    return _region;
  }

  /**
   * @param region the region Identifier to set
   */
  public void setRegion(final IdentifierBean region) {
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
      .append(getMaturity(), cash.getMaturity()).isEquals();
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder()
      .append(getCurrency())
      .append(getRegion())
      .append(getMaturity())
      .toHashCode();
  }
}
