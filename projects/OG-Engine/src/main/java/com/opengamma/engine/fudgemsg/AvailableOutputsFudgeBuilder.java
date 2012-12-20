/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.view.helper.AvailableOutput;
import com.opengamma.engine.view.helper.AvailableOutputs;
import com.opengamma.engine.view.helper.AvailableOutputsImpl;

/**
 * Fudge object builder for {@link AvailableOutputs}.
 */
@GenericFudgeBuilderFor(AvailableOutputs.class)
public class AvailableOutputsFudgeBuilder implements FudgeBuilder<AvailableOutputs> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final AvailableOutputs outputs) {
    final MutableFudgeMsg msg = serializer.newMessage();
    MutableFudgeMsg submsg = serializer.newMessage();
    for (AvailableOutput output : outputs.getPortfolioNodeOutputs()) {
      serializer.addToMessage(submsg, output.getValueName(), null, output.getPortfolioNodeProperties());
    }
    msg.add(null, null, submsg);
    for (String securityType : outputs.getSecurityTypes()) {
      submsg = serializer.newMessage();
      for (AvailableOutput output : outputs.getPositionOutputs(securityType)) {
        serializer.addToMessage(submsg, output.getValueName(), null, output.getPositionProperties(securityType));
      }
      msg.add(securityType, null, submsg);
    }
    return msg;
  }

  @Override
  public AvailableOutputs buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final AvailableOutputsImpl object = new AvailableOutputsImpl();
    for (FudgeField typeField : message) {
      if (!(typeField.getValue() instanceof FudgeMsg)) {
        continue;
      }
      final FudgeMsg submsg = (FudgeMsg) typeField.getValue();
      if (typeField.getName() == null) {
        // Portfolio node values
        for (FudgeField valueField : submsg) {
          object.portfolioNodeOutput(valueField.getName(), deserializer.fieldValueToObject(ValueProperties.class, valueField));
        }
      } else {
        // Position values
        for (FudgeField valueField : submsg) {
          object.positionOutput(valueField.getName(), typeField.getName(), deserializer.fieldValueToObject(ValueProperties.class, valueField));
        }
      }
    }
    return object;
  }

}
