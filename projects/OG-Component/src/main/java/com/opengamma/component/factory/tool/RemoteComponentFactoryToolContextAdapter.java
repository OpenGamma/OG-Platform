/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.tool;

import java.util.Arrays;
import java.util.List;

import com.opengamma.component.factory.RemoteComponentFactory;
import com.opengamma.financial.convention.DefaultConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.tool.ToolContext;

/**
 * A convenience class to pull the most likely desired masters and sources from a RemoteComponentFactory
 * and populate a ToolContext.  This eases porting of other tools that use the ToolContext.
 */
public class RemoteComponentFactoryToolContextAdapter extends ToolContext {

  /**
   * A set of classifiers to search for.
   */
  private static final List<String> DEFAULT_CLASSIFIER_CHAIN = Arrays.asList(new String[] {"central", "main", "default", "shared", "combined", "standard" });

  /**
   * Creates an instance.
   * 
   * @param remoteComponentFactory  the remote factory, not null
   */
  public RemoteComponentFactoryToolContextAdapter(RemoteComponentFactory remoteComponentFactory) {
    this(remoteComponentFactory, DEFAULT_CLASSIFIER_CHAIN);
  }

  /**
   * Creates an instance.
   * 
   * @param remoteComponentFactory  the remote factory, not null
   * @param classifierPreferences  the classifiers to search for, not null
   */
  public RemoteComponentFactoryToolContextAdapter(RemoteComponentFactory remoteComponentFactory, List<String> classifierPreferences) {
    setConfigMaster(remoteComponentFactory.getConfigMaster(classifierPreferences));
    setExchangeMaster(remoteComponentFactory.getExchangeMaster(classifierPreferences));
    setHolidayMaster(remoteComponentFactory.getHolidayMaster(classifierPreferences));
    setRegionMaster(remoteComponentFactory.getRegionMaster(classifierPreferences));
    setSecurityMaster(remoteComponentFactory.getSecurityMaster(classifierPreferences));
    setPositionMaster(remoteComponentFactory.getPositionMaster(classifierPreferences));
    setPortfolioMaster(remoteComponentFactory.getPortfolioMaster(classifierPreferences));
    setLegalEntityMaster(remoteComponentFactory.getLegalEntityMaster(classifierPreferences));
    setHistoricalTimeSeriesMaster(remoteComponentFactory.getHistoricalTimeSeriesMaster(classifierPreferences));
    setMarketDataSnapshotMaster(remoteComponentFactory.getMarketDataSnapshotMaster(classifierPreferences));
    
    setConfigSource(remoteComponentFactory.getConfigSource(classifierPreferences));
    setExchangeSource(remoteComponentFactory.getExchangeSource(classifierPreferences));
    setHolidaySource(remoteComponentFactory.getHolidaySource(classifierPreferences));
    setRegionSource(remoteComponentFactory.getRegionSource(classifierPreferences));
    setSecuritySource(remoteComponentFactory.getSecuritySource(classifierPreferences));
    setPositionSource(remoteComponentFactory.getPositionSource(classifierPreferences));
    setLegalEntitySource(remoteComponentFactory.getLegalEntitySource(classifierPreferences));
    setHistoricalTimeSeriesSource(remoteComponentFactory.getHistoricalTimeSeriesSource(classifierPreferences));
    setMarketDataSnapshotSource(remoteComponentFactory.getMarketDataSnapshotSource(classifierPreferences));
    
    setSecurityLoader(remoteComponentFactory.getSecurityLoader(classifierPreferences));
    setHistoricalTimeSeriesLoader(remoteComponentFactory.getHistoricalTimeSeriesLoader(classifierPreferences));
    
    // this may need customizing per-project
    setConventionBundleSource(new DefaultConventionBundleSource(new InMemoryConventionBundleMaster()));
    
    setFunctionConfigSource(remoteComponentFactory.getFunctionConfigurationSource(classifierPreferences));
  }

  @Override
  public void close() {
    // No need to shutdown remote components
  }

}
