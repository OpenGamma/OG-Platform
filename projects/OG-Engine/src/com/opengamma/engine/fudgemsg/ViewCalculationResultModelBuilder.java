/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.view.ViewCalculationResultModel;

/**
 * 
 */
@GenericFudgeBuilderFor(ViewCalculationResultModel.class)
public class ViewCalculationResultModelBuilder implements FudgeBuilder<ViewCalculationResultModel> {
  
  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, ViewCalculationResultModel resultModel) {
    final MutableFudgeFieldContainer message = context.newMessage();
    final Collection<ComputationTargetSpecification> targets = resultModel.getAllTargets();
    for (ComputationTargetSpecification target : targets) {
      final Map<String, ComputedValue> values = resultModel.getValues(target);
      for (Map.Entry<String, ComputedValue> value : values.entrySet()) {
        context.objectToFudgeMsg(message, null, null, value.getValue());
      }
    }
    return message;
  }
  
  @Override
  public ViewCalculationResultModel buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    final Map<ComputationTargetSpecification, Map<String, ComputedValue>> map = new HashMap<ComputationTargetSpecification, Map<String, ComputedValue>>();
    for (FudgeField field : message) {
      final ComputedValue value = context.fieldValueToObject(ComputedValue.class, field);
      final ComputationTargetSpecification target = value.getSpecification().getTargetSpecification();
      if (!map.containsKey(target)) {
        map.put(target, new HashMap<String, ComputedValue>());
      }
      map.get(target).put(value.getSpecification().getValueName(), value);
    }
    return new ViewCalculationResultModel() {
      
      @Override
      public Collection<ComputationTargetSpecification> getAllTargets() {
        return map.keySet();
      }

      @Override
      public Map<String, ComputedValue> getValues(ComputationTargetSpecification target) {
        return map.get(target);
      }
      
    };
  }
 
}
