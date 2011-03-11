/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language;

import com.opengamma.util.ArgumentChecker;

/**
 * Utility methods for converting to/from the {@link Data} type.
 */
public final class DataUtil {

  /**
   * Prevent instantiation.
   */
  private DataUtil() {
  }

  public static Data of(final Value value) {
    ArgumentChecker.notNull(value, "value");
    final Data data = new Data();
    data.setSingle(value);
    return data;
  }

  public static Data of(final Value[] values) {
    ArgumentChecker.notNull(values, "values");
    for (int i = 0; i < values.length; i++) {
      ArgumentChecker.notNull(values[i], "value[" + i + "]");
    }
    final Data data = new Data();
    data.setLinear(values);
    return data;
  }

  public static Data of(final Value[][] values) {
    ArgumentChecker.notNull(values, "values");
    for (int i = 0; i < values.length; i++) {
      ArgumentChecker.notNull(values[i], "value[" + i + "]");
      for (int j = 0; j < values[i].length; j++) {
        ArgumentChecker.notNull(values[i][j], "value[" + i + "][" + j + "]");
      }
    }
    final Data data = new Data();
    data.setMatrix(values);
    return data;
  }

}
