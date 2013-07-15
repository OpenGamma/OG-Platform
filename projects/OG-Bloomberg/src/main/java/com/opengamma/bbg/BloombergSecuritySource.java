/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import static com.opengamma.bbg.BloombergConstants.FIELD_SECURITY_TYPE;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.bbg.loader.BloombergBulkSecurityLoader;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.util.ReferenceDataProviderUtils;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.AbstractSecuritySource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.timeseries.exchange.ExchangeDataProvider;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * A security source based on the Bloomberg data source.
 */
public final class BloombergSecuritySource extends AbstractSecuritySource implements SecuritySource {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergSecuritySource.class);
  /**
   * Bloomberg scheme.
   */
  public static final String BLOOMBERG_SCHEME = "Bloomberg";

  /**
   * The Bloomberg data source.
   */
  private final BloombergBulkSecurityLoader _bloombergBulkSecurityLoader;
  /**
   * The reference data provider.
   */
  private final ReferenceDataProvider _refDataProvider;

  /**
   * Creates a unique identifier.
   * 
   * @param value the value, not null
   * @return a Bloomberg unique identifier, not null
   */
  public static UniqueId createUniqueId(String value) {
    return UniqueId.of(BLOOMBERG_SCHEME, value);
  }

  /**
   * Creates the security master.
   * 
   * @param refDataProvider the reference data provider, not null
   * @param exchangeDataProvider the data provider, not null
   */
  public BloombergSecuritySource(ReferenceDataProvider refDataProvider, ExchangeDataProvider exchangeDataProvider) {
    ArgumentChecker.notNull(refDataProvider, "Reference Data Provider");
    ArgumentChecker.notNull(exchangeDataProvider, "Exchange Data Provider");
    _refDataProvider = refDataProvider;
    _bloombergBulkSecurityLoader = new BloombergBulkSecurityLoader(refDataProvider, exchangeDataProvider);
  }

  //-------------------------------------------------------------------------
  @Override
  public Security get(UniqueId uniqueId) {
    if (BLOOMBERG_SCHEME.equals(uniqueId.getScheme()) == false) {
      throw new IllegalArgumentException("Identifier must be a Bloomberg unique identifier: " + uniqueId);
    }
    return getSecurity(uniqueId.getValue());
  }

  @Override
  public Security get(ObjectId objectId, VersionCorrection versionCorrection) {
    if (BLOOMBERG_SCHEME.equals(objectId.getScheme()) == false) {
      throw new IllegalArgumentException("Identifier must be a Bloomberg object identifier: " + objectId);
    }
    return getSecurity(objectId.getValue());
  }

  @Override
  public Collection<Security> get(ExternalIdBundle bundle) {
    Security sec = getSingle(bundle);
    if (sec != null) {
      return Collections.<Security>singleton(getSingle(bundle));
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public Collection<Security> get(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    return get(bundle);
  }

  @Override
  public ManageableSecurity getSingle(ExternalIdBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    Validate.isTrue(bundle.size() > 0, "Cannot load security for empty identifiers");

    Map<ExternalIdBundle, ManageableSecurity> securities = _bloombergBulkSecurityLoader.loadSecurity(Collections.singleton(bundle));
    if (securities.size() == 1) {
      return securities.get(bundle);
    } else {
      s_logger.warn("Bloomberg return security={} for id={}", securities.values(), bundle);
      return null;
    }
  }

  @Override
  public Security getSingle(ExternalIdBundle bundle, VersionCorrection versionCorrection) {
    return getSingle(bundle);
  }

  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return DummyChangeManager.INSTANCE;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the security type by id.
   * 
   * @param securityID the security id, null returns null
   * @return the security type, null if not found
   */
  /* package for testing */String getSecurityType(final String securityID) {
    return ReferenceDataProviderUtils.singleFieldSearch(securityID, FIELD_SECURITY_TYPE, _refDataProvider);
  }

  private Security getSecurity(String bbgIdValue) {
    ExternalId bbgId = ExternalSchemes.bloombergBuidSecurityId(bbgIdValue);
    ExternalIdBundle bundle = ExternalIdBundle.of(bbgId);
    return getSingle(bundle);
  }

  @Override
  public Map<UniqueId, Security> get(Collection<UniqueId> uniqueIds) {
    final Map<UniqueId, Security> result = Maps.newHashMap();
    Map<ExternalIdBundle, UniqueId> uniqueIdMap = createBundle2UniqueIdMap(uniqueIds);
    Map<ExternalIdBundle, ManageableSecurity> securities = _bloombergBulkSecurityLoader.loadSecurity(uniqueIdMap.keySet());
    for (Entry<ExternalIdBundle, ManageableSecurity> entry : securities.entrySet()) {
      result.put(uniqueIdMap.get(entry.getKey()), entry.getValue());
    }
    return result;
  }

  private Map<ExternalIdBundle, UniqueId> createBundle2UniqueIdMap(Collection<UniqueId> uniqueIds) {
    Map<ExternalIdBundle, UniqueId> result = Maps.newHashMap();
    for (UniqueId uniqueId : uniqueIds) {
      if (BLOOMBERG_SCHEME.equals(uniqueId.getScheme()) == false) {
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
