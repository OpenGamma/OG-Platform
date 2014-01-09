/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;

/**
 * Fudge builder for objects used in ISDA pricing of CDS, CDX and CDS options
 *
 */
final class ISDAResultsBuilder {

  private ISDAResultsBuilder() {
  }

  /**
   * Fudge builder for {@link ISDACompliantCurve}
   */
  @FudgeBuilderFor(ISDACompliantCurve.class)
  public static final class ISDACompliantCurveBuilder extends AbstractFudgeBuilder<ISDACompliantCurve> {
    private static final String T_FIELD_NAME = "t";
    private static final String RT_FIELD_NAME = "rt";

    @Override
    public ISDACompliantCurve buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final double[] t = deserializer.fieldValueToObject(double[].class, message.getByName(T_FIELD_NAME));
      final double[] rt = deserializer.fieldValueToObject(double[].class, message.getByName(RT_FIELD_NAME));
      return ISDACompliantCurve.makeFromRT(t, rt);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final ISDACompliantCurve object) {
      serializer.addToMessage(message, T_FIELD_NAME, null, object.getT());
      serializer.addToMessage(message, RT_FIELD_NAME, null, object.getRt());

    }
  }

  /**
   * Fudge builder for {@link ISDACompliantYieldCurve}
   */
  @FudgeBuilderFor(ISDACompliantYieldCurve.class)
  public static final class ISDACompliantYieldCurveBuilder extends AbstractFudgeBuilder<ISDACompliantYieldCurve> {
    private static final String T_FIELD_NAME = "t";
    private static final String R_FIELD_NAME = "r";
    private static final String RT_FIELD_NAME = "rt";
    private static final String DF_FIELD_NAME = "df";

    @Override
    public ISDACompliantYieldCurve buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final double[] t = deserializer.fieldValueToObject(double[].class, message.getByName(T_FIELD_NAME));
      final double[] rt = deserializer.fieldValueToObject(double[].class, message.getByName(RT_FIELD_NAME));
      return ISDACompliantYieldCurve.makeFromRT(t, rt);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final ISDACompliantYieldCurve object) {
      serializer.addToMessage(message, T_FIELD_NAME, null, object.getT());
      serializer.addToMessage(message, RT_FIELD_NAME, null, object.getRt());
    }
  }

  /**
   * Fudge builder for {@link ISDACompliantCreditCurve}
   */
  @FudgeBuilderFor(ISDACompliantCreditCurve.class)
  public static final class ISDACompliantCreditCurveBuilder extends AbstractFudgeBuilder<ISDACompliantCreditCurve> {
    private static final String T_FIELD_NAME = "t";
    private static final String RT_FIELD_NAME = "rt";

    @Override
    public ISDACompliantCreditCurve buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final double[] t = deserializer.fieldValueToObject(double[].class, message.getByName(T_FIELD_NAME));
      final double[] rt = deserializer.fieldValueToObject(double[].class, message.getByName(RT_FIELD_NAME));
      return ISDACompliantCreditCurve.makeFromRT(t, rt);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final ISDACompliantCreditCurve object) {
      serializer.addToMessage(message, T_FIELD_NAME, null, object.getT());
      serializer.addToMessage(message, RT_FIELD_NAME, null, object.getRt());;
    }
  }

}
