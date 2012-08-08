/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_DATA_SOURCE_NAME;

import java.util.Map;

import com.opengamma.bbg.loader.BloombergBulkSecurityLoader;
import com.opengamma.financial.provider.security.SecurityProviderGetRequest;
import com.opengamma.financial.provider.security.SecurityProviderGetResult;
import com.opengamma.financial.provider.security.impl.AbstractSecurityProvider;
import com.opengamma.financial.timeseries.exchange.ExchangeDataProvider;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.security.ManageableSecurity;

/**
 * Provider of security information from the Bloomberg data source.
 */
public final class BloombergSecurityProvider extends AbstractSecurityProvider {

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
  protected SecurityProviderGetResult doBulkGet(SecurityProviderGetRequest request) {
    Map<ExternalIdBundle, ManageableSecurity> map = _bloombergBulkSecurityLoader.loadSecurity(request.getExternalIdBundles());
    SecurityProviderGetResult result = new SecurityProviderGetResult();
    result.getResultMap().putAll(map);
    return result;
  }

}
