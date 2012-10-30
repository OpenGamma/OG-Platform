/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.permission;

import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.livedata.UserPrincipal;

/**
 * Implementation of {@link ViewPermissionProvider} that always responds positively.
 */
public class PermissiveViewPermissionProvider implements ViewPermissionProvider {

  @Override
  public boolean canAccessCompiledViewDefinition(UserPrincipal user, CompiledViewDefinition compiledViewDefinition) {
    return true;
  }

  @Override
  public boolean canAccessComputationResults(UserPrincipal user, CompiledViewDefinition compiledViewDefinition, boolean hasMarketDataPermissions) {
    return true;
  }

}
