/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collection;

/**
 * A simple implementation of the per-target calculation result model.
 */
public class ViewTargetResultModelImpl extends AbstractResultModel<String> implements ViewTargetResultModel {

  @Override
  public Collection<String> getCalculationConfigurationNames() {
    return getKeys();
  }

}
