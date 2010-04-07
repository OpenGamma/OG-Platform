/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.server;

import java.util.Collection;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.view.ViewCalculationResultModel;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;

public class ViewDeltaResultModelBuilder implements FudgeBuilder<ViewDeltaResultModel> {
  
  private static final String FIELD_PREVIOUSTS = "previousTS";
  
  @Override
  public MutableFudgeFieldContainer buildMessage (FudgeSerializationContext context, ViewDeltaResultModel deltaModel) {
    final MutableFudgeFieldContainer message = ViewComputationResultModelBuilder.createMessage (context, deltaModel);
    message.add (FIELD_PREVIOUSTS, deltaModel.getPreviousResultTimestamp ());
    return message;
  }
  
  @Override
  public ViewDeltaResultModel buildObject (FudgeDeserializationContext context, FudgeFieldContainer message) {
    final ViewComputationResultModel parent = ViewComputationResultModelBuilder.createObject (context, message);
    final long parentResultTimestamp = message.getFieldValue (Long.class, message.getByName (FIELD_PREVIOUSTS));
    return new ViewDeltaResultModel () {

      @Override
      public long getPreviousResultTimestamp() {
        return parentResultTimestamp;
      }

      @Override
      public Collection<ComputationTargetSpecification> getAllTargets() {
        return parent.getAllTargets ();
      }

      @Override
      public Collection<String> getCalculationConfigurationNames() {
        return parent.getCalculationConfigurationNames ();
      }

      @Override
      public ViewCalculationResultModel getCalculationResult(
          String calcConfigurationName) {
        return parent.getCalculationResult (calcConfigurationName);
      }

      @Override
      public long getInputDataTimestamp() {
        return parent.getInputDataTimestamp ();
      }

      @Override
      public long getResultTimestamp() {
        return parent.getResultTimestamp ();
      }
    };
  }
 
}