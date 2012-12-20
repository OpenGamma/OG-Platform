/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.debug;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.opengamma.language.Data;
import com.opengamma.language.DataUtils;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.definition.Categories;
import com.opengamma.language.definition.MetaParameter;
import com.opengamma.language.procedure.AbstractProcedureInvoker;
import com.opengamma.language.procedure.MetaProcedure;
import com.opengamma.language.procedure.PublishedProcedure;

/**
 * Trivial procedure for debugging. Increments an internal counter and returns the new value.
 */
public class DebugProcedureIncrement implements PublishedProcedure {

  private final AtomicInteger _value = new AtomicInteger();

  private Data execute() {
    return DataUtils.of(_value.incrementAndGet());
  }

  @Override
  public MetaProcedure getMetaProcedure() {
    final List<MetaParameter> args = Collections.emptyList();
    return new MetaProcedure(Categories.DEBUG, "DebugProcedureIncrement", args, new AbstractProcedureInvoker.SingleResult(args) {
      @Override
      protected Object invokeImpl(SessionContext sessionContext, Object[] parameters) {
        return execute();
      }
    }, 1);
  }

}
