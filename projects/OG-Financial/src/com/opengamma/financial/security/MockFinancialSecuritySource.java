/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.time.Instant;

import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.core.security.AbstractSecuritySource;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.IdUtils;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSupplier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.RegexUtils;

/**
 * A simple mutable implementation of a source of securities.
 * <p>
 * This class is intended for testing scenarios.
 * It is not thread-safe and must not be used in production.
 */
public class MockFinancialSecuritySource extends AbstractSecuritySource implements FinancialSecuritySource {

  /**
   * The securities keyed by identifier.
   */
  private final Map<ObjectId, Security> _securities = Maps.newHashMap();
  /**
   * The suppler of unique identifiers.
   */
  private final UniqueIdSupplier _uidSupplier;
  /**
   * Change manager.
   */
  private final BasicChangeManager _changeManager = new BasicChangeManager();

  /**
   * Creates the security master.
   */
  public MockFinancialSecuritySource() {
    _uidSupplier = new UniqueIdSupplier("Mock");
  }

  //-------------------------------------------------------------------------
  @Override
  public Security getSecurity(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    Security security = _securities.get(uniqueId.getObjectId());
    if (security == null) {
      throw new DataNotFoundException("Security not found: " + uniqueId);
    }
    return security;
  }

  @Override
  public Security getSecurity(ObjectId objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    Security security = _securities.get(objectId);
    if (security == null) {
      throw new DataNotFoundException("Security not found: " + objectId);
    }
    return security;
  }

  @Override
  public Collection<Security> getSecurities(ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    List<Security> result = new ArrayList<Security>();
    for (Security sec : _securities.values()) {
      if (sec.getExternalIdBundle().containsAny(bundle)) {
        result.add(sec);
      }
    }
    return result;
  }

  @Override
  public Collection<Security> getSecurities(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    // Versioning not supported
    return getSecurities(bundle);
  }

  @Override
  public Security getSecurity(ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    for (ExternalId secId : bundle.getExternalIds()) {
      for (Security sec : _securities.values()) {
        if (sec.getExternalIdBundle().contains(secId)) {
          return sec;
        }
      }
    }
    return null;
  }
  
  @Override
  public Security getSecurity(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    // Versioning not supported
    return getSecurity(bundle);
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
    IdUtils.setInto(security, _uidSupplier.get());
    _securities.put(security.getUniqueId().getObjectId(), security);
  }

  public void removeSecurity(Security security) {
    ArgumentChecker.notNull(security, "security");
    ArgumentChecker.notNull(security.getUniqueId(), "security.uniqueId");
    Security prev = _securities.remove(security.getUniqueId().getObjectId());
    if (prev == null) {
      throw new IllegalArgumentException("Security not found");
    }
    if (prev != security) {
      throw new IllegalArgumentException("Security passed was not the one in this source");
    }
    _changeManager.entityChanged(ChangeType.REMOVED, security.getUniqueId(), null, Instant.now());
  }

  
  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }
  
}
