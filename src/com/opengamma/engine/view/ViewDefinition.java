/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Map;
import java.util.Set;

/**
 * A definition for a particular {@link View} to be managed
 * by a {@link ViewProcessor}.
 *
 * @author kirk
 */
public interface ViewDefinition {

  String getName();
  
  String getRootPortfolioName();
  
  Set<String> getAllValueDefinitions();
  
  Map<String, Set<String>> getValueDefinitionsBySecurityTypes();
}
