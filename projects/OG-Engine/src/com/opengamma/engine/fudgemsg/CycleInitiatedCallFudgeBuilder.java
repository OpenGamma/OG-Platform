/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.listener.CycleCompletedCall;
import com.opengamma.engine.view.listener.CycleInitiatedCall;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import java.util.Map;
import java.util.Set;

/**
 * Fudge message builder for {@link com.opengamma.engine.view.listener.CycleInitiatedCall}
 */
@FudgeBuilderFor(CycleInitiatedCall.class)
public class CycleInitiatedCallFudgeBuilder implements FudgeBuilder<CycleInitiatedCall> {

  private static final String VIEW_CYCLE_EXECUTION_OPTIONS_FIELD = "viewCycleExecutionOptions";
  private static final String SPECIFICATION_TO_REQUIREMENT_MAPPING_FIELD = "specificationToRequirementMapping";

  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CycleInitiatedCall object) {
    MutableFudgeMsg msg = serializer.newMessage();
    ViewCycleExecutionOptions viewCycleExecutionOptions = object.getViewCycleExecutionOptions();

    serializer.addToMessage(msg, VIEW_CYCLE_EXECUTION_OPTIONS_FIELD, null, viewCycleExecutionOptions);

    Map<String, Map<ValueSpecification, Set<ValueRequirement>>> specificationToRequirementMapping = object.getSpecificationToRequirementMapping();

    serializer.addToMessage(msg, SPECIFICATION_TO_REQUIREMENT_MAPPING_FIELD, null, specificationToRequirementMapping);
    return msg;
  }

  @Override
  public CycleInitiatedCall buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    FudgeField viewCycleExecutionOptionsField = msg.getByName(VIEW_CYCLE_EXECUTION_OPTIONS_FIELD);
    ViewCycleExecutionOptions viewCycleExecutionOptions = viewCycleExecutionOptionsField != null ? deserializer.fieldValueToObject(ViewCycleExecutionOptions.class, viewCycleExecutionOptionsField) : null;

    FudgeField specificationToRequirementMappingField = msg.getByName(SPECIFICATION_TO_REQUIREMENT_MAPPING_FIELD);
    Map<String, Map<ValueSpecification, Set<ValueRequirement>>> specificationToRequirementMapping = specificationToRequirementMappingField != null ? deserializer.fieldValueToObject(Map.class, specificationToRequirementMappingField) : null;
    return new CycleInitiatedCall(viewCycleExecutionOptions, specificationToRequirementMapping);
  }

}
