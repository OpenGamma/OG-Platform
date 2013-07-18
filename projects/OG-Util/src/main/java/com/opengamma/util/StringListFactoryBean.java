/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Creates a list of strings from a file.
 */
public class StringListFactoryBean extends SingletonFactoryBean<List<String>> {

  /**
   * The file.
   */
  private File _file;

  /**
   * Gets the file
   * 
   * @return the file
   */
  public File getFile() {
    return _file;
  }

  /**
   * Sets the file.
   * 
   * @param file  the file
   */
  public void setFile(File file) {
    _file = file;
  }

  //-------------------------------------------------------------------------

  @Override
  protected List<String> createObject() {
    if (getFile() == null) {
      throw new IllegalArgumentException("file must be set");
    }
    try {
      return FileUtils.readLines(getFile());
    } catch (FileNotFoundException e) {
      throw new IllegalArgumentException("File not found: " + getFile(), e);
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Error while reading file: " + getFile(), e);
    }
  }

}
