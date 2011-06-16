/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
@GenericFudgeBuilderFor(ViewCalculationResultModel.class)
public class ViewCalculationResultModelBuilder implements FudgeBuilder<ViewCalculationResultModel> {
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, ViewCalculationResultModel resultModel) {
    final MutableFudgeMsg message = context.newMessage();
    final Collection<ComputationTargetSpecification> targets = resultModel.getAllTargets();
    for (ComputationTargetSpecification target : targets) {
      final Collection<ComputedValue> values = resultModel.getAllValues(target);
      for (ComputedValue value : values) {
        context.addToMessage(message, null, null, value);
      }
    }
    return message;
  }
  
  @Override
  public ViewCalculationResultModel buildObject(FudgeDeserializationContext context, FudgeMsg message) {
    final Map<ComputationTargetSpecification, Map<Pair<String, ValueProperties>, ComputedValue>> mapNames =
      new HashMap<ComputationTargetSpecification, Map<Pair<String, ValueProperties>, ComputedValue>>();
    for (FudgeField field : message) {
      final ComputedValue value = context.fieldValueToObject(ComputedValue.class, field);
      final ComputationTargetSpecification target = value.getSpecification().getTargetSpecification();
      if (!mapNames.containsKey(target)) {
        mapNames.put(target, new HashMap<Pair<String, ValueProperties>, ComputedValue>());
      }
      mapNames.get(target).put(Pair.of(value.getSpecification().getValueName(), value.getSpecification().getProperties()), value);
    }
    return new ViewCalculationResultModel() {
      
      @Override
      public Collection<ComputationTargetSpecification> getAllTargets() {
        return mapNames.keySet();
      }

      @Override
      public Map<Pair<String, ValueProperties>, ComputedValue> getValues(ComputationTargetSpecification target) {
        return mapNames.get(target);
      }
      
      @Override
      public Collection<ComputedValue> getAllValues(ComputationTargetSpecification target) {
        Map<Pair<String, ValueProperties>, ComputedValue> targetValuesMap = mapNames.get(target);
        return targetValuesMap != null ? Collections.unmodifiableCollection(targetValuesMap.values()) : null;
      }

    };
  }
 
}
