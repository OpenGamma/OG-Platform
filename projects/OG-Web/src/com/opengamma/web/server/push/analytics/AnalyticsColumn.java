/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.Set;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.server.RequirementBasedColumnKey;

/**
 * Specifies the header label of a column and the type of data it displays.
 */
public class AnalyticsColumn {

  private final String _header;
  private final String _description;
  private final Class<?> _type;

  public AnalyticsColumn(String header, String description, Class<?> type) {
    ArgumentChecker.notNull(header, "header");
    _header = header;
    if (description != null) {
      _description = description;
    } else {
      _description = header;
    }
    _type = type;
  }

  public static AnalyticsColumn forKey(RequirementBasedColumnKey key, Class<?> columnType) {
    return new AnalyticsColumn(createHeader(key), createDescription(key.getValueProperties()), columnType);
  }

  /* package */ String getHeader() {
    return _header;
  }

  /* package */ String getDescription() {
    return _description;
  }

  public Class<?> getType() {
    return _type;
  }

  private static String createHeader(RequirementBasedColumnKey columnKey) {
    String header;
    String normalizedConfigName = columnKey.getCalcConfigName().toLowerCase().trim();
    if ("default".equals(normalizedConfigName) || "portfolio".equals(normalizedConfigName)) {
      header = columnKey.getValueName();
    } else {
      header = columnKey.getCalcConfigName() + "/" + columnKey.getValueName();
    }
    return header;
  }

  private static String createDescription(ValueProperties constraints) {
    if (constraints.isEmpty()) {
      return "No constraints";
    }
    StringBuilder sb = new StringBuilder();
    boolean firstProperty = true;
    for (String propertyName : constraints.getProperties()) {
      if (ValuePropertyNames.FUNCTION.equals(propertyName)) {
        continue;
      }
      if (firstProperty) {
        firstProperty = false;
      } else {
        sb.append("; \n");
      }
      sb.append(propertyName).append("=");
      Set<String> propertyValues = constraints.getValues(propertyName);
      boolean isOptional = constraints.isOptional(propertyName);
      if (propertyValues.size() == 0) {
        sb.append("[empty]");
      } else if (propertyValues.size() == 1 && !isOptional) {
        sb.append(propertyValues.iterator().next());
      } else {
        sb.append("(");
        boolean firstValue = true;
        for (String propertyValue : propertyValues) {
          if (firstValue) {
            firstValue = false;
          } else {
            sb.append(", ");
          }
          sb.append(propertyValue);
        }
        sb.append(")");
      }
      if (isOptional) {
        sb.append("?");
      }
    }
    return sb.toString();
  }

  @Override
  public String toString() {
    return "AnalyticsColumn [_header='" + _header + '\'' + ", _description='" + _description + '\'' + "]";
  }
}
