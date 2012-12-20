/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import com.opengamma.engine.value.ValuePropertyNames;

/**
 * Summing function that only considers applicable child values.
 */
public class FilteringSummingFunction extends SummingFunction {

  /**
   * Value of the {@link ValuePropertyNames#AGGREGATION} property set on the output produced. This
   * allows the result to be distinguished from a related summing function that converts its
   * inputs somehow.
   */
  public static final String AGGREGATION_STYLE_FILTERED = "Filtered";

  public FilteringSummingFunction(final String valueName) {
    super(valueName);
  }

  @Override
  protected String getAggregationStyleFull() {
    return AGGREGATION_STYLE_FILTERED;
  }

  @Override
  public String getShortName() {
    return AGGREGATION_STYLE_FILTERED + super.getShortName();
  }

  @Override
  public boolean canHandleMissingRequirements() {
    return true;
  }

}
