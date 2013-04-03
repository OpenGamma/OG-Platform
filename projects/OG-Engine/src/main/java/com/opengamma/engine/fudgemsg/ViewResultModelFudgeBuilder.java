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

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.impl.InMemoryViewResultModel;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * Base operation for {@link ViewDeltaResultModelFudgeBuilder} and {@link ViewComputationResultModelFudgeBuilder}.
 */
public abstract class ViewResultModelFudgeBuilder {
  private static final String FIELD_VIEWPROCESSID = "viewProcessId";
  private static final String FIELD_VIEWCYCLEID = "viewCycleId";
  private static final String FIELD_VIEW_CYCLE_EXECUTION_OPTIONS = "viewCycleExecutionOptions";
  private static final String FIELD_CALCULATION_TIME = "calculationTime";
  private static final String FIELD_CALCULATION_DURATION = "calculationDuration";
  private static final String FIELD_VERSION_CORRECTION = "versionCorrection";
  private static final String FIELD_RESULTS = "results";

  protected static MutableFudgeMsg createResultModelMessage(final FudgeSerializer serializer, final ViewResultModel resultModel) {
    final MutableFudgeMsg message = serializer.newMessage();
    message.add(FIELD_VIEWPROCESSID, resultModel.getViewProcessId());
    message.add(FIELD_VIEWCYCLEID, resultModel.getViewCycleId());
    message.add(FIELD_CALCULATION_TIME, resultModel.getCalculationTime());
    serializer.addToMessage(message, FIELD_VIEW_CYCLE_EXECUTION_OPTIONS, null, resultModel.getViewCycleExecutionOptions());
    serializer.addToMessage(message, FIELD_CALCULATION_DURATION, null, resultModel.getCalculationDuration());
    serializer.addToMessage(message, FIELD_VERSION_CORRECTION, null, resultModel.getVersionCorrection());
    final Collection<String> calculationConfigurations = resultModel.getCalculationConfigurationNames();
    final MutableFudgeMsg resultMsg = serializer.newMessage();
    for (String calculationConfiguration : calculationConfigurations) {
      resultMsg.add(null, 1, calculationConfiguration);
      serializer.addToMessage(resultMsg, null, 2, resultModel.getCalculationResult(calculationConfiguration));
    }
    message.add(FIELD_RESULTS, resultMsg);
    return message;
  }

  protected InMemoryViewResultModel bootstrapCommonDataFromMessage(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final UniqueId viewProcessId = message.getValue(UniqueId.class, FIELD_VIEWPROCESSID);
    final UniqueId viewCycleId = message.getValue(UniqueId.class, FIELD_VIEWCYCLEID);
    final ViewCycleExecutionOptions viewCycleExecutionOptions = deserializer.fieldValueToObject(ViewCycleExecutionOptions.class, message.getByName(FIELD_VIEW_CYCLE_EXECUTION_OPTIONS));
    final Instant calculationTime = message.getFieldValue(Instant.class, message.getByName(FIELD_CALCULATION_TIME));
    FudgeField durationField = message.getByName(FIELD_CALCULATION_DURATION);
    final Duration calculationDuration = durationField != null ? deserializer.fieldValueToObject(Duration.class, durationField) : null;
    final VersionCorrection versionCorrection = deserializer.fieldValueToObject(VersionCorrection.class, message.getByName(FIELD_VERSION_CORRECTION));
    final Map<String, ViewCalculationResultModel> configurationMap = new HashMap<String, ViewCalculationResultModel>();
    final Queue<String> keys = new LinkedList<String>();
    final Queue<ViewCalculationResultModel> values = new LinkedList<ViewCalculationResultModel>();
    for (FudgeField field : message.getFieldValue(FudgeMsg.class, message.getByName(FIELD_RESULTS))) {
      if (field.getOrdinal() == 1) {
        final String key = deserializer.fieldValueToObject(String.class, field);
        if (values.isEmpty()) {
          keys.add(key);
        } else {
          configurationMap.put(key, values.remove());
        }
      } else if (field.getOrdinal() == 2) {
        final ViewCalculationResultModel value = deserializer.fieldValueToObject(ViewCalculationResultModel.class, field);
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
        for (ComputedValueResult value : configurationEntry.getValue().getAllValues(targetSpec)) {
          resultModel.addValue(configurationEntry.getKey(), value);
        }
      }
    }
    
    resultModel.setViewProcessId(viewProcessId);
    resultModel.setViewCycleId(viewCycleId);
    resultModel.setViewCycleExecutionOptions(viewCycleExecutionOptions);
    resultModel.setCalculationTime(calculationTime);
    resultModel.setCalculationDuration(calculationDuration);
    resultModel.setVersionCorrection(versionCorrection);
    
    return resultModel;
  }
  
  protected abstract InMemoryViewResultModel constructImpl();

}
