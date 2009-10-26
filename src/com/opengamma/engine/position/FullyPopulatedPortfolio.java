/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.util.Collection;

import com.opengamma.engine.view.FullyPopulatedPortfolioNode;


/**
 * 
 *
 * @author jim
 */
public class FullyPopulatedPortfolio extends FullyPopulatedPortfolioNode
    implements Portfolio {
  
  public FullyPopulatedPortfolio(String name) {
    super(name);
  }

  @Override
  public String getPortfolioName() {
    return getName();
  }


}
