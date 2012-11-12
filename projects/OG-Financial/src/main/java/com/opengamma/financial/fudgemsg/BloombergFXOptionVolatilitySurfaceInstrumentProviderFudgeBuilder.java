/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static com.opengamma.financial.analytics.volatility.surface.SurfaceInstrumentProvider.DATA_FIELD_NAME;
import static com.opengamma.financial.analytics.volatility.surface.SurfaceInstrumentProvider.POSTFIX_FIELD_NAME;
import static com.opengamma.financial.analytics.volatility.surface.SurfaceInstrumentProvider.PREFIX_FIELD_NAME;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.volatility.surface.BloombergFXOptionVolatilitySurfaceInstrumentProvider;

/**
 *
 */
@FudgeBuilderFor(BloombergFXOptionVolatilitySurfaceInstrumentProvider.class)
public class BloombergFXOptionVolatilitySurfaceInstrumentProviderFudgeBuilder implements FudgeBuilder<BloombergFXOptionVolatilitySurfaceInstrumentProvider> {
  private static final String SCHEME_NAME_FIELD = "SCHEME_NAME";
  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final BloombergFXOptionVolatilitySurfaceInstrumentProvider object) {
    final MutableFudgeMsg message = serializer.newMessage();
    FudgeSerializer.addClassHeader(message, BloombergFXOptionVolatilitySurfaceInstrumentProvider.class);
    message.add(PREFIX_FIELD_NAME, object.getFXPrefix());
    message.add(POSTFIX_FIELD_NAME, object.getPostfix());
    message.add(DATA_FIELD_NAME, object.getDataFieldName());
    message.add(SCHEME_NAME_FIELD, object.getSchemeName());
    return message;
  }

  @Override
  public BloombergFXOptionVolatilitySurfaceInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    String prefix = message.getString(PREFIX_FIELD_NAME);
    //backward compatibility
    if (prefix == null) {
      prefix = message.getString("FX_PREFIX");
    }
    String schemeName = message.getString(SCHEME_NAME_FIELD);
    if (schemeName == null) {
      schemeName = ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName();
    }
    return new BloombergFXOptionVolatilitySurfaceInstrumentProvider(prefix, message.getString(POSTFIX_FIELD_NAME), message.getString(DATA_FIELD_NAME), schemeName);
  }

}
