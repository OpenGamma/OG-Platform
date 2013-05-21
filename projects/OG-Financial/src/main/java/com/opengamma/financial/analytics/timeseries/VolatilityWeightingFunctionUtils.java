/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import java.util.Set;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;

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
    ValueProperties.Builder builder = properties.copy();
    addVolatilityWeightingProperties(builder);
    return builder.get();
  }
  
  public static void addVolatilityWeightingProperties(ValueProperties.Builder builder) {
    addVolatilityWeightingProperties(builder, null);
  }
  
  public static void addVolatilityWeightingProperties(ValueProperties.Builder builder, ValueRequirement desiredValue) {
    builder.withoutAny(ValuePropertyNames.TRANSFORMATION_METHOD)
        .with(ValuePropertyNames.TRANSFORMATION_METHOD, TRANSFORMATION_METHOD_VALUE);
    
    Set<String> startDates = desiredValue != null ? desiredValue.getConstraints().getValues(VOLATILITY_WEIGHTING_START_DATE_PROPERTY) : null;
    if (startDates != null && !startDates.isEmpty()) {
      builder.with(VOLATILITY_WEIGHTING_START_DATE_PROPERTY, startDates);
    } else {
      builder.withAny(VOLATILITY_WEIGHTING_START_DATE_PROPERTY);
    }
    
    Set<String> lambdas = desiredValue != null ? desiredValue.getConstraints().getValues(VOLATILITY_WEIGHTING_LAMBDA_PROPERTY) : null;
    if (lambdas != null && !lambdas.isEmpty()) {
      builder.with(VOLATILITY_WEIGHTING_LAMBDA_PROPERTY, lambdas);
    } else {
      builder.withAny(VOLATILITY_WEIGHTING_LAMBDA_PROPERTY);
    }
  }
  
}
