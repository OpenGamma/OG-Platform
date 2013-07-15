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

import com.opengamma.engine.calcnode.CalculationJobResult;
import com.opengamma.engine.calcnode.CalculationJobResultItem;
import com.opengamma.engine.calcnode.CalculationJobSpecification;

/**
 * Fudge message builder for {@code CalculationJob}.
 * 
 * <pre>
 * message CalculationJobResult extends CalculationJobSpecification {
 *   required long duration;                    // job execution time
 *   required CalculationJobResultItem[] items; // job items - in the same order as the original CalculationJob
 *   required string nodeId;                    // node identifier
 * }
 * </pre>
 */
@FudgeBuilderFor(CalculationJobResult.class)
public class CalculationJobResultFudgeBuilder implements FudgeBuilder<CalculationJobResult> {

  private static final String DURATION_FIELD_NAME = "duration";
  private static final String ITEMS_FIELD_NAME = "items";
  private static final String NODE_ID_FIELD_NAME = "nodeId";

  protected FudgeMsg buildItemsMessage(final FudgeSerializer serializer, final List<CalculationJobResultItem> items) {
    final MutableFudgeMsg msg = serializer.newMessage();
    for (CalculationJobResultItem item : items) {
      msg.add(null, null, CalculationJobResultItemFudgeBuilder.buildMessageImpl(serializer, item));
    }
    return msg;
  }

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CalculationJobResult object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    CalculationJobSpecificationFudgeBuilder.buildMessageImpl(msg, object.getSpecification());
    msg.add(DURATION_FIELD_NAME, object.getDuration());
    msg.add(ITEMS_FIELD_NAME, buildItemsMessage(serializer, object.getResultItems()));
    msg.add(NODE_ID_FIELD_NAME, object.getComputeNodeId());
    return msg;
  }

  protected List<CalculationJobResultItem> buildItemsObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final List<CalculationJobResultItem> result = new ArrayList<CalculationJobResultItem>(msg.getNumFields());
    for (FudgeField field : msg) {
      result.add(CalculationJobResultItemFudgeBuilder.buildObjectImpl(deserializer, (FudgeMsg) field.getValue()));
    }
    return result;
  }

  @Override
  public CalculationJobResult buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    final CalculationJobSpecification jobSpec = CalculationJobSpecificationFudgeBuilder.buildObjectImpl(msg);
    final long duration = msg.getLong(DURATION_FIELD_NAME);
    final List<CalculationJobResultItem> jobItems = buildItemsObject(deserializer, msg.getMessage(ITEMS_FIELD_NAME));
    final String nodeId = msg.getString(NODE_ID_FIELD_NAME);
    return new CalculationJobResult(jobSpec, duration, jobItems, nodeId);
  }

}
