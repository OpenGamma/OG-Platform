/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

/**
 * A named {@link PortfolioNode} which has metadata and usually represents
 * a business-level construct.
 *
 * @author kirk
 */
public interface Portfolio extends PortfolioNode {
  
  String getPortfolioName();

}
