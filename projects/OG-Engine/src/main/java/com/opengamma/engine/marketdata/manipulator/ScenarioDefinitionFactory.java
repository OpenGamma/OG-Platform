/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import java.util.Map;

/**
 * Interface for classes that can create {@link ScenarioDefinition}s.
 * TODO fudge building
 * different impls need to be loaded from the config DB using ScenarioDefinitionFactory.class which means they
 * need to be saved with class headers. but if they're saved as top-level objects there's no way to guarantee that.
 * is it possible to create a fudge builder for this type that serializes the impl using its fudge builder but
 * ensures the class header is included? would the impl builders have to know about and call the methods in this
 * type's builder? presumably the builder for the most specific type is used so they'd have to.
 */
public interface ScenarioDefinitionFactory {

  /**
   * Creates a scenario.
   * @param parameters Parameters required to create the scenario, can be null or empty
   * @return A scenario
   * TODO should the parameters be map<string,string> and type hints from the script be used to convert the values?
   */
  ScenarioDefinition create(Map<String, Object> parameters);
}
