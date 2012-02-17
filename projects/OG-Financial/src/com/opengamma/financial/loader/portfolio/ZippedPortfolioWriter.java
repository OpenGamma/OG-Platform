package com.opengamma.financial.loader.portfolio;

import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;

public class ZippedPortfolioWriter implements PortfolioWriter {

  @Override
  public ManageableSecurity writeSecurity(ManageableSecurity security) {
    return null;
  }

  @Override
  public ManageablePosition writePosition(ManageablePosition position) {
    return null;
  }

  @Override
  public ManageablePortfolio getPortfolio() {
    return null;
  }

  @Override
  public ManageablePortfolioNode getCurrentNode() {
    return null;
  }

  @Override
  public ManageablePortfolioNode setCurrentNode(ManageablePortfolioNode node) {
    return null;
  }

  @Override
  public void flush() {
  }

}
