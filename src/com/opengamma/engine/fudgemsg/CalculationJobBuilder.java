/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.ArrayList;
import java.util.Collection;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.view.cache.CacheSelectHint;
import com.opengamma.engine.view.calcnode.CalculationJob;
import com.opengamma.engine.view.calcnode.CalculationJobItem;
import com.opengamma.engine.view.calcnode.CalculationJobSpecification;
import com.opengamma.util.FudgeFieldChecker;

/**
 * Fudge message builder for {@code CalculationJob}.
 */
@FudgeBuilderFor(CalculationJob.class)
public class CalculationJobBuilder implements FudgeBuilder<CalculationJob> {
  private static final String REQUIRED_FIELD_NAME = "requiredJobId";
  private static final String ITEM_FIELD_NAME = "calculationJobItem";

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, CalculationJob object) {
    MutableFudgeFieldContainer msg = context.objectToFudgeMsg(object.getSpecification());
    if (object.getRequiredJobIds() != null) {
      for (Long required : object.getRequiredJobIds()) {
        msg.add(REQUIRED_FIELD_NAME, required);
      }
    }
    for (CalculationJobItem item : object.getJobItems()) {
      context.objectToFudgeMsg(msg, ITEM_FIELD_NAME, null, item);
    }
    MutableFudgeFieldContainer cacheSelectHintMsg = context.objectToFudgeMsg(object.getCacheSelectHint());
    for (FudgeField fudgeField : cacheSelectHintMsg.getAllFields()) {
      msg.add(fudgeField);
    }
    return msg;
  }

  @Override
  public CalculationJob buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    CalculationJobSpecification jobSpec = context.fudgeMsgToObject(CalculationJobSpecification.class, message);
    FudgeFieldChecker.notNull(jobSpec, "Fudge message is not a CalculationJob - field 'calculationJobSpecification' is not present");
    
    Collection<FudgeField> fields = message.getAllByName(REQUIRED_FIELD_NAME);
    ArrayList<Long> requiredJobIds = null;
    if (!fields.isEmpty()) {
      requiredJobIds = new ArrayList<Long>(fields.size());
      for (FudgeField field : fields) {
        requiredJobIds.add(((Number) field.getValue()).longValue());
      }
    }
    fields = message.getAllByName(ITEM_FIELD_NAME);
    final ArrayList<CalculationJobItem> jobItems = new ArrayList<CalculationJobItem>(fields.size());
    for (FudgeField field : fields) {
      CalculationJobItem jobItem = context.fudgeMsgToObject(CalculationJobItem.class, (FudgeFieldContainer) field.getValue());
      jobItems.add(jobItem);
    }
    CacheSelectHint cacheSelectFilter = context.fudgeMsgToObject(CacheSelectHint.class, message);
    return new CalculationJob(jobSpec, requiredJobIds, jobItems, cacheSelectFilter);
  }

}
