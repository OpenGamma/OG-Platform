/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.db.script;

import java.util.Collection;

/**
 * Provides access to part of a logical directory structure of database scripts.
 */
public interface DbScriptDirectory {

  /**
   * Gets the name of the directory.
   * 
   * @return the name of the directory, not null
   */
  String getName();
  
  /**
   * Gets all subdirectories.
   * 
   * @return a collection of all subdirectories, not null
   */
  Collection<DbScriptDirectory> getSubdirectories();
  
  /**
   * Gets a named subdirectory.
   * 
   * @param name  the subdirectory name, not null
   * @return a directory representing the named subdirectory, not null
   */
  DbScriptDirectory getSubdirectory(String name);
  
  /**
   * Gets all scripts in the directory represented by this instance.
   * 
   * @return a collection of all scripts, not null
   */
  Collection<DbScript> getScripts();
  
}
