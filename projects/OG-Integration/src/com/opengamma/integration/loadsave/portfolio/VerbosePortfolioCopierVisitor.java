package com.opengamma.integration.loadsave.portfolio;

import java.util.List;

import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;

public class VerbosePortfolioCopierVisitor implements PortfolioCopierVisitor {

  @Override
  public void error(String message) {
    System.out.println("Error: " + message);
  }
  @Override
  public void info(String message, ManageablePosition position, List<ManageableSecurity> securities) {
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
