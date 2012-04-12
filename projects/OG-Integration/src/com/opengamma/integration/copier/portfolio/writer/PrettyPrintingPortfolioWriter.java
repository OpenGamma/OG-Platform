/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.integration.copier.portfolio.writer;

import org.springframework.util.StringUtils;

import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * A dummy portfolio writer, which pretty-prints information instead of persisting
 */
public class PrettyPrintingPortfolioWriter implements PortfolioWriter {

  private String[] _currentPath = new String[] {};
  private boolean _prettyPrint;
  
  public PrettyPrintingPortfolioWriter(boolean prettyPrint) {
    _prettyPrint = prettyPrint;
  }
  
  @Override
  public ManageableSecurity writeSecurity(ManageableSecurity security) {
    
    ArgumentChecker.notNull(security, "security");
    if (_prettyPrint) {
      System.out.println("Security: " + security.toString());
    }
    return security;
  }

  @Override
  public ManageablePosition writePosition(ManageablePosition position) {
    
    ArgumentChecker.notNull(position, "position");
    if (_prettyPrint) {
      System.out.println("Position: " + position.toString());
    }
    return position;
  }

  @Override
  public void flush() {
    if (_prettyPrint) {
      System.out.println("Flushed writer");
    }
  }

  @Override
  public void close() {
    if (_prettyPrint) {
      System.out.println("Closed writer");
    }
  }

  @Override
  public void setPath(String[] newPath) {
    
    ArgumentChecker.notNull(newPath, "newPath");
    _currentPath = newPath;
    if (_prettyPrint) {
      System.out.println("Set path to: " + StringUtils.arrayToDelimitedString(newPath, "/"));
    }
  }

  @Override
  public String[] getCurrentPath() {
    return _currentPath;
  }
  
}
