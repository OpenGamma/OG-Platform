/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.portfoliotheory;

import com.opengamma.engine.target.ComputationTargetType;

/**
 * 
 */
public class CAPMBetaModelPositionFunction extends CAPMBetaModelFunction {

  public CAPMBetaModelPositionFunction(final String resolutionKey) {
    super(ComputationTargetType.POSITION, resolutionKey);
  }

}
