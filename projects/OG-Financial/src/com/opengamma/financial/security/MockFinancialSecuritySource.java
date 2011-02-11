/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.core.security.Security;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifiables;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.UniqueIdentifierSupplier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.RegexUtils;

/**
 * A simple mutable implementation of a source of securities.
 * <p>
 * This class is intended for testing scenarios.
 * It is not thread-safe and must not be used in production.
 */
public class MockFinancialSecuritySource implements FinancialSecuritySource {

  /**
   * The securities keyed by identifier.
   */
  private final Map<UniqueIdentifier, Security> _securities = new HashMap<UniqueIdentifier, Security>();
  /**
   * The suppler of unique identifiers.
   */
  private final UniqueIdentifierSupplier _uidSupplier;

  /**
   * Creates the security master.
   */
  public MockFinancialSecuritySource() {
    _uidSupplier = new UniqueIdentifierSupplier("Mock");
  }

  //-------------------------------------------------------------------------
  @Override
  public Security getSecurity(UniqueIdentifier identifier) {
    return identifier == null ? null : _securities.get(identifier);
  }

  @Override
  public Collection<Security> getSecurities(IdentifierBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    List<Security> result = new ArrayList<Security>();
    for (Security sec : _securities.values()) {
      if (sec.getIdentifiers().containsAny(bundle)) {
        result.add(sec);
      }
    }
    return result;
  }

  @Override
  public Security getSecurity(IdentifierBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    for (Identifier secId : bundle.getIdentifiers()) {
      for (Security sec : _securities.values()) {
        if (sec.getIdentifiers().contains(secId)) {
          return sec;
        }
      }
    }
    return null;
  }

  @Override
  public Collection<Security> getBondsWithIssuerName(String issuerName) {
    ArgumentChecker.notNull(issuerName, "issuerName");
    List<Security> result = new ArrayList<Security>();
    for (Security sec : _securities.values()) {
      if (sec instanceof BondSecurity && RegexUtils.wildcardMatch(issuerName, ((BondSecurity) sec).getIssuerName())) {
        result.add(sec);
      }
    }
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a security to the master.
   * 
   * @param security  the security to add, not null
   */
  public void addSecurity(Security security) {
    ArgumentChecker.notNull(security, "security");
    UniqueIdentifiables.setInto(security, _uidSupplier.get());
    _securities.put(security.getUniqueId(), security);
  }

}
