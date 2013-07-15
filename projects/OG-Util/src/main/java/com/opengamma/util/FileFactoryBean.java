/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;

import com.opengamma.OpenGammaRuntimeException;

/**
 * FactoryBean for creating File objects within Spring configuration files. 
 */
public class FileFactoryBean implements FactoryBean<File> {

  private File _file;

  public FileFactoryBean() {
  }

  public File getFile() {
    return _file;
  }

  public void setFile(final File file) {
    _file = file;
  }
  
  public void setFilename(final String filename) {
    setFile(new File(filename));
  }

  public void setResource(final Resource resource) {
    // Because File objects can't point to things in JAR files, we extract any resources to
    // the local file system so we can return a proper "File".
    try (InputStream in = resource.getInputStream()) {
      if (in == null) {
        throw new OpenGammaRuntimeException("Resource " + resource.getDescription() + " not found");
      }
      _file = File.createTempFile("FileFactoryBean", null);
      try (OutputStream out = new FileOutputStream(_file)) {
        final byte[] buffer = new byte[4096];
        int i;
        while ((i = in.read(buffer)) > 0) {
          out.write(buffer, 0, i);
        }
      }
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("error loading resource", e);
    }
  }

  @Override
  public File getObject() throws Exception {
    return getFile();
  }

  @Override
  public Class<?> getObjectType() {
    return File.class;
  }

  @Override
  public boolean isSingleton() {
    return false;
  }
  
}
