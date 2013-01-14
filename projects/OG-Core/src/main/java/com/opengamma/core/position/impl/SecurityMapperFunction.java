/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;

/**
 * Implements a mapper function that returns the security referenced by a position.
 */
public class SecurityMapperFunction implements PortfolioMapperFunction<Security> {

  @Override
  public Security apply(final PortfolioNode node) {
    return null;
  }

  @Override
  public Security apply(final PortfolioNode parentNode, final Position position) {
    return position.getSecurity();
  }

}
