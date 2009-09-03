/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collection;

/**
 * Allows modules, specifically the {@link ViewProcessor}, access to
 * {@link ViewDefinition}s.
 *
 * @author kirk
 */
public interface ViewDefinitionRepository {
  
  Collection<String> getDefinitionNames();
  
  ViewDefinition getDefinition(String definitionName);

}
