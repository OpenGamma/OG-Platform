/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.google.common.collect.Sets;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.InMemoryViewComputationResultModel;
import com.opengamma.engine.view.InMemoryViewResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;

/**
 */
@GenericFudgeBuilderFor(ViewComputationResultModel.class)
public class ViewComputationResultModelFudgeBuilder extends ViewResultModelFudgeBuilder implements FudgeBuilder<ViewComputationResultModel> {

  private static final String FIELD_LIVEDATA = "liveData";
  private static final String FIELD_SPECIFICATION_MAPPING = "specificationMapping";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ViewComputationResultModel resultModel) {
    final MutableFudgeMsg message = ViewResultModelFudgeBuilder.createResultModelMessage(serializer, resultModel);
    // Prevent subclass headers from being added to the message later, ensuring that this builder will be used for deserialization
    FudgeSerializer.addClassHeader(message, ViewComputationResultModel.class);
    final MutableFudgeMsg liveDataMsg = message.addSubMessage(FIELD_LIVEDATA, null);
    for (final ComputedValue value : resultModel.getAllMarketData()) {
      serializer.addToMessage(liveDataMsg, null, 1, value);
    }
    final MutableFudgeMsg mappingMsg = message.addSubMessage(FIELD_SPECIFICATION_MAPPING, null);
    for (final Map.Entry<ValueSpecification, Set<ValueRequirement>> specMappingEntry : resultModel.getRequirementToSpecificationMapping().entrySet()) {
      serializer.addToMessage(mappingMsg, null, 1, specMappingEntry.getKey());
      final MutableFudgeMsg requirements = mappingMsg.addSubMessage(null, 2);
      for (final ValueRequirement requirement : specMappingEntry.getValue()) {
        serializer.addToMessage(requirements, null, null, requirement);
      }
    }
    return message;
  }

  private static ValueSpecification getSpecification(final FudgeDeserializer deserializer, final FudgeField specification) {
    return deserializer.fieldValueToObject(ValueSpecification.class, specification);
  }

  private Set<ValueRequirement> getRequirements(final FudgeDeserializer deserializer, final FudgeField requirements) {
    final FudgeMsg msg = (FudgeMsg) requirements.getValue();
    final Set<ValueRequirement> result = Sets.newHashSetWithExpectedSize(msg.getNumFields());
    for (final FudgeField requirement : msg) {
      result.add(deserializer.fieldValueToObject(ValueRequirement.class, requirement));
    }
    return result;
  }

  @Override
  public ViewComputationResultModel buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final InMemoryViewComputationResultModel resultModel = (InMemoryViewComputationResultModel) bootstrapCommonDataFromMessage(deserializer, message);
    for (final FudgeField field : message.getFieldValue(FudgeMsg.class, message.getByName(FIELD_LIVEDATA))) {
      final ComputedValue liveData = deserializer.fieldValueToObject(ComputedValue.class, field);
      resultModel.addMarketData(liveData);
    }
    final FudgeMsg mappingMsg = message.getMessage(FIELD_SPECIFICATION_MAPPING);
    if (mappingMsg != null) {
      final LinkedList<FudgeField> buffer = new LinkedList<FudgeField>();
      FudgeField specification = null;
      FudgeField requirements = null;
      for (final FudgeField field : mappingMsg) {
        final Integer ord = field.getOrdinal();
        if (ord != null) {
          if (ord.intValue() == 1) {
            if (specification == null) {
              if (requirements != null) {
                resultModel.addRequirements(getRequirements(deserializer, requirements), getSpecification(deserializer, field));
                requirements = buffer.pollFirst();
              } else {
                specification = field;
              }
            } else {
              buffer.add(field);
            }
          } else if (ord.intValue() == 2) {
            if (requirements == null) {
              if (specification != null) {
                resultModel.addRequirements(getRequirements(deserializer, field), getSpecification(deserializer, specification));
                specification = buffer.pollFirst();
              } else {
                requirements = field;
              }
            } else {
              buffer.add(field);
            }
          }
        }
      }
      assert specification == null;
      assert requirements == null;
    }
    return resultModel;
  }

  @Override
  protected InMemoryViewResultModel constructImpl() {
    return new InMemoryViewComputationResultModel();
  }

}
