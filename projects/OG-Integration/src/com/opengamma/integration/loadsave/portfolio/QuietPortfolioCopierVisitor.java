package com.opengamma.integration.loadsave.portfolio;

import java.util.List;

import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;

public class QuietPortfolioCopierVisitor implements PortfolioCopierVisitor {

  @Override
  public void info(String message, ManageablePosition position, List<ManageableSecurity> securities) {
  }

  @Override
  public void info(String message) {
  }

  @Override
  public void error(String message) {
  }

}
