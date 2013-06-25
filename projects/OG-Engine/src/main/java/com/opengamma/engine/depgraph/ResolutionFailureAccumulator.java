/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.ArrayList;
import java.util.List;


/**
 * Simple implementation of the {@link ResolutionFailureListener} which simply
 * adds the failures to a list.
 */
public class ResolutionFailureAccumulator implements ResolutionFailureListener {
  
  private List<ResolutionFailure> _resolutionFailures = new ArrayList<>();
  
  @Override
  public void notifyFailure(ResolutionFailure resolutionFailure) {
    _resolutionFailures.add(resolutionFailure);
  }
  
  /**
   * Gets the accumulated resolution failures.
   * @return the resolution failures
   */
  public List<ResolutionFailure> getResolutionFailures() {
    return _resolutionFailures;
  }

}
