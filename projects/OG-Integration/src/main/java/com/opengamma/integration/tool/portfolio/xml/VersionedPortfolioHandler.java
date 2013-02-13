package com.opengamma.integration.tool.portfolio.xml;

import java.util.Iterator;

import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.tuple.ObjectsPair;

public class VersionedPortfolioHandler {

  private Iterator<ObjectsPair<ManageablePosition, ManageableSecurity[]>> _positionIterator;
  private String _portfolioName;

  public VersionedPortfolioHandler(String name,
                                   Iterator<ObjectsPair<ManageablePosition, ManageableSecurity[]>> iterator) {
    _portfolioName = name;
    _positionIterator = iterator;
  }

  public Iterator<ObjectsPair<ManageablePosition, ManageableSecurity[]>> getPositionIterator() {
    return _positionIterator;
  }

  public String getPortfolioName() {
    return _portfolioName;
  }
}
