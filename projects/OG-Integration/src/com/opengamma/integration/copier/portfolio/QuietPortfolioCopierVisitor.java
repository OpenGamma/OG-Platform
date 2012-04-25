/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.portfolio;

import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;

/**
 * Portfolio copier visitor that stays quiet
 */
public class QuietPortfolioCopierVisitor implements PortfolioCopierVisitor {

  @Override
  public void info(String message, ManageablePosition position, ManageableSecurity[] securities) {
  }

  @Override
  public void info(String message) {
  }

  @Override
  public void error(String message) {
  }

}
