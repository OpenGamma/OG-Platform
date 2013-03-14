/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
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

import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.id.ExternalId;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
/* package */ final class CurveNodeBuilders {
  private static final String CURVE_MAPPER_ID_FIELD = "curveNodeIdMapper";

  private CurveNodeBuilders() {
  }

  @FudgeBuilderFor(CurveNodeWithIdentifier.class)
  public static class CurveNodeWithIdentifierBuilder implements FudgeBuilder<CurveNodeWithIdentifier> {
    private static final String CURVE_STRIP_FIELD = "curveStrip";
    private static final String ID_FIELD = "id";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CurveNodeWithIdentifier object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
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

  @FudgeBuilderFor(CashNode.class)
  public static class CashNodeBuilder implements FudgeBuilder<CashNode> {
    private static final String START_TENOR_FIELD = "startTenor";
    private static final String MATURITY_TENOR_FIELD = "maturityTenor";
    private static final String CONVENTION_ID_FIELD = "conventionId";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CashNode object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(START_TENOR_FIELD, object.getStartTenor());
      message.add(MATURITY_TENOR_FIELD, object.getMaturityTenor());
      return null;
    }

    @Override
    public CashNode buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      return null;
    }

  }

  @FudgeBuilderFor(CreditSpreadNode.class)
  public static class CreditSpreadNodeBuilder implements FudgeBuilder<CreditSpreadNode> {
    private static final String TENOR_FIELD = "tenor";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CreditSpreadNode object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(CURVE_MAPPER_ID_FIELD, object.getCurveNodeIdMapperName());
      message.add(TENOR_FIELD, object.getTenor());
      return message;
    }

    @Override
    public CreditSpreadNode buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String curveSpecificationName = message.getString(CURVE_MAPPER_ID_FIELD);
      final Tenor tenor = deserializer.fieldValueToObject(Tenor.class, message.getByName(TENOR_FIELD));
      final CreditSpreadNode strip = new CreditSpreadNode(curveSpecificationName, tenor);
      return strip;
    }
  }

}
