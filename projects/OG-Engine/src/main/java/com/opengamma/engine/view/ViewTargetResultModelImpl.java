/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collection;

import com.opengamma.engine.value.ComputedValue;

/**
 * A simple implementation of the per-target calculation result model.
 */
public class ViewTargetResultModelImpl extends AbstractResultModel<String> implements ViewTargetResultModel {

  private static final long serialVersionUID = 1L;

  @Override
  public Collection<String> getCalculationConfigurationNames() {
    return getKeys();
  }
  
  @Override
  public Collection<ComputedValue> getAllValues(final String calcConfigurationName) {
    return super.getAllValues(calcConfigurationName);
  }

}
