/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.swap;

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
import com.opengamma.id.ExternalIdFudgeBuilder;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * A Fudge builder for {@code SwapLeg} implementations.
 */
public class SwapLegFudgeBuilder extends AbstractFudgeBuilder {

  /** Field name. */
  public static final String DAY_COUNT_FIELD_NAME = "dayCount";
  /** Field name. */
  public static final String FREQUENCY_FIELD_NAME = "frequency";
  /** Field name. */
  public static final String REGION_IDENTIFIER_FIELD_NAME = "regionIdentifier";
  /** Field name. */
  public static final String BUSINESS_DAY_CONVENTION_FIELD_NAME = "businessDayConvention";
  /** Field name. */
  public static final String NOTIONAL_FIELD_NAME = "notional";

  public static void toFudgeMsg(FudgeSerializer serializer, SwapLeg object, final MutableFudgeMsg msg) {
    addToMessage(msg, DAY_COUNT_FIELD_NAME, object.getDayCount());
    addToMessage(msg, FREQUENCY_FIELD_NAME, object.getFrequency());
    addToMessage(msg, REGION_IDENTIFIER_FIELD_NAME, ExternalIdFudgeBuilder.toFudgeMsg(serializer, object.getRegionId()));
    addToMessage(msg, BUSINESS_DAY_CONVENTION_FIELD_NAME, object.getBusinessDayConvention());
    addToMessage(serializer, msg, NOTIONAL_FIELD_NAME, object.getNotional(), Notional.class);
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, BeanBuilder<? extends SwapLeg> object) {
    object.set(SwapLeg.meta().dayCount().name(), msg.getValue(DayCount.class, DAY_COUNT_FIELD_NAME));
    object.set(SwapLeg.meta().frequency().name(), msg.getValue(Frequency.class, FREQUENCY_FIELD_NAME));
    object.set(SwapLeg.meta().regionId().name(), ExternalIdFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(REGION_IDENTIFIER_FIELD_NAME)));
    object.set(SwapLeg.meta().businessDayConvention().name(), msg.getValue(BusinessDayConvention.class, BUSINESS_DAY_CONVENTION_FIELD_NAME));
    object.set(SwapLeg.meta().notional().name(), deserializer.fudgeMsgToObject(Notional.class, msg.getMessage(NOTIONAL_FIELD_NAME)));
  }

  //-------------------------------------------------------------------------
  /**
   * A Fudge builder for {@code FixedInterestRateLeg}.
   */
  @FudgeBuilderFor(FixedInterestRateLeg.class)
  public static class FixedInterestRateLegBuilder extends SwapLegFudgeBuilder implements FudgeBuilder<FixedInterestRateLeg>  {
    /** Field name. */
    public static final String RATE_FIELD_NAME = "rate";

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, FixedInterestRateLeg object) {
      final MutableFudgeMsg msg = serializer.newMessage();
      SwapLegFudgeBuilder.toFudgeMsg(serializer, object, msg);
      addToMessage(msg, RATE_FIELD_NAME, object.getRate());
      return msg;
    }

    @Override
    public FixedInterestRateLeg buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
      BeanBuilder<? extends FixedInterestRateLeg> builder = FixedInterestRateLeg.meta().builder();
      SwapLegFudgeBuilder.fromFudgeMsg(deserializer, msg, builder);
      builder.set(FixedInterestRateLeg.meta().rate().name(), msg.getDouble(RATE_FIELD_NAME));
      return builder.build();
    }
  }

  //-------------------------------------------------------------------------
  /**
   * A Fudge builder for {@code FloatingInterestRateLeg}.
   */
  @FudgeBuilderFor(FloatingInterestRateLeg.class)
  public static class FloatingInterestRateLegBuilder extends SwapLegFudgeBuilder implements FudgeBuilder<FloatingInterestRateLeg>  {
    /** Field name. */
    public static final String FLOATING_REFERENCE_RATE_IDENTIFIER_FIELD_NAME = "floatingReferenceRateIdentifier";
    /** Field name. */
    public static final String INITIAL_FLOATING_RATE_FIELD_NAME = "initialFloatingRate";
    /** Field name. */
    public static final String SPREAD_FIELD_NAME = "spread";
    /** Field name. */
    public static final String IS_IBOR_FIELD_NAME = "isIBOR";

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, FloatingInterestRateLeg object) {
      final MutableFudgeMsg msg = serializer.newMessage();
      SwapLegFudgeBuilder.toFudgeMsg(serializer, object, msg);
      addToMessage(msg, FLOATING_REFERENCE_RATE_IDENTIFIER_FIELD_NAME, ExternalIdFudgeBuilder.toFudgeMsg(serializer, object.getFloatingReferenceRateId()));
      addToMessage(msg, INITIAL_FLOATING_RATE_FIELD_NAME, object.getInitialFloatingRate());
      addToMessage(msg, SPREAD_FIELD_NAME, object.getSpread());
      addToMessage(msg, IS_IBOR_FIELD_NAME, object.isIbor());
      return msg;
    }

    @Override
    public FloatingInterestRateLeg buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
      BeanBuilder<? extends FloatingInterestRateLeg> builder = FloatingInterestRateLeg.meta().builder();
      SwapLegFudgeBuilder.fromFudgeMsg(deserializer, msg, builder);
      builder.set(FloatingInterestRateLeg.meta().floatingReferenceRateId().name(), ExternalIdFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(FLOATING_REFERENCE_RATE_IDENTIFIER_FIELD_NAME)));
      builder.set(FloatingInterestRateLeg.meta().initialFloatingRate().name(), msg.getDouble(INITIAL_FLOATING_RATE_FIELD_NAME));
      builder.set(FloatingInterestRateLeg.meta().spread().name(), msg.getDouble(SPREAD_FIELD_NAME));
      builder.set(FloatingInterestRateLeg.meta().ibor().name(), msg.getBoolean(IS_IBOR_FIELD_NAME));
      return builder.build();
    }
  }

}
