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
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * 
 */
@GenericFudgeBuilderFor(ViewCalculationResultModel.class)
public class ViewCalculationResultModelFudgeBuilder implements FudgeBuilder<ViewCalculationResultModel> {
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ViewCalculationResultModel resultModel) {
    final MutableFudgeMsg message = serializer.newMessage();
    final Collection<ComputationTargetSpecification> targets = resultModel.getAllTargets();
    for (ComputationTargetSpecification target : targets) {
      final Collection<ComputedValueResult> values = resultModel.getAllValues(target);
      for (ComputedValueResult value : values) {
        serializer.addToMessage(message, null, null, value);
      }
    }
    return message;
  }
  
  @Override
  public ViewCalculationResultModel buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    final Map<ComputationTargetSpecification, Map<Pair<String, ValueProperties>, ComputedValueResult>> mapNames =
      new HashMap<ComputationTargetSpecification, Map<Pair<String, ValueProperties>, ComputedValueResult>>();
    for (FudgeField field : message) {
      final ComputedValueResult value = deserializer.fieldValueToObject(ComputedValueResult.class, field);
      final ComputationTargetSpecification target = value.getSpecification().getTargetSpecification();
      if (!mapNames.containsKey(target)) {
        mapNames.put(target, new HashMap<Pair<String, ValueProperties>, ComputedValueResult>());
      }
      mapNames.get(target).put(Pairs.of(value.getSpecification().getValueName(), value.getSpecification().getProperties()), value);
    }
    return new ViewCalculationResultModel() {
      
      @Override
      public Collection<ComputationTargetSpecification> getAllTargets() {
        return mapNames.keySet();
      }

      @Override
      public Map<Pair<String, ValueProperties>, ComputedValueResult> getValues(ComputationTargetSpecification target) {
        return mapNames.get(target);
      }
      
      @Override
      public Collection<ComputedValueResult> getAllValues(ComputationTargetSpecification target) {
        Map<Pair<String, ValueProperties>, ComputedValueResult> targetValuesMap = mapNames.get(target);
        return targetValuesMap != null ? Collections.unmodifiableCollection(targetValuesMap.values()) : null;
      }

    };
  }
 
}
