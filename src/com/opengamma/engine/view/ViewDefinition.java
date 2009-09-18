/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collection;
import java.util.Map;

import com.opengamma.engine.analytics.AnalyticValueDefinition;

/**
 * A definition for a particular {@link View} to be managed
 * by a {@link ViewProcessor}.
 *
 * @author kirk
 */
public interface ViewDefinition {

  String getName();
  
  String getRootPortfolioName();
  
  Collection<AnalyticValueDefinition<?>> getAllValueDefinitions();
  
  Map<String, Collection<AnalyticValueDefinition<?>>> getValueDefinitionsBySecurityTypes();
}
