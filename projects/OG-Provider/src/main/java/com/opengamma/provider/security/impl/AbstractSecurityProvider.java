/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.security.impl;

import java.util.Collection;
import java.util.Map;

import com.opengamma.core.security.Security;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.security.SecurityProvider;
import com.opengamma.provider.security.SecurityProviderRequest;
import com.opengamma.provider.security.SecurityProviderResult;
import com.opengamma.util.ArgumentChecker;

/**
 * Abstract implementation of a provider of security information.
 * <p>
 * This provides default implementations of the interface methods that delegate to a
 * protected method that subclasses must implement.
 */
public abstract class AbstractSecurityProvider implements SecurityProvider {

  /**
   * The data source name.
   */
  private final String _dataSourceRegex;

  /**
   * Creates an instance.
   * 
   * @param dataSourceRegex  the data source regex, not null
   */
  public AbstractSecurityProvider(String dataSourceRegex) {
    ArgumentChecker.notNull(dataSourceRegex, "dataSourceRegex");
    _dataSourceRegex = dataSourceRegex;
  }

  //-------------------------------------------------------------------------
  @Override
  public Security getSecurity(ExternalIdBundle externalIdBundle) {
    SecurityProviderRequest request = SecurityProviderRequest.createGet(externalIdBundle, null);
    SecurityProviderResult result = getSecurities(request);
    return result.getResultMap().get(externalIdBundle);
  }

  @Override
  public Map<ExternalIdBundle, Security> getSecurities(Collection<ExternalIdBundle> externalIdBundles) {
    SecurityProviderRequest request = SecurityProviderRequest.createGet(externalIdBundles, null);
    SecurityProviderResult result = getSecurities(request);
    return result.getResultMap();
  }

  //-------------------------------------------------------------------------
  @Override
  public SecurityProviderResult getSecurities(SecurityProviderRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.isTrue(request.getDataSource() == null ||
        request.getDataSource().matches(_dataSourceRegex), "Unsupported data source: " + request.getDataSource());
    
    // short-cut empty case
    if (request.getExternalIdBundles().isEmpty()) {
      return new SecurityProviderResult();
    }
    
    // get securities
    return doBulkGet(request);
  }

  /**
   * Gets the securities.
   * <p>
   * The data source is checked before this method is invoked.
   * 
   * @param request  the request, with a non-empty set of identifiers, not null
   * @return the result, not null
   */
  protected abstract SecurityProviderResult doBulkGet(SecurityProviderRequest request);

}
