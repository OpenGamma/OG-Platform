/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.db.script;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * Implementation of {@link DbScriptDirectory} which references a base directory on the file system.
 */
public class FileDbScriptDirectory implements DbScriptDirectory {

  private final File _dir;
  
  /**
   * Constructs an instance.
   * 
   * @param dir  a valid base directory, not null
   */
  public FileDbScriptDirectory(File dir) {
    ArgumentChecker.notNull(dir, "dir");
    if (!dir.exists() || !dir.isDirectory()) {
      throw new OpenGammaRuntimeException("Scripts base directory not found at: " + dir);
    }
    _dir = dir;
  }
  
  //-------------------------------------------------------------------------
  public File getDir() {
    return _dir;
  }
  
  //-------------------------------------------------------------------------
  @Override
  public String getName() {
    return getDir().getName();
  }
  
  @Override
  public Collection<DbScriptDirectory> getSubdirectories() {
    Collection<DbScriptDirectory> subdirectories = new ArrayList<DbScriptDirectory>();
    for (File file : getDir().listFiles()) {
      if (file.isDirectory()) {
        subdirectories.add(new FileDbScriptDirectory(file));
      }
    }
    return subdirectories;
  }
  
  @Override
  public DbScriptDirectory getSubdirectory(String name) {
    File subdirectory = new File(getDir(), name);
    if (!subdirectory.exists() || !subdirectory.isDirectory()) {
      throw new OpenGammaRuntimeException("Scripts subdirectory '" + name + "' does not exist in " + getDir());
    }
    return new FileDbScriptDirectory(subdirectory);
  }

  @Override
  public Collection<DbScript> getScripts() {
    Collection<DbScript> scripts  = new ArrayList<DbScript>();
    for (File file : getDir().listFiles()) {
      if (file.isFile()) {
        scripts.add(new FileDbScript(file));
      }
    }
    return scripts;
  }

}
