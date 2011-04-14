/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import javax.time.Instant;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;

/**
 * Fudge message builder for {@link ViewCycleExecutionOptions}
 */
@FudgeBuilderFor(ViewCycleExecutionOptions.class)
public class ViewCycleExecutionOptionsBuilder implements FudgeBuilder<ViewCycleExecutionOptions> {

  private static final String VALUATION_TIME_FIELD = "valuation";
  private static final String INPUT_DATA_TIME_FIELD = "inputData";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, ViewCycleExecutionOptions object) {
    MutableFudgeMsg msg = context.newMessage();
    msg.add(VALUATION_TIME_FIELD, object.getValuationTime());
    msg.add(INPUT_DATA_TIME_FIELD, object.getInputDataTime());
    return msg;
  }

  @Override
  public ViewCycleExecutionOptions buildObject(FudgeDeserializationContext context, FudgeMsg msg) {
    Instant valuationTime = msg.getValue(Instant.class, VALUATION_TIME_FIELD);
    Instant inputDataTime = msg.getValue(Instant.class, INPUT_DATA_TIME_FIELD);
    return new ViewCycleExecutionOptions(valuationTime, inputDataTime);
  }

}
