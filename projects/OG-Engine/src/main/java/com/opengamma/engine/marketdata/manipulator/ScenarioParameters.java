/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator;

import java.util.Map;

/**
 * Config object for storing parameters required to build a scenario. This object stores a Groovy script which is
 * executed to populate the parameter values. The script should be of the form:
 * <pre>
 * aString = "FOO"
 * aList = [1, 2, 3]
 * aMap = [key1: "val1", key2: "val2"]
 * </pre>
 */
public interface ScenarioParameters {
  Map<String, Object> getParameters();
}
