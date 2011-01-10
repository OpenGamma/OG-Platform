/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.core.common.Currency;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceDefinition;

/**
 * Builder for converting VolatilitySurfaceDefinition instances to/from Fudge messages.
 */
@FudgeBuilderFor(VolatilitySurfaceDefinition.class)
public class VolatilitySurfaceDefinitionBuilder implements FudgeBuilder<VolatilitySurfaceDefinition<?, ?>> {

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, VolatilitySurfaceDefinition<?, ?> object) {
    MutableFudgeFieldContainer message = context.newMessage();
    context.objectToFudgeMsg(message, "currency", null, object.getCurrency());
    message.add("name", object.getName());
    message.add("interpolatorName", object.getInterpolatorName());
    for (Object x : object.getXs()) {
      message.add("xs", null, FudgeSerializationContext.addClassHeader(context.objectToFudgeMsg(x), x.getClass()));
    }
    for (Object y : object.getYs()) {
      message.add("ys", null, FudgeSerializationContext.addClassHeader(context.objectToFudgeMsg(y), y.getClass()));
    }    
    return message; 
  }

  @Override
  public VolatilitySurfaceDefinition<?, ?> buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    Currency currency = context.fieldValueToObject(Currency.class, message.getByName("currency"));
    String name = message.getString("name");
    String interpolatorName = message.getString("interpolatorName");
    List<FudgeField> xsFields = message.getAllByName("xs");
    List<Object> xs = new ArrayList<Object>();
    for (FudgeField xField : xsFields) {
      Object x = context.fieldValueToObject(xField);
      xs.add(x);
    }
    List<FudgeField> ysFields = message.getAllByName("ys");
    List<Object> ys = new ArrayList<Object>();
    for (FudgeField yField : ysFields) {
      Object y = context.fieldValueToObject(yField);
      ys.add(y);
    }    
    return new VolatilitySurfaceDefinition<Object, Object>(name, currency, interpolatorName, xs.toArray(), ys.toArray());
  }

}
