/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.permission;

import com.opengamma.engine.view.View;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.PublicAPI;

// REVIEW jonathan 2010-08-18 -- went with 'permission' rather than 'entitlement' for this to avoid confusion with live
// data entitlements.

/**
 * Provides the ability to determine permissions on views.
 */
@PublicAPI
public interface ViewPermissionProvider {

  /**
   * Determines whether a user has a permission on a view.
   * 
   * @param permission  the permission
   * @param user  the user
   * @param view  the view
   * @return  <code>true</code> if the user has the given permission on the view, <code>false</code> otherwise.
   */
  boolean hasPermission(ViewPermission permission, UserPrincipal user, View view);

  /**
   * Checks that a user has a permission on a view, and throws an exception if this is not the case.
   * 
   * @param permission  the permission
   * @param user  the user
   * @param view  the view
   * 
   * @throws ViewPermissionException  if the user does not have the given permission on the specified view.
   */
  void assertPermission(ViewPermission permission, UserPrincipal user, View view);
  
}
