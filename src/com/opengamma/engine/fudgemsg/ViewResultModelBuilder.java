/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import javax.time.Instant;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewResultModel;

/**
 * Base operation for {@link ViewDeltaResultModelBuilder} and {@link ViewComputationResultModelBuilder}.
 */
public abstract class ViewResultModelBuilder {
  private static final String FIELD_VALUATIONTS = "valuationTS";
  private static final String FIELD_RESULTTS = "resultTS";
  private static final String FIELD_RESULTS = "results";

  protected static MutableFudgeFieldContainer createResultModelMessage(final FudgeSerializationContext context, final ViewResultModel resultModel) {
    final MutableFudgeFieldContainer message = context.newMessage();
    message.add(FIELD_VALUATIONTS, resultModel.getValuationTime());
    message.add(FIELD_RESULTTS, resultModel.getResultTimestamp());
    final Collection<String> calculationConfigurations = resultModel.getCalculationConfigurationNames();
    final MutableFudgeFieldContainer resultMsg = context.newMessage();
    for (String calculationConfiguration : calculationConfigurations) {
      resultMsg.add(null, 1, calculationConfiguration);
      context.objectToFudgeMsg(resultMsg, null, 2, resultModel.getCalculationResult(calculationConfiguration));
    }
    message.add(FIELD_RESULTS, resultMsg);
    return message;
  }
  
  protected ViewResultModel bootstrapCommonDataFromMessage(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    final Instant inputDataTimestamp = message.getFieldValue(Instant.class, message.getByName(FIELD_VALUATIONTS));
    final Instant resultTimestamp = message.getFieldValue(Instant.class, message.getByName(FIELD_RESULTTS));
    final Map<String, ViewCalculationResultModel> map = new HashMap<String, ViewCalculationResultModel>();
    final Queue<String> keys = new LinkedList<String>();
    final Queue<ViewCalculationResultModel> values = new LinkedList<ViewCalculationResultModel>();
    for (FudgeField field : message.getFieldValue(FudgeFieldContainer.class, message.getByName(FIELD_RESULTS))) {
      if (field.getOrdinal() == 1) {
        final String key = context.fieldValueToObject(String.class, field);
        if (values.isEmpty()) {
          keys.add(key);
        } else {
          map.put(key, values.remove());
        }
      } else if (field.getOrdinal() == 2) {
        final ViewCalculationResultModel value = context.fieldValueToObject(ViewCalculationResultModel.class, field);
        if (keys.isEmpty()) {
          values.add(value);
        } else {
          map.put(keys.remove(), value);
        }
      }
    }
    
    return constructImpl(context, message, inputDataTimestamp, resultTimestamp, map);
  }

  protected abstract ViewResultModel constructImpl(
      FudgeDeserializationContext context, FudgeFieldContainer message,
      Instant inputDataTimestamp, Instant resultTimestamp,
      Map<String, ViewCalculationResultModel> map);
  
}
