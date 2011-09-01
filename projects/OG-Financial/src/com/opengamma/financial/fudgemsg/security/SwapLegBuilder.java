/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg.security;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.joda.beans.BeanBuilder;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.Notional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.fudgemsg.ExternalIdBuilder;

/**
 * A Fudge builder for {@code SwapLeg} implementations.
 */
public class SwapLegBuilder extends AbstractFudgeBuilder {

  /** Field name. */
  public static final String DAY_COUNT_KEY = "dayCount";
  /** Field name. */
  public static final String FREQUENCY_KEY = "frequency";
  /** Field name. */
  public static final String REGION_IDENTIFIER_KEY = "regionIdentifier";
  /** Field name. */
  public static final String BUSINESS_DAY_CONVENTION_KEY = "businessDayConvention";
  /** Field name. */
  public static final String NOTIONAL_KEY = "notional";

  public static void toFudgeMsg(FudgeSerializer serializer, SwapLeg object, final MutableFudgeMsg msg) {
    addToMessage(msg, DAY_COUNT_KEY, object.getDayCount());
    addToMessage(msg, FREQUENCY_KEY, object.getFrequency());
    addToMessage(msg, REGION_IDENTIFIER_KEY, ExternalIdBuilder.toFudgeMsg(serializer, object.getRegionIdentifier()));
    addToMessage(msg, BUSINESS_DAY_CONVENTION_KEY, object.getBusinessDayConvention());
    addToMessage(serializer, msg, NOTIONAL_KEY, object.getNotional(), Notional.class);
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, BeanBuilder<? extends SwapLeg> object) {
    object.set(SwapLeg.meta().dayCount().name(), msg.getValue(DayCount.class, DAY_COUNT_KEY));
    object.set(SwapLeg.meta().frequency().name(), msg.getValue(Frequency.class, FREQUENCY_KEY));
    object.set(SwapLeg.meta().regionIdentifier().name(), ExternalIdBuilder.fromFudgeMsg(deserializer, msg.getMessage(REGION_IDENTIFIER_KEY)));
    object.set(SwapLeg.meta().businessDayConvention().name(), msg.getValue(BusinessDayConvention.class, BUSINESS_DAY_CONVENTION_KEY));
    object.set(SwapLeg.meta().notional().name(), deserializer.fudgeMsgToObject(Notional.class, msg.getMessage(NOTIONAL_KEY)));
  }

  //-------------------------------------------------------------------------
  /**
   * A Fudge builder for {@code FixedInterestRateLeg}.
   */
  @FudgeBuilderFor(FixedInterestRateLeg.class)
  public static class FixedInterestRateLegBuilder extends SwapLegBuilder implements FudgeBuilder<FixedInterestRateLeg>  {
    /** Field name. */
    public static final String RATE_KEY = "rate";

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, FixedInterestRateLeg object) {
      final MutableFudgeMsg msg = serializer.newMessage();
      SwapLegBuilder.toFudgeMsg(serializer, object, msg);
      addToMessage(msg, RATE_KEY, object.getRate());
      return msg;
    }

    @Override
    public FixedInterestRateLeg buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
      BeanBuilder<? extends FixedInterestRateLeg> builder = FixedInterestRateLeg.meta().builder();
      SwapLegBuilder.fromFudgeMsg(deserializer, msg, builder);
      builder.set(FixedInterestRateLeg.meta().rate().name(), msg.getDouble(RATE_KEY));
      return builder.build();
    }
  }

  //-------------------------------------------------------------------------
  /**
   * A Fudge builder for {@code FloatingInterestRateLeg}.
   */
  @FudgeBuilderFor(FloatingInterestRateLeg.class)
  public static class FloatingInterestRateLegBuilder extends SwapLegBuilder implements FudgeBuilder<FloatingInterestRateLeg>  {
    /** Field name. */
    public static final String FLOATING_REFERENCE_RATE_IDENTIFIER_KEY = "floatingReferenceRateIdentifier";
    /** Field name. */
    public static final String INITIAL_FLOATING_RATE_KEY = "initialFloatingRate";
    /** Field name. */
    public static final String SPREAD_KEY = "spread";
    /** Field name. */
    public static final String IS_IBOR_KEY = "isIBOR";

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, FloatingInterestRateLeg object) {
      final MutableFudgeMsg msg = serializer.newMessage();
      SwapLegBuilder.toFudgeMsg(serializer, object, msg);
      addToMessage(msg, FLOATING_REFERENCE_RATE_IDENTIFIER_KEY, ExternalIdBuilder.toFudgeMsg(serializer, object.getFloatingReferenceRateIdentifier()));
      addToMessage(msg, INITIAL_FLOATING_RATE_KEY, object.getInitialFloatingRate());
      addToMessage(msg, SPREAD_KEY, object.getSpread());
      addToMessage(msg, IS_IBOR_KEY, object.isIbor());
      return msg;
    }

    @Override
    public FloatingInterestRateLeg buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
      BeanBuilder<? extends FloatingInterestRateLeg> builder = FloatingInterestRateLeg.meta().builder();
      SwapLegBuilder.fromFudgeMsg(deserializer, msg, builder);
      builder.set(FloatingInterestRateLeg.meta().floatingReferenceRateIdentifier().name(), ExternalIdBuilder.fromFudgeMsg(deserializer, msg.getMessage(FLOATING_REFERENCE_RATE_IDENTIFIER_KEY)));
      builder.set(FloatingInterestRateLeg.meta().initialFloatingRate().name(), msg.getDouble(INITIAL_FLOATING_RATE_KEY));
      builder.set(FloatingInterestRateLeg.meta().spread().name(), msg.getDouble(SPREAD_KEY));
      builder.set(FloatingInterestRateLeg.meta().ibor().name(), msg.getBoolean(IS_IBOR_KEY));
      return builder.build();
    }
  }

}
