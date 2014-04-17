/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.provider.permission;

import java.util.Map;
import java.util.Set;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.PublicSPI;

/**
 * A provider of permission information.
 * <p>
 * This allows a permission check to be performed on an underlying data source.
 * <p>
 * Implementations must be thread-safe.
 */
@PublicSPI
public interface PermissionCheckProvider {

  /**
   * Checks if a given user has the requested permissions.
   * <p>
   *
   * @param userIdBundle  the external identifier bundle with the user credential, not null
   * @param ipAddress  the IP address of the user, not null
   * @param requestedPermissions  the requested permissions, not null
   * @return the map of permission check result of individual permission request, true if permitted, false otherwise
   * @throws RuntimeException if a problem occurs
   */
  Map<String, Boolean> isPermitted(ExternalIdBundle userIdBundle, String ipAddress, Set<String> requestedPermissions);

  /**
   * Checks if a given user has the requested permissions.
   * <p>
   * This is the underlying operation.
   * All other methods delegate to this one.
   * 
   * @param request  the request, not null
   * @return the historical time-series result, not null
   * @throws RuntimeException if a problem occurs
   */
  PermissionCheckProviderResult isPermitted(PermissionCheckProviderRequest request);

}
