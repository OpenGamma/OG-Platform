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

import com.opengamma.financial.analytics.volatility.surface.BloombergEquityOptionVolatilitySurfaceInstrumentProvider;

/**
 * @deprecated The instrument provider for which this is a fudge builder has been deprecated
 */
@Deprecated
@FudgeBuilderFor(BloombergEquityOptionVolatilitySurfaceInstrumentProvider.class)
public class BloombergEquityOptionVolatilitySurfaceInstrumentProviderFudgeBuilderDeprecated implements FudgeBuilder<BloombergEquityOptionVolatilitySurfaceInstrumentProvider> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final BloombergEquityOptionVolatilitySurfaceInstrumentProvider object) {
    final MutableFudgeMsg message = serializer.newMessage();
    FudgeSerializer.addClassHeader(message, BloombergEquityOptionVolatilitySurfaceInstrumentProvider.class);
    message.add(PREFIX_FIELD_NAME, object.getUnderlyingPrefix());
    message.add(POSTFIX_FIELD_NAME, object.getPostfix());
    message.add(DATA_FIELD_NAME, object.getDataFieldName());
    return message;
  }

  @Override
  public BloombergEquityOptionVolatilitySurfaceInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    String prefix = message.getString(PREFIX_FIELD_NAME);
    //backward compatibility
    if (prefix == null) {
      prefix = message.getString("underlyingPrefix");
    }
    String postfix = message.getString(POSTFIX_FIELD_NAME);
    //backward compatibility
    if (postfix == null) {
      postfix = message.getString("postfix");
    }
    String dataField = message.getString(DATA_FIELD_NAME);
    //backward compatibility
    if (dataField == null) {
      dataField = message.getString("dataFieldName");
    }
    return new BloombergEquityOptionVolatilitySurfaceInstrumentProvider(prefix, postfix, dataField);
  }

}
