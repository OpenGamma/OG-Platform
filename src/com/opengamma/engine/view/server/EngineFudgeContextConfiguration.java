/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.server;

import org.fudgemsg.FudgeContextConfiguration;
import org.fudgemsg.mapping.FudgeObjectDictionary;

import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;

/**
 * Registers custom builders for Portfolio, PortfolioNode, and Position with a FudgeContext
 * 
 * @author Andrew Griffin
 */
public class EngineFudgeContextConfiguration extends FudgeContextConfiguration {
  
  public static final FudgeContextConfiguration INSTANCE = new EngineFudgeContextConfiguration ();
  
  public void configureFudgeObjectDictionary (final FudgeObjectDictionary dictionary) {
    dictionary.getDefaultBuilderFactory ().addGenericBuilder (Portfolio.class, new PortfolioBuilder ());
    dictionary.getDefaultBuilderFactory ().addGenericBuilder (PortfolioNode.class, new PortfolioNodeBuilder ());
    dictionary.getDefaultBuilderFactory ().addGenericBuilder (Position.class, new PositionBuilder ());
  }
  
}