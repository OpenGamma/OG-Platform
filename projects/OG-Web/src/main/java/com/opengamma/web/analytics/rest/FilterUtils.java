/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.rest;

import java.security.Principal;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import com.opengamma.util.rest.HttpMethodFilter;
import com.opengamma.web.analytics.push.LongPollingServlet;
import com.sun.jersey.api.core.ExtendedUriInfo;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.spi.container.ContainerRequest;

/**
 * Helper methods for the subscription filters.
 */
/* package */ final class FilterUtils {

  private FilterUtils() {
  }

  /**
   * Returns the client ID from a request.  See {@link HttpMethodFilter} for an explanation of why the client ID
   * handling is the way it is.
   * @param request The request
   * @param httpContext The HTTP context of the request
   * @return The client ID extracted from the request.  For GET requests the ID comes from the {@code clientId}
   * query parameter and for POST requests it's a form parameter.
   * @see HttpMethodFilter
   */
  /* package */ static String getClientId(ContainerRequest request, HttpContext httpContext) {
    List<String> clientIds = null;
    ExtendedUriInfo uriInfo = httpContext.getUriInfo();
    // try to get the client ID from the query params (for a GET request)
    MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
    clientIds = queryParameters.get(LongPollingServlet.CLIENT_ID);
    if (clientIds == null || clientIds.size() != 1) {
      // try to get the client ID from the form params (in case it's a POST request disguised as a GET)
      clientIds = httpContext.getRequest().getFormParameters().get(LongPollingServlet.CLIENT_ID, String.class);
    }
    if (clientIds == null || clientIds.size() != 1) {
      return null;
    } else {
      return clientIds.get(0);
    }
  }

  /**
   * Returns the user ID from a request's user principal.
   * TODO this doesn't do anything at the moment, we have no user logins
   * @param httpContext The HTTP context
   * @return The user ID from the request
   */
  /* package */ static String getUserId(HttpContext httpContext) {
    Principal userPrincipal = httpContext.getRequest().getUserPrincipal();
    if (userPrincipal == null) {
      // TODO reinstate this if / when we have user logins
      /*s_logger.debug("No user principal, not subscribing, url: {}", url);
     return response;*/
      return null;
    } else {
      return userPrincipal.getName();
    }
  }
}
