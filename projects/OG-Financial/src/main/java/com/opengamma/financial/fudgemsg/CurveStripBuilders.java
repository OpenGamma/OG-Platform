/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadStrip;
import com.opengamma.financial.analytics.ircurve.strips.CurveStrip;
import com.opengamma.financial.analytics.ircurve.strips.CurveStripWithIdentifier;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
/* package */ final class CurveStripBuilders {

  private CurveStripBuilders() {
  }

  @FudgeBuilderFor(CurveStripWithIdentifier.class)
  public static class CurveStripWithIdentifierBuilder implements FudgeBuilder<CurveStripWithIdentifier> {
    private static final String CURVE_STRIP_FIELD = "curveStrip";
    private static final String ID_FIELD = "id";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CurveStripWithIdentifier object) {
      final MutableFudgeMsg message = serializer.newMessage();
      serializer.addToMessageWithClassHeaders(message, CURVE_STRIP_FIELD, null, object.getCurveStrip());
      serializer.addToMessage(message, ID_FIELD, null, object.getIdentifier());
      return message;
    }

    @Override
    public CurveStripWithIdentifier buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final CurveStrip curveStrip = (CurveStrip) deserializer.fieldValueToObject(message.getByName(CURVE_STRIP_FIELD));
      final ExternalId id = deserializer.fieldValueToObject(ExternalId.class, message.getByName(ID_FIELD));
      return new CurveStripWithIdentifier(curveStrip, id);
    }

  }

  @FudgeBuilderFor(CreditSpreadStrip.class)
  public static class CreditSpreadStripBuilder implements FudgeBuilder<CreditSpreadStrip> {
    private static final String UNIQUE_ID_FIELD = "uniqueId";
    private static final String CURVE_SPECIFICATION_FIELD = "curveSpecification";
    private static final String TENOR_FIELD = "tenor";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CreditSpreadStrip object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(UNIQUE_ID_FIELD, object.getUniqueId());
      message.add(CURVE_SPECIFICATION_FIELD, object.getCurveSpecificationName());
      serializer.addToMessageWithClassHeaders(message, TENOR_FIELD, null, object.getTenor());
      return message;
    }

    @Override
    public CreditSpreadStrip buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String curveSpecificationName = message.getString(CURVE_SPECIFICATION_FIELD);
      final FudgeField tenorField = message.getByName(TENOR_FIELD);
      final Tenor tenor = deserializer.fieldValueToObject(Tenor.class, tenorField);
      final CreditSpreadStrip strip = new CreditSpreadStrip(curveSpecificationName, tenor);
      final FudgeField uniqueId = message.getByName(UNIQUE_ID_FIELD);
      if (uniqueId != null) {
        strip.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueId));
      }
      return strip;
    }
  }

}
