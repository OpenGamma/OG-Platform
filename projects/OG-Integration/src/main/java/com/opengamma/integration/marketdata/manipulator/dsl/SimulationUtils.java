/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Collection;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * Utilities for creating and running {@link Simulation}s.
 */
public class SimulationUtils {

  private SimulationUtils() {
  }

  /**
   * Returns the ID of the latest version of a view definition.
   * @param viewDefName The view definition name
   * @param configSource A source for looking up the view definition
   * @return The ID of the latest version of the named view definition, not null
   * @throws DataNotFoundException If no view definition is found with the specified name
   */
  public static UniqueId latestViewDefinitionId(String viewDefName, ConfigSource configSource) {
    Collection<ConfigItem<ViewDefinition>> viewDefs =
        configSource.get(ViewDefinition.class, viewDefName, VersionCorrection.LATEST);
    if (viewDefs.isEmpty()) {
      throw new DataNotFoundException("No view definition found with name '" + viewDefName + "'");
    }
    return viewDefs.iterator().next().getValue().getUniqueId();
  }
}
