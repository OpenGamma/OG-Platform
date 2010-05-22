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
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDeltaResultModel;

/**
 * Registers custom builders for Portfolio, PortfolioNode, and Position with a FudgeContext
 */
public class EngineFudgeContextConfiguration extends FudgeContextConfiguration {
  
  // REVIEW kirk 2010-05-22 -- Any reason this shouldn't be a pure singleton?
  
  /**
   * The singleton Fudge context for a running Engine. 
   */
  public static final FudgeContextConfiguration INSTANCE = new EngineFudgeContextConfiguration();
  
  public void configureFudgeObjectDictionary(final FudgeObjectDictionary dictionary) {
    dictionary.getDefaultBuilderFactory().addGenericBuilder(Portfolio.class, new PortfolioBuilder());
    dictionary.getDefaultBuilderFactory().addGenericBuilder(PortfolioNode.class, new PortfolioNodeBuilder());
    dictionary.getDefaultBuilderFactory().addGenericBuilder(Position.class, new PositionBuilder());
    dictionary.addBuilder(ViewDefinition.class, new ViewDefinitionBuilder());
    dictionary.getDefaultBuilderFactory().addGenericBuilder(ViewCalculationResultModel.class, new ViewCalculationResultModelBuilder());
    dictionary.getDefaultBuilderFactory().addGenericBuilder(ViewComputationResultModel.class, new ViewComputationResultModelBuilder());
    dictionary.getDefaultBuilderFactory().addGenericBuilder(ViewDeltaResultModel.class, new ViewDeltaResultModelBuilder());
  }
  
}