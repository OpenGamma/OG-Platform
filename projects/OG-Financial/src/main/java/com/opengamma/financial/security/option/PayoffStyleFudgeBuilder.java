/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryFudgeBuilder;
import com.opengamma.util.time.ZonedDateTimeFudgeBuilder;

/**
 * A Fudge builder for {@code PayoffStyle} implementations.
 */
public class PayoffStyleFudgeBuilder extends AbstractFudgeBuilder {

  //-------------------------------------------------------------------------
  /**
   * A Fudge builder for {@code AssetOrNothingPayoffStyle}.
   */
  @FudgeBuilderFor(AssetOrNothingPayoffStyle.class)
  public static class AssetOrNothingPayoffStyleBuilder extends PayoffStyleFudgeBuilder implements FudgeBuilder<AssetOrNothingPayoffStyle>  {

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, AssetOrNothingPayoffStyle object) {
      final MutableFudgeMsg msg = serializer.newMessage();
      return msg;
    }

    @Override
    public AssetOrNothingPayoffStyle buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
      return new AssetOrNothingPayoffStyle();
    }
  }

  //-------------------------------------------------------------------------
  /**
   * A Fudge builder for {@code AsymmetricPoweredPayoffStyle}.
   */
  @FudgeBuilderFor(AsymmetricPoweredPayoffStyle.class)
  public static class AsymmetricPoweredPayoffStyleBuilder extends PayoffStyleFudgeBuilder implements FudgeBuilder<AsymmetricPoweredPayoffStyle>  {
    /** Field name. */
    public static final String POWER_FIELD_NAME = "power";

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, AsymmetricPoweredPayoffStyle object) {
      final MutableFudgeMsg msg = serializer.newMessage();
      addToMessage(msg, POWER_FIELD_NAME, object.getPower());
      return msg;
    }

    @Override
    public AsymmetricPoweredPayoffStyle buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
      double power = msg.getDouble(POWER_FIELD_NAME);
      return new AsymmetricPoweredPayoffStyle(power);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * A Fudge builder for {@code BarrierPayoffStyle}.
   */
  @FudgeBuilderFor(BarrierPayoffStyle.class)
  public static class BarrierPayoffStyleBuilder extends PayoffStyleFudgeBuilder implements FudgeBuilder<BarrierPayoffStyle>  {

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, BarrierPayoffStyle object) {
      final MutableFudgeMsg msg = serializer.newMessage();
      return msg;
    }

    @Override
    public BarrierPayoffStyle buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
      return new BarrierPayoffStyle();
    }
  }

  //-------------------------------------------------------------------------
  /**
   * A Fudge builder for {@code CappedPoweredPayoffStyle}.
   */
  @FudgeBuilderFor(CappedPoweredPayoffStyle.class)
  public static class CappedPoweredPayoffStyleBuilder extends PayoffStyleFudgeBuilder implements FudgeBuilder<CappedPoweredPayoffStyle>  {
    /** Field name. */
    public static final String POWER_FIELD_NAME = "power";
    /** Field name. */
    public static final String CAP_FIELD_NAME = "cap";

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CappedPoweredPayoffStyle object) {
      final MutableFudgeMsg msg = serializer.newMessage();
      addToMessage(msg, POWER_FIELD_NAME, object.getPower());
      addToMessage(msg, CAP_FIELD_NAME, object.getCap());
      return msg;
    }

    @Override
    public CappedPoweredPayoffStyle buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
      double power = msg.getDouble(POWER_FIELD_NAME);
      double cap = msg.getDouble(CAP_FIELD_NAME);
      return new CappedPoweredPayoffStyle(power, cap);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * A Fudge builder for {@code CashOrNothingPayoffStyle}.
   */
  @FudgeBuilderFor(CashOrNothingPayoffStyle.class)
  public static class CashOrNothingPayoffStyleBuilder extends PayoffStyleFudgeBuilder implements FudgeBuilder<CashOrNothingPayoffStyle>  {
    /** Field name. */
    public static final String PAYMENT_FIELD_NAME = "payment";

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CashOrNothingPayoffStyle object) {
      final MutableFudgeMsg msg = serializer.newMessage();
      addToMessage(msg, PAYMENT_FIELD_NAME, object.getPayment());
      return msg;
    }

    @Override
    public CashOrNothingPayoffStyle buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
      double payment = msg.getDouble(PAYMENT_FIELD_NAME);
      return new CashOrNothingPayoffStyle(payment);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * A Fudge builder for {@code ExtremeSpreadPayoffStyle}.
   */
  @FudgeBuilderFor(ExtremeSpreadPayoffStyle.class)
  public static class ExtremeSpreadPayoffStyleBuilder extends PayoffStyleFudgeBuilder implements FudgeBuilder<ExtremeSpreadPayoffStyle>  {
    /** Field name. */
    public static final String PERIOD_END_FIELD_NAME = "periodEnd";
    /** Field name. */
    public static final String IS_REVERSE_FIELD_NAME = "isReverse";

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ExtremeSpreadPayoffStyle object) {
      final MutableFudgeMsg msg = serializer.newMessage();
      addToMessage(msg, PERIOD_END_FIELD_NAME, ZonedDateTimeFudgeBuilder.toFudgeMsg(serializer, object.getPeriodEnd()));
      addToMessage(msg, IS_REVERSE_FIELD_NAME, object.isReverse());
      return msg;
    }

    @Override
    public ExtremeSpreadPayoffStyle buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
      ZonedDateTime periodEnd = ZonedDateTimeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(PERIOD_END_FIELD_NAME));
      boolean reverse = msg.getBoolean(IS_REVERSE_FIELD_NAME);
      return new ExtremeSpreadPayoffStyle(periodEnd, reverse);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * A Fudge builder for {@code FadeInPayoffStyle}.
   */
  @FudgeBuilderFor(FadeInPayoffStyle.class)
  public static class FadeInPayoffStyleBuilder extends PayoffStyleFudgeBuilder implements FudgeBuilder<FadeInPayoffStyle>  {
    /** Field name. */
    public static final String LOWER_BOUND_FIELD_NAME = "lowerBound";
    /** Field name. */
    public static final String UPPER_BOUND_FIELD_NAME = "upperBound";

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, FadeInPayoffStyle object) {
      final MutableFudgeMsg msg = serializer.newMessage();
      addToMessage(msg, LOWER_BOUND_FIELD_NAME, object.getUpperBound());
      addToMessage(msg, UPPER_BOUND_FIELD_NAME, object.getLowerBound());
      return msg;
    }

    @Override
    public FadeInPayoffStyle buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
      double upperBound = msg.getDouble(LOWER_BOUND_FIELD_NAME);
      double lowerBound = msg.getDouble(UPPER_BOUND_FIELD_NAME);
      return new FadeInPayoffStyle(upperBound, lowerBound);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * A Fudge builder for {@code FixedStrikeLookbackPayoffStyle}.
   */
  @FudgeBuilderFor(FixedStrikeLookbackPayoffStyle.class)
  public static class FixedStrikeLookbackPayoffStyleBuilder extends PayoffStyleFudgeBuilder implements FudgeBuilder<FixedStrikeLookbackPayoffStyle>  {

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, FixedStrikeLookbackPayoffStyle object) {
      final MutableFudgeMsg msg = serializer.newMessage();
      return msg;
    }

    @Override
    public FixedStrikeLookbackPayoffStyle buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
      return new FixedStrikeLookbackPayoffStyle();
    }
  }

  //-------------------------------------------------------------------------
  /**
   * A Fudge builder for {@code FloatingStrikeLookbackPayoffStyle}.
   */
  @FudgeBuilderFor(FloatingStrikeLookbackPayoffStyle.class)
  public static class FloatingStrikeLookbackPayoffStyleBuilder extends PayoffStyleFudgeBuilder implements FudgeBuilder<FloatingStrikeLookbackPayoffStyle>  {

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, FloatingStrikeLookbackPayoffStyle object) {
      final MutableFudgeMsg msg = serializer.newMessage();
      return msg;
    }

    @Override
    public FloatingStrikeLookbackPayoffStyle buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
      return new FloatingStrikeLookbackPayoffStyle();
    }
  }

  //-------------------------------------------------------------------------
  /**
   * A Fudge builder for {@code GapPayoffStyle}.
   */
  @FudgeBuilderFor(GapPayoffStyle.class)
  public static class GapPayoffStyleBuilder extends PayoffStyleFudgeBuilder implements FudgeBuilder<GapPayoffStyle>  {
    /** Field name. */
    public static final String PAYMENT_FIELD_NAME = "payment";

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, GapPayoffStyle object) {
      final MutableFudgeMsg msg = serializer.newMessage();
      addToMessage(msg, PAYMENT_FIELD_NAME, object.getPayment());
      return msg;
    }

    @Override
    public GapPayoffStyle buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
      double payment = msg.getDouble(PAYMENT_FIELD_NAME);
      return new GapPayoffStyle(payment);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * A Fudge builder for {@code PoweredPayoffStyle}.
   */
  @FudgeBuilderFor(PoweredPayoffStyle.class)
  public static class PoweredPayoffStyleBuilder extends PayoffStyleFudgeBuilder implements FudgeBuilder<PoweredPayoffStyle>  {
    /** Field name. */
    public static final String POWER_FIELD_NAME = "power";

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, PoweredPayoffStyle object) {
      final MutableFudgeMsg msg = serializer.newMessage();
      addToMessage(msg, POWER_FIELD_NAME, object.getPower());
      return msg;
    }

    @Override
    public PoweredPayoffStyle buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
      double power = msg.getDouble(POWER_FIELD_NAME);
      return new PoweredPayoffStyle(power);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * A Fudge builder for {@code SimpleChooserPayoffStyle}.
   */
  @FudgeBuilderFor(SimpleChooserPayoffStyle.class)
  public static class SimpleChooserPayoffStyleBuilder extends PayoffStyleFudgeBuilder implements FudgeBuilder<SimpleChooserPayoffStyle>  {
    /** Field name. */
    public static final String CHOOSE_DATE_FIELD_NAME = "chooseDate";
    /** Field name. */
    public static final String UNDERLYING_STRIKE_FIELD_NAME = "underlyingStrike";
    /** Field name. */
    public static final String UNDERLYING_EXPIRY_FIELD_NAME = "underlyingExpiry";

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, SimpleChooserPayoffStyle object) {
      final MutableFudgeMsg msg = serializer.newMessage();
      addToMessage(msg, CHOOSE_DATE_FIELD_NAME, ZonedDateTimeFudgeBuilder.toFudgeMsg(serializer, object.getChooseDate()));
      addToMessage(msg, UNDERLYING_STRIKE_FIELD_NAME, object.getUnderlyingStrike());
      addToMessage(msg, UNDERLYING_EXPIRY_FIELD_NAME, ExpiryFudgeBuilder.toFudgeMsg(serializer, object.getUnderlyingExpiry()));
      return msg;
    }

    @Override
    public SimpleChooserPayoffStyle buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
      ZonedDateTime chooseDate = ZonedDateTimeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(CHOOSE_DATE_FIELD_NAME));
      double strike = msg.getDouble(UNDERLYING_STRIKE_FIELD_NAME);
      Expiry expiry = ExpiryFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(UNDERLYING_EXPIRY_FIELD_NAME));
      return new SimpleChooserPayoffStyle(chooseDate, strike, expiry);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * A Fudge builder for {@code SupersharePayoffStyle}.
   */
  @FudgeBuilderFor(SupersharePayoffStyle.class)
  public static class SupersharePayoffStyleBuilder extends PayoffStyleFudgeBuilder implements FudgeBuilder<SupersharePayoffStyle>  {
    /** Field name. */
    public static final String LOWER_BOUND_FIELD_NAME = "lowerBound";
    /** Field name. */
    public static final String UPPER_BOUND_FIELD_NAME = "upperBound";

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, SupersharePayoffStyle object) {
      final MutableFudgeMsg msg = serializer.newMessage();
      addToMessage(msg, LOWER_BOUND_FIELD_NAME, object.getUpperBound());
      addToMessage(msg, UPPER_BOUND_FIELD_NAME, object.getLowerBound());
      return msg;
    }

    @Override
    public SupersharePayoffStyle buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
      double upperBound = msg.getDouble(LOWER_BOUND_FIELD_NAME);
      double lowerBound = msg.getDouble(UPPER_BOUND_FIELD_NAME);
      return new SupersharePayoffStyle(upperBound, lowerBound);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * A Fudge builder for {@code VanillaPayoffStyle}.
   */
  @FudgeBuilderFor(VanillaPayoffStyle.class)
  public static class VanillaPayoffStyleBuilder extends PayoffStyleFudgeBuilder implements FudgeBuilder<VanillaPayoffStyle>  {

    @Override
    public MutableFudgeMsg buildMessage(FudgeSerializer serializer, VanillaPayoffStyle object) {
      final MutableFudgeMsg msg = serializer.newMessage();
      return msg;
    }

    @Override
    public VanillaPayoffStyle buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
      return new VanillaPayoffStyle();
    }
  }

}
