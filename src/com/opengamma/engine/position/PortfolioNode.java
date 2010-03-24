/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import java.util.Collection;

import com.opengamma.id.Identifiable;
import com.opengamma.id.IdentificationDomain;


/**
 * 
 *
 * @author kirk
 */
public interface PortfolioNode extends Identifiable {
  
  public static final IdentificationDomain PORTFOLIO_NODE_IDENTITY_KEY_DOMAIN = new IdentificationDomain("PortfolioNodeIdentityKey");   
  
  Collection<Position> getPositions();
  
  Collection<PortfolioNode> getSubNodes();

  String getName();
  
}
