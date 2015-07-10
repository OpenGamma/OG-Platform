/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import java.net.URI;
import java.util.Collections;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.joda.beans.ser.JodaBeanMimeType;

import com.google.common.collect.ImmutableMap;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

/**
 * Servlet filter to handle GET requests with a suffix instead of a mime type.
 * <p>
 * This is useful to access non HTML data in a browser. You cannot manipulate the
 * browser's Accept header easily (other than by writing JavaScript), so this filter
 * examines the incoming URL, sees if it contains a known suffix like .csv or .fudge.
 * If it does then it changes the Accept HTTP header to match.
 */
public class UrlSuffixFilter implements ContainerRequestFilter {

  /**
   * Map of suffix to mime type.
   */
  private static final ImmutableMap<String, String> SUFFIXES;
  static {
    SUFFIXES = ImmutableMap.<String, String>builder()
        .put(".csv", RestUtils.TEXT_CSV)
        .put(".json", MediaType.APPLICATION_JSON)
        .put(".xml", MediaType.APPLICATION_XML)
        .put(".fudge", FudgeRest.MEDIA)
        .put(".html", MediaType.TEXT_HTML)
        .put(".jbjson", JodaBeanMimeType.JSON)
        .put(".jbxml", JodaBeanMimeType.XML)
        .put(".jbbin", JodaBeanMimeType.BINARY)
        .build();
  }

  @Override
  public ContainerRequest filter(ContainerRequest request) {
    if (request.getMethod().equalsIgnoreCase("GET")) {
      URI requestUri = request.getRequestUri();
      String path = requestUri.getPath();
      for (String suffix : SUFFIXES.keySet()) {
        if (path.endsWith(suffix)) {
          adjustHeader(request, path, suffix);
          break;
        }
      }
    }
    return request;
  }

  private void adjustHeader(ContainerRequest request, String path, String suffix) {
    String mime = SUFFIXES.get(suffix);
    
    // change accept header
    InBoundHeaders headers = (InBoundHeaders) request.getRequestHeaders();
    headers.put(HttpHeaders.ACCEPT, Collections.singletonList(mime));
    request.setHeaders(headers);
    
    // remove suffix from the URL
    String newPath = path.substring(0, path.length() - suffix.length());
    URI requestUri = request.getRequestUri();
    URI newURI = UriBuilder.fromUri(requestUri).replacePath(newPath).build();
    request.setUris(request.getBaseUri(), newURI);
  }

}
