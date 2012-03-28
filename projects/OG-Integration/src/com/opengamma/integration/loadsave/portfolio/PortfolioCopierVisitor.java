package com.opengamma.integration.loadsave.portfolio;

import java.util.List;

import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.tuple.ObjectsPair;

public interface PortfolioCopierVisitor {

  void info(String message, ManageablePosition position, List<ManageableSecurity> securities);
  
  void info(String message);
  
  void error(String message);
}
