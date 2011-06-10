/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

/**
 * Servlet filter to handle incoming HTML forms.
 * <p>
 * HTML only supports GET and POST, so PUT and DELETE must be tunneled.
 * This class examines the {@code method} form parameter and uses that if it matches
 * {@code PUT} or {@code DELETE}.
 */
public class HttpMethodFilter implements ContainerRequestFilter {

  @Override
  public ContainerRequest filter(ContainerRequest request) {
    if (request.getMethod().equalsIgnoreCase("POST") == false) {
      return request;
    }
    String methodFormParam = request.getFormParameters().getFirst("method");  // getFormParameters() returns empty when not a form
    if ("PUT".equals(methodFormParam)) {
      request.setMethod("PUT");
    } else if ("DELETE".equals(methodFormParam)) {
      request.setMethod("DELETE");
    } else if ("OPTIONS".equals(methodFormParam)) {
      request.setMethod("OPTIONS");
    } else if ("HEAD".equals(methodFormParam)) {
      request.setMethod("HEAD");
    } else if ("POST".equals(methodFormParam)) {
      request.setMethod("POST");  // for completeness
    } else if ("GET".equals(methodFormParam)) {
      request.setMethod("GET");  // for IE
    }
    return request;
  }

}
