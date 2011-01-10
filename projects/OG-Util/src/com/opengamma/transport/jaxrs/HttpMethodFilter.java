/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import javax.ws.rs.core.MediaType;

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
    MediaType mediaType = request.getMediaType();
    if (mediaType != null && mediaType.equals(MediaType.APPLICATION_FORM_URLENCODED_TYPE) == false) {
      return request;
    }
    String methodFormParam = request.getFormParameters().getFirst("method");
    if ("PUT".equals(methodFormParam)) {
      request.setMethod("PUT");
    } else if ("DELETE".equals(methodFormParam)) {
      request.setMethod("DELETE");
    }
    return request;
  }

}
