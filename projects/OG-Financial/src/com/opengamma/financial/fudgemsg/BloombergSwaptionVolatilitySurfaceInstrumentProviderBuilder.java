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
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.financial.analytics.volatility.surface.BloombergSwaptionVolatilitySurfaceInstrumentProvider;

/**
 * Builder for converting BloombergSwaptionVolatilitySurfaceInstrumentProvider instances to/from Fudge messages.
 */
@FudgeBuilderFor(BloombergSwaptionVolatilitySurfaceInstrumentProvider.class)
public class BloombergSwaptionVolatilitySurfaceInstrumentProviderBuilder implements FudgeBuilder<BloombergSwaptionVolatilitySurfaceInstrumentProvider> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializationContext context, final BloombergSwaptionVolatilitySurfaceInstrumentProvider object) {
    final MutableFudgeMsg message = context.newMessage();
    FudgeSerializationContext.addClassHeader(message, BloombergSwaptionVolatilitySurfaceInstrumentProvider.class);
    message.add("countryPrefix", object.getCountryPrefix());
    message.add("type", object.getTypePrefix());
    message.add("postfix", object.getPostfix());
    message.add("zeroPadFirstTenor", object.isZeroPadFirstTenor());
    message.add("zeroPadSecondTenor", object.isZeroPadSecondTenor());
    message.add("dataFieldName", object.getDataFieldName());
    return message;
  }

  @Override
  public BloombergSwaptionVolatilitySurfaceInstrumentProvider buildObject(final FudgeDeserializationContext context, final FudgeMsg message) {
    final String dataFieldName = message.getString("dataFieldName");
    if (dataFieldName == null) {
      return new BloombergSwaptionVolatilitySurfaceInstrumentProvider(message.getString("countryPrefix"), message.getString("type"),
                   message.getBoolean("zeroPadFirstTenor"), message.getBoolean("zeroPadSecondTenor"), message.getString("postfix"));
    }
    return new BloombergSwaptionVolatilitySurfaceInstrumentProvider(message.getString("countryPrefix"), message.getString("type"),
        message.getBoolean("zeroPadFirstTenor"), message.getBoolean("zeroPadSecondTenor"), message.getString("postfix"), dataFieldName);
  }

}
