/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.util.Collection;

import com.opengamma.id.Identifiable;
import com.opengamma.id.IdentificationScheme;


/**
 * 
 *
 * @author kirk
 */
public interface PortfolioNode extends Identifiable {
  
  public static final IdentificationScheme PORTFOLIO_NODE_IDENTITY_KEY_DOMAIN = new IdentificationScheme("PortfolioNodeIdentityKey");   
  
  Collection<Position> getPositions();
  
  Collection<PortfolioNode> getSubNodes();

  String getName();
  
}
