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

import javax.time.Duration;
import javax.time.Instant;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
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
  private static final String FIELD_VIEWCYCLEID = "viewCycleId";
  private static final String FIELD_VALUATION_TIME = "valuationTime";
  private static final String FIELD_CALCULATION_TIME = "calculationTime";
  private static final String FIELD_CALCULATION_DURATION = "calculationDuration";
  private static final String FIELD_RESULTS = "results";

  protected static MutableFudgeMsg createResultModelMessage(final FudgeSerializationContext context, final ViewResultModel resultModel) {
    final MutableFudgeMsg message = context.newMessage();
    message.add(FIELD_VIEWPROCESSID, resultModel.getViewProcessId());
    message.add(FIELD_VIEWCYCLEID, resultModel.getViewCycleId());
    message.add(FIELD_VALUATION_TIME, resultModel.getValuationTime());
    message.add(FIELD_CALCULATION_TIME, resultModel.getCalculationTime());
    message.add(FIELD_CALCULATION_DURATION, resultModel.getCalculationDuration());
    final Collection<String> calculationConfigurations = resultModel.getCalculationConfigurationNames();
    final MutableFudgeMsg resultMsg = context.newMessage();
    for (String calculationConfiguration : calculationConfigurations) {
      resultMsg.add(null, 1, calculationConfiguration);
      context.addToMessage(resultMsg, null, 2, resultModel.getCalculationResult(calculationConfiguration));
    }
    message.add(FIELD_RESULTS, resultMsg);
    return message;
  }

  protected InMemoryViewResultModel bootstrapCommonDataFromMessage(final FudgeDeserializationContext context, final FudgeMsg message) {
    final UniqueIdentifier viewProcessId = message.getValue(UniqueIdentifier.class, FIELD_VIEWPROCESSID);
    final UniqueIdentifier viewCycleId = message.getValue(UniqueIdentifier.class, FIELD_VIEWCYCLEID);
    final Instant valuationTime = message.getFieldValue(Instant.class, message.getByName(FIELD_VALUATION_TIME));
    final Instant calculationTime = message.getFieldValue(Instant.class, message.getByName(FIELD_CALCULATION_TIME));
    final Duration calculationDuration = message.getFieldValue(Duration.class, message.getByName(FIELD_CALCULATION_DURATION));
    final Map<String, ViewCalculationResultModel> configurationMap = new HashMap<String, ViewCalculationResultModel>();
    final Queue<String> keys = new LinkedList<String>();
    final Queue<ViewCalculationResultModel> values = new LinkedList<ViewCalculationResultModel>();
    for (FudgeField field : message.getFieldValue(FudgeMsg.class, message.getByName(FIELD_RESULTS))) {
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
        for (ComputedValue value : configurationEntry.getValue().getAllValues(targetSpec)) {
          resultModel.addValue(configurationEntry.getKey(), value);
        }
      }
    }
    
    resultModel.setViewProcessId(viewProcessId);
    resultModel.setViewCycleId(viewCycleId);
    resultModel.setValuationTime(valuationTime);
    resultModel.setCalculationTime(calculationTime);
    resultModel.setCalculationDuration(calculationDuration);
    
    return resultModel;
  }
  
  protected abstract InMemoryViewResultModel constructImpl();

}
