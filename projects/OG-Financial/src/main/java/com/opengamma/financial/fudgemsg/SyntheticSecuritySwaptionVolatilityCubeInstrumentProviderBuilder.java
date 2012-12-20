/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import com.opengamma.financial.analytics.volatility.cube.SyntheticSecuritySwaptionVolatilityCubeInstrumentProvider;

/**
 * 
 */
@FudgeBuilderFor(SyntheticSecuritySwaptionVolatilityCubeInstrumentProvider.class)
public class SyntheticSecuritySwaptionVolatilityCubeInstrumentProviderBuilder implements FudgeBuilder<SyntheticSecuritySwaptionVolatilityCubeInstrumentProvider> {
  private static final String PREFIX_FIELD = "prefix";
  private static final String DATA_FIELD = "dataField";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final SyntheticSecuritySwaptionVolatilityCubeInstrumentProvider object) {
    final MutableFudgeMsg message = serializer.newMessage();
    FudgeSerializer.addClassHeader(message, SyntheticSecuritySwaptionVolatilityCubeInstrumentProvider.class);
    message.add(PREFIX_FIELD, object.getPrefix());
    message.add(DATA_FIELD, object.getDataFieldName());
    return message;
  }

  @Override
  public SyntheticSecuritySwaptionVolatilityCubeInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final String prefix = message.getString(PREFIX_FIELD);
    final String dataFieldName = message.getString(DATA_FIELD);
    return new SyntheticSecuritySwaptionVolatilityCubeInstrumentProvider(prefix, dataFieldName);
  }

}
