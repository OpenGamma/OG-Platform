/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.UniqueId;
import com.opengamma.web.server.push.ConnectionManager;
import com.opengamma.web.server.push.LongPollingServlet;
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
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MultivaluedMap;
import java.security.Principal;
import java.util.List;

/**
 * Jersey filter that sets up subscriptions for entities returned from REST methods.  When the entity changes
 * a notification is sent to the client containing the REST URL used to request the entity.
 * An instance of the filter is associated with each REST method annotated with {@link Subscribe}.
 */
public class EntitySubscriptionFilter implements ResourceFilter {

  private static final Logger s_logger = LoggerFactory.getLogger(EntitySubscriptionFilter.class);

  private final HttpContext _httpContext;
  private final List<String> _uidParamNames;
  private final ConnectionManager _restUpdateManager;
  private final HttpServletRequest _servletRequest;

  /**
   * @param uidParamNames Parameter names (specified by {@link PathParam}) that contain {@link UniqueId}s for which
   * subscriptions should be created
   * @param connectionManager For setting up the subscriptions
   * @param httpContext The HTTP context of the request
   * @param servletRequest The HTTP request
   */
  public EntitySubscriptionFilter(List<String> uidParamNames,
                                  ConnectionManager connectionManager,
                                  HttpContext httpContext,
                                  HttpServletRequest servletRequest) {
    _httpContext = httpContext;
    _uidParamNames = uidParamNames;
    _restUpdateManager = connectionManager;
    _servletRequest = servletRequest;
  }

  /**
   * @return {@code null}
   */
  @Override
  public ContainerRequestFilter getRequestFilter() {
    return null;
  }

  /**
   * @return A {@link ResponseFilter} for setting up the subscription
   */
  @Override
  public ContainerResponseFilter getResponseFilter() {
    return new ResponseFilter(_uidParamNames);
  }

  /**
   * Filter that examines the response and sets up the subscription with
   * {@link ConnectionManager#subscribe(String, String, UniqueId, String)}.
   */
  private class ResponseFilter implements ContainerResponseFilter {

    private final List<String> uidParamNames;

    /**
     * @param uidParamNames Names of the method parameters that contain {@link UniqueId}s.  These are the names
     * specified in the {@link PathParam} annotations and they are also annotated with {@link Subscribe}.
     */
    public ResponseFilter(List<String> uidParamNames) {
      this.uidParamNames = uidParamNames;
    }

    /**
     * Extracts the client ID from the query parameter named {@link LongPollingServlet#CLIENT_ID} and subscribes
     * for updates for {@link UniqueId}s in the parameters named {@link #uidParamNames}.
     * @param request The request
     * @param response The response
     * @return The unmodified response
     * TODO this is almost identical to MasterSubscriptionFilter, common superclass? helper method / class?
     */
    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
      // TODO check the response status, only subscribe if successful
      // TODO don't subscribe if specific version was requested - probably need @NoSubscribe annotation on sub-resource methods for versions
      ExtendedUriInfo uriInfo = _httpContext.getUriInfo();
      MultivaluedMap<String,String> pathParameters = uriInfo.getPathParameters();
      String url = _servletRequest.getRequestURI();
      // try to get the client ID from the query params (for a GET request)
      MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
      List<String> clientIds = queryParameters.get(LongPollingServlet.CLIENT_ID);
      if (clientIds == null || clientIds.size() != 1) {
        // try to get the client ID from the form params (in case it's a POST)
        clientIds = _httpContext.getRequest().getFormParameters().get(LongPollingServlet.CLIENT_ID, String.class);
      }
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
