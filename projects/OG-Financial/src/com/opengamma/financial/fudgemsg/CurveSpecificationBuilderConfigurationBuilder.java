/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.time.calendar.Period;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.util.time.Tenor;

/**
 * Builder for converting CurveSpecificationBuilderConfiguration instances to/from Fudge messages.
 */
@FudgeBuilderFor(CurveSpecificationBuilderConfiguration.class)
public class CurveSpecificationBuilderConfigurationBuilder implements FudgeBuilder<CurveSpecificationBuilderConfiguration> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CurveSpecificationBuilderConfiguration object) {
    MutableFudgeMsg message = serializer.newMessage();
    FudgeSerializer.addClassHeader(message, CurveSpecificationBuilderConfiguration.class);
    MutableFudgeMsg cashInstrumentProvidersMessage = serializer.newMessage();
    for (Entry<Tenor, CurveInstrumentProvider> entry : object.getCashInstrumentProviders().entrySet()) {
      serializer.addToMessage(cashInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue());
    }
    message.add("cashInstrumentProviders", cashInstrumentProvidersMessage);
    
    MutableFudgeMsg fraInstrumentProvidersMessage = serializer.newMessage();
    for (Entry<Tenor, CurveInstrumentProvider> entry : object.getFraInstrumentProviders().entrySet()) {
      if (entry.getKey().getPeriod().toString() == null) {
        throw new OpenGammaRuntimeException("null");
      }
      serializer.addToMessage(fraInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue());
    }
    message.add("fraInstrumentProviders", fraInstrumentProvidersMessage);
    
    MutableFudgeMsg futureInstrumentProvidersMessage = serializer.newMessage();
    for (Entry<Tenor, CurveInstrumentProvider> entry : object.getFutureInstrumentProviders().entrySet()) {
      serializer.addToMessage(futureInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue());
    }
    message.add("futureInstrumentProviders", futureInstrumentProvidersMessage);

    MutableFudgeMsg rateInstrumentProvidersMessage = serializer.newMessage();
    for (Entry<Tenor, CurveInstrumentProvider> entry : object.getRateInstrumentProviders().entrySet()) {
      serializer.addToMessage(rateInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue());
    }
    message.add("rateInstrumentProviders", rateInstrumentProvidersMessage);
    
    MutableFudgeMsg swapInstrumentProvidersMessage = serializer.newMessage();
    for (Entry<Tenor, CurveInstrumentProvider> entry : object.getSwapInstrumentProviders().entrySet()) {
      serializer.addToMessage(swapInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue());
    }
    message.add("swapInstrumentProviders", swapInstrumentProvidersMessage);
    
    MutableFudgeMsg basisSwapInstrumentProvidersMessage = serializer.newMessage();
    for (Entry<Tenor, CurveInstrumentProvider> entry : object.getBasisSwapInstrumentProviders().entrySet()) {
      serializer.addToMessage(basisSwapInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue());
    }
    message.add("basisSwapInstrumentProviders", basisSwapInstrumentProvidersMessage);
    
    MutableFudgeMsg tenorSwapInstrumentProvidersMessage = serializer.newMessage();
    for (Entry<Tenor, CurveInstrumentProvider> entry : object.getTenorSwapInstrumentProviders().entrySet()) {
      serializer.addToMessage(tenorSwapInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue());
    }
    message.add("tenorSwapInstrumentProviders", tenorSwapInstrumentProvidersMessage);

    if (object.getOISSwapInstrumentProviders() != null) {
      MutableFudgeMsg oisSwapInstrumentProvidersMessage = serializer.newMessage();
      for (Entry<Tenor, CurveInstrumentProvider> entry : object.getOISSwapInstrumentProviders().entrySet()) {
        serializer.addToMessage(oisSwapInstrumentProvidersMessage, entry.getKey().getPeriod().toString(), null, entry.getValue());
      }
      message.add("oisSwapInstrumentProviders", oisSwapInstrumentProvidersMessage);
    }
   
    return message; 
  }

  @Override
  public CurveSpecificationBuilderConfiguration buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    FudgeMsg cashInstrumentProvidersMessage = message.getMessage("cashInstrumentProviders");
    Map<Tenor, CurveInstrumentProvider> cashInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    for (FudgeField field : cashInstrumentProvidersMessage.getAllFields()) {
      cashInstrumentProviders.put(new Tenor(Period.parse(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
    }
    
    FudgeMsg fraInstrumentProvidersMessage = message.getMessage("fraInstrumentProviders");
    Map<Tenor, CurveInstrumentProvider> fraInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    for (FudgeField field : fraInstrumentProvidersMessage.getAllFields()) {
      fraInstrumentProviders.put(new Tenor(Period.parse(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
    }

    FudgeMsg futureInstrumentProvidersMessage = message.getMessage("futureInstrumentProviders");
    Map<Tenor, CurveInstrumentProvider> futureInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    for (FudgeField field : futureInstrumentProvidersMessage.getAllFields()) {
      futureInstrumentProviders.put(new Tenor(Period.parse(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
    }
    
    FudgeMsg rateInstrumentProvidersMessage = message.getMessage("rateInstrumentProviders");
    Map<Tenor, CurveInstrumentProvider> rateInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    for (FudgeField field : rateInstrumentProvidersMessage.getAllFields()) {
      rateInstrumentProviders.put(new Tenor(Period.parse(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
    }

    FudgeMsg swapInstrumentProvidersMessage = message.getMessage("swapInstrumentProviders");
    Map<Tenor, CurveInstrumentProvider> swapInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    for (FudgeField field : swapInstrumentProvidersMessage.getAllFields()) {
      swapInstrumentProviders.put(new Tenor(Period.parse(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
    }
    
    FudgeMsg basisSwapInstrumentProvidersMessage = message.getMessage("basisSwapInstrumentProviders");
    Map<Tenor, CurveInstrumentProvider> basisSwapInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    for (FudgeField field : basisSwapInstrumentProvidersMessage.getAllFields()) {
      basisSwapInstrumentProviders.put(new Tenor(Period.parse(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
    }
    
    FudgeMsg tenorSwapInstrumentProvidersMessage = message.getMessage("tenorSwapInstrumentProviders");
    Map<Tenor, CurveInstrumentProvider> tenorSwapInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
    for (FudgeField field : tenorSwapInstrumentProvidersMessage.getAllFields()) {
      tenorSwapInstrumentProviders.put(new Tenor(Period.parse(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
    }

    FudgeMsg oisSwapInstrumentProvidersMessage = message.getMessage("oisSwapInstrumentProviders");
    Map<Tenor, CurveInstrumentProvider> oisSwapInstrumentProviders = null;
    if (oisSwapInstrumentProvidersMessage != null) {
      oisSwapInstrumentProviders = new HashMap<Tenor, CurveInstrumentProvider>();
      for (FudgeField field : oisSwapInstrumentProvidersMessage.getAllFields()) {
        oisSwapInstrumentProviders.put(new Tenor(Period.parse(field.getName())), deserializer.fieldValueToObject(CurveInstrumentProvider.class, field));
      }
    }
    return new CurveSpecificationBuilderConfiguration(cashInstrumentProviders, 
                                                      fraInstrumentProviders, 
                                                      rateInstrumentProviders, 
                                                      futureInstrumentProviders,
                                                      swapInstrumentProviders, 
                                                      basisSwapInstrumentProviders, 
                                                      tenorSwapInstrumentProviders,
                                                      oisSwapInstrumentProviders);
  }

}
