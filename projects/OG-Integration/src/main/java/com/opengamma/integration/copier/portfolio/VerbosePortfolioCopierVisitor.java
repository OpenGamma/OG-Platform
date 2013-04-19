/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.portfolio;

import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;

/**
 * Portfolio copier visitor that prints messages to stdout
 */
public class VerbosePortfolioCopierVisitor extends PortfolioCopierStats {

  @Override
  public void error(String message) {
    System.out.println("Error: " + message);
  }
  @Override
  public void info(String message, ManageablePosition position, ManageableSecurity[] securities) {
    super.info(message, position, securities);
    if (message != null && message.length() > 0) {
      System.out.print("[" + message + "] ");
    }
    System.out.print("Wrote position '" + position.getName() + "' and securities [");
    for (ManageableSecurity security : securities) {
      System.out.print(" '" + security.getName() + "'");
    }
    System.out.println(" ]");
  }
  @Override
  public void info(String message) {
    System.out.println(message);
  }

}
