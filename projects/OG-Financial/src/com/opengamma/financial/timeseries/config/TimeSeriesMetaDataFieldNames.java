/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.config;

import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.Sets;

/**
 * Utility for TimeSeriesMetaData fields names
 */
public final class TimeSeriesMetaDataFieldNames {
  /**
   * Field name for data source
   */
  public static final String DATA_SOURCE_NAME = "dataSource";
  /**
   * Field name for data provider
   */
  public static final String DATA_PROVIDER_NAME = "dataProvider";
  /**
   * Field name for security type
   */
  public static final String SECURITY_TYPE_NAME = "securityType";
  /**
   * Field value for star(*)
   */
  public static final String STAR_VALUE = "*";
  /**
   * Valid field names
   */
  public static final Collection<String> VALID_FIELD_NAMES = Collections.unmodifiableSet(Sets.newHashSet(DATA_SOURCE_NAME,
      DATA_PROVIDER_NAME, SECURITY_TYPE_NAME));
  /**
   * Default name for the default TSS configuration
   */
  public static final String DEFAULT_CONFIG_NAME = "DEFAULT_TSS_CONFIG";

  private TimeSeriesMetaDataFieldNames() {
  }
  
}
