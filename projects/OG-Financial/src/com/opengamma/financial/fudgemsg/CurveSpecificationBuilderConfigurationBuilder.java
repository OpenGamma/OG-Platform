/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.time.calendar.Period;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.util.time.Tenor;

/**
 * Builder for converting Region instances to/from Fudge messages.
 */
@FudgeBuilderFor(CurveSpecificationBuilderConfiguration.class)
public class CurveSpecificationBuilderConfigurationBuilder implements FudgeBuilder<CurveSpecificationBuilderConfiguration> {

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, CurveSpecificationBuilderConfiguration object) {
    MutableFudgeFieldContainer message = context.newMessage();
    FudgeSerializationContext.addClassHeader(message, CurveSpecificationBuilderConfiguration.class);
    MutableFudgeFieldContainer cashInstrumentProvidersMessage = context.newMessage();
    for (Entry<Tenor, CurveInstrumentProvider> entry : object.getCashInstrumentProviders().entrySet()) {
      context.objectToFudgeMsg(cashInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue());
    }
    message.add("cashInstrumentProviders", cashInstrumentProvidersMessage);
    
    MutableFudgeFieldContainer fraInstrumentProvidersMessage = context.newMessage();
    for (Entry<Tenor, CurveInstrumentProvider> entry : object.getFraInstrumentProviders().entrySet()) {
      if (entry.getKey().getPeriod().toString() == null) {
        throw new OpenGammaRuntimeException("null");
      }
      context.objectToFudgeMsg(fraInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue());
    }
    message.add("fraInstrumentProviders", fraInstrumentProvidersMessage);
    
    MutableFudgeFieldContainer futureInstrumentProvidersMessage = context.newMessage();
    for (Entry<Tenor, CurveInstrumentProvider> entry : object.getFutureInstrumentProviders().entrySet()) {
      context.objectToFudgeMsg(futureInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue());
    }
    message.add("futureInstrumentProviders", futureInstrumentProvidersMessage);

    MutableFudgeFieldContainer rateInstrumentProvidersMessage = context.newMessage();
    for (Entry<Tenor, CurveInstrumentProvider> entry : object.getRateInstrumentProviders().entrySet()) {
      context.objectToFudgeMsg(rateInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue());
    }
    message.add("rateInstrumentProviders", rateInstrumentProvidersMessage);
    
    MutableFudgeFieldContainer swapInstrumentProvidersMessage = context.newMessage();
    for (Entry<Tenor, CurveInstrumentProvider> entry : object.getSwapInstrumentProviders().entrySet()) {
      context.objectToFudgeMsg(swapInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue());
    }
    message.add("swapInstrumentProviders", swapInstrumentProvidersMessage);
    
    MutableFudgeFieldContainer basisSwapInstrumentProvidersMessage = context.newMessage();
    for (Entry<Tenor, CurveInstrumentProvider> entry : object.getBasisSwapInstrumentProviders().entrySet()) {
      context.objectToFudgeMsg(basisSwapInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue());
    }
    message.add("basisSwapInstrumentProviders", basisSwapInstrumentProvidersMessage);
    
    MutableFudgeFieldContainer tenorSwapInstrumentProvidersMessage = context.newMessage();
    for (Entry<Tenor, CurveInstrumentProvider> entry : object.getTenorSwapInstrumentProviders().entrySet()) {
      context.objectToFudgeMsg(tenorSwapInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue());
    }
    message.add("tenorSwapInstrumentProviders", tenorSwapInstrumentProvidersMessage);
    return message; 
  }

  @Override
  public CurveSpecificationBuilderConfiguration buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    FudgeFieldContainer cashInstrumentProvidersMessage = message.getMessage("cashInstrumentProviders");
    Map<Tenor, CurveInstrumentProvider> cashInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    for (FudgeField field : cashInstrumentProvidersMessage.getAllFields()) {
      cashInstrumentProviders.put(new Tenor(Period.parse(field.getName())), context.fieldValueToObject(CurveInstrumentProvider.class, field));
    }
    
    FudgeFieldContainer fraInstrumentProvidersMessage = message.getMessage("fraInstrumentProviders");
    Map<Tenor, CurveInstrumentProvider> fraInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    for (FudgeField field : fraInstrumentProvidersMessage.getAllFields()) {
      fraInstrumentProviders.put(new Tenor(Period.parse(field.getName())), context.fieldValueToObject(CurveInstrumentProvider.class, field));
    }

    FudgeFieldContainer futureInstrumentProvidersMessage = message.getMessage("futureInstrumentProviders");
    Map<Tenor, CurveInstrumentProvider> futureInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    for (FudgeField field : futureInstrumentProvidersMessage.getAllFields()) {
      futureInstrumentProviders.put(new Tenor(Period.parse(field.getName())), context.fieldValueToObject(CurveInstrumentProvider.class, field));
    }
    
    FudgeFieldContainer rateInstrumentProvidersMessage = message.getMessage("rateInstrumentProviders");
    Map<Tenor, CurveInstrumentProvider> rateInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    for (FudgeField field : rateInstrumentProvidersMessage.getAllFields()) {
      rateInstrumentProviders.put(new Tenor(Period.parse(field.getName())), context.fieldValueToObject(CurveInstrumentProvider.class, field));
    }

    FudgeFieldContainer swapInstrumentProvidersMessage = message.getMessage("swapInstrumentProviders");
    Map<Tenor, CurveInstrumentProvider> swapInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    for (FudgeField field : swapInstrumentProvidersMessage.getAllFields()) {
      swapInstrumentProviders.put(new Tenor(Period.parse(field.getName())), context.fieldValueToObject(CurveInstrumentProvider.class, field));
    }
    
    FudgeFieldContainer basisSwapInstrumentProvidersMessage = message.getMessage("basisSwapInstrumentProviders");
    Map<Tenor, CurveInstrumentProvider> basisSwapInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    for (FudgeField field : basisSwapInstrumentProvidersMessage.getAllFields()) {
      basisSwapInstrumentProviders.put(new Tenor(Period.parse(field.getName())), context.fieldValueToObject(CurveInstrumentProvider.class, field));
    }
    
    FudgeFieldContainer tenorSwapInstrumentProvidersMessage = message.getMessage("tenorSwapInstrumentProviders");
    Map<Tenor, CurveInstrumentProvider> tenorSwapInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    for (FudgeField field : tenorSwapInstrumentProvidersMessage.getAllFields()) {
      tenorSwapInstrumentProviders.put(new Tenor(Period.parse(field.getName())), context.fieldValueToObject(CurveInstrumentProvider.class, field));
    }
    return new CurveSpecificationBuilderConfiguration(
      cashInstrumentProviders, fraInstrumentProviders, rateInstrumentProviders, futureInstrumentProviders,
      swapInstrumentProviders, basisSwapInstrumentProviders, tenorSwapInstrumentProviders);
  }

}
