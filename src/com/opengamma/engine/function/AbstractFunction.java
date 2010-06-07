/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.value.ValueRequirement;


/**
 * The base class from which most {@link FunctionDefinition} implementations
 * should inherit.
 *
 * @author kirk
 */
public abstract class AbstractFunction implements FunctionDefinition {
  private String _uniqueIdentifier;

  /**
   * @return the uniqueIdentifier
   */
  public String getUniqueIdentifier() {
    return _uniqueIdentifier;
  }

  /**
   * @param uniqueIdentifier the uniqueIdentifier to set
   */
  public void setUniqueIdentifier(String uniqueIdentifier) {
    _uniqueIdentifier = uniqueIdentifier;
  }
  
  @Override
  public boolean buildsOwnSubGraph() {
    // TODO Auto-generated method stub
    return false;
  }
  
  @Override
  public void init(FunctionCompilationContext context) {
  }

  @Override
  public Set<ValueRequirement> getAllRequiredLiveData() {
    return Collections.emptySet();
  }

}
