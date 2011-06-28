/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.permission;

import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.livedata.UserPrincipal;

/**
 * Default implementation of {@code ViewPermissionProvider}.
 */
public class DefaultViewPermissionProvider implements ViewPermissionProvider {
  
  @Override
  public boolean canAccessCompiledViewDefinition(UserPrincipal user, CompiledViewDefinition compiledViewDefinition) {
    // REVIEW jonathan 2011-03-28 -- if/when we have fine-grained per-user permissions on view definitions or view
    // processes, then this would need to check against those.
    return true;
  }

  @Override
  public boolean canAccessComputationResults(UserPrincipal user, CompiledViewDefinition compiledViewDefinition, boolean hasMarketDataPermissions) {
    return hasMarketDataPermissions;
  }
  
}
