/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.permission;

import java.util.Set;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.livedata.UserPrincipal;

/**
 * Used to query permissions on market data.
 */
public interface MarketDataPermissionProvider {

  boolean canAccessMarketData(UserPrincipal user, Set<ValueRequirement> requirements);
  
}
