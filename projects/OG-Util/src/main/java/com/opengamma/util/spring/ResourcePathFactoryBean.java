/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.spring;

import java.io.IOException;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ResourceUtils;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Factory bean for obtaining a Spring resource URI.
 */
public class ResourcePathFactoryBean extends SingletonFactoryBean<String> {

  private String _resource;
  
  public String getResource() {
    return _resource;
  }
  
  public void setResource(String resource) {
    _resource = resource;
  }
  
  @Override
  protected String createObject() {
    ArgumentChecker.notNull(getResource(), "resource");
    if (!getResource().contains(":")) {
      // Assume resource is a direct path
      return getResource();
    }
    try {
      // Get URL of resource which may be of the form 'classpath:'
      return ResourceUtils.getURL(getResource()).toString();
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Error obtaining URI of resource " + getResource(), e);
    }
  }

}
