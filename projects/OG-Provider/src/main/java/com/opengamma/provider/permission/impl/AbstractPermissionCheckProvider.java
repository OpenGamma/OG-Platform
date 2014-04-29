/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.provider.permission.impl;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.BooleanUtils;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.permission.PermissionCheckProvider;
import com.opengamma.provider.permission.PermissionCheckProviderRequest;
import com.opengamma.provider.permission.PermissionCheckProviderResult;

/**
 * Abstract class to make implementation of PermissionCheckProvider easier.
 * 
 * Subclass should provide implementation for isPermitted(PermissionCheckProviderRequest)
 */
public abstract class AbstractPermissionCheckProvider implements PermissionCheckProvider {

  @Override
  public boolean isPermitted(ExternalIdBundle userIdBundle, String ipAddress, String requestedPermission) {
    PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(userIdBundle, ipAddress, requestedPermission);
    PermissionCheckProviderResult holderResult = isPermitted(request);
    return BooleanUtils.isTrue(holderResult.getCheckedPermissions().get(requestedPermission));
  }

  @Override
  public Map<String, Boolean> isPermitted(ExternalIdBundle userIdBundle, String ipAddress, Set<String> requestedPermissions) {
    PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(userIdBundle, ipAddress, requestedPermissions);
    PermissionCheckProviderResult holderResult = isPermitted(request);
    return holderResult.getCheckedPermissions();
  }

}
