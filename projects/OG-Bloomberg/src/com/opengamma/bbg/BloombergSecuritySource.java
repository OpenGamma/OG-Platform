/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.bbg.security.BloombergSecurityProvider;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.provider.security.SecurityProvider;
import com.opengamma.util.ArgumentChecker;

/**
 * A security source based on the Bloomberg data source.
 * This class is now implemented on top of SecurityProvider and is effectively deprecated. 
 */
public final class BloombergSecuritySource implements SecuritySource {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergSecuritySource.class);

  /**
   * The provider.
   */
  private final SecurityProvider _provider;

  /**
   * Creates an instance.
   * 
   * @param provider  the security provider, not null
   */
  public BloombergSecuritySource(SecurityProvider provider) {
    ArgumentChecker.notNull(provider, "provider");
    _provider = provider;
  }

  //-------------------------------------------------------------------------
  @Override
  public Security getSecurity(UniqueId uniqueId) {
    if (BloombergSecurityProvider.BLOOMBERG_SCHEME.equals(uniqueId.getScheme()) == false) {
      throw new IllegalArgumentException("Identifier must be a Bloomberg unique identifier: " + uniqueId);
    }
    return getSecurity(uniqueId.getValue());
  }

  @Override
  public Security getSecurity(ObjectId objectId, VersionCorrection versionCorrection) {
    if (BloombergSecurityProvider.BLOOMBERG_SCHEME.equals(objectId.getScheme()) == false) {
      throw new IllegalArgumentException("Identifier must be a Bloomberg object identifier: " + objectId);
    }
    return getSecurity(objectId.getValue());
  }

  @Override
  public Collection<Security> getSecurities(ExternalIdBundle bundle) {
    Security sec = getSecurity(bundle);
    if (sec != null) {
      return Collections.<Security>singleton(getSecurity(bundle));
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public Collection<Security> getSecurities(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    return getSecurities(bundle);
  }

  @Override
  public Security getSecurity(ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    Validate.isTrue(bundle.size() > 0, "Cannot load security for empty identifiers");
    
    Security security = _provider.getSecurity(bundle);
    if (security == null) {
      s_logger.warn("Bloomberg returned no security for id {}", bundle);
    }
    return security;
  }

  @Override
  public Security getSecurity(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    return getSecurity(bundle);
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return DummyChangeManager.INSTANCE;
  }

  private Security getSecurity(String bbgIdValue) {
    ExternalId bbgId = ExternalSchemes.bloombergBuidSecurityId(bbgIdValue);
    ExternalIdBundle bundle = ExternalIdBundle.of(bbgId);
    return getSecurity(bundle);
  }

  @Override
  public Map<UniqueId, Security> getSecurities(Collection<UniqueId> uniqueIds) {
    final Map<UniqueId, Security> result = Maps.newHashMap();
    Map<ExternalIdBundle, UniqueId> uniqueIdMap = createBundle2UniqueIdMap(uniqueIds);
    Map<ExternalIdBundle, Security> securities = _provider.getSecurities(uniqueIdMap.keySet());
    for (Entry<ExternalIdBundle, Security> entry : securities.entrySet()) {
      result.put(uniqueIdMap.get(entry.getKey()), entry.getValue());
    }
    return result;
  }

  private Map<ExternalIdBundle, UniqueId> createBundle2UniqueIdMap(Collection<UniqueId> uniqueIds) {
    Map<ExternalIdBundle, UniqueId> result = Maps.newHashMap();
    for (UniqueId uniqueId : uniqueIds) {
      if (BloombergSecurityProvider.BLOOMBERG_SCHEME.equals(uniqueId.getScheme()) == false) {
        throw new IllegalArgumentException("Identifier must be a Bloomberg unique identifier: " + uniqueId);
      }
      String bbgIdValue = uniqueId.getValue();
      ExternalId bbgId = ExternalSchemes.bloombergBuidSecurityId(bbgIdValue);
      ExternalIdBundle bundle = ExternalIdBundle.of(bbgId);
      result.put(bundle, uniqueId);
    }
    return result;
  }

}
