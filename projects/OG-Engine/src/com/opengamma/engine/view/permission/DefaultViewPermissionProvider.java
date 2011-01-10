/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.permission;

import com.opengamma.engine.view.View;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple permission checker which allows access to any view, but does not allow access to the results unless the
 * user is entitled to view each of the live data inputs. 
 */
public class DefaultViewPermissionProvider implements ViewPermissionProvider {

  @Override
  public boolean hasPermission(ViewPermission permission, UserPrincipal user, View view) {
    try {
      assertPermission(permission, user, view);
      return true;
    } catch (ViewPermissionException e) {
      return false;
    }
  }

  @Override
  public void assertPermission(ViewPermission permission, UserPrincipal user, View view) {
    ArgumentChecker.notNull(permission, "permission");
    ArgumentChecker.notNull(user, "user");
    ArgumentChecker.notNull(view, "view");
    
    switch (permission) {
      case ACCESS:
        return;
      case READ_RESULTS:
        // REVIEW jonathan 2010-08-18 -- this is probably way too strict. Live data agreements normally allow access to
        // derived data even if the user is not entitled to the raw inputs. Anyway, that's the benefit of a pluggable
        // permission provider.
        view.assertAccessToLiveDataRequirements(user);
        return;
      default:
        throw new IllegalArgumentException("Unsupported permission: " + permission);
    }
  }

}
