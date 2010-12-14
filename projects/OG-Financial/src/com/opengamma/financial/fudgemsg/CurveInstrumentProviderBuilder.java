/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.ircurve.BloombergFutureCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider;

/**
 * Builder for converting CurveInstrumentProvider instances to/from Fudge messages.  Not sure if this is ever actually used, shouldn't fudge deal with the subclasses?
 */
@FudgeBuilderFor(CurveInstrumentProvider.class)
public class CurveInstrumentProviderBuilder implements FudgeBuilder<CurveInstrumentProvider> {

  private BloombergFutureCurveInstrumentProviderBuilder _bloombergFutureBuilder = new BloombergFutureCurveInstrumentProviderBuilder();
  private StaticCurveInstrumentProviderBuilder _staticBuilder = new StaticCurveInstrumentProviderBuilder();
  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, CurveInstrumentProvider object) {
    if (object instanceof BloombergFutureCurveInstrumentProvider) {
      return _bloombergFutureBuilder.buildMessage(context, (BloombergFutureCurveInstrumentProvider) object);
    } else if (object instanceof StaticCurveInstrumentProvider) {
      return _staticBuilder.buildMessage(context, (StaticCurveInstrumentProvider) object);
    } else {
      throw new OpenGammaRuntimeException("Unsupported subclass - needs explicit support for mongo serialization");
    }
  }

  @Override
  public CurveInstrumentProvider buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    String type = message.getString("type");
    if (type.equals(BloombergFutureCurveInstrumentProviderBuilder.TYPE)) {
      return _bloombergFutureBuilder.buildObject(context, message);
    } else if (type.equals(StaticCurveInstrumentProviderBuilder.TYPE)) {
      return _staticBuilder.buildObject(context, message);
    } else {
      throw new OpenGammaRuntimeException("Unsupported subclass type ('" + type + "') - needs explicit support for mongo serialization");
    }
  }

}
