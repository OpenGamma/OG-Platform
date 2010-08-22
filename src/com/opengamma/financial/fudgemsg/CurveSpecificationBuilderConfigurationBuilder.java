/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.util.Map;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.fudgemsg.mapping.MapBuilder;

import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.util.time.Tenor;

/**
 * Builder for converting Region instances to/from Fudge messages.
 */
public class CurveSpecificationBuilderConfigurationBuilder implements FudgeBuilder<CurveSpecificationBuilderConfiguration> {

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, CurveSpecificationBuilderConfiguration object) {
    MutableFudgeFieldContainer message = context.newMessage();
    FudgeSerializationContext.addClassHeader(message, CurveSpecificationBuilderConfiguration.class);
    context.objectToFudgeMsgWithClassHeaders(message, "cashInstrumentProviders", null, object.getCashInstrumentProviders());
    context.objectToFudgeMsgWithClassHeaders(message, "fraInstrumentProviders", null, object.getFraInstrumentProviders());
    context.objectToFudgeMsgWithClassHeaders(message, "futureInstrumentProviders", null, object.getFutureInstrumentProviders());
    context.objectToFudgeMsgWithClassHeaders(message, "rateInstrumentProviders", null, object.getRateInstrumentProviders());
    context.objectToFudgeMsgWithClassHeaders(message, "swapInstrumentProviders", null, object.getSwapInstrumentProviders());
    return message; 
  }

  @SuppressWarnings("unchecked")
  @Override
  public CurveSpecificationBuilderConfiguration buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    Map<Tenor, CurveInstrumentProvider> cashInstrumentProviders = MapBuilder.buildObject(context, message.getMessage("cashInstrumentProviders"), Tenor.class, CurveInstrumentProvider.class);
    Map<Tenor, CurveInstrumentProvider> fraInstrumentProviders = MapBuilder.buildObject(context, message.getMessage("fraInstrumentProviders"), Tenor.class, CurveInstrumentProvider.class);;
    Map<Tenor, CurveInstrumentProvider> futureInstrumentProviders = MapBuilder.buildObject(context, message.getMessage("futureInstrumentProviders"), Tenor.class, CurveInstrumentProvider.class);
    Map<Tenor, CurveInstrumentProvider> rateInstrumentProviders = MapBuilder.buildObject(context, message.getMessage("rateInstrumentProviders"), Tenor.class, CurveInstrumentProvider.class);
    Map<Tenor, CurveInstrumentProvider> swapInstrumentProviders = MapBuilder.buildObject(context, message.getMessage("swapInstrumentProviders"), Tenor.class, CurveInstrumentProvider.class);
    return new CurveSpecificationBuilderConfiguration(cashInstrumentProviders, fraInstrumentProviders, rateInstrumentProviders, futureInstrumentProviders, swapInstrumentProviders);
  }

}
