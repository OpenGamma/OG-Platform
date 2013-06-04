/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.db.script;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * Wraps a database script that is accessible on the classpath.
 */
public class ClasspathDbScript implements DbScript {

  private final URL _scriptResource;
  
  public ClasspathDbScript(URL scriptResource) {
    ArgumentChecker.notNull(scriptResource, "scriptResource");
    _scriptResource = scriptResource;
  }
  
  @Override
  public String getName() {
    return _scriptResource.getPath();
  }

  @Override
  public boolean exists() {
    try {
      try {
        InputStream in = _scriptResource.openStream();
        in.close();
      } catch (IllegalArgumentException e) {
        throw new OpenGammaRuntimeException(_scriptResource + " caused exception", e);
      }
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  @Override
  public String getScript() throws IOException {
    return IOUtils.toString(_scriptResource);
  }

}
