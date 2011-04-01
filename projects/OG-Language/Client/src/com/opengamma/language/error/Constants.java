/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.error;

import java.util.HashSet;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Defines some reserved error constants. A language binding should use custom errors taken from the
 * declared ranges here. Must match the code in Connector/Errors.h
 */
public final class Constants {

  private static final Set<Integer> s_declared = new HashSet<Integer>();

  public static int declare(final int err) {
    if (!s_declared.add(err)) {
      throw new OpenGammaRuntimeException("Error code " + err + " already declared");
    }
    return err;
  }

  private Constants() {
  }

  /**
   * Errors 1 to 99 reserved for infrastructure.
   */
  public static final int FIRST_RESERVED_ERROR = 1, LAST_RESERVED_ERROR = 99;

  private static final int ERROR_INVOCATION = FIRST_RESERVED_ERROR;

  /**
   * A parameter could not be converted. The parameter index is in the integer field. A description of the problem is in the string field.
   */
  public static final int ERROR_PARAMETER_CONVERSION = declare(ERROR_INVOCATION + 0);

  /**
   * A result could not be converted. The result index is in the integer field. A description of the problem is in the string field. 
   */
  public static final int ERROR_RESULT_CONVERSION = declare(ERROR_INVOCATION + 1);

  /**
   * Errors 100 to 999 reserved for built-in OpenGamma functions.
   */
  public static final int FIRST_OPENGAMMA_ERROR = 100, LAST_OPENGAMMA_ERROR = 999;

  /**
   * Errors 1000 to 9999 for use by a custom language binding only; these codes must not be used
   * by the core infrastructure or OpenGamma built-in functions.
   */
  public static final int FIRST_USER_ERROR = 1000, LAST_USER_ERROR = 9999;

}
