/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceData;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceDefinition;

/**
 * Builder for converting VolatilitySurfaceDefinition instances to/from Fudge messages.
 */
@FudgeBuilderFor(VolatilitySurfaceData.class)
public class VolatilitySurfaceDataBuilder implements FudgeBuilder<VolatilitySurfaceData<?, ?>> {

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, VolatilitySurfaceData<?, ?> object) {
    MutableFudgeFieldContainer message = context.newMessage();
    context.objectToFudgeMsg(message, "currency", null, object.getCurrency());
    message.add("definitionName", object.getDefinitionName());
    message.add("specificationName", object.getSpecificationName());
    message.add("interpolatorName", object.getInterpolatorName());
    for (Object x : object.getXs()) {
      message.add("xs", null, FudgeSerializationContext.addClassHeader(context.objectToFudgeMsg(x), x.getClass ()));
    }
    for (Object y : object.getYs()) {
      message.add("ys", null, FudgeSerializationContext.addClassHeader(context.objectToFudgeMsg(y), y.getClass ()));
    }    
    
    return message; 
  }

  @Override
  public VolatilitySurfaceData<?, ?> buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    Currency currency = context.fieldValueToObject(Currency.class, message.getByName("currency"));
    String name = message.getString("name");
    String interpolatorName = message.getString("interpolatorName");
    List<FudgeField> xs_fields = message.getAllByName("xs");
    List<Object> xs = new ArrayList<Object>();
    for (FudgeField x_field : xs_fields) {
      Object x = context.fieldValueToObject(x_field);
      xs.add(x);
    }
    List<FudgeField> ys_fields = message.getAllByName("ys");
    List<Object> ys = new ArrayList<Object>();
    for (FudgeField y_field : ys_fields) {
      Object y = context.fieldValueToObject(y_field);
      ys.add(y);
    }    
    return new VolatilitySurfaceDefinition<Object, Object>(name, currency, interpolatorName, xs.toArray(), ys.toArray());
  }

}
