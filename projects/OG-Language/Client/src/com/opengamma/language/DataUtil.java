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

  public static Data of(final boolean boolValue) {
    return of(ValueUtil.of(boolValue));
  }

  public static Data of(final double doubleValue) {
    return of(ValueUtil.of(doubleValue));
  }

  public static Data ofError(final int errorValue) {
    return of(ValueUtil.ofError(errorValue));
  }

  public static Data of(final int intValue) {
    return of(ValueUtil.of(intValue));
  }

  public static Data of(final FudgeMsg messageValue) {
    return of(ValueUtil.of(messageValue));
  }

  public static Data of(final String stringValue) {
    return of(ValueUtil.of(stringValue));
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
   * @param data the object to convert to a string
   * @return the displayable string
   */
  public static String toString(final Data data) {
    if (data.getSingle() != null) {
      return ValueUtil.toString(data.getSingle());
    } else if (data.getLinear() != null) {
      final StringBuilder sb = new StringBuilder();
      sb.append('[');
      for (int i = 0; i < data.getLinear().length; i++) {
        if (i > 0) {
          sb.append(", ");
        }
        sb.append(ValueUtil.toString(data.getLinear()[i]));
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
          sb.append(ValueUtil.toString(data.getMatrix()[i][j]));
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
    return ValueUtil.toBool(toValue(data));
  }

  public static Double toDouble(final Data data) {
    return ValueUtil.toDouble(toValue(data));
  }

  public static Integer toError(final Data data) {
    return ValueUtil.toError(toValue(data));
  }

  public static Integer toInt(final Data data) {
    return ValueUtil.toInt(toValue(data));
  }

  public static FudgeFieldContainer toMessage(final Data data) {
    return ValueUtil.toMessage(toValue(data));
  }

}
