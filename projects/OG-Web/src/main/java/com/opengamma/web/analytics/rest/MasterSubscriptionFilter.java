/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.rest;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.opengamma.util.auth.AuthUtils;
import com.opengamma.web.analytics.push.ConnectionManager;
import com.opengamma.web.analytics.push.LongPollingServlet;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;

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
   * @return null
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
     */
    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
      // TODO check response status
      String clientId = FilterUtils.getClientId(request, _httpContext);
      // don't subscribe if there's no client ID
      if (clientId == null) {
        return response;
      }
      String userId = (AuthUtils.isPermissive() ? null : FilterUtils.getUserId(_httpContext));
      String url = _servletRequest.getRequestURI();
      // TODO should we only subscribe if there were query params, i.e. it was a search request, not just a request for the search page
      for (MasterType masterType : _masterTypes) {
        _updateManager.subscribe(userId, clientId, masterType, url);
      }
      return response;
    }
  }
}
