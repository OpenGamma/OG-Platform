/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.time.Instant;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.ViewTargetResultModel;

/**
 * 
 */
@GenericFudgeBuilderFor(ViewDeltaResultModel.class)
public class ViewDeltaResultModelBuilder extends ViewResultModelBuilder implements FudgeBuilder<ViewDeltaResultModel> {

  private static final String FIELD_PREVIOUSTS = "previousTS";

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, ViewDeltaResultModel deltaModel) {
    final MutableFudgeFieldContainer message = ViewResultModelBuilder.createResultModelMessage(context, deltaModel);
    message.add(FIELD_PREVIOUSTS, deltaModel.getPreviousResultTimestamp());
    return message;
  }

  @Override
  public ViewDeltaResultModel buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    return (ViewDeltaResultModel) bootstrapCommonDataFromMessage(context, message);
  }

  @Override
  protected ViewResultModel constructImpl(final FudgeDeserializationContext context, final FudgeFieldContainer message, final Instant inputDataTimestamp, final Instant resultTimestamp,
      final Map<String, ViewCalculationResultModel> configurationMap, final Map<ComputationTargetSpecification, ViewTargetResultModel> targetMap, final String viewName) {
    final Instant parentResultTimestamp = message.getFieldValue(Instant.class, message.getByName(FIELD_PREVIOUSTS));
    return new ViewDeltaResultModel() {

      @Override
      public Instant getPreviousResultTimestamp() {
        return parentResultTimestamp;
      }

      @Override
      public Collection<ComputationTargetSpecification> getAllTargets() {
        return Collections.unmodifiableSet(targetMap.keySet());
      }

      @Override
      public Collection<String> getCalculationConfigurationNames() {
        return Collections.unmodifiableSet(configurationMap.keySet());
      }

      @Override
      public ViewCalculationResultModel getCalculationResult(String calcConfigurationName) {
        return configurationMap.get(calcConfigurationName);
      }

      @Override
      public ViewTargetResultModel getTargetResult(final ComputationTargetSpecification targetSpec) {
        return targetMap.get(targetSpec);
      }

      @Override
      public Instant getValuationTime() {
        return inputDataTimestamp;
      }

      @Override
      public Instant getResultTimestamp() {
        return resultTimestamp;
      }

      @Override
      public String getViewName() {
        return viewName;
      }
      
    };
  }
}
