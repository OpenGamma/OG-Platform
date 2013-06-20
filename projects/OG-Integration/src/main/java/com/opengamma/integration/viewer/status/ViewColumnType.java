/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.MetaProperty;

import com.opengamma.integration.viewer.status.impl.ViewStatusKeyBean;

/**
 * View Status Report Column type
 */
public enum ViewColumnType {
  /**
   * By security type
   */
  SECURITY("S", ViewStatusKeyBean.meta().securityType()),
  /**
   * By Value Requirement Name
   */
  VALUE_REQUIREMENT_NAME("V", ViewStatusKeyBean.meta().valueRequirementName()),
  /**
   * By Currency
   */
  CURRENCY("C", ViewStatusKeyBean.meta().currency()),
  /**
   * By Computation target type
   */
  TARGET_TYPE("T", ViewStatusKeyBean.meta().targetType());
  
  /**
   * Short name
   */
  private final String _shortName;
  /**
   * Meta property
   */
  private final MetaProperty<String> _metaProperty;
  
  private ViewColumnType(String shortName, MetaProperty<String> metaProperty) {
    _shortName = shortName;
    _metaProperty = metaProperty;
  }
  
  /**
   * Gets the shortName.
   * @return the shortName
   */
  public String getShortName() {
    return _shortName;
  }
  
  /**
   * Gets the metaProperty.
   * @return the metaProperty
   */
  public MetaProperty<String> getMetaProperty() {
    return _metaProperty;
  }

  /**
   * Produce a ViewColumnType equivalent of a given short name.
   * 
   * @param shortName the shortname, not-null
   * @return the view columntype for the short name or null if there is no match
   */
  public static ViewColumnType of(String shortName) {
    shortName = StringUtils.trimToNull(shortName);
    if (shortName != null) {
      ViewColumnType[] values = ViewColumnType.values();
      for (ViewColumnType type : values) {
        if (type.getShortName().equalsIgnoreCase(shortName)) {
          return type;
        }
      }
    }
    return null;
  }

  public static ViewColumnType of(char shortName) {
    return of(String.valueOf(shortName));
  }
  
}
