/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.apache.commons.lang.Validate;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.financial.batch.AdHocBatchResult;
import com.opengamma.financial.batch.BatchId;

/**
 * Fudge message builder for {@code AdHocBatchResult}.
 */
@FudgeBuilderFor(AdHocBatchResult.class)
public class AdHocBatchResultBuilder implements FudgeBuilder<AdHocBatchResult> {
  /**
   * Fudge field name.
   */
  private static final String BATCHID_KEY = "batchId";
  /**
   * Fudge field name.
   */
  private static final String RESULT_KEY = "result";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, AdHocBatchResult object) {
    MutableFudgeMsg msg = context.newMessage();
    context.objectToFudgeMsg(msg, BATCHID_KEY, null, object.getBatchId());
    context.objectToFudgeMsg(msg, RESULT_KEY, null, object.getResult());
    return msg;
  }

  @Override
  public AdHocBatchResult buildObject(FudgeDeserializationContext context, FudgeMsg message) {
    FudgeField batchIdField = message.getByName(BATCHID_KEY);
    FudgeField resultField = message.getByName(RESULT_KEY);

    Validate.notNull(batchIdField, "Fudge message is not a AdHocBatchResult - field " + BATCHID_KEY + " is not present");
    Validate.notNull(resultField, "Fudge message is not a AdHocBatchResult - field " + RESULT_KEY + " is not present");

    BatchId batchId = context.fieldValueToObject(BatchId.class, batchIdField);
    ViewComputationResultModel result = context.fieldValueToObject(ViewComputationResultModel.class, resultField);
    
    return new AdHocBatchResult(batchId, result);
  }

}
