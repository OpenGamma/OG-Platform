/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;

/**
 * 
 */
public final class VolatilityWeightingFunctionUtils {

  /**
   * Transformation method
   */
  public static final String TRANSFORMATION_METHOD_VALUE = "VolatilityWeighting";
  /**
   * Volatility weighting start date property.
   */
  public static final String VOLATILITY_WEIGHTING_START_DATE_PROPERTY = "VolatilityWeightingStartDate";
  /**
   * Volatility weighting decay property.
   */
  public static final String VOLATILITY_WEIGHTING_LAMBDA_PROPERTY = "VolatilityWeightingLambda";
  
  /**
   * Hidden constructor.
   */
  private VolatilityWeightingFunctionUtils() {
  }
  
  public static ValueProperties addVolatilityWeightingProperties(ValueProperties properties) {
    return properties.copy()
        .withoutAny(ValuePropertyNames.TRANSFORMATION_METHOD)
        .with(ValuePropertyNames.TRANSFORMATION_METHOD, TRANSFORMATION_METHOD_VALUE)
        .withAny(VOLATILITY_WEIGHTING_START_DATE_PROPERTY)
        .withAny(VOLATILITY_WEIGHTING_LAMBDA_PROPERTY)
        .get();
  }
  
}
