/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaldata.impl;

import java.util.Collection;

import com.google.common.collect.ImmutableSet;

/**
 * Constants for historical time-series info field names.
 */
public final class HistoricalTimeSeriesInfoFieldNames {

  /**
   * Field name for data source.
   */
  public static final String DATA_SOURCE_NAME = "dataSource";
  /**
   * Field name for data provider.
   */
  public static final String DATA_PROVIDER_NAME = "dataProvider";
  /**
   * Field name for security type.
   */
  public static final String SECURITY_TYPE_NAME = "securityType";
  /**
   * Field value for star(*).
   */
  public static final String STAR_VALUE = "*";
  /**
   * Valid field names
   */
  public static final Collection<String> VALID_FIELD_NAMES = ImmutableSet.of(DATA_SOURCE_NAME, DATA_PROVIDER_NAME, SECURITY_TYPE_NAME);
  /**
   * Default name for the default configuration
   */
  public static final String DEFAULT_CONFIG_NAME = "DEFAULT_TSS_CONFIG";

  /**
   * Restricted constructor.
   */
  private HistoricalTimeSeriesInfoFieldNames() {
  }

}
