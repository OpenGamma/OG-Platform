/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import java.net.URI;
import java.net.URL;

import javax.servlet.ServletContext;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * Implementation of {@link UriProvider} which uses a Jetty {@link ServletContext} to obtain the URI.
 */
public class ServletContextUriProvider implements UriProvider {

  private final String _basePath;
  private final ServletContext _servletContext;
  
  public ServletContextUriProvider(String basePath, ServletContext servletContext) {
    ArgumentChecker.notNull(basePath, "baseDir");
    ArgumentChecker.notNull(servletContext, "servletContext");
    _basePath = basePath.startsWith("/") ? basePath : "/" + basePath;
    _servletContext = servletContext;
  }
  
  @Override
  public URI getUri(String resourceReference) {
    try {
      URL resource = _servletContext.getResource(_basePath + resourceReference);
      if (resource == null) {
        throw new OpenGammaRuntimeException("Resource not found: " + resourceReference);
      }
      return resource.toURI();
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Error obtaining URI for resource " + resourceReference, e);
    }
  }

}
