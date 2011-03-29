/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.permission;

import com.opengamma.engine.view.compilation.ViewEvaluationModel;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.PublicAPI;

/**
 * Provides the ability to determine permissions on a view.
 */
@PublicAPI
public interface ViewPermissionProvider {

  /**
   * Checks that a user has a permission on a view process, and throws an exception if this is not the case.
   * 
   * @param user  the user, not null
   * @param viewEvaluationModel  the view compilation output to which access is being determined, not null
   * @return {@code true} if the user may access the compilation output, {@code false} otherwise.
   */
  boolean canAccessCompilationOutput(UserPrincipal user, ViewEvaluationModel viewEvaluationModel);

  /**
   * Determines whether a user has a permission on a view process.
   * 
   * @param user  the user, not null
   * @param viewEvaluationModel  the view compilation from which computation results would be produced, not null 
   * @return {@code true} if the user may access results produced from the view compilation, {@code false} otherwise.
   */
  boolean canAccessComputationResults(UserPrincipal user, ViewEvaluationModel viewEvaluationModel);
  
}
