/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 *
 * @author kirk
 */
public class InMemoryPositionMaster implements PositionMaster {
  private final Map<String, Portfolio> _portfoliosByName = new ConcurrentHashMap<String, Portfolio>();

  @Override
  public Portfolio getRootPortfolio(String portfolioName) {
    return _portfoliosByName.get(portfolioName);
  }

  @Override
  public Collection<String> getRootPortfolioNames() {
    return new TreeSet<String>(_portfoliosByName.keySet());
  }
  
  public void addPortfolio(String portfolioName, Portfolio rootPortfolio) {
    _portfoliosByName.put(portfolioName, rootPortfolio);
  }

}
