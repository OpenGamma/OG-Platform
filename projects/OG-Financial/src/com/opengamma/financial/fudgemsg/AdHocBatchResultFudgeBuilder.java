/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.financial.batch.AdHocBatchResult;
import com.opengamma.financial.batch.BatchId;

/**
 * Fudge message builder for {@code AdHocBatchResult}.
 */
@FudgeBuilderFor(AdHocBatchResult.class)
public class AdHocBatchResultFudgeBuilder implements FudgeBuilder<AdHocBatchResult> {
  /**
   * Fudge field name.
   */
  private static final String BATCHID_KEY = "batchId";
  /**
   * Fudge field name.
   */
  private static final String RESULT_KEY = "result";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, AdHocBatchResult object) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, BATCHID_KEY, null, object.getBatchId());
    serializer.addToMessage(msg, RESULT_KEY, null, object.getResult());
    return msg;
  }

  @Override
  public AdHocBatchResult buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    FudgeField batchIdField = message.getByName(BATCHID_KEY);
    FudgeField resultField = message.getByName(RESULT_KEY);

    Validate.notNull(batchIdField, "Fudge message is not a AdHocBatchResult - field " + BATCHID_KEY + " is not present");
    Validate.notNull(resultField, "Fudge message is not a AdHocBatchResult - field " + RESULT_KEY + " is not present");

    BatchId batchId = deserializer.fieldValueToObject(BatchId.class, batchIdField);
    ViewComputationResultModel result = deserializer.fieldValueToObject(ViewComputationResultModel.class, resultField);
    
    return new AdHocBatchResult(batchId, result);
  }

}
