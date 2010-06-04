/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.server;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.time.Instant;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;

/**
 */
public class ViewComputationResultModelBuilder implements FudgeBuilder<ViewComputationResultModel> {
  
  private static final String FIELD_INPUTTS = "inputTS";
  private static final String FIELD_RESULTTS = "resultTS";
  private static final String FIELD_RESULTS = "results";
  
  protected static MutableFudgeFieldContainer createMessage(final FudgeSerializationContext context, final ViewComputationResultModel resultModel) {
    final MutableFudgeFieldContainer message = context.newMessage();
    message.add(FIELD_INPUTTS, resultModel.getInputDataTimestamp());
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
  
  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, ViewComputationResultModel resultModel) {
    return createMessage(context, resultModel);
  }
  
  protected static ViewComputationResultModel createObject(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    final Instant inputDataTimestamp = message.getFieldValue(Instant.class, message.getByName(FIELD_INPUTTS));
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
    return new ViewComputationResultModel() {
      @Override
      public Collection<ComputationTargetSpecification> getAllTargets() {
        Set<ComputationTargetSpecification> allTargetSpecs = new HashSet<ComputationTargetSpecification>();
        for (ViewCalculationResultModel calcResultModel : map.values()) {
          allTargetSpecs.addAll(calcResultModel.getAllTargets());
        }
        return allTargetSpecs;
      }
      @Override
      public Collection<String> getCalculationConfigurationNames() {
        return map.keySet();
      }
      @Override
      public ViewCalculationResultModel getCalculationResult(String calcConfigurationName) {
        return map.get(calcConfigurationName);
      }
      @Override
      public Instant getInputDataTimestamp() {
        return inputDataTimestamp;
      }
      @Override
      public Instant getResultTimestamp() {
        return resultTimestamp;
      }
      @Override
      public String toString() {
        return "ViewComputationResultModel, input timestamp=" + getInputDataTimestamp() + ", result timestamp=" + getResultTimestamp();
      }
    };
  }
  
  @Override
  public ViewComputationResultModel buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    return createObject(context, message);
  }
 
}
