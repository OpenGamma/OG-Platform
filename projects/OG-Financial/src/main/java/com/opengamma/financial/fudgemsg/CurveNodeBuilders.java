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

import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.id.ExternalId;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
/* package */ final class CurveNodeBuilders {

  private CurveNodeBuilders() {
  }

  @FudgeBuilderFor(CurveNodeWithIdentifier.class)
  public static class CurveNodeWithIdentifierBuilder implements FudgeBuilder<CurveNodeWithIdentifier> {
    private static final String CURVE_STRIP_FIELD = "curveStrip";
    private static final String ID_FIELD = "id";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CurveNodeWithIdentifier object) {
      final MutableFudgeMsg message = serializer.newMessage();
      serializer.addToMessageWithClassHeaders(message, CURVE_STRIP_FIELD, null, object.getCurveStrip());
      serializer.addToMessage(message, ID_FIELD, null, object.getIdentifier());
      return message;
    }

    @Override
    public CurveNodeWithIdentifier buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final CurveNode curveStrip = (CurveNode) deserializer.fieldValueToObject(message.getByName(CURVE_STRIP_FIELD));
      final ExternalId id = deserializer.fieldValueToObject(ExternalId.class, message.getByName(ID_FIELD));
      return new CurveNodeWithIdentifier(curveStrip, id);
    }

  }

  @FudgeBuilderFor(CreditSpreadNode.class)
  public static class CreditSpreadNodeBuilder implements FudgeBuilder<CreditSpreadNode> {
    private static final String CURVE_SPECIFICATION_FIELD = "curveSpecification";
    private static final String TENOR_FIELD = "tenor";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CreditSpreadNode object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(CURVE_SPECIFICATION_FIELD, object.getCurveSpecificationName());
      serializer.addToMessageWithClassHeaders(message, TENOR_FIELD, null, object.getTenor());
      return message;
    }

    @Override
    public CreditSpreadNode buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String curveSpecificationName = message.getString(CURVE_SPECIFICATION_FIELD);
      final FudgeField tenorField = message.getByName(TENOR_FIELD);
      final Tenor tenor = deserializer.fieldValueToObject(Tenor.class, tenorField);
      final CreditSpreadNode strip = new CreditSpreadNode(curveSpecificationName, tenor);
      return strip;
    }
  }

}
