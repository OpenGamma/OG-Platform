/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.debug;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import com.opengamma.language.Data;
import com.opengamma.language.DataUtil;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.procedure.MetaProcedure;
import com.opengamma.language.procedure.PublishedProcedure;

/**
 * Trivial procedure for debugging. Increments an internal counter and returns the new value.
 */
public class DebugProcedureIncrement implements PublishedProcedure {

  private final AtomicInteger _value = new AtomicInteger();

  private Data execute() {
    return DataUtil.of(_value.incrementAndGet());
  }

  @Override
  public MetaProcedure getMetaProcedure() {
    // TODO: invocation
    return new MetaProcedure("DebugProcedureIncrement", Collections.<MetaParameter>emptyList(), 1);
  }

}
