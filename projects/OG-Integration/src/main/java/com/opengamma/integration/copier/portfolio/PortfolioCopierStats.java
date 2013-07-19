/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.portfolio;

import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;

/**
 * Portfolio copier visitor that gathers statistics
 */
public class PortfolioCopierStats implements PortfolioCopierVisitor {

  private int _positionLoadCount;
  private int _securityLoadCount;
  private int _positionErrorCount;
  private int _securityErrorCount;

  @Override
  public void info(String message, ManageablePosition position, ManageableSecurity[] securities) {
    if (position == null) {
      _positionErrorCount++;
    } else {
      _positionLoadCount++;
    }
    if (securities == null || securities.length == 0) {
      _securityErrorCount++;
    } else {
      _securityLoadCount++;
    }
  }

  @Override
  public void info(String message) {
  }

  @Override
  public void error(String message) {

  }

  public int getPositionLoadCount() {
    return _positionLoadCount;
  }

  public int getSecurityLoadCount() {
    return _securityLoadCount;
  }

  public int getPositionErrorCount() {
    return _positionErrorCount;
  }

  public int getSecurityErrorCount() {
    return _securityErrorCount;
  }

}
