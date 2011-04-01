/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.permission;

import com.opengamma.engine.view.compilation.CompiledViewDefinitionImpl;
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
   * @return {@code true} if the user may access the compilation output, {@code false} otherwise.
   */
  boolean canAccessCompiledViewDefinition(UserPrincipal user, CompiledViewDefinitionImpl compiledViewDefinition);

  /**
   * Determines whether a user has permission to access the computation results of a view process.
   * 
   * @param user  the user, not null
   * @param compiledViewDefinition  the view compilation from which computation results would be produced, not null 
   * @return {@code true} if the user may access results produced from the view compilation, {@code false} otherwise.
   */
  boolean canAccessComputationResults(UserPrincipal user, CompiledViewDefinitionImpl compiledViewDefinition);
  
}
