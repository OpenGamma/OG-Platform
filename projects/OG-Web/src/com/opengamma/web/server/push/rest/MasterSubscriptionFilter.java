/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import com.opengamma.web.server.push.ConnectionManager;
import com.opengamma.web.server.push.LongPollingServlet;
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
 * Jersey filter that sets up subscriptions for masters that are queried via the REST interface.  When any data changes
 * in the master a notification is sent to the client containing the REST URL used to perform the original query.
 * An instance of the filter is associated with each REST method annotated with {@link SubscribeMaster}.
 */
public class MasterSubscriptionFilter implements ResourceFilter {

  private final HttpContext _httpContext;
  private final List<MasterType> _masterTypes;
  private final ConnectionManager _updateManager;
  private final HttpServletRequest _servletRequest;

  public MasterSubscriptionFilter(ConnectionManager updateManager,
                                  List<MasterType> masterTypes,
                                  HttpContext httpContext,
                                  HttpServletRequest servletRequest) {
    _httpContext = httpContext;
    _updateManager = updateManager;
    _masterTypes = masterTypes;
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
   * @return A {@link ResponseFilter}
   */
  @Override
  public ContainerResponseFilter getResponseFilter() {
    return new ResponseFilter(_masterTypes);
  }

  /**
   * Filter that examines the response and sets up a subscription with
   * {@link ConnectionManager#subscribe(String, String, MasterType, String)}.
   */
  private class ResponseFilter implements ContainerResponseFilter {

    /** The masters whose data is returned by the REST method */
    private final List<MasterType> _masterTypes;

    /**
     * @param masterTypes The masters whose data is returned by the REST method
     */
    public ResponseFilter(List<MasterType> masterTypes) {
      _masterTypes = masterTypes;
    }

    /**
     * Extracts the client ID from the query parameter named {@link LongPollingServlet#CLIENT_ID} and subscribes
     * for updates when the data changes in any of the masters in {@link #_masterTypes}.
     * @param request The request
     * @param response The response
     * @return The unmodified response
     * TODO this is copy-pasted from EntitySubscriptionFilter, common superclass? helper method / class?
     */
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
      // TODO should we only subscribe if there were query params, i.e. it was a search request, not just a request for the search page
      for (MasterType masterType : _masterTypes) {
        _updateManager.subscribe(userId, clientId, masterType, url);
      }
      return response;
    }
  }
}
