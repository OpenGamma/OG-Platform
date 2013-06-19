/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.credit;

import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.id.VersionCorrection;

/**
 * 
 */
public interface CurveDefinitionSource {

  /**
   * Gets the named curve definition.
   * @param name The name of the curve definition, not null
   * @return The curve definition
   */
  CurveDefinition getCurveDefinition(String name);

  /**
   * Gets the named curve definition that matches the version correction.
   * @param name The name of the curve definition, not null
   * @param versionCorrection The version correction of the definition, not null
   * @return The curve definition
   */
  CurveDefinition getCurveDefinition(String name, VersionCorrection versionCorrection);

}
