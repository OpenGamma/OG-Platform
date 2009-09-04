/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import com.opengamma.engine.position.AggregatePosition;

/**
 * The base implementation of the {@link View} interface.
 *
 * @author kirk
 */
public class ViewImpl implements View {
  private final ViewDefinition _definition;
  
  public ViewImpl(ViewDefinition definition) {
    if(definition == null) {
      throw new NullPointerException("Must provide a definition.");
    }
    _definition = definition;
  }
  
  // TODO kirk 2009-09-03 -- Flesh out a bootstrap system:
  // - Walk through an AnalyticFunctionRepository to get all functions
  // - Load up the contents of the portfolio from a PortfolioMaster
  // - Load up the securities of the portfolio from a SecurityMaster
  // - Gather up all problems if anything isn't available

  /**
   * @return the definition
   */
  public ViewDefinition getDefinition() {
    return _definition;
  }

  @Override
  public ViewComputationResultModel getMostRecentResult() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public AggregatePosition getPositionRoot() {
    // TODO Auto-generated method stub
    return null;
  }
  
  public void recalculationPerformed(ViewComputationResultModel result) {
  }

}
