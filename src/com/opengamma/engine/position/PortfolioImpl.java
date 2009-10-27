/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.io.Serializable;

/**
 * A simple JavaBean-based implementation of {@link Portfolio}.
 *
 * @author kirk
 */
public class PortfolioImpl extends PortfolioNodeImpl implements Portfolio,
    Serializable {
  
  public PortfolioImpl(String name) {
    super(name);
  }

  @Override
  public String getPortfolioName() {
    return getName();
  }

}
