/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.security;

import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_DATA_SOURCE_NAME;

import java.util.Map;

import com.opengamma.bbg.loader.BloombergBulkSecurityLoader;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.financial.timeseries.exchange.ExchangeDataProvider;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.provider.security.SecurityProviderRequest;
import com.opengamma.provider.security.SecurityProviderResult;
import com.opengamma.provider.security.impl.AbstractSecurityProvider;

/**
 * Provider of security information from the Bloomberg data source.
 */
public final class BloombergSecurityProvider extends AbstractSecurityProvider {

  /**
   * Bloomberg scheme.
   */
  public static final String BLOOMBERG_SCHEME = "Bloomberg";

  /**
   * The underlying loader.
   */
  private final BloombergBulkSecurityLoader _bloombergBulkSecurityLoader;

  /**
   * Creates an instance.
   * 
   * @param refDataProvider  the reference data provider, not null
   * @param exchangeDataProvider  the data provider, not null
   */
  public BloombergSecurityProvider(ReferenceDataProvider refDataProvider, ExchangeDataProvider exchangeDataProvider) {
    super(BLOOMBERG_DATA_SOURCE_NAME);
    _bloombergBulkSecurityLoader = new BloombergBulkSecurityLoader(refDataProvider, exchangeDataProvider);
  }

  //-------------------------------------------------------------------------
  @Override
  protected SecurityProviderResult doBulkGet(SecurityProviderRequest request) {
    Map<ExternalIdBundle, ManageableSecurity> map = _bloombergBulkSecurityLoader.loadSecurity(request.getExternalIdBundles());
    return new SecurityProviderResult(map);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a unique identifier.
   * 
   * @param value  the value, not null
   * @return a Bloomberg unique identifier, not null
   */
  public static UniqueId createUniqueId(String value) {
    return UniqueId.of(BLOOMBERG_SCHEME, value);
  }

}
