/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.credit;

import com.opengamma.financial.analytics.curve.AbstractCurveDefinition;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.id.VersionCorrection;

/**
 * Source for {@link AbstractCurveDefinition}s.
 */
public interface CurveDefinitionSource {

  /**
   * Gets the named curve definition. This method works only for curve definitions of type {@link CurveDefinition} and
   * {@link InterpolatedCurveDefinition} and is maintained for backwards compatibility.
   * @param name The name of the curve definition, not null
   * @return The curve definition
   * @deprecated This method does not handle all types of {@link AbstractCurveDefinition}. Use {@link #getDefinition(String)}.
   */
  @Deprecated
  CurveDefinition getCurveDefinition(String name);

  /**
   * Gets the named curve definition that matches the version correction. This method works only for curve definitions
   * of type {@link CurveDefinition} and {@link InterpolatedCurveDefinition} and is maintained for backwards
   * compatibility.
   * @param name The name of the curve definition, not null
   * @param versionCorrection The version correction of the definition, not null
   * @return The curve definition
   * @deprecated This method does not handle all types of {@link AbstractCurveDefinition}. Use {@link #getDefinition(String, VersionCorrection)}
   */
  @Deprecated
  CurveDefinition getCurveDefinition(String name, VersionCorrection versionCorrection);

  /**
   * Gets the named curve definition.
   * @param name The name of the curve definition, not null
   * @return The curve definition
   */
  AbstractCurveDefinition getDefinition(String name);

  /**
   * Gets the named curve definition that matches the version correction.
   * @param name The name of the curve definition, not null
   * @param versionCorrection The version correction of the definition, not null
   * @return The curve definition
   */
  AbstractCurveDefinition getDefinition(String name, VersionCorrection versionCorrection);
}
