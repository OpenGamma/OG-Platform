/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.util.Collection;


/**
 * 
 *
 * @author kirk
 */
public interface PortfolioNode {
  Collection<Position> getPositions();
  
  Collection<PortfolioNode> getSubNodes();

  String getName();
  
  String getIdentityKey();
}
