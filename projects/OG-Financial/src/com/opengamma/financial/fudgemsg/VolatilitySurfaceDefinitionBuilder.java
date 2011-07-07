/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceDefinition;
import com.opengamma.util.money.Currency;

/**
 * Builder for converting VolatilitySurfaceDefinition instances to/from Fudge messages.
 */
@FudgeBuilderFor(VolatilitySurfaceDefinition.class)
public class VolatilitySurfaceDefinitionBuilder implements FudgeBuilder<VolatilitySurfaceDefinition<?, ?>> {
  private static final Logger s_logger = LoggerFactory.getLogger(VolatilitySurfaceDefinitionBuilder.class);
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, VolatilitySurfaceDefinition<?, ?> object) {
    MutableFudgeMsg message = context.newMessage();
    context.addToMessage(message, "currency", null, object.getCurrency());
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
  public VolatilitySurfaceDefinition<?, ?> buildObject(FudgeDeserializationContext context, FudgeMsg message) {
    Currency currency = context.fieldValueToObject(Currency.class, message.getByName("currency"));
    String name = message.getString("name");
    String interpolatorName;
    if (!message.hasField("interpolatorName")) {
      interpolatorName = "Linear"; 
      s_logger.warn("Inserting Linear interpolation as future version doesn't require an interpolator");
    } else {
      interpolatorName = message.getString("interpolatorName");
    }
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
