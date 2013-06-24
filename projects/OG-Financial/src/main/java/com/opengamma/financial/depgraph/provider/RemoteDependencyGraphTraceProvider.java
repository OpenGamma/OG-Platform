/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.depgraph.provider;

import java.net.URI;

import org.threeten.bp.Instant;

import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.financial.depgraph.rest.DependencyGraphBuildTrace;
import com.opengamma.financial.depgraph.rest.DependencyGraphTraceProviderResource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * Provides remote access to a {@link DependencyGraphTraceProvider}.
 */
public class RemoteDependencyGraphTraceProvider extends AbstractRemoteClient implements DependencyGraphTraceProvider {

  /**
   * Creates an instance.
   * 
   * @param baseUri the target URI for all RESTful web services, not null
   */
  public RemoteDependencyGraphTraceProvider(URI baseUri) {
    super(baseUri);
  }

  @Override
  public DependencyGraphBuildTrace getTraceWithCalculationConfigurationName(String calculationConfigurationName) {
    ArgumentChecker.notNull(calculationConfigurationName, "calculationConfigurationName");
    
    URI uri = DependencyGraphTraceProviderResource.uriCalculationConfigurationName(getBaseUri(), calculationConfigurationName);
    return accessRemote(uri).get(DependencyGraphBuildTrace.class);
  }

  @Override
  public DependencyGraphBuildTrace getTraceWithValuationTime(Instant valuationTime) {
    ArgumentChecker.notNull(valuationTime, "valuationTime");
    
    URI uri = DependencyGraphTraceProviderResource.uriValuationTime(getBaseUri(), valuationTime);
    return accessRemote(uri).get(DependencyGraphBuildTrace.class);  
  }

  @Override
  public DependencyGraphBuildTrace getTraceWithResolutionTime(VersionCorrection resolutionTime) {
    ArgumentChecker.notNull(resolutionTime, "resolutionTime");
    
    URI uri = DependencyGraphTraceProviderResource.uriResolutionTime(getBaseUri(), resolutionTime);
    return accessRemote(uri).get(DependencyGraphBuildTrace.class);  
  }

  @Override
  public DependencyGraphBuildTrace getTraceWithDefaultProperties(ValueProperties defaultProperties) {
    ArgumentChecker.notNull(defaultProperties, "defaultProperties");
    
    URI uri = DependencyGraphTraceProviderResource.uriDefaultProperties(getBaseUri(), defaultProperties);
    return accessRemote(uri).get(DependencyGraphBuildTrace.class);  
  }

  @Override
  public DependencyGraphBuildTrace getTraceWithValueRequirementByUniqueId(String valueName, String targetType, UniqueId uniqueId) {
    ArgumentChecker.notNull(valueName, "valueName");
    ArgumentChecker.notNull(targetType, "targetType");
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    URI uri = DependencyGraphTraceProviderResource.uriValueRequirementByUniqueId(getBaseUri(), valueName, targetType, uniqueId);
    return accessRemote(uri).get(DependencyGraphBuildTrace.class);  
  }

  @Override
  public DependencyGraphBuildTrace getTraceWithValueRequirementByExternalId(String valueName, String targetType, ExternalId externalId) {
    ArgumentChecker.notNull(valueName, "valueName");
    ArgumentChecker.notNull(targetType, "targetType");
    ArgumentChecker.notNull(externalId, "externalId");
    
    URI uri = DependencyGraphTraceProviderResource.uriValueRequirementByExternalId(getBaseUri(), valueName, targetType, externalId);
    return accessRemote(uri).get(DependencyGraphBuildTrace.class);  
  }

  @Override
  public DependencyGraphBuildTrace getTraceWithMarketData(UserMarketDataSpecification marketData) {
    ArgumentChecker.notNull(marketData, "marketData");
    
    URI uri = DependencyGraphTraceProviderResource.uriMarketData(getBaseUri(), marketData);
    return accessRemote(uri).get(DependencyGraphBuildTrace.class);  
  }
  

}
