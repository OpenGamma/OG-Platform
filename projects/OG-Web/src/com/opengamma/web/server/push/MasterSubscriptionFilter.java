/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import com.sun.jersey.api.core.ExtendedUriInfo;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import java.security.Principal;
import java.util.List;

/**
 *
 */
public class MasterSubscriptionFilter implements ResourceFilter {

  private final HttpContext _httpContext;
  private final List<MasterType> _masterTypes;
  private final RestUpdateManager _updateManager;
  private final HttpServletRequest _servletRequest;

  public MasterSubscriptionFilter(RestUpdateManager updateManager,
                                  List<MasterType> masterTypes,
                                  HttpContext httpContext,
                                  HttpServletRequest servletRequest) {
    _httpContext = httpContext;
    _updateManager = updateManager;
    _masterTypes = masterTypes;
    _servletRequest = servletRequest;
  }

  @Override
  public ContainerRequestFilter getRequestFilter() {
    return null;
  }

  @Override
  public ContainerResponseFilter getResponseFilter() {
    return new ResponseFilter(_masterTypes);
  }

  private class ResponseFilter implements ContainerResponseFilter {

    private final List<MasterType> _masterTypes;

    public ResponseFilter(List<MasterType> masterTypes) {
      _masterTypes = masterTypes;
    }

    // TODO this is copy-pasted from EntitySubscriptionFilter, common superclass? helper method / class?
    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
      // TODO check response status
      ExtendedUriInfo uriInfo = _httpContext.getUriInfo();
      MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
      String url = _servletRequest.getRequestURI();

      List<String> clientIds = queryParameters.get(LongPollingServlet.CLIENT_ID);
      if (clientIds == null || clientIds.size() != 1) {
        // don't subscribe if there's no client ID
        return response;
      }
      String clientId = clientIds.get(0);

      Principal userPrincipal = _httpContext.getRequest().getUserPrincipal();
      String userId;
      if (userPrincipal == null) {
        // TODO reinstate this if / when we have user logons
        /*s_logger.debug("No user principal, not subscribing, url: {}", url);
        return response;*/
        userId = null;
      } else {
        userId = userPrincipal.getName();
      }
      for (MasterType masterType : _masterTypes) {
        _updateManager.subscribe(userId, clientId, masterType, url);
      }
      return response;
    }
  }
}
