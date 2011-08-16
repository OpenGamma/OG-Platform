/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.web;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedMap;
import java.util.List;

/**
 *
 */
class EntitySubscriptionFilter implements ResourceFilter {

  private static final Logger s_logger = LoggerFactory.getLogger(EntitySubscriptionFilter.class);

  private final HttpContext _httpContext;
  private final List<String> _uidParamNames;

  public EntitySubscriptionFilter(HttpContext httpContext, List<String> uidParamNames) {
    _httpContext = httpContext;
    _uidParamNames = uidParamNames;
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
      MultivaluedMap<String,String> pathParameters = _httpContext.getUriInfo().getPathParameters();
      for (String paramName : uidParamNames) {
        List<String> paramValues = pathParameters.get(paramName);
        s_logger.debug(paramName + ": " + paramValues);
      }
      return response;
    }
  }
}
