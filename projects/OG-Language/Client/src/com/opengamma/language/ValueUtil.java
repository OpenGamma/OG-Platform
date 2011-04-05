/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language;

import org.apache.commons.lang.StringEscapeUtils;
import org.fudgemsg.FudgeFieldContainer;

import com.opengamma.util.ArgumentChecker;

/**
 * Utility methods for converting to/from the {@link Value} type.
 */
public final class ValueUtil {

  /**
   * Prevent instantiation.
   */
  private ValueUtil() {
  }

  public static boolean isNull(final Value value) {
    if (value == null) {
      return true;
    }
    return (value.getBoolValue() == null)
        && (value.getDoubleValue() == null)
        && (value.getErrorValue() == null)
        && (value.getIntValue() == null)
        && (value.getMessageValue() == null)
        && (value.getStringValue() == null);
  }

  public static Value of(final Boolean boolValue) {
    ArgumentChecker.notNull(boolValue, "boolValue");
    final Value value = new Value();
    value.setBoolValue(boolValue);
    return value;
  }

  public static Value of(final Double doubleValue) {
    ArgumentChecker.notNull(doubleValue, "doubleValue");
    final Value value = new Value();
    value.setDoubleValue(doubleValue);
    return value;
  }

  public static Value ofError(final int errorValue) {
    final Value value = new Value();
    value.setErrorValue(errorValue);
    return value;
  }

  public static Value of(final Integer intValue) {
    ArgumentChecker.notNull(intValue, "intValue");
    final Value value = new Value();
    value.setIntValue(intValue);
    return value;
  }

  public static Value of(final FudgeFieldContainer messageValue) {
    ArgumentChecker.notNull(messageValue, "messageValue");
    final Value value = new Value();
    value.setMessageValue(messageValue);
    return value;
  }

  public static Value of(final String stringValue) {
    ArgumentChecker.notNull(stringValue, "stringValue");
    final Value value = new Value();
    value.setStringValue(stringValue);
    return value;
  }

  public static Boolean toBool(final Value data) {
    if (data == null) {
      return null;
    } else if (data.getErrorValue() != null) {
      return null;
    } else if (data.getBoolValue() != null) {
      return data.getBoolValue();
    } else if (data.getIntValue() != null) {
      return data.getIntValue() != 0;
    } else if (data.getDoubleValue() != null) {
      return data.getDoubleValue() != 0;
    } else {
      return null;
    }
  }

  public static Double toDouble(final Value data) {
    if (data == null) {
      return null;
    } else if (data.getErrorValue() != null) {
      return null;
    } else if (data.getDoubleValue() != null) {
      return data.getDoubleValue();
    } else if (data.getIntValue() != null) {
      return (double) data.getIntValue();
    } else if (data.getBoolValue() != null) {
      return data.getBoolValue() ? 1.0 : 0.0;
    } else if (data.getStringValue() != null) {
      return Double.parseDouble(data.getStringValue());
    } else {
      return null;
    }
  }

  public static Integer toError(final Value data) {
    if (data == null) {
      return null;
    }
    if (data.getErrorValue() != null) {
      return data.getErrorValue();
    } else {
      return null;
    }
  }

  public static Integer toInt(final Value data) {
    if (data == null) {
      return null;
    } else if (data.getErrorValue() != null) {
      return null;
    } else if (data.getIntValue() != null) {
      return data.getIntValue();
    } else if (data.getDoubleValue() != null) {
      return data.getDoubleValue().intValue();
    } else if (data.getBoolValue() != null) {
      return data.getBoolValue() ? 1 : 0;
    } else if (data.getStringValue() != null) {
      return Integer.parseInt(data.getStringValue());
    } else {
      return null;
    }
  }

  public static FudgeFieldContainer toMessage(final Value data) {
    if (data == null) {
      return null;
    } else if (data.getMessageValue() != null) {
      return data.getMessageValue();
    } else {
      return null;
    }
  }

  /**
   * Displayable form of the Value object.
   * 
   * @param value value to convert to a string
   * @return a displayable string representation
   */
  public static String toString(final Value value) {
    if (value == null) {
      return null;
    }
    final StringBuilder sb = new StringBuilder();
    if (value.getErrorValue() != null) {
      sb.append("{Error ").append(value.getErrorValue());
    }
    if (value.getBoolValue() != null) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(value.getBoolValue());
    }
    if (value.getDoubleValue() != null) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(value.getDoubleValue());
    }
    if (value.getIntValue() != null) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(value.getIntValue());
    }
    if (value.getMessageValue() != null) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(value.getMessageValue());
    }
    if (value.getStringValue() != null) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append("\"");
      sb.append(StringEscapeUtils.escapeJava(value.getStringValue()));
      sb.append("\"");
    }
    if (value.getErrorValue() != null) {
      sb.append("}");
    }
    return sb.toString();
  }

}
