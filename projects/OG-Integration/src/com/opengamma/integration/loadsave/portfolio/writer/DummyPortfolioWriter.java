/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.integration.loadsave.portfolio.writer;

import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * A dummy portfolio writer, which pretty-prints information instead of persisting
 * TODO implement portfolio tree methods
 */
public class DummyPortfolioWriter implements PortfolioWriter {

  private String[] _currentPath = new String[] {};
  
  @Override
  public ManageableSecurity writeSecurity(ManageableSecurity security) {
    
    ArgumentChecker.notNull(security, "security");
    
    System.out.println("Security: " + security.toString());
    return security;
  }

  @Override
  public ManageablePosition writePosition(ManageablePosition position) {
    
    ArgumentChecker.notNull(position, "position");
    
    System.out.println("Position: " + position.toString());
    return position;
  }

  @Override
  public void flush() {
    System.out.println("Flushed writer");
  }

  @Override
  public void close() {
    System.out.println("Closed writer");
  }

  @Override
  public void setPath(String[] newPath) {
    
    ArgumentChecker.notNull(newPath, "newPath");
    _currentPath = newPath;
    
    System.out.println("Set path to: " + newPath);
  }

  @Override
  public String[] getCurrentPath() {
    return _currentPath;
  }
  
}
