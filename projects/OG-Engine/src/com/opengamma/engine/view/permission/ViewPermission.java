/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.permission;

import com.opengamma.util.PublicAPI;

/**
 * Enumerates the permissions that apply to views.
 */
@PublicAPI
public enum ViewPermission {
  
  /**
   * The permission that allows a user to access a view.
   */
  ACCESS,
  /**
   * The permission that allows a user to see the results from a view.
   */
  READ_RESULTS

}
