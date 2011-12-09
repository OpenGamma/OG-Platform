/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.position;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

/**
 * MockUriBuilder intended for testing in memory web resources
 */
/*package*/class MockUriBuilder extends UriBuilder {

  @Override
  public UriBuilder clone() {
    return this;
  }

  @Override
  public UriBuilder uri(URI uri) throws IllegalArgumentException {
    return this;
  }

  @Override
  public UriBuilder scheme(String scheme) throws IllegalArgumentException {
    return this;
  }

  @Override
  public UriBuilder schemeSpecificPart(String ssp) throws IllegalArgumentException {
    return this;
  }

  @Override
  public UriBuilder userInfo(String ui) {
    return this;
  }

  @Override
  public UriBuilder host(String host) throws IllegalArgumentException {
    return this;
  }

  @Override
  public UriBuilder port(int port) throws IllegalArgumentException {
    return this;
  }

  @Override
  public UriBuilder replacePath(String path) {
    return this;
  }

  @Override
  public UriBuilder path(String path) throws IllegalArgumentException {
    return this;
  }

  @Override
  public UriBuilder path(Class resource) throws IllegalArgumentException {
    return this;
  }

  @Override
  public UriBuilder path(Class resource, String method) throws IllegalArgumentException {
    return this;
  }

  @Override
  public UriBuilder path(Method method) throws IllegalArgumentException {
    return this;
  }

  @Override
  public UriBuilder segment(String... segments) throws IllegalArgumentException {
    return this;
  }

  @Override
  public UriBuilder replaceMatrix(String matrix) throws IllegalArgumentException {
    return this;
  }

  @Override
  public UriBuilder matrixParam(String name, Object... values) throws IllegalArgumentException {
    return this;
  }

  @Override
  public UriBuilder replaceMatrixParam(String name, Object... values) throws IllegalArgumentException {
    return this;
  }

  @Override
  public UriBuilder replaceQuery(String query) throws IllegalArgumentException {
    return this;
  }

  @Override
  public UriBuilder queryParam(String name, Object... values) throws IllegalArgumentException {
    return this;
  }

  @Override
  public UriBuilder replaceQueryParam(String name, Object... values) throws IllegalArgumentException {
    return this;
  }

  @Override
  public UriBuilder fragment(String fragment) {
    return this;
  }

  @Override
  public URI buildFromMap(Map<String, ? extends Object> values) throws IllegalArgumentException, UriBuilderException {
    return null;
  }

  @Override
  public URI buildFromEncodedMap(Map<String, ? extends Object> values) throws IllegalArgumentException, UriBuilderException {
    return null;
  }

  @Override
  public URI build(Object... values) throws IllegalArgumentException, UriBuilderException {
    return null;
  }

  @Override
  public URI buildFromEncoded(Object... values) throws IllegalArgumentException, UriBuilderException {
    return null;
  }

}
