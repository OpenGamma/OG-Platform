/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.db.script;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.opengamma.util.ArgumentChecker;

/**
 * Implementation of {@link DbScript} which backs onto a file.
 */
public class FileDbScript implements DbScript {

  private final File _file;
  
  /**
   * Creates an instance.
   * 
   * @param file  the file, not null
   */
  public FileDbScript(File file) {
    ArgumentChecker.notNull(file, "file");
    _file = file;
  }
  
  public File getFile() {
    return _file;
  }

  //-------------------------------------------------------------------------
  @Override
  public String getName() {
    return getFile().getName();
  }
  
  @Override
  public String getScript() throws IOException {
    return FileUtils.readFileToString(getFile());
  }

  @Override
  public String toString() {
    return getFile().toString();
  }
  
}
