/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.equity;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.core.security.Security;
import com.opengamma.masterdb.security.hibernate.CurrencyBean;
import com.opengamma.masterdb.security.hibernate.ExchangeBean;
import com.opengamma.masterdb.security.hibernate.SecurityBean;

/**
 * A concrete, JavaBean-based implementation of {@link Security}. 
 */
public class EquitySecurityBean extends SecurityBean {
  private ExchangeBean _exchange;
  private String _shortName;
  private String _companyName;
  private CurrencyBean _currency;
  private GICSCodeBean _gicsCode;
  
  // ExternalIds that might be valid for equities:
  // - Bloomberg ticker (in BbgId)
  // - CUSIP (in CUSIP)
  // - ISIN (in ISIN)
  // - Bloomberg Unique ID (in BbgUniqueId)
    
  public void setShortName(final String shortName) {
    _shortName = shortName;
  }
  
  public String getShortName() {
    return _shortName;
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

  /**
   * @return the gicsCode
   */
  public GICSCodeBean getGICSCode() {
    return _gicsCode;
  }

  /**
   * @param gicsCode the gicsCode to set
   */
  public void setGICSCode(GICSCodeBean gicsCode) {
    _gicsCode = gicsCode;
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
                              .append(getCurrency(), equity.getCurrency()).isEquals();
  }
  
  public int hashCode() {
    return new HashCodeBuilder().append(getExchange())
                                .append(getCompanyName())
                                .append(getCurrency())
                                .toHashCode(); 
  }
  
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
