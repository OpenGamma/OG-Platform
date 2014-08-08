/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.value;

/**
 * A set of common names used to refer to externally provided data values.
 */
public interface ExternalDataRequirementNames {

  // All external data field names must be prefixed with "External_" to distinguish them as external data fields in the
  // global namespace of field names. The field name that follows should be in Pascal case.

  /**
   * Externally provided first order sensitivities
   */
  String SENSITIVITY = "External_Sensitivity";

  /**
   * Externally provided second order sensitivities (convexity)
   */
  String CONVEXITY = "External_Convexity";

}
