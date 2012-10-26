/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.opengamma.OpenGammaRuntimeException;

/**
 * 
 */
public class StringListFactoryBean extends SingletonFactoryBean<List<String>> {

  private File _file;
  
  public File getFile() {
    return _file;
  }
  
  public void setFile(File file) {
    _file = file;
  }

  //-------------------------------------------------------------------------
  
  @Override
  protected List<String> createObject() {
    if (getFile() == null) {
      throw new IllegalArgumentException("file must be set");
    }
    List<String> stringList = new ArrayList<String>();
    try {
      FileReader reader = new FileReader(getFile());
      BufferedReader bufferedReader = new BufferedReader(reader);
      String nextLine;
      while ((nextLine = bufferedReader.readLine()) != null) {
        stringList.add(nextLine);
      }
    } catch (FileNotFoundException e) {
      throw new IllegalArgumentException("File not found: " + getFile(), e);
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Error while reading file: " + getFile(), e);
    }
    return stringList;
  }
  
}
