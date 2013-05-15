/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.impl;

import java.util.Collection;
import java.util.Map;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.util.tuple.Pair;

/**
 * A simple implementation of the calculation result model.
 */
public class ViewCalculationResultModelImpl extends AbstractResultModel<ComputationTargetSpecification> implements ViewCalculationResultModel {

  private static final long serialVersionUID = 1L;

  @Override
  public Map<Pair<String, ValueProperties>, ComputedValueResult> getValues(final ComputationTargetSpecification target) {
    return super.getValuesByName(target);
  }

  @Override
  public Collection<ComputedValueResult> getAllValues(final ComputationTargetSpecification target) {
    return super.getAllValues(target);
  }

  @Override
  public Collection<ComputationTargetSpecification> getAllTargets() {
    return getKeys();
  }

}
