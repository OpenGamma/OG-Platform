/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language;

import org.fudgemsg.FudgeMsg;

import com.opengamma.util.ArgumentChecker;

/**
 * Utility methods for converting to/from the {@link Data} type.
 */
public final class DataUtils {

  /**
   * Prevent instantiation.
   */
  private DataUtils() {
  }

  public static boolean isNull(final Data value) {
    if (value.getSingle() != null) {
      return ValueUtils.isNull(value.getSingle());
    } else if (value.getLinear() != null) {
      return false;
    } else if (value.getMatrix() != null) {
      return false;
    } else {
      return true;
    }
  }

  public static Data of(final Value value) {
    ArgumentChecker.notNull(value, "value");
    final Data data = new Data();
    data.setSingle(value);
    return data;
  }

  public static Data of(final boolean boolValue) {
    return of(ValueUtils.of(boolValue));
  }

  public static Data of(final double doubleValue) {
    return of(ValueUtils.of(doubleValue));
  }

  public static Data ofError(final int errorValue) {
    return of(ValueUtils.ofError(errorValue));
  }

  public static Data of(final int intValue) {
    return of(ValueUtils.of(intValue));
  }

  public static Data of(final FudgeMsg messageValue) {
    return of(ValueUtils.of(messageValue));
  }

  public static Data of(final String stringValue) {
    return of(ValueUtils.of(stringValue));
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

  /**
   * Displayable form of the Data object.
   * 
   * @param data  the object to convert to a string
   * @param quoted  true to put quote marks around strings and escape them
   * @return the displayable string
   */
  public static String toString(final Data data, final boolean quoted) {
    if (data.getSingle() != null) {
      return ValueUtils.toString(data.getSingle(), quoted);
    } else if (data.getLinear() != null) {
      final StringBuilder sb = new StringBuilder();
      sb.append('[');
      for (int i = 0; i < data.getLinear().length; i++) {
        if (i > 0) {
          sb.append(", ");
        }
        sb.append(ValueUtils.toString(data.getLinear()[i], quoted));
      }
      sb.append(']');
      return sb.toString();
    } else if (data.getMatrix() != null) {
      final StringBuilder sb = new StringBuilder();
      sb.append('[');
      for (int i = 0; i < data.getMatrix().length; i++) {
        if (i > 0) {
          sb.append(", ");
        }
        sb.append('[');
        for (int j = 0; j < data.getMatrix()[i].length; j++) {
          if (j > 0) {
            sb.append(", ");
          }
          sb.append(ValueUtils.toString(data.getMatrix()[i][j], quoted));
        }
        sb.append(']');
      }
      sb.append(']');
      return sb.toString();
    } else {
      return "Data";
    }
  }

  public static Value toValue(final Data data) {
    if (data.getSingle() != null) {
      return data.getSingle();
    } else if (data.getLinear() != null) {
      if (data.getLinear().length > 0) {
        return data.getLinear()[0];
      } else {
        return null;
      }
    } else if (data.getMatrix() != null) {
      if (data.getMatrix().length > 0) {
        if (data.getMatrix()[0].length > 0) {
          return data.getMatrix()[0][0];
        } else {
          return null;
        }
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  public static Boolean toBool(final Data data) {
    return ValueUtils.toBool(toValue(data));
  }

  public static Double toDouble(final Data data) {
    return ValueUtils.toDouble(toValue(data));
  }

  public static Integer toError(final Data data) {
    return ValueUtils.toError(toValue(data));
  }

  public static Integer toInt(final Data data) {
    return ValueUtils.toInt(toValue(data));
  }

  public static FudgeMsg toMessage(final Data data) {
    return ValueUtils.toMessage(toValue(data));
  }

}
