/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;


/**
 * MockUriInfo for testing purposed
 */
public class MockUriInfo implements UriInfo {
  
  private final MultivaluedMap<String, String> _pathParameters = new MultivaluedMapImpl();
  private final MultivaluedMap<String, String> _queryParameters = new MultivaluedMapImpl();
  private final UriBuilder _uriBuilder = new MockUriBuilder();

  @Override
  public String getPath() {
    return null;
  }

  @Override
  public String getPath(boolean decode) {
    return null;
  }

  @Override
  public List<PathSegment> getPathSegments() {
    return null;
  }

  @Override
  public List<PathSegment> getPathSegments(boolean decode) {
    return null;
  }

  @Override
  public URI getRequestUri() {
    return null;
  }

  @Override
  public UriBuilder getRequestUriBuilder() {
    return _uriBuilder;
  }

  @Override
  public URI getAbsolutePath() {
    return null;
  }

  @Override
  public UriBuilder getAbsolutePathBuilder() {
    return new MockUriBuilder();
  }

  @Override
  public URI getBaseUri() {
    return null;
  }

  @Override
  public UriBuilder getBaseUriBuilder() {
    return new MockUriBuilder();
  }

  @Override
  public MultivaluedMap<String, String> getPathParameters() {
    return _pathParameters;
  }

  @Override
  public MultivaluedMap<String, String> getPathParameters(boolean decode) {
    return _pathParameters;
  }

  @Override
  public MultivaluedMap<String, String> getQueryParameters() {
    return _queryParameters;
  }

  @Override
  public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
    return _queryParameters;
  }

  @Override
  public List<String> getMatchedURIs() {
    return null;
  }

  @Override
  public List<String> getMatchedURIs(boolean decode) {
    return null;
  }

  @Override
  public List<Object> getMatchedResources() {
    return null;
  }

}
