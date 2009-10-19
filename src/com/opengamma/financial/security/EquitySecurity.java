/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.engine.security.DefaultSecurity;
import com.opengamma.engine.security.Security;
import com.opengamma.financial.securities.Currency;
import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.id.IdentificationDomain;

/**
 * A concrete, JavaBean-based implementation of {@link Security}. 
 *
 * @author kirk
 */
public class EquitySecurity extends DefaultSecurity {
  public static final String EQUITY_TYPE = "EQUITY";
  private String _ticker;
  private String _exchange;
  private String _companyName;
  private Currency _currency; 
  
  // Identifiers that might be valid for equities:
  // - Bloomberg ticker (in BbgId)
  // - CUSIP (in CUSIP)
  // - ISIN (in ISIN)
  // - Bloomberg Unique ID (in BbgUniqueId)
    
  /**
   * 
   */
  public EquitySecurity() {
    super();
    setSecurityType(EQUITY_TYPE);
    setIdentifiers(Collections.<DomainSpecificIdentifier>emptyList());
  }
  
  /**
   * This should be removed after the demo is fully Bloomberg modified.
   * 
   * @param ticker
   * @param domain
   */
  public EquitySecurity(String ticker, String domain) {
    this();
    addDomainSpecificIdentifier(ticker, domain);
  }
  
  public void addDomainSpecificIdentifier(DomainSpecificIdentifier identifier) {
    // REVIEW kirk 2009-10-19 -- Is this the right approach?
    Set<DomainSpecificIdentifier> identifiers = new HashSet<DomainSpecificIdentifier>(getIdentifiers());
    identifiers.add(identifier);
    setIdentifiers(identifiers);
  }
  
  public void addDomainSpecificIdentifier(String domainValue, String domain) {
    addDomainSpecificIdentifier(new DomainSpecificIdentifier(new IdentificationDomain(domain), domainValue));
  }

  /**
   * @return the ticker
   */
  public String getTicker() {
    return _ticker;
  }

  /**
   * @param ticker the ticker to set
   */
  public void setTicker(String ticker) {
    _ticker = ticker;
  }

  /**
   * @return the exchange
   */
  public String getExchange() {
    return _exchange;
  }

  /**
   * @param exchange the exchange to set
   */
  public void setExchange(String exchange) {
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
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * @param currency the currency to set
   */
  public void setCurrency(Currency currency) {
    _currency = currency;
  }
  
}
