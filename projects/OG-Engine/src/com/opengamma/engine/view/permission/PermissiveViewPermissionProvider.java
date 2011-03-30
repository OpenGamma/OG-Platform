/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.permission;

import com.opengamma.engine.view.compilation.ViewEvaluationModel;
import com.opengamma.livedata.UserPrincipal;

/**
 * Implementation of {@link ViewPermissionProvider} that always responds positively.
 */
public class PermissiveViewPermissionProvider implements ViewPermissionProvider {

  @Override
  public boolean canAccessCompilationOutput(UserPrincipal user, ViewEvaluationModel viewEvaluationModel) {
    return true;
  }

  @Override
  public boolean canAccessComputationResults(UserPrincipal user, ViewEvaluationModel viewEvaluationModel) {
    return true;
  }

}
