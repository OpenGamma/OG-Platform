/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.Instant;

import com.opengamma.engine.calcnode.CalculationJobSpecification;
import com.opengamma.id.UniqueId;

/**
 * Fudge message builder for {@code CalculationJobSpecification}.
 * 
 * <pre>
 * message CalculationJobSpecification {
 *   required UniqueId viewCycleId;     // the view cycle identifier
 *   required string calcConfig;        // the calculation configuration name
 *   required datetime valuationTime;   // the valuation time
 *   required long jobId;               // the job identifier
 * }
 * </pre>
 */
@FudgeBuilderFor(CalculationJobSpecification.class)
public class CalculationJobSpecificationFudgeBuilder implements FudgeBuilder<CalculationJobSpecification> {

  private static final String VIEW_CYCLE_ID_FIELD_NAME = "viewCycleId";
  private static final String CALCULATION_CONFIGURATION_FIELD_NAME = "calcConfig";
  private static final String VALUATION_TIME_FIELD_NAME = "valuationTime";
  private static final String JOB_ID_FIELD_NAME = "jobId";

  public static void buildMessageImpl(final MutableFudgeMsg msg, final CalculationJobSpecification object) {
    msg.add(VIEW_CYCLE_ID_FIELD_NAME, object.getViewCycleId());
    msg.add(CALCULATION_CONFIGURATION_FIELD_NAME, object.getCalcConfigName());
    msg.add(VALUATION_TIME_FIELD_NAME, object.getValuationTime());
    msg.add(JOB_ID_FIELD_NAME, object.getJobId());
  }

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CalculationJobSpecification object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    buildMessageImpl(msg, object);
    return msg;
  }

  public static CalculationJobSpecification buildObjectImpl(final FudgeMsg msg) {
    UniqueId viewCycleId = msg.getValue(UniqueId.class, VIEW_CYCLE_ID_FIELD_NAME);
    String calcConfigName = msg.getString(CALCULATION_CONFIGURATION_FIELD_NAME);
    Instant valuationTime = msg.getValue(Instant.class, VALUATION_TIME_FIELD_NAME);
    long jobId = msg.getLong(JOB_ID_FIELD_NAME);
    return new CalculationJobSpecification(viewCycleId, calcConfigName, valuationTime, jobId);
  }

  @Override
  public CalculationJobSpecification buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    return buildObjectImpl(msg);
  }

}
