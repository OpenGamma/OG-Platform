/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * A concrete, JavaBean-based implementation of {@link Security}. 
 *
 * @author kirk
 */

public class EquitySecurityBean extends SecurityBean {
  private ExchangeBean _exchange;
  private String _companyName;
  private CurrencyBean _currency;
  
  // Identifiers that might be valid for equities:
  // - Bloomberg ticker (in BbgId)
  // - CUSIP (in CUSIP)
  // - ISIN (in ISIN)
  // - Bloomberg Unique ID (in BbgUniqueId)
    
  /**
   * 
   */
  public EquitySecurityBean() {
    super();
  }
  
  /**
   * This should be removed after the demo is fully Bloomberg modified.
   * 
   * @param domain
   * @param domainIdentifier
   */
  public EquitySecurityBean(ExchangeBean exchange, String companyName, CurrencyBean currency) {
    this();
    _exchange = exchange;
    _companyName = companyName;
    _currency = currency;
  }

  /**
   * @return the exchange
   */
  public ExchangeBean getExchange() {
    return _exchange;
  }

  /**
   * @param exchange the exchange to set
   */
  public void setExchange(ExchangeBean exchange) {
    _exchange = exchange;
  }

  /**
   * @return the companyName
   */
  public String getCompanyName() {
    return _companyName;
  }

  /**
   * @param companyName the companyName to set
   */
  public void setCompanyName(String companyName) {
    _companyName = companyName;
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
  public void setCurrency(CurrencyBean currency) {
    _currency = currency;
  }

  public boolean equals(Object other) {
    if (!(other instanceof EquitySecurityBean)) {
      return false;
    }
    EquitySecurityBean equity = (EquitySecurityBean) other;
    if (getId() != -1 && equity.getId() != -1) {
      return getId().longValue() == equity.getId().longValue();
    }
    return new EqualsBuilder().append(getExchange(), equity.getExchange())
                              .append(getCompanyName(), equity.getCompanyName())
                              .append(getCurrency(), equity.getCurrency())
                              .append(getEffectiveDateTime(), equity.getEffectiveDateTime())
                              .append(isDeleted(), equity.isDeleted()).isEquals(); 
  }
  
  public int hashCode() {
    return new HashCodeBuilder().append(getExchange())
                                .append(getCompanyName())
                                .append(getCurrency())
                                .append(getEffectiveDateTime())
                                .append(isDeleted())
                                .toHashCode(); 
  }
  
  @Override
  public <T> T accept(SecurityBeanVisitor<T> visitor) {
    return visitor.visitEquitySecurityBean(this);
  }
  
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
