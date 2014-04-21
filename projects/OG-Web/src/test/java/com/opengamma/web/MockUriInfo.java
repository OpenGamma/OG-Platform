/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;


/**
 * MockUriInfo for testing purposed
 */
public class MockUriInfo implements UriInfo {
  
  public static final URI SAMPLE_URI = URI.create("http://localhost:8080/");

  private final MultivaluedMap<String, String> _pathParameters = new MultivaluedMapImpl();
  private final MultivaluedMap<String, String> _queryParameters = new MultivaluedMapImpl();
  private final boolean _withData;
  private final UriBuilder _uriBuilder = new MockUriBuilder();

  public MockUriInfo() {
    this(false);
  }

  public MockUriInfo(boolean withData) {
    _withData = withData;
  }

  @Override
  public String getPath() {
    return (_withData ? "" : null);
  }

  @Override
  public String getPath(boolean decode) {
    return (_withData ? "" : null);
  }

  @Override
  public List<PathSegment> getPathSegments() {
    return (_withData ? Collections.<PathSegment>emptyList() : null);
  }

  @Override
  public List<PathSegment> getPathSegments(boolean decode) {
    return (_withData ? Collections.<PathSegment> emptyList() : null);
  }

  @Override
  public URI getRequestUri() {
    return (_withData ? SAMPLE_URI : null);
  }

  @Override
  public UriBuilder getRequestUriBuilder() {
    return _uriBuilder;
  }

  @Override
  public URI getAbsolutePath() {
    return (_withData ? SAMPLE_URI : null);
  }

  @Override
  public UriBuilder getAbsolutePathBuilder() {
    return new MockUriBuilder();
  }

  @Override
  public URI getBaseUri() {
    return (_withData ? SAMPLE_URI : null);
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
    return (_withData ? Collections.<String> emptyList() : null);
  }

  @Override
  public List<String> getMatchedURIs(boolean decode) {
    return (_withData ? Collections.<String> emptyList() : null);
  }

  @Override
  public List<Object> getMatchedResources() {
    return (_withData ? Collections.<Object> emptyList() : null);
  }

}
