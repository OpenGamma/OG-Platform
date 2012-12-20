/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.permission;

import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.PublicAPI;

/**
 * Provides the ability to determine permissions on a view.
 */
@PublicAPI
public interface ViewPermissionProvider {

  /**
   * Determines whether a user has permission to access a compiled view definition.
   * 
   * @param user  the user, not null
   * @param compiledViewDefinition  the view compilation output to which access is being determined, not null
   * @return true if the user may access the compilation output
   */
  boolean canAccessCompiledViewDefinition(UserPrincipal user, CompiledViewDefinition compiledViewDefinition);

  /**
   * Determines whether a user has permission to access the computation results of a view process. This should take
   * into account permission to access the underlying market data as well as any other permissions required.
   * 
   * @param user  the user, not null
   * @param compiledViewDefinition  the view compilation from which computation results would be produced, not null
   * @param hasMarketDataPermissions  true if the user has permission to access the market data requirements
   * @return true if the user may access results produced from the view compilation
   */
  boolean canAccessComputationResults(UserPrincipal user, CompiledViewDefinition compiledViewDefinition, boolean hasMarketDataPermissions);

}
