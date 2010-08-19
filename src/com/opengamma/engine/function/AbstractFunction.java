/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Collections;
import java.util.Set;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.value.ValueSpecification;

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
    if (_uniqueIdentifier != null) {
      throw new IllegalStateException("Function unique ID already set");
    }
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
  public Set<ValueSpecification> getRequiredLiveData() {
    return Collections.emptySet();
  }

  private static final String UNIQUE_IDENTIFIER_KEY = "uniqueIdentifier";

  public void toFudgeMsg(final FudgeSerializationContext context, final MutableFudgeFieldContainer message) {
    // Store the leaf class only; there's no point in the receiver doing a partial deserialisation
    context.objectToFudgeMsg(message, null, 0, getClass().getName());
    // Add the identifier
    if (getUniqueIdentifier() != null) {
      message.add(UNIQUE_IDENTIFIER_KEY, null, getUniqueIdentifier());
    }
  }

  protected static <T extends AbstractFunction> T fromFudgeMsg(final T object, final FudgeFieldContainer message) {
    object.setUniqueIdentifier(message.getString(UNIQUE_IDENTIFIER_KEY));
    return object;
  }

}
