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
  private final String _name;
  
  public PortfolioImpl(String name) {
    _name = name;
  }

  @Override
  public String getName() {
    return _name;
  }

}
