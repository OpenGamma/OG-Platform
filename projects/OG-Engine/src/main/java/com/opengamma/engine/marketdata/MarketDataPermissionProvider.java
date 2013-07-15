/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.Set;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.PublicSPI;

/**
 * Used to query permissions on market data.
 */
@PublicSPI
public interface MarketDataPermissionProvider {

  /**
   * Checks whether has permission to view market data and returns the values which were in the query but the user <em>doesn't</em> have permission for.
   * 
   * @param user The user whose market data permissions should be checked
   * @param specifications The market data to test for
   * @return The values for which the user <em>doesn't</em> have permission
   */
  Set<ValueSpecification> checkMarketDataPermissions(UserPrincipal user, Set<ValueSpecification> specifications);

}
