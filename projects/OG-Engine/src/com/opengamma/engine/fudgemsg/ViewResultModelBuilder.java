/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.view.InMemoryViewResultModel;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.id.UniqueIdentifier;

/**
 * Base operation for {@link ViewDeltaResultModelBuilder} and {@link ViewComputationResultModelBuilder}.
 */
public abstract class ViewResultModelBuilder {
  private static final String FIELD_VIEWPROCESSID = "viewProcessId";
  private static final String FIELD_VALUATIONTS = "valuationTS";
  private static final String FIELD_RESULTTS = "resultTS";
  private static final String FIELD_RESULTS = "results";

  protected static MutableFudgeFieldContainer createResultModelMessage(final FudgeSerializationContext context, final ViewResultModel resultModel) {
    final MutableFudgeFieldContainer message = context.newMessage();
    message.add(FIELD_VIEWPROCESSID, resultModel.getViewProcessId());
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

  protected InMemoryViewResultModel bootstrapCommonDataFromMessage(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    final UniqueIdentifier viewProcessId = message.getValue(UniqueIdentifier.class, FIELD_VIEWPROCESSID);
    final Instant inputDataTimestamp = message.getFieldValue(Instant.class, message.getByName(FIELD_VALUATIONTS));
    final Instant resultTimestamp = message.getFieldValue(Instant.class, message.getByName(FIELD_RESULTTS));
    final Map<String, ViewCalculationResultModel> configurationMap = new HashMap<String, ViewCalculationResultModel>();
    final Queue<String> keys = new LinkedList<String>();
    final Queue<ViewCalculationResultModel> values = new LinkedList<ViewCalculationResultModel>();
    for (FudgeField field : message.getFieldValue(FudgeFieldContainer.class, message.getByName(FIELD_RESULTS))) {
      if (field.getOrdinal() == 1) {
        final String key = context.fieldValueToObject(String.class, field);
        if (values.isEmpty()) {
          keys.add(key);
        } else {
          configurationMap.put(key, values.remove());
        }
      } else if (field.getOrdinal() == 2) {
        final ViewCalculationResultModel value = context.fieldValueToObject(ViewCalculationResultModel.class, field);
        if (keys.isEmpty()) {
          values.add(value);
        } else {
          configurationMap.put(keys.remove(), value);
        }
      }
    }
    
    InMemoryViewResultModel resultModel = constructImpl();
    for (Map.Entry<String, ViewCalculationResultModel> configurationEntry : configurationMap.entrySet()) {
      for (ComputationTargetSpecification targetSpec : configurationEntry.getValue().getAllTargets()) {
        for (ComputedValue value : configurationEntry.getValue().getValues(targetSpec).values()) {
          resultModel.addValue(configurationEntry.getKey(), value);
        }
      }
    }
    
    resultModel.setViewProcessId(viewProcessId);
    resultModel.setValuationTime(inputDataTimestamp);
    resultModel.setResultTimestamp(resultTimestamp);
    
    return resultModel;
  }
  
  protected abstract InMemoryViewResultModel constructImpl();

}
