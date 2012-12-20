/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.util.ArgumentChecker;

/**
 * An individual rule for rating for a field name and value.
 * <p>
 * This class is immutable and thread-safe.
 */
public final class HistoricalTimeSeriesRatingRule {

  /**
   * The field name
   */
  private final String _fieldName;
  /**
   * The field value
   */
  private final String _fieldValue;
  /**
   * The rating
   */
  private final int _rating;

  /**
   * Creates an instance.
   * 
   * @param fieldName  the field name, not null
   * @param fieldValue  the field value, not null
   * @param rating  the rating, zero or greater
   * @throws IllegalArgumentException if the input is invalid
   */
  public HistoricalTimeSeriesRatingRule(String fieldName, String fieldValue, int rating) {
    ArgumentChecker.notNull(fieldName, "fieldName");
    ArgumentChecker.isTrue(HistoricalTimeSeriesRatingFieldNames.VALID_FIELD_NAMES.contains(fieldName), "invalid field name");
    ArgumentChecker.notNull(fieldValue, "fieldValue");
    ArgumentChecker.isTrue(rating >= 0, "rating cannot be negative");
    _fieldName = fieldName;
    _fieldValue = fieldValue;
    _rating = rating;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the fieldName field.
   * 
   * @return the field name, not null
   */
  public String getFieldName() {
    return _fieldName;
  }

  /**
   * Gets the fieldValue field.
   * 
   * @return the field value, not null
   */
  public String getFieldValue() {
    return _fieldValue;
  }

  /**
   * Gets the rating field.
   * 
   * @return the rating, zero or greater
   */
  public int getRating() {
    return _rating;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof HistoricalTimeSeriesRatingRule) {
      HistoricalTimeSeriesRatingRule other = (HistoricalTimeSeriesRatingRule) obj;
      return ObjectUtils.equals(_fieldName, other._fieldName) && ObjectUtils.equals(_fieldValue, other._fieldValue) && _rating == other._rating;
    }
    return false;
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
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
