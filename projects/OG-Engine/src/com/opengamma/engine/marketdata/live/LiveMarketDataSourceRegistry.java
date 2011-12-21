/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import java.util.Collection;

import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;

/**
 * A registry which provides options for the DataSource in {@link LiveMarketDataSpecification} 
 */
public interface LiveMarketDataSourceRegistry {
  
  //TODO PLAT-1080  
  Collection<String> getDataSources();
  
}
