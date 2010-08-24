/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.fudgemsg.FudgeContextConfiguration;
import org.fudgemsg.mapping.FudgeObjectDictionary;

/**
 * Configuration for Fudge of the OG-Engine library.
 * <p>
 * This configures Fudge builders.
 */
public final class EngineFudgeContextConfiguration extends FudgeContextConfiguration {

  /**
   * The singleton configuration. 
   */
  public static final FudgeContextConfiguration INSTANCE = new EngineFudgeContextConfiguration();

  /**
   * Restricted constructor.
   */
  private EngineFudgeContextConfiguration() {
  }

  // -------------------------------------------------------------------------
  @Override
  public void configureFudgeObjectDictionary(final FudgeObjectDictionary dictionary) {
    dictionary.addAllClasspathBuilders();
    // For reference, these are the ones that were converted when FRJ-87 was tested.
    //dictionary.getDefaultBuilderFactory().addGenericBuilder(Portfolio.class, new PortfolioBuilder());
    //dictionary.getDefaultBuilderFactory().addGenericBuilder(PortfolioNode.class, new PortfolioNodeBuilder());
    //dictionary.getDefaultBuilderFactory().addGenericBuilder(Position.class, new PositionBuilder());
    //dictionary.getDefaultBuilderFactory().addGenericBuilder(ViewDefinition.class, new ViewDefinitionBuilder());
    //dictionary.getDefaultBuilderFactory().addGenericBuilder(ViewCalculationResultModel.class, new ViewCalculationResultModelBuilder());
    //dictionary.getDefaultBuilderFactory().addGenericBuilder(ViewComputationResultModel.class, new ViewComputationResultModelBuilder());
    //dictionary.getDefaultBuilderFactory().addGenericBuilder(ViewDeltaResultModel.class, new ViewDeltaResultModelBuilder());
  }

}
