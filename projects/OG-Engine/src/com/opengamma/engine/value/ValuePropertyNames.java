/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import com.opengamma.util.PublicAPI;

/**
 * A set of common names used to refer to particular computed value properties. These should be used
 * by function definitions to annotate their requirements or provide additional context about their
 * outputs.
 */
@PublicAPI
public final class ValuePropertyNames {
  
  /**
   * The currency of a value, specified as an ISO code. This should only be used where it is meaningful
   * to describe a value with a single currency. For example, an exchange rate should not make use of
   * this property.
   */
  public static final String CURRENCY = "Currency";

  /**
   * The function identifier that produced a value. If there are multiple functions in a repository
   * that can compute a given value, this can be used as a constraint to force a particular one to
   * be used.
   * <p>
   * The result {@link ValueSpecification} objects created by functions must always include an
   * appropriate function identifier.
   */
  public static final String FUNCTION = "Function";
  
  private ValuePropertyNames() {
  }

}
