/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.portfolio;

import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;

/**
 * Abstract visitor for portfolio copier
 */
public interface PortfolioCopierVisitor {

  void info(String message, ManageablePosition position, ManageableSecurity[] securities);
  
  void info(String message);
  
  void error(String message);
}
