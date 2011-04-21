/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.permission;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.livedata.entitlement.LiveDataEntitlementChecker;

/**
 * Implements a factory for {@link ViewPermissionProvider} instances.
 */
public interface ViewPermissionProviderFactory {

  /**
   * Obtains a {@link ViewPermissionProvider}.
   * 
   * @param securitySource  the security source, not null
   * @param entitlementChecker  the live data entitlement checker, not null
   * @return A new {@link ViewPermissionProvider} instance, not null
   */
  ViewPermissionProvider getViewPermissionProvider(SecuritySource securitySource, LiveDataEntitlementChecker entitlementChecker);
  
}
