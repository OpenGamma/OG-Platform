/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import com.opengamma.financial.analytics.volatility.surface.BloombergSwaptionVolatilitySurfaceInstrumentProvider;

/**
 * Builder for converting BloombergSwaptionVolatilitySurfaceInstrumentProvider instances to/from Fudge messages.
 */
@FudgeBuilderFor(BloombergSwaptionVolatilitySurfaceInstrumentProvider.class)
public class BloombergSwaptionVolatilitySurfaceInstrumentProviderBuilder implements FudgeBuilder<BloombergSwaptionVolatilitySurfaceInstrumentProvider> {
 
  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, BloombergSwaptionVolatilitySurfaceInstrumentProvider object) {
    MutableFudgeFieldContainer message = context.newMessage();
    FudgeSerializationContext.addClassHeader(message, BloombergSwaptionVolatilitySurfaceInstrumentProvider.class);
    message.add("countryPrefix", object.getCountryPrefix());
    message.add("type", object.getTypePrefix());
    message.add("postfix", object.getPostfix());
    message.add("zeroPadFirstTenor", object.isZeroPadFirstTenor());
    message.add("zeroPadSecondTenor", object.isZeroPadSecondTenor());
    return message; 
  }

  @Override
  public BloombergSwaptionVolatilitySurfaceInstrumentProvider buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    return new BloombergSwaptionVolatilitySurfaceInstrumentProvider(message.getString("countryPrefix"), message.getString("type"),  
                   message.getBoolean("zeroPadFirstTenor"), message.getBoolean("zeroPadSecondTenor"), message.getString("postfix"));
  }

}
