/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.db.script;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.io.IOUtils;

import com.opengamma.util.ArgumentChecker;

/**
 * Wraps a database script that is accessible on the classpath.
 */
public class ClasspathDbScript implements DbScript {

  private final URI _scriptResource;
  
  public ClasspathDbScript(URI scriptResource) {
    ArgumentChecker.notNull(scriptResource, "scriptResource");
    _scriptResource = scriptResource;
  }
  
  @Override
  public String getName() {
    return _scriptResource.getPath();
  }

  @Override
  public String getScript() throws IOException {
    return IOUtils.toString(_scriptResource);
  }

}
