/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.web;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.UniqueId;
import com.opengamma.web.server.push.subscription.RestUpdateManager;
import com.sun.jersey.api.core.ExtendedUriInfo;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import java.security.Principal;
import java.util.List;

/**
 *
 */
class EntitySubscriptionFilter implements ResourceFilter {

  private static final Logger s_logger = LoggerFactory.getLogger(EntitySubscriptionFilter.class);

  private final HttpContext _httpContext;
  private final List<String> _uidParamNames;
  private final RestUpdateManager _restUpdateManager;
  private final HttpServletRequest _servletRequest;

  public EntitySubscriptionFilter(HttpContext httpContext,
                                  List<String> uidParamNames,
                                  RestUpdateManager restUpdateManager,
                                  HttpServletRequest servletRequest) {
    _httpContext = httpContext;
    _uidParamNames = uidParamNames;
    _restUpdateManager = restUpdateManager;
    _servletRequest = servletRequest;
  }

  @Override
  public ContainerRequestFilter getRequestFilter() {
    return null;
  }

  @Override
  public ContainerResponseFilter getResponseFilter() {
    return new ResponseFilter(_uidParamNames);
  }

  private class ResponseFilter implements ContainerResponseFilter {

    private final List<String> uidParamNames;

    public ResponseFilter(List<String> uidParamNames) {
      this.uidParamNames = uidParamNames;
    }

    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
      // TODO check the response status, only subscribe if successful
      // TODO don't subscribe if specific version was requested - probably need @NoSubscribe annotation on sub-resource methods for versions
      ExtendedUriInfo uriInfo = _httpContext.getUriInfo();
      MultivaluedMap<String,String> pathParameters = uriInfo.getPathParameters();
      MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
      // TODO check this is the right value
      //String url = uriInfo.getPath();
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
      subscribe(userId, clientId, url, pathParameters);
      return response;
    }

    private void subscribe(String userId, String clientId, String url, MultivaluedMap<String, String> pathParameters) {
      for (String paramName : uidParamNames) {
        List<String> uidStrs = pathParameters.get(paramName);
        s_logger.debug(paramName + ": " + uidStrs);
        for (String uidStr : uidStrs) {
          UniqueId uniqueId = null;
          try {
            uniqueId = UniqueId.parse(uidStr);
          } catch (IllegalArgumentException e) {
            s_logger.warn("Unable to parse unique ID: " + uidStr, e);
          }
          if (uniqueId != null) {
            try {
              _restUpdateManager.subscribe(userId, clientId, uniqueId, url);
            } catch (OpenGammaRuntimeException e) {
              s_logger.warn("Failed to subscribe for updates to REST entity, userId: " + userId + ", clientId: "
                                + clientId + ", url: " + url, e);
            }
          }
        }
      }
    }
  }
}
