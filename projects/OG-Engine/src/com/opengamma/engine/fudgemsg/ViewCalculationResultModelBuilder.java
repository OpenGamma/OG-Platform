/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    final Map<ComputationTargetSpecification, Map<String, ComputedValue>> mapNames = new HashMap<ComputationTargetSpecification, Map<String, ComputedValue>>();
    final Map<ComputationTargetSpecification, Set<ComputedValue>> mapAll = new HashMap<ComputationTargetSpecification, Set<ComputedValue>>();
    for (FudgeField field : message) {
      final ComputedValue value = context.fieldValueToObject(ComputedValue.class, field);
      final ComputationTargetSpecification target = value.getSpecification().getTargetSpecification();
      if (!mapNames.containsKey(target)) {
        mapNames.put(target, new HashMap<String, ComputedValue>());
        mapAll.put(target, new HashSet<ComputedValue>());
      }
      mapNames.get(target).put(value.getSpecification().getValueName(), value);
      mapAll.get(target).add(value);
    }
    return new ViewCalculationResultModel() {
      
      @Override
      public Collection<ComputationTargetSpecification> getAllTargets() {
        return mapNames.keySet();
      }

      @Override
      public Map<String, ComputedValue> getValues(ComputationTargetSpecification target) {
        return mapNames.get(target);
      }
      
      @Override
      public Set<ComputedValue> getAllValues(ComputationTargetSpecification target) {
        return mapAll.get(target);
      }

    };
  }
 
}
