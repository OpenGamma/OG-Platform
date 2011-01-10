/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.view.calcnode.CalculationJobSpecification;

/**
 * Fudge message builder for {@code CalculationJobSpecification}.
 */
@FudgeBuilderFor(CalculationJobSpecification.class)
public class CalculationJobSpecificationBuilder implements FudgeBuilder<CalculationJobSpecification> {
  private static final String VIEW_NAME_FIELD_NAME = "viewName";
  private static final String CALCULATION_CONFIGURATION_FIELD_NAME = "calcConfig";
  private static final String ITERATION_TIMESTAMP_FIELD_NAME = "iterationTimestamp";
  private static final String JOB_ID_FIELD_NAME = "jobId";

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, CalculationJobSpecification object) {
    MutableFudgeFieldContainer msg = context.newMessage();
    msg.add(VIEW_NAME_FIELD_NAME, object.getViewName());
    msg.add(CALCULATION_CONFIGURATION_FIELD_NAME, object.getCalcConfigName());
    msg.add(ITERATION_TIMESTAMP_FIELD_NAME, object.getIterationTimestamp());
    msg.add(JOB_ID_FIELD_NAME, object.getJobId());
    return msg;
  }

  @Override
  public CalculationJobSpecification buildObject(FudgeDeserializationContext context, FudgeFieldContainer msg) {
    String viewName = msg.getString(VIEW_NAME_FIELD_NAME);
    String calcConfigName = msg.getString(CALCULATION_CONFIGURATION_FIELD_NAME);
    long iterationTimestamp = msg.getLong(ITERATION_TIMESTAMP_FIELD_NAME);
    long jobId = msg.getLong(JOB_ID_FIELD_NAME);
    return new CalculationJobSpecification(viewName, calcConfigName, iterationTimestamp, jobId);
  }

}
