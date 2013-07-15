/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import com.opengamma.engine.value.ValueRequirement;

/* package */abstract class DirectResolvedValueProducer extends AbstractResolvedValueProducer {

  private volatile boolean _recursionDetected;

  public DirectResolvedValueProducer(final ValueRequirement valueRequirement) {
    super(valueRequirement);
  }

  @Override
  protected void setRecursionDetected() {
    _recursionDetected = true;
    super.setRecursionDetected();
  }

  public boolean wasRecursionDetected() {
    return _recursionDetected;
  }

  @Override
  public Cancelable addCallback(final GraphBuildingContext context, final ResolvedValueCallback valueCallback) {
    final Cancelable cancelable = super.addCallback(context, valueCallback);
    if (_recursionDetected) {
      valueCallback.recursionDetected();
    }
    return cancelable;
  }

}
