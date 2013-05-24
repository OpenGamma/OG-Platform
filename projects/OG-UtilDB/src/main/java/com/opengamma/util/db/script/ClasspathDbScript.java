/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.db.script;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Wraps a database script that is accessible on the classpath.
 */
public class ClasspathDbScript implements DbScript {

  private final String _scriptResource;
  
  public ClasspathDbScript(String scriptResource) {
    _scriptResource = scriptResource;
  }
  
  @Override
  public String getName() {
    return _scriptResource;
  }

  @Override
  public String getScript() throws IOException {
    URL scriptResource = getClass().getClassLoader().getResource(_scriptResource);
    if (scriptResource == null) {
      throw new OpenGammaRuntimeException("Could not find database script resource at " + _scriptResource);
    }
    return IOUtils.toString(scriptResource);
  }

}
