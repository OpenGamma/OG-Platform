/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Formatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import com.opengamma.util.ArgumentChecker;

/**
 * MockUriBuilder intended for testing in memory web resources
 */
public class MockUriBuilder extends UriBuilder {
  
  private static final Pattern s_pathPattern = Pattern.compile("\\{\\w+\\}");
  
  private String _pathFormat = "";

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
    ArgumentChecker.notNull(path, "path");
    formathPath(path);
    return this;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public UriBuilder path(Class resource) throws IllegalArgumentException {
    ArgumentChecker.notNull(resource, "class");
    @SuppressWarnings("unchecked")
    Annotation annotation = resource.getAnnotation(Path.class);
    if (annotation == null) {
      throw new IllegalArgumentException();
    }
    formatPath((Path) annotation);
    return this;
  }

  private void formatPath(Path annotation) {
    String path = annotation.value();
    formathPath(path);
  }

  private void formathPath(String path) {
    Matcher matcher = s_pathPattern.matcher(path);
    int start = 0;
    int end = 0;
    StringBuilder buf = new StringBuilder();
    int count = 0;
    while (matcher.find()) {
      end = matcher.start();
      buf.append(path.substring(start, end)).append("%" + ++count + "$s");
      start = matcher.end();
    }
    buf.append(path.substring(start, path.length()));
    
    if (path.startsWith("/")) {
      _pathFormat += buf.toString();
    } else {
      _pathFormat += "/" + buf.toString();
    }
  }

  @SuppressWarnings("rawtypes")
  @Override
  public UriBuilder path(Class resource, String methodName) throws IllegalArgumentException {
    ArgumentChecker.notNull(resource, "class");
    Method[] methods = resource.getMethods();
    Method method = null;
    for (Method aMethod : methods) {
      if (aMethod.getName().equals(methodName)) {
        method = aMethod;
        break;
      }
    }
    if (method == null) {
      throw new IllegalArgumentException("Method " + methodName + " can not be found in class " + resource);
    }
    path(method);
    return this;
  }

  @Override
  public UriBuilder path(Method method) throws IllegalArgumentException {
    ArgumentChecker.notNull(method, "method");
    Path annotation = method.getAnnotation(Path.class);
    if (annotation == null) {
      throw new IllegalArgumentException("Path annotation missing in method " + method.getName());
    }
    formatPath(annotation);
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
    String url = null;
    try {
      url = new Formatter().format(_pathFormat, values).toString();
    } catch (Exception ex) {
      throw new UriBuilderException("Problem building url from format[" + _pathFormat + "] and values[" + values + "]", ex);
    }
    return URI.create(url);
  }

  @Override
  public URI buildFromEncoded(Object... values) throws IllegalArgumentException, UriBuilderException {
    return null;
  }
  
}
