/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;

/**
 * Implements a mapper function that returns the type string of the security referenced by a position.
 */
public class SecurityTypeMapperFunction implements PortfolioMapperFunction<String> {

  @Override
  public String apply(final PortfolioNode node) {
    return null;
  }

  @Override
  public String apply(final PortfolioNode parentNode, final Position position) {
    final Security security = position.getSecurity();
    return security != null ? security.getSecurityType() : null;
  }

}
