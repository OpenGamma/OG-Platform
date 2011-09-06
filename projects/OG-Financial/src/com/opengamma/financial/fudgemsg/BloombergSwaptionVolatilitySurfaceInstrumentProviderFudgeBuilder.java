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

import com.opengamma.financial.analytics.volatility.surface.BloombergSwaptionVolatilitySurfaceInstrumentProvider;

/**
 * Builder for converting BloombergSwaptionVolatilitySurfaceInstrumentProvider instances to/from Fudge messages.
 */
@FudgeBuilderFor(BloombergSwaptionVolatilitySurfaceInstrumentProvider.class)
public class BloombergSwaptionVolatilitySurfaceInstrumentProviderFudgeBuilder implements FudgeBuilder<BloombergSwaptionVolatilitySurfaceInstrumentProvider> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final BloombergSwaptionVolatilitySurfaceInstrumentProvider object) {
    final MutableFudgeMsg message = serializer.newMessage();
    FudgeSerializer.addClassHeader(message, BloombergSwaptionVolatilitySurfaceInstrumentProvider.class);
    message.add("countryPrefix", object.getCountryPrefix());
    message.add("type", object.getTypePrefix());
    message.add("postfix", object.getPostfix());
    message.add("zeroPadFirstTenor", object.isZeroPadSwapMaturityTenor()); //TODO rename the field name
    message.add("zeroPadSecondTenor", object.isZeroPadSwaptionExpiryTenor()); //TODO rename the field name
    message.add("dataFieldName", object.getDataFieldName());
    return message;
  }

  @Override
  public BloombergSwaptionVolatilitySurfaceInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final String dataFieldName = message.getString("dataFieldName");
    if (dataFieldName == null) {
      return new BloombergSwaptionVolatilitySurfaceInstrumentProvider(message.getString("countryPrefix"), message.getString("type"),
                   message.getBoolean("zeroPadFirstTenor"), message.getBoolean("zeroPadSecondTenor"), message.getString("postfix"));
    }
    return new BloombergSwaptionVolatilitySurfaceInstrumentProvider(message.getString("countryPrefix"), message.getString("type"),
        message.getBoolean("zeroPadFirstTenor"), message.getBoolean("zeroPadSecondTenor"), message.getString("postfix"), dataFieldName);
  }

}
