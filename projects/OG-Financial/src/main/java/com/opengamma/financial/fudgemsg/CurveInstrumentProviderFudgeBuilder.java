/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.ircurve.BloombergFutureCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.SyntheticIdentifierCurveInstrumentProvider;

/**
 * Builder for converting CurveInstrumentProvider instances to/from Fudge messages.  Not sure if this is ever actually used, shouldn't fudge deal with the subclasses?
 */
@FudgeBuilderFor(CurveInstrumentProvider.class)
public class CurveInstrumentProviderFudgeBuilder implements FudgeBuilder<CurveInstrumentProvider> {

  private BloombergFutureCurveInstrumentProviderFudgeBuilder _bloombergFutureBuilder = new BloombergFutureCurveInstrumentProviderFudgeBuilder();
  private StaticCurveInstrumentProviderFudgeBuilder _staticBuilder = new StaticCurveInstrumentProviderFudgeBuilder();
  private SyntheticIdentifierCurveInstrumentProviderFudgeBuilder _syntheticBuilder = new SyntheticIdentifierCurveInstrumentProviderFudgeBuilder();
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CurveInstrumentProvider object) {
    if (object instanceof BloombergFutureCurveInstrumentProvider) {
      return _bloombergFutureBuilder.buildMessage(serializer, (BloombergFutureCurveInstrumentProvider) object);
    } else if (object instanceof StaticCurveInstrumentProvider) {
      return _staticBuilder.buildMessage(serializer, (StaticCurveInstrumentProvider) object);
    } else if (object instanceof SyntheticIdentifierCurveInstrumentProvider) {
      return _syntheticBuilder.buildMessage(serializer, (SyntheticIdentifierCurveInstrumentProvider) object);
    } else {
      throw new OpenGammaRuntimeException("Unsupported subclass - needs explicit support for mongo serialization");
    }
  }

  @Override
  public CurveInstrumentProvider buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    String type = message.getString("type");
    if (type.equals(BloombergFutureCurveInstrumentProviderFudgeBuilder.TYPE)) {
      return _bloombergFutureBuilder.buildObject(deserializer, message);
    } else if (type.equals(StaticCurveInstrumentProviderFudgeBuilder.TYPE)) {
      return _staticBuilder.buildObject(deserializer, message);
    } else if (type.equals(SyntheticIdentifierCurveInstrumentProviderFudgeBuilder.TYPE)) {
      return _syntheticBuilder.buildObject(deserializer, message);
    } else {
      throw new OpenGammaRuntimeException("Unsupported subclass type ('" + type + "') - needs explicit support for mongo serialization");
    }
  }

}
