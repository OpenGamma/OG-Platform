/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.math.curve.DoublesCurve;

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
    private static final String R_FIELD_NAME = "r";
    private static final String RT_FIELD_NAME = "rt";
    private static final String DF_FIELD_NAME = "df";
    private static final String OFFSET_R_FIELD_NAME = "or";
    private static final String OFFSET_RT_FIELD_NAME = "ort";

    @Override
    public ISDACompliantCurve buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final double[] t = deserializer.fieldValueToObject(double[].class, message.getByName(T_FIELD_NAME));
      final double[] r = deserializer.fieldValueToObject(double[].class, message.getByName(R_FIELD_NAME));
      final double[] rt = deserializer.fieldValueToObject(double[].class, message.getByName(RT_FIELD_NAME));
      final double[] df = deserializer.fieldValueToObject(double[].class, message.getByName(DF_FIELD_NAME));
      final double offsetR  =  message.getDouble(OFFSET_R_FIELD_NAME);
      final double offsetRt =  message.getDouble(OFFSET_RT_FIELD_NAME);
      return new ISDACompliantCurve(t, r, rt, df, offsetR, offsetRt);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final ISDACompliantCurve object) {
      serializer.addToMessage(message, T_FIELD_NAME, null, object.getT());
      serializer.addToMessage(message, R_FIELD_NAME, null, object.getR());
      serializer.addToMessage(message, RT_FIELD_NAME, null, object.getRt());
      serializer.addToMessage(message, DF_FIELD_NAME, null, object.getDf());
      serializer.addToMessage(message, OFFSET_R_FIELD_NAME, null, object.getOffsetTime());
      serializer.addToMessage(message, OFFSET_RT_FIELD_NAME, null, object.getOffsetRT());
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
    private static final String OFFSET_R_FIELD_NAME = "or";
    private static final String OFFSET_RT_FIELD_NAME = "ort";

    @Override
    public ISDACompliantYieldCurve buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final double[] t = deserializer.fieldValueToObject(double[].class, message.getByName(T_FIELD_NAME));
      final double[] r = deserializer.fieldValueToObject(double[].class, message.getByName(R_FIELD_NAME));
      final double[] rt = deserializer.fieldValueToObject(double[].class, message.getByName(RT_FIELD_NAME));
      final double[] df = deserializer.fieldValueToObject(double[].class, message.getByName(DF_FIELD_NAME));
      final double offsetR  =  message.getDouble(OFFSET_R_FIELD_NAME);
      final double offsetRt =  message.getDouble(OFFSET_RT_FIELD_NAME);
      return new ISDACompliantYieldCurve(t, r, rt, df, offsetR, offsetRt);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final ISDACompliantYieldCurve object) {
      serializer.addToMessage(message, T_FIELD_NAME, null, object.getT());
      serializer.addToMessage(message, R_FIELD_NAME, null, object.getR());
      serializer.addToMessage(message, RT_FIELD_NAME, null, object.getRt());
      serializer.addToMessage(message, DF_FIELD_NAME, null, object.getDf());
      serializer.addToMessage(message, OFFSET_R_FIELD_NAME, null, object.getOffsetTime());
      serializer.addToMessage(message, OFFSET_RT_FIELD_NAME, null, object.getOffsetRT());
    }
  }

  /**
   * Fudge builder for {@link ISDACompliantCreditCurve}
   */
  @FudgeBuilderFor(ISDACompliantCreditCurve.class)
  public static final class ISDACompliantCreditCurveBuilder extends AbstractFudgeBuilder<ISDACompliantCreditCurve> {
    private static final String T_FIELD_NAME = "t";
    private static final String R_FIELD_NAME = "r";
    private static final String RT_FIELD_NAME = "rt";
    private static final String DF_FIELD_NAME = "df";
    private static final String OFFSET_R_FIELD_NAME = "or";
    private static final String OFFSET_RT_FIELD_NAME = "ort";

    @Override
    public ISDACompliantCreditCurve buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final double[] t = deserializer.fieldValueToObject(double[].class, message.getByName(T_FIELD_NAME));
      final double[] r = deserializer.fieldValueToObject(double[].class, message.getByName(R_FIELD_NAME));
      final double[] rt = deserializer.fieldValueToObject(double[].class, message.getByName(RT_FIELD_NAME));
      final double[] df = deserializer.fieldValueToObject(double[].class, message.getByName(DF_FIELD_NAME));
      final double offsetR  =  message.getDouble(OFFSET_R_FIELD_NAME);
      final double offsetRt =  message.getDouble(OFFSET_RT_FIELD_NAME);
      return new ISDACompliantCreditCurve(t, r, rt, df, offsetR, offsetRt);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final ISDACompliantCreditCurve object) {
      serializer.addToMessage(message, T_FIELD_NAME, null, object.getT());
      serializer.addToMessage(message, R_FIELD_NAME, null, object.getR());
      serializer.addToMessage(message, RT_FIELD_NAME, null, object.getRt());
      serializer.addToMessage(message, DF_FIELD_NAME, null, object.getDf());
      serializer.addToMessage(message, OFFSET_R_FIELD_NAME, null, object.getOffsetTime());
      serializer.addToMessage(message, OFFSET_RT_FIELD_NAME, null, object.getOffsetRT());
    }
  }


}
