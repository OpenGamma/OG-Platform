/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.model;

import java.util.Collection;
import java.util.Set;

import com.opengamma.core.security.Security;

/**
 * 
 *
 * @author yomi
 */
public class SecurityMasterResponseMessage {
  private Set<String> _allSecurityTypes;
  private Collection<Security> _securities;
  private Security _security;
  private Set<String> _optionChain;
  /**
   * @return the allSecurityTypes
   */
  public Set<String> getAllSecurityTypes() {
    return _allSecurityTypes;
  }
  /**
   * @param allSecurityTypes the allSecurityTypes to set
   */
  public void setAllSecurityTypes(Set<String> allSecurityTypes) {
    _allSecurityTypes = allSecurityTypes;
  }
  /**
   * @return the securities
   */
  public Collection<Security> getSecurities() {
    return _securities;
  }
  /**
   * @param securities the securities to set
   */
  public void setSecurities(Collection<Security> securities) {
    _securities = securities;
  }
  /**
   * @return the security
   */
  public Security getSecurity() {
    return _security;
  }
  /**
   * @param security the security to set
   */
  public void setSecurity(Security security) {
    _security = security;
  }
  /**
   * @return the optionChain
   */
  public Set<String> getOptionChain() {
    return _optionChain;
  }
  /**
   * @param optionChain the optionChain to set
   */
  public void setOptionChain(Set<String> optionChain) {
    _optionChain = optionChain;
  }
  
}
