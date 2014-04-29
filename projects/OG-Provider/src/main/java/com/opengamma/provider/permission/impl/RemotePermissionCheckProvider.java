/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.permission.impl;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.BooleanUtils;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.provider.permission.PermissionCheckProvider;
import com.opengamma.provider.permission.PermissionCheckProviderRequest;
import com.opengamma.provider.permission.PermissionCheckProviderResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractRemoteClient;

/**
 * Provides access to a remote permission check provider.
 * <p>
 * This is a client that connects to a permission check provider at a remote URI.
 */
public class RemotePermissionCheckProvider extends AbstractRemoteClient implements PermissionCheckProvider {

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemotePermissionCheckProvider(final URI baseUri) {
    super(baseUri);
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isPermitted(ExternalIdBundle userIdBundle, String ipAddress, String requestedPermission) {
    PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(userIdBundle, ipAddress, requestedPermission);
    PermissionCheckProviderResult holderResult = isPermitted(request);
    return BooleanUtils.isTrue(holderResult.getCheckedPermissions().get(requestedPermission));
  }

  @Override
  public Map<String, Boolean> isPermitted(ExternalIdBundle userIdBundle, String ipAddress, Set<String> requestedPermissions) {
    PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(userIdBundle, ipAddress, requestedPermissions);
    PermissionCheckProviderResult permissionResult = isPermitted(request);
    return permissionResult.getCheckedPermissions();
  }

  @Override
  public PermissionCheckProviderResult isPermitted(PermissionCheckProviderRequest request) {
    ArgumentChecker.notNull(request, "request");
    URI uri = DataPermissionCheckProviderResource.uriGet(getBaseUri());
    return accessRemote(uri).post(PermissionCheckProviderResult.class, request);
  }

}
