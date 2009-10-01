/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.util.Collection;

/**
 * 
 *
 * @author kirk
 */
public interface PrimitiveAnalyticFunctionDefinition extends
    AnalyticFunctionDefinition {

  Collection<AnalyticValueDefinition<?>> getPossibleResults();
  
  Collection<AnalyticValueDefinition<?>> getInputs();
  
}
