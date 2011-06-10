/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

/**
 * Servlet filter to add no-caching headers to outgoing GET requests.
 * <p>
 * Some browsers assume that a response is cachable in the absence of other information.
 * This filter adds no-caching headers.
 */
public class NoCachingFilter implements ContainerResponseFilter {

  @Override
  public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
    if (request.getMethod().equalsIgnoreCase("GET") == false) {
      return response;
    }
    MultivaluedMap<String, Object> headers = response.getHttpHeaders();
    if (headers.containsKey(HttpHeaders.ETAG) == false &&
        headers.containsKey(HttpHeaders.CACHE_CONTROL) == false &&
        headers.containsKey(HttpHeaders.EXPIRES) == false) {
      headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, max-age=0, must-revalidate");
      headers.add(HttpHeaders.EXPIRES, "Mon, 26 Jul 1997 05:00:00 GMT");
    }
    return response;
  }

}
