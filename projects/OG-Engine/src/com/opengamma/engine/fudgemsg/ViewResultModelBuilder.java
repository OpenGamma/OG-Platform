/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewResultEntry;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.ViewTargetResultModel;

/**
 * Base operation for {@link ViewDeltaResultModelBuilder} and {@link ViewComputationResultModelBuilder}.
 */
public abstract class ViewResultModelBuilder {
  private static final String FIELD_VIEWNAME = "viewName";
  private static final String FIELD_VALUATIONTS = "valuationTS";
  private static final String FIELD_RESULTTS = "resultTS";
  private static final String FIELD_RESULTS = "results";

  protected static MutableFudgeFieldContainer createResultModelMessage(final FudgeSerializationContext context, final ViewResultModel resultModel) {
    final MutableFudgeFieldContainer message = context.newMessage();
    message.add(FIELD_VIEWNAME, resultModel.getViewName());
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

  private static final class ViewTargetResultModelImpl extends com.opengamma.engine.view.ViewTargetResultModelImpl {

    private void putAll(final String configuration, final Collection<ComputedValue> values) {
      for (ComputedValue value : values) {
        addValue(configuration, value);
      }
    }

  }

  @SuppressWarnings("unchecked")
  protected ViewResultModel bootstrapCommonDataFromMessage(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    final String viewName = message.getString(FIELD_VIEWNAME);
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
    final Map<ComputationTargetSpecification, ViewTargetResultModelImpl> targetMap = new HashMap<ComputationTargetSpecification, ViewTargetResultModelImpl>();
    for (Map.Entry<String, ViewCalculationResultModel> configurationEntry : configurationMap.entrySet()) {
      for (ComputationTargetSpecification targetSpec : configurationEntry.getValue().getAllTargets()) {
        ViewTargetResultModelImpl targetResult = targetMap.get(targetSpec);
        if (targetResult == null) {
          targetResult = new ViewTargetResultModelImpl();
          targetMap.put(targetSpec, targetResult);
        }
        targetResult.putAll(configurationEntry.getKey(), configurationEntry.getValue().getValues(targetSpec).values());
      }
    }
    
    final List<ViewResultEntry> allResults = new ArrayList<ViewResultEntry>();
    for (Map.Entry<String, ViewCalculationResultModel> configurationEntry : configurationMap.entrySet()) {
      for (ComputationTargetSpecification targetSpec : configurationEntry.getValue().getAllTargets()) {
        Map<String, ComputedValue> results = configurationEntry.getValue().getValues(targetSpec);
        for (ComputedValue value : results.values()) {
          allResults.add(new ViewResultEntry(configurationEntry.getKey(), value));
        }
      }
    }
    
    return constructImpl(context, message, inputDataTimestamp, resultTimestamp, configurationMap,
        (Map<ComputationTargetSpecification, ViewTargetResultModel>) (Map<ComputationTargetSpecification, ?>) targetMap, viewName, allResults);
  }

  protected abstract ViewResultModel constructImpl(FudgeDeserializationContext context, 
      FudgeFieldContainer message, 
      Instant inputDataTimestamp, 
      Instant resultTimestamp,
      Map<String, ViewCalculationResultModel> configurationMap, 
      Map<ComputationTargetSpecification, ViewTargetResultModel> targetMap, 
      String viewName,
      List<ViewResultEntry> allResults);

}
