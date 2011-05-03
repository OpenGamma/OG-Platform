/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.util.PublicAPI;

/**
 * Standard names used to refer to particular value properties.
 * <p>
 * These name are used as keys to define specific value properties for the engine.
 * They should be used by a {@link FunctionDefinition} to annotate their requirements
 * or provide additional context about their outputs.
 */
@PublicAPI
public final class ValuePropertyNames {

  /**
   * Restricted constructor.
   */
  private ValuePropertyNames() {
  }

  /**
   * The currency of a value, specified as an ISO code.
   * <p>
   * This should only be used where it is meaningful to describe a value with a single currency.
   * For example, an exchange rate should not make use of this property.
   */
  public static final String CURRENCY = "Currency";

  /**
   * The symbolic name of a curve used to produce a value, valid within the naming context of
   * the function repository containing the function definition used.
   * <p>
   * This should only be used where it is meaningful to describe a value based on a single
   * curve. If produced by functions requiring multiple curve inputs, a suitable prefix should
   * be used to differentiate the input curves.
   */
  public static final String CURVE = "Curve";

  /**
   * The function identifier that produced a value.
   * <p>
   * If there are multiple functions in a repository that can compute a given value, this can
   * be used as a constraint to force a particular one to be used.
   * <p>
   * The result {@link ValueSpecification} objects created by functions must always include an
   * appropriate function identifier.
   */
  public static final String FUNCTION = "Function";

  /**
   * The country of a value, specified as an ISO 2 digit code.
   * <p>
   * This should only be used where it is meaningful to describe a value with a single country
   * instead of a currency (e.g. a bond issued by a euro-zone country).
   */
  public static final String COUNTRY = "Country";
}
