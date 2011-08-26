/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.calcnode.CalculationJobResultItem;
import com.opengamma.engine.view.calcnode.CalculationJobSpecification;

/**
 * Fudge message builder for {@code CalculationJob}.
 */
@FudgeBuilderFor(CalculationJobResult.class)
public class CalculationJobResultBuilder implements FudgeBuilder<CalculationJobResult> {
  private static final String DURATION_FIELD_NAME = "duration";
  private static final String ITEMS_FIELD_NAME = "resultItems";
  private static final String COMPUTE_NODE_ID_FIELD_NAME = "computeNodeId";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CalculationJobResult object) {
    MutableFudgeMsg msg = serializer.objectToFudgeMsg(object.getSpecification());
    msg.add(DURATION_FIELD_NAME, object.getDuration());
    msg.add(COMPUTE_NODE_ID_FIELD_NAME, object.getComputeNodeId());
    for (CalculationJobResultItem item : object.getResultItems()) {
      serializer.addToMessage(msg, ITEMS_FIELD_NAME, null, item);
    }
    return msg;
  }

  @Override
  public CalculationJobResult buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    CalculationJobSpecification jobSpec = deserializer.fudgeMsgToObject(CalculationJobSpecification.class, msg);
    long duration = msg.getLong(DURATION_FIELD_NAME);
    String nodeId = msg.getString(COMPUTE_NODE_ID_FIELD_NAME);
    List<CalculationJobResultItem> jobItems = new ArrayList<CalculationJobResultItem>();
    for (FudgeField field : msg.getAllByName(ITEMS_FIELD_NAME)) {
      CalculationJobResultItem jobItem = deserializer.fudgeMsgToObject(CalculationJobResultItem.class, (FudgeMsg) field.getValue());
      jobItems.add(jobItem);
    }
    return new CalculationJobResult(jobSpec, duration, jobItems, nodeId);
  }

}
