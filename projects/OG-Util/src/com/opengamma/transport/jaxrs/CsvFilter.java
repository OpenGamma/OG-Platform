/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

/**
 * Servlet filter to handle browser requests to export a resource as a CSV file.
 * <p>
 * You cannot manipulate the browser's Accept header easily (other than
 * by writing JavaScript), so this filter examines the incoming URL, sees if it contains
 * .csv, and, if so, changes the Accept HTTP header to text/csv. 
 */
public class CsvFilter implements ContainerRequestFilter {
  
  private static final String SUFFIX = ".csv";

  @Override
  public ContainerRequest filter(ContainerRequest request) {
    if (!request.getMethod().equalsIgnoreCase("GET")) {
      return request;
    }
    URI requestUri = request.getRequestUri();
    String path = requestUri.getPath();
    if (!path.toLowerCase().endsWith(SUFFIX)) {
      return request;
    } 
    
    // set Accept: text/csv header
    MultivaluedMap<String, String> headers = request.getRequestHeaders();
    headers.put(HttpHeaders.ACCEPT, Collections.singletonList("text/csv"));
    request.setHeaders((InBoundHeaders) headers);
    
    // remove .csv from the URL
    try {
      requestUri = new URI(path.substring(0, path.length() - SUFFIX.length()));
    } catch (URISyntaxException e) {
      throw new RuntimeException("Could not build URI", e);
    }
    
    request.setUris(request.getBaseUri(), requestUri);
    return request;
  }

}
