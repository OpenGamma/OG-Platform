/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.config;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.util.ArgumentChecker;

/**
 * An immutable TimeSeriesMetaData Rule
 */
public class TimeSeriesMetaDataRating {
  
  /**
   * The field name
   */
  private String _fieldName;
  
  /**
   * The field value
   */
  private String _fieldValue;
  
  /**
   * The rating
   */
  private int _rating;
  
  /**
   * @param fieldName the field name, not null
   * @param fieldValue the field value, not null
   * @param rating the rating, >= 0
   * @throws IllegalArgumentException if fieldName and fieldValue is null and fieldName not a valid one
   */
  public TimeSeriesMetaDataRating(String fieldName, String fieldValue, int rating) {
    ArgumentChecker.notNull(fieldName, "fieldName");
    ArgumentChecker.isTrue(TimeSeriesMetaDataFieldNames.VALID_FIELD_NAMES.contains(fieldName), "invalid field name");
    ArgumentChecker.notNull(fieldValue, "fieldValue");
    ArgumentChecker.isTrue(rating >= 0, "rating can not be negative");
    _fieldName = fieldName;
    _fieldValue = fieldValue;
    _rating = rating;
  }
  
  /**
   * Gets the fieldName field.
   * @return the fieldName
   */
  public String getFieldName() {
    return _fieldName;
  }
  
  /**
   * Gets the fieldValue field.
   * @return the fieldValue
   */
  public String getFieldValue() {
    return _fieldValue;
  }
  
  /**
   * Gets the rating field.
   * @return the rating
   */
  public int getRating() {
    return _rating;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ObjectUtils.hashCode(_fieldName);
    result = prime * result + ObjectUtils.hashCode(_fieldValue);
    result = prime * result + _rating;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof TimeSeriesMetaDataRating) {
      TimeSeriesMetaDataRating other = (TimeSeriesMetaDataRating) obj;
      return ObjectUtils.equals(_fieldName, other._fieldName) && ObjectUtils.equals(_fieldValue, other._fieldValue) && _rating == other._rating;
    }
    return false;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
  
}
