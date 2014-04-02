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

import com.opengamma.financial.analytics.ircurve.strips.BillNode;
import com.opengamma.financial.analytics.ircurve.strips.BondNode;
import com.opengamma.financial.analytics.ircurve.strips.CalendarSwapNode;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.ContinuouslyCompoundedRateNode;
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.financial.analytics.ircurve.strips.DeliverableSwapFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.DiscountFactorNode;
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.financial.analytics.ircurve.strips.FXForwardNode;
import com.opengamma.financial.analytics.ircurve.strips.InflationNodeType;
import com.opengamma.financial.analytics.ircurve.strips.PeriodicallyCompoundedRateNode;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.RollDateFRANode;
import com.opengamma.financial.analytics.ircurve.strips.RollDateSwapNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.analytics.ircurve.strips.ThreeLegBasisSwapNode;
import com.opengamma.financial.analytics.ircurve.strips.ZeroCouponInflationNode;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Contains fudge builders for curve nodes.
 */
/* package */ final class CurveNodeBuilders {
  /** The curve node id mapper field */
  private static final String CURVE_MAPPER_ID_FIELD = "curveNodeIdMapper";
  /** The curve name field */
  private static final String NAME_FIELD = "name";

  /**
   * Private constructor
   */
  private CurveNodeBuilders() {
  }

  /**
   * Fudge builder for {@link CurveNodeWithIdentifier}
   */
  @FudgeBuilderFor(CurveNodeWithIdentifier.class)
  public static class CurveNodeWithIdentifierBuilder implements FudgeBuilder<CurveNodeWithIdentifier> {
    /** The curve strip field */
    private static final String CURVE_STRIP_FIELD = "curveStrip";
    /** The id field */
    private static final String ID_FIELD = "id";
    /** The data field */
    private static final String DATA_FIELD = "dataField";
    /** The data type field */
    private static final String TYPE_FIELD = "dataType";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CurveNodeWithIdentifier object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      serializer.addToMessageWithClassHeaders(message, CURVE_STRIP_FIELD, null, object.getCurveNode());
      serializer.addToMessage(message, ID_FIELD, null, object.getIdentifier());
      serializer.addToMessage(message, DATA_FIELD, null, object.getDataField());
      serializer.addToMessage(message, TYPE_FIELD, null, object.getFieldType().toString());
      return message;
    }

    @Override
    public CurveNodeWithIdentifier buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final CurveNode curveStrip = (CurveNode) deserializer.fieldValueToObject(message.getByName(CURVE_STRIP_FIELD));
      final ExternalId id = deserializer.fieldValueToObject(ExternalId.class, message.getByName(ID_FIELD));
      final String dataField = message.getString(DATA_FIELD);
      final DataFieldType fieldType = DataFieldType.valueOf(message.getString(TYPE_FIELD));
      return new CurveNodeWithIdentifier(curveStrip, id, dataField, fieldType);
    }

  }

  /**
   * Fudge builder for {@link BillNode}
   */
  @FudgeBuilderFor(BillNode.class)
  public static class BillNodeBuilder implements FudgeBuilder<BillNode> {
    /** The tenor field */
    private static final String TENOR_FIELD = "tenor";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final BillNode object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(CURVE_MAPPER_ID_FIELD, object.getCurveNodeIdMapperName());
      message.add(TENOR_FIELD, object.getMaturityTenor().toFormattedString());
      if (object.getName() != null) {
        message.add(NAME_FIELD, object.getName());
      }
      return message;
    }

    @Override
    public BillNode buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String curveNodeIdMapperName = message.getString(CURVE_MAPPER_ID_FIELD);
      final Tenor tenor = Tenor.parse(message.getString(TENOR_FIELD));
      if (message.hasField(NAME_FIELD)) {
        final String name = message.getString(NAME_FIELD);
        return new BillNode(tenor, curveNodeIdMapperName, name);
      }
      return new BillNode(tenor, curveNodeIdMapperName);
    }
  }

  /**
   * Fudge builder for {@link BondNode}
   */
  @FudgeBuilderFor(BondNode.class)
  public static class BondNodeBuilder implements FudgeBuilder<BondNode> {
    /** The tenor field */
    private static final String TENOR_FIELD = "tenor";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final BondNode object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(CURVE_MAPPER_ID_FIELD, object.getCurveNodeIdMapperName());
      message.add(TENOR_FIELD, object.getMaturityTenor().toFormattedString());
      if (object.getName() != null) {
        message.add(NAME_FIELD, object.getName());
      }
      return message;
    }

    @Override
    public BondNode buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String curveNodeIdMapperName = message.getString(CURVE_MAPPER_ID_FIELD);
      final Tenor tenor = Tenor.parse(message.getString(TENOR_FIELD));
      if (message.hasField(NAME_FIELD)) {
        final String name = message.getString(NAME_FIELD);
        return new BondNode(tenor, curveNodeIdMapperName, name);
      }
      return new BondNode(tenor, curveNodeIdMapperName);
    }
  }

  /**
   * Fudge builder for {@link CashNode}
   */
  @FudgeBuilderFor(CashNode.class)
  public static class CashNodeBuilder implements FudgeBuilder<CashNode> {
    /** The start tenor field */
    private static final String START_TENOR_FIELD = "startTenor";
    /** The maturity tenor field */
    private static final String MATURITY_TENOR_FIELD = "maturityTenor";
    /** The convention field */
    private static final String CONVENTION_ID_FIELD = "convention";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CashNode object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(START_TENOR_FIELD, object.getStartTenor().toFormattedString());
      message.add(MATURITY_TENOR_FIELD, object.getMaturityTenor().toFormattedString());
      message.add(CONVENTION_ID_FIELD, object.getConvention());
      message.add(CURVE_MAPPER_ID_FIELD, object.getCurveNodeIdMapperName());
      if (object.getName() != null) {
        message.add(NAME_FIELD, object.getName());
      }
      return message;
    }

    @Override
    public CashNode buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Tenor startTenor = Tenor.parse(message.getString(START_TENOR_FIELD));
      final Tenor maturityTenor = Tenor.parse(message.getString(MATURITY_TENOR_FIELD));
      final ExternalId conventionId = deserializer.fieldValueToObject(ExternalId.class, message.getByName(CONVENTION_ID_FIELD));
      final String curveNodeIdMapperName = message.getString(CURVE_MAPPER_ID_FIELD);
      if (message.hasField(NAME_FIELD)) {
        final String name = message.getString(NAME_FIELD);
        return new CashNode(startTenor, maturityTenor, conventionId, curveNodeIdMapperName, name);
      }
      return new CashNode(startTenor, maturityTenor, conventionId, curveNodeIdMapperName);
    }
  }

  /**
   * Fudge builder for {@link ContinuouslyCompoundedRateNode}
   */
  @FudgeBuilderFor(ContinuouslyCompoundedRateNode.class)
  public static class ContinuouslyCompoundedRateNodeBuilder implements FudgeBuilder<ContinuouslyCompoundedRateNode> {
    /** The tenor field */
    private static final String TENOR_FIELD = "tenor";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ContinuouslyCompoundedRateNode object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(CURVE_MAPPER_ID_FIELD, object.getCurveNodeIdMapperName());
      message.add(TENOR_FIELD, object.getTenor());
      if (object.getName() != null) {
        message.add(NAME_FIELD, object.getName());
      }
      return message;
    }

    @Override
    public ContinuouslyCompoundedRateNode buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String curveNodeIdMapperName = message.getString(CURVE_MAPPER_ID_FIELD);
      //TODO should just use Tenor string for these objects
      final Tenor tenor = deserializer.fieldValueToObject(Tenor.class, message.getByName(TENOR_FIELD));
      if (message.hasField(NAME_FIELD)) {
        final String name = message.getString(NAME_FIELD);
        return new ContinuouslyCompoundedRateNode(curveNodeIdMapperName, tenor, name);
      }
      return new ContinuouslyCompoundedRateNode(curveNodeIdMapperName, tenor);
    }
  }

  /**
   * Fudge builder for {@link PeriodicallyCompoundedRateNode}
   */
  @FudgeBuilderFor(PeriodicallyCompoundedRateNode.class)
  public static class PeriodicallyCompoundedRateNodeBuilder implements FudgeBuilder<PeriodicallyCompoundedRateNode> {
    /** The tenor field */
    private static final String TENOR_FIELD = "tenor";
    /** The periods per year field */
    private static final String PERIODS_PER_YEAR_FIELD = "nPerYear";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final PeriodicallyCompoundedRateNode object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(CURVE_MAPPER_ID_FIELD, object.getCurveNodeIdMapperName());
      message.add(TENOR_FIELD, object.getTenor());
      message.add(PERIODS_PER_YEAR_FIELD, object.getCompoundingPeriodsPerYear());
      if (object.getName() != null) {
        message.add(NAME_FIELD, object.getName());
      }
      return message;
    }

    @Override
    public PeriodicallyCompoundedRateNode buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String curveNodeIdMapperName = message.getString(CURVE_MAPPER_ID_FIELD);
      //TODO should just use Tenor string for these objects
      final Tenor tenor = deserializer.fieldValueToObject(Tenor.class, message.getByName(TENOR_FIELD));
      final int periodsPerYear = message.getInt(PERIODS_PER_YEAR_FIELD);
      if (message.hasField(NAME_FIELD)) {
        final String name = message.getString(NAME_FIELD);
        return new PeriodicallyCompoundedRateNode(curveNodeIdMapperName, tenor, name, periodsPerYear);
      }
      return new PeriodicallyCompoundedRateNode(curveNodeIdMapperName, tenor, periodsPerYear);
    }
  }

  /**
   * Fudge builder for {@link CreditSpreadNode}
   */
  @FudgeBuilderFor(CreditSpreadNode.class)
  public static class CreditSpreadNodeBuilder implements FudgeBuilder<CreditSpreadNode> {
    /** The tenor field */
    private static final String TENOR_FIELD = "tenor";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CreditSpreadNode object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      //TODO should just use the Tenor string for Tenor
      message.add(CURVE_MAPPER_ID_FIELD, object.getCurveNodeIdMapperName());
      message.add(TENOR_FIELD, object.getTenor());
      if (object.getName() != null) {
        message.add(NAME_FIELD, object.getName());
      }
      return message;
    }

    @Override
    public CreditSpreadNode buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String curveNodeIdMapperName = message.getString(CURVE_MAPPER_ID_FIELD);
      final Tenor tenor = deserializer.fieldValueToObject(Tenor.class, message.getByName(TENOR_FIELD));
      if (message.hasField(NAME_FIELD)) {
        final String name = message.getString(NAME_FIELD);
        return new CreditSpreadNode(curveNodeIdMapperName, tenor, name);
      }
      return new CreditSpreadNode(curveNodeIdMapperName, tenor);
    }
  }

  /**
   * Fudge builder for {@link DeliverableSwapFutureNode}
   */
  @FudgeBuilderFor(DeliverableSwapFutureNode.class)
  public static class DeliverableSwapFutureNodeBuilder implements FudgeBuilder<DeliverableSwapFutureNode> {
    /** The number of the future from the start tenor field */
    private static final String FUTURE_NUMBER_FIELD = "futureNumber";
    /** The start tenor field */
    private static final String START_TENOR_FIELD = "startTenor";
    /** The future tenor field */
    private static final String FUTURE_TENOR_FIELD = "futureTenor";
    /** The tenor of the underlying rate field */
    private static final String UNDERLYING_TENOR_FIELD = "underlyingTenor";
    /** The future convention field */
    private static final String FUTURE_CONVENTION_FIELD = "futureConvention";
    /** The underlying convention field */
    private static final String UNDERLYING_CONVENTION_FIELD = "underlyingConvention";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final DeliverableSwapFutureNode object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(FUTURE_NUMBER_FIELD, object.getFutureNumber());
      message.add(START_TENOR_FIELD, object.getStartTenor().toFormattedString());
      message.add(FUTURE_TENOR_FIELD, object.getFutureTenor().toFormattedString());
      message.add(UNDERLYING_TENOR_FIELD, object.getUnderlyingTenor().toFormattedString());
      message.add(FUTURE_CONVENTION_FIELD, object.getFutureConvention());
      message.add(UNDERLYING_CONVENTION_FIELD, object.getSwapConvention());
      message.add(CURVE_MAPPER_ID_FIELD, object.getCurveNodeIdMapperName());
      if (object.getName() != null) {
        message.add(NAME_FIELD, object.getName());
      }
      return message;
    }

    @Override
    public DeliverableSwapFutureNode buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final int futureNumber = message.getInt(FUTURE_NUMBER_FIELD);
      final Tenor startTenor = Tenor.parse(message.getString(START_TENOR_FIELD));
      final Tenor futureTenor = Tenor.parse(message.getString(FUTURE_TENOR_FIELD));
      final Tenor underlyingTenor = Tenor.parse(message.getString(UNDERLYING_TENOR_FIELD));
      final ExternalId futureConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(FUTURE_CONVENTION_FIELD));
      final ExternalId swapConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(UNDERLYING_CONVENTION_FIELD));
      final String curveNodeIdMapperName = message.getString(CURVE_MAPPER_ID_FIELD);
      if (message.hasField(NAME_FIELD)) {
        final String name = message.getString(NAME_FIELD);
        return new DeliverableSwapFutureNode(futureNumber, startTenor, futureTenor, underlyingTenor, futureConvention, swapConvention, curveNodeIdMapperName, name);
      }
      return new DeliverableSwapFutureNode(futureNumber, startTenor, futureTenor, underlyingTenor, futureConvention, swapConvention, curveNodeIdMapperName);
    }

  }
  /**
   * Fudge builder for {@link DiscountFactorNode}
   */
  @FudgeBuilderFor(DiscountFactorNode.class)
  public static class DiscountFactorNodeBuilder implements FudgeBuilder<DiscountFactorNode> {
    /** The tenor field */
    private static final String TENOR_FIELD = "tenor";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final DiscountFactorNode object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(CURVE_MAPPER_ID_FIELD, object.getCurveNodeIdMapperName());
      message.add(TENOR_FIELD, object.getTenor());
      if (object.getName() != null) {
        message.add(NAME_FIELD, object.getName());
      }
      return message;
    }

    @Override
    public DiscountFactorNode buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String curveNodeIdMapperName = message.getString(CURVE_MAPPER_ID_FIELD);
      //TODO should just use the period string for Tenor
      final Tenor tenor = deserializer.fieldValueToObject(Tenor.class, message.getByName(TENOR_FIELD));
      if (message.hasField(NAME_FIELD)) {
        final String name = message.getString(NAME_FIELD);
        return new DiscountFactorNode(curveNodeIdMapperName, tenor, name);
      }
      return new DiscountFactorNode(curveNodeIdMapperName, tenor);
    }
  }

  /**
   * Fudge builder for {@link FRANode}
   */
  @FudgeBuilderFor(FRANode.class)
  public static class FRANodeBuilder implements FudgeBuilder<FRANode> {
    /** The fixing start field */
    private static final String FIXING_START_FIELD = "fixingStart";
    /** The fixing end field */
    private static final String FIXING_END_FIELD = "fixingEnd";
    /** The convention field */
    private static final String CONVENTION_ID_FIELD = "convention";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final FRANode object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(FIXING_START_FIELD, object.getFixingStart().toFormattedString());
      message.add(FIXING_END_FIELD, object.getFixingEnd().toFormattedString());
      message.add(CONVENTION_ID_FIELD, object.getConvention());
      message.add(CURVE_MAPPER_ID_FIELD, object.getCurveNodeIdMapperName());
      if (object.getName() != null) {
        message.add(NAME_FIELD, object.getName());
      }
      return message;
    }

    @Override
    public FRANode buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String curveNodeIdMapperName = message.getString(CURVE_MAPPER_ID_FIELD);
      final Tenor fixingStart = Tenor.parse(message.getString(FIXING_START_FIELD));
      final Tenor fixingEnd = Tenor.parse(message.getString(FIXING_END_FIELD));
      final ExternalId conventionId = deserializer.fieldValueToObject(ExternalId.class, message.getByName(CONVENTION_ID_FIELD));
      if (message.hasField(NAME_FIELD)) {
        final String name = message.getString(NAME_FIELD);
        return new FRANode(fixingStart, fixingEnd, conventionId, curveNodeIdMapperName, name);
      }
      return new FRANode(fixingStart, fixingEnd, conventionId, curveNodeIdMapperName);
    }

  }

  /**
   * Fudge builder for {@link FXForwardNode}
   */
  @FudgeBuilderFor(FXForwardNode.class)
  public static class FXForwardNodeBuilder implements FudgeBuilder<FXForwardNode> {
    /** The start tenor field */
    private static final String START_TENOR_FIELD = "startTenor";
    /** The maturity tenor field */
    private static final String MATURITY_TENOR_FIELD = "maturityTenor";
    /** The convention field */
    private static final String CONVENTION_FIELD = "convention";
    /** The pay currency field */
    private static final String PAY_CURRENCY_FIELD = "payCurrency";
    /** The receive currency field */
    private static final String RECEIVE_CURRENCY_FIELD = "receiveCurrency";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final FXForwardNode object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(START_TENOR_FIELD, object.getStartTenor().toFormattedString());
      message.add(MATURITY_TENOR_FIELD, object.getMaturityTenor().toFormattedString());
      message.add(CONVENTION_FIELD, object.getFxForwardConvention());
      message.add(PAY_CURRENCY_FIELD, object.getPayCurrency().getCode());
      message.add(RECEIVE_CURRENCY_FIELD, object.getReceiveCurrency().getCode());
      message.add(CURVE_MAPPER_ID_FIELD, object.getCurveNodeIdMapperName());
      if (object.getName() != null) {
        message.add(NAME_FIELD, object.getName());
      }
      return message;
    }

    @Override
    public FXForwardNode buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Tenor startTenor = Tenor.parse(message.getString(START_TENOR_FIELD));
      final Tenor maturityTenor = Tenor.parse(message.getString(MATURITY_TENOR_FIELD));
      final ExternalId fxForwardConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(CONVENTION_FIELD));
      final Currency payCurrency = Currency.of(message.getString(PAY_CURRENCY_FIELD));
      final Currency receiveCurrency = Currency.of(message.getString(RECEIVE_CURRENCY_FIELD));
      final String curveNodeIdMapperName = message.getString(CURVE_MAPPER_ID_FIELD);
      if (message.hasField(NAME_FIELD)) {
        final String name = message.getString(NAME_FIELD);
        return new FXForwardNode(startTenor, maturityTenor, fxForwardConvention, payCurrency, receiveCurrency, curveNodeIdMapperName, name);
      }
      return new FXForwardNode(startTenor, maturityTenor, fxForwardConvention, payCurrency, receiveCurrency, curveNodeIdMapperName);
    }

  }

  /**
   * Fudge builder for {@link RollDateFRANode}
   */
  @FudgeBuilderFor(RollDateFRANode.class)
  public static class RollDateFRANodeBuilder implements FudgeBuilder<RollDateFRANode> {
    /** The start tenor field */
    private static final String START_TENOR_FIELD = "startTenor";
    /** The IMM tenor field */
    private static final String INDEX_TENOR_FIELD = "indexTenor";
    /** The start IMM date number field */
    private static final String ROLL_DATE_START_NUMBER_FIELD = "rollDateStartNumber";
    /** The end IMM date number field */
    private static final String ROLL_DATE_END_NUMBER_FIELD = "rollDateEndNumber";
    /** The swap convention field */
    private static final String ROLL_DATE_FRA_CONVENTION_FIELD = "rollDateFRAConvention";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final RollDateFRANode object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(START_TENOR_FIELD, object.getStartTenor().toFormattedString());
      message.add(INDEX_TENOR_FIELD, object.getIndexTenor().toFormattedString());
      message.add(ROLL_DATE_START_NUMBER_FIELD, object.getRollDateStartNumber());
      message.add(ROLL_DATE_END_NUMBER_FIELD, object.getRollDateEndNumber());
      message.add(ROLL_DATE_FRA_CONVENTION_FIELD, object.getRollDateFRAConvention());
      message.add(CURVE_MAPPER_ID_FIELD, object.getCurveNodeIdMapperName());
      if (object.getName() != null) {
        message.add(NAME_FIELD, object.getName());
      }
      return message;
    }

    @Override
    public RollDateFRANode buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Tenor startTenor = Tenor.parse(message.getString(START_TENOR_FIELD));
      final Tenor immTenor = Tenor.parse(message.getString(INDEX_TENOR_FIELD));
      final int immDateStartNumber = message.getInt(ROLL_DATE_START_NUMBER_FIELD);
      final int immDateEndNumber = message.getInt(ROLL_DATE_END_NUMBER_FIELD);
      final ExternalId indexConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(ROLL_DATE_FRA_CONVENTION_FIELD));
      final String curveNodeIdMapperName = message.getString(CURVE_MAPPER_ID_FIELD);
      if (message.hasField(NAME_FIELD)) {
        final String name = message.getString(NAME_FIELD);
        return new RollDateFRANode(startTenor, immTenor, immDateStartNumber, immDateEndNumber, indexConvention, curveNodeIdMapperName, name);
      }
      return new RollDateFRANode(startTenor, immTenor, immDateStartNumber, immDateEndNumber, indexConvention, curveNodeIdMapperName);
    }

  }

  /**
   * Fudge builder for {@link RollDateSwapNode}
   */
  @FudgeBuilderFor(RollDateSwapNode.class)
  public static class RollDateSwapNodeBuilder implements FudgeBuilder<RollDateSwapNode> {
    /** The start tenor field */
    private static final String START_TENOR_FIELD = "startTenor";
    /** The start IMM date number field */
    private static final String START_IMM_DATE_NUMBER_FIELD = "rollDateStartNumber";
    /** The end IMM date number field */
    private static final String END_IMM_DATE_NUMBER_FIELD = "rollDateEndNumber";
    /** The swap convention field */
    private static final String IMM_SWAP_CONVENTION_FIELD = "rollDateSwapConvention";
    /** The use fixings field */
    private static final String USE_FIXINGS_FIELD = "useFixings";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final RollDateSwapNode object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(START_TENOR_FIELD, object.getStartTenor().toFormattedString());
      message.add(START_IMM_DATE_NUMBER_FIELD, object.getRollDateStartNumber());
      message.add(END_IMM_DATE_NUMBER_FIELD, object.getRollDateEndNumber());
      message.add(IMM_SWAP_CONVENTION_FIELD, object.getRollDateSwapConvention());
      message.add(CURVE_MAPPER_ID_FIELD, object.getCurveNodeIdMapperName());
      if (object.getName() != null) {
        message.add(NAME_FIELD, object.getName());
      }
      message.add(USE_FIXINGS_FIELD, object.isUseFixings());
      return message;
    }

    @Override
    public RollDateSwapNode buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Tenor startTenor = Tenor.parse(message.getString(START_TENOR_FIELD));
      final int immDateStartNumber = message.getInt(START_IMM_DATE_NUMBER_FIELD);
      final int immDateEndNumber = message.getInt(END_IMM_DATE_NUMBER_FIELD);
      final ExternalId swapConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(IMM_SWAP_CONVENTION_FIELD));
      final String curveNodeIdMapperName = message.getString(CURVE_MAPPER_ID_FIELD);
      if (message.hasField(NAME_FIELD)) {
        final String name = message.getString(NAME_FIELD);
        if (message.hasField(USE_FIXINGS_FIELD)) {
          final boolean useFixings = message.getBoolean(USE_FIXINGS_FIELD);
          return new RollDateSwapNode(startTenor, immDateStartNumber, immDateEndNumber, swapConvention, useFixings, curveNodeIdMapperName, name);
        }
        return new RollDateSwapNode(startTenor, immDateStartNumber, immDateEndNumber, swapConvention, curveNodeIdMapperName, name);
      }
      if (message.hasField(USE_FIXINGS_FIELD)) {
        final boolean useFixings = message.getBoolean(USE_FIXINGS_FIELD);
        return new RollDateSwapNode(startTenor, immDateStartNumber, immDateEndNumber, swapConvention, useFixings, curveNodeIdMapperName);
      }
      return new RollDateSwapNode(startTenor, immDateStartNumber, immDateEndNumber, swapConvention, curveNodeIdMapperName);
    }

  }

  /**
   * Fudge builder for {@link RateFutureNode}
   */
  @FudgeBuilderFor(RateFutureNode.class)
  public static class RateFutureNodeBuilder implements FudgeBuilder<RateFutureNode> {
    /** The number of the future from the start tenor field */
    private static final String FUTURE_NUMBER_FIELD = "futureNumber";
    /** The start tenor field */
    private static final String START_TENOR_FIELD = "startTenor";
    /** The future tenor field */
    private static final String FUTURE_TENOR_FIELD = "futureTenor";
    /** The tenor of the underlying rate field */
    private static final String UNDERLYING_TENOR_FIELD = "underlyingTenor";
    /** The future convention field */
    private static final String FUTURE_CONVENTION_FIELD = "futureConvention";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final RateFutureNode object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(FUTURE_NUMBER_FIELD, object.getFutureNumber());
      message.add(START_TENOR_FIELD, object.getStartTenor().toFormattedString());
      message.add(FUTURE_TENOR_FIELD, object.getFutureTenor().toFormattedString());
      message.add(UNDERLYING_TENOR_FIELD, object.getUnderlyingTenor().toFormattedString());
      message.add(FUTURE_CONVENTION_FIELD, object.getFutureConvention());
      message.add(CURVE_MAPPER_ID_FIELD, object.getCurveNodeIdMapperName());
      if (object.getName() != null) {
        message.add(NAME_FIELD, object.getName());
      }
      return message;
    }

    @Override
    public RateFutureNode buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final int futureNumber = message.getInt(FUTURE_NUMBER_FIELD);
      final Tenor startTenor = Tenor.parse(message.getString(START_TENOR_FIELD));
      final Tenor futureTenor = Tenor.parse(message.getString(FUTURE_TENOR_FIELD));
      final Tenor underlyingTenor = Tenor.parse(message.getString(UNDERLYING_TENOR_FIELD));
      final ExternalId futureConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(FUTURE_CONVENTION_FIELD));
      final String curveNodeIdMapperName = message.getString(CURVE_MAPPER_ID_FIELD);
      if (message.hasField(NAME_FIELD)) {
        final String name = message.getString(NAME_FIELD);
        return new RateFutureNode(futureNumber, startTenor, futureTenor, underlyingTenor, futureConvention, curveNodeIdMapperName, name);
      }
      return new RateFutureNode(futureNumber, startTenor, futureTenor, underlyingTenor, futureConvention, curveNodeIdMapperName);
    }

  }

  /**
   * Fudge builder for {@link SwapNode}
   */
  @FudgeBuilderFor(SwapNode.class)
  public static final class SwapNodeBuilder implements FudgeBuilder<SwapNode> {
    /** The start tenor field */
    private static final String START_TENOR_FIELD = "startTenor";
    /** The maturity tenor field */
    private static final String MATURITY_TENOR_FIELD = "maturityTenor";
    /** The pay leg convention field */
    private static final String PAY_LEG_CONVENTION_FIELD = "payLegConvention";
    /** The receive leg convention field */
    private static final String RECEIVE_LEG_CONVENTION_FIELD = "receiveLegConvention";
    /** The use fixings field */
    private static final String USE_FIXINGS_FIELD = "useFixings";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final SwapNode object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(START_TENOR_FIELD, object.getStartTenor().toFormattedString());
      message.add(MATURITY_TENOR_FIELD, object.getMaturityTenor().toFormattedString());
      message.add(PAY_LEG_CONVENTION_FIELD, object.getPayLegConvention());
      message.add(RECEIVE_LEG_CONVENTION_FIELD, object.getReceiveLegConvention());
      message.add(CURVE_MAPPER_ID_FIELD, object.getCurveNodeIdMapperName());
      if (object.getName() != null) {
        message.add(NAME_FIELD, object.getName());
      }
      message.add(USE_FIXINGS_FIELD, object.isUseFixings());
      return message;
    }

    @Override
    public SwapNode buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Tenor startTenor = Tenor.parse(message.getString(START_TENOR_FIELD));
      final Tenor maturityTenor = Tenor.parse(message.getString(MATURITY_TENOR_FIELD));
      final ExternalId payLegConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(PAY_LEG_CONVENTION_FIELD));
      final ExternalId receiveLegConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(RECEIVE_LEG_CONVENTION_FIELD));
      final String curveNodeIdMapperName = message.getString(CURVE_MAPPER_ID_FIELD);
      if (message.hasField(NAME_FIELD)) {
        final String name = message.getString(NAME_FIELD);
        if (message.hasField(USE_FIXINGS_FIELD)) {
          final boolean useFixings = message.getBoolean(USE_FIXINGS_FIELD);
          return new SwapNode(startTenor, maturityTenor, payLegConvention, receiveLegConvention, useFixings, curveNodeIdMapperName, name);
        }
        return new SwapNode(startTenor, maturityTenor, payLegConvention, receiveLegConvention, curveNodeIdMapperName, name);
      }
      if (message.hasField(USE_FIXINGS_FIELD)) {
        final boolean useFixings = message.getBoolean(USE_FIXINGS_FIELD);
        return new SwapNode(startTenor, maturityTenor, payLegConvention, receiveLegConvention, useFixings, curveNodeIdMapperName);
      }
      return new SwapNode(startTenor, maturityTenor, payLegConvention, receiveLegConvention, curveNodeIdMapperName);
    }

  }

  /**
   * Fudge builder for {@link ThreeLegBasisSwapNode}
   */
  @FudgeBuilderFor(ThreeLegBasisSwapNode.class)
  public static final class ThreeLegBasisSwapNodeBuilder implements FudgeBuilder<ThreeLegBasisSwapNode> {
    /** The start tenor field */
    private static final String START_TENOR_FIELD = "startTenor";
    /** The maturity tenor field */
    private static final String MATURITY_TENOR_FIELD = "maturityTenor";
    /** The pay leg convention field */
    private static final String PAY_LEG_CONVENTION_FIELD = "payLegConvention";
    /** The receive leg convention field */
    private static final String RECEIVE_LEG_CONVENTION_FIELD = "receiveLegConvention";
    /** The spread leg convention field */
    private static final String SPREAD_LEG_CONVENTION_FIELD = "spreadLegConvention";
    /** The use fixings field */
    private static final String USE_FIXINGS_FIELD = "useFixings";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ThreeLegBasisSwapNode object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(START_TENOR_FIELD, object.getStartTenor().toFormattedString());
      message.add(MATURITY_TENOR_FIELD, object.getMaturityTenor().toFormattedString());
      message.add(PAY_LEG_CONVENTION_FIELD, object.getPayLegConvention());
      message.add(RECEIVE_LEG_CONVENTION_FIELD, object.getReceiveLegConvention());
      message.add(SPREAD_LEG_CONVENTION_FIELD, object.getSpreadLegConvention());
      message.add(CURVE_MAPPER_ID_FIELD, object.getCurveNodeIdMapperName());
      if (object.getName() != null) {
        message.add(NAME_FIELD, object.getName());
      }
      message.add(USE_FIXINGS_FIELD, object.isUseFixings());
      return message;
    }

    @Override
    public ThreeLegBasisSwapNode buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Tenor startTenor = Tenor.parse(message.getString(START_TENOR_FIELD));
      final Tenor maturityTenor = Tenor.parse(message.getString(MATURITY_TENOR_FIELD));
      final ExternalId payLegConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(PAY_LEG_CONVENTION_FIELD));
      final ExternalId receiveLegConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(RECEIVE_LEG_CONVENTION_FIELD));
      final ExternalId spreadLegConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(SPREAD_LEG_CONVENTION_FIELD));
      final String curveNodeIdMapperName = message.getString(CURVE_MAPPER_ID_FIELD);
      if (message.hasField(NAME_FIELD)) {
        final String name = message.getString(NAME_FIELD);
        if (message.hasField(USE_FIXINGS_FIELD)) {
          final boolean useFixings = message.getBoolean(USE_FIXINGS_FIELD);
          return new ThreeLegBasisSwapNode(startTenor, maturityTenor, payLegConvention, receiveLegConvention, spreadLegConvention, useFixings, curveNodeIdMapperName, name);
        }
        return new ThreeLegBasisSwapNode(startTenor, maturityTenor, payLegConvention, receiveLegConvention, spreadLegConvention, curveNodeIdMapperName, name);
      }
      if (message.hasField(USE_FIXINGS_FIELD)) {
        final boolean useFixings = message.getBoolean(USE_FIXINGS_FIELD);
        return new ThreeLegBasisSwapNode(startTenor, maturityTenor, payLegConvention, receiveLegConvention, spreadLegConvention, useFixings, curveNodeIdMapperName);
      }
      return new ThreeLegBasisSwapNode(startTenor, maturityTenor, payLegConvention, receiveLegConvention, spreadLegConvention, curveNodeIdMapperName);
    }

  }

  /**
   * Fudge builder for {@link ZeroCouponInflationNode}
   */
  @FudgeBuilderFor(ZeroCouponInflationNode.class)
  public static final class ZeroCouponInflationNodeBuilder implements FudgeBuilder<ZeroCouponInflationNode> {

    /** The tenor field */
    private static final String TENOR_FIELD = "tenor";
    /** The inflation convention field */
    private static final String INFLATION_CONVENTION_FIELD = "inflationLegConvention";
    /** The fixed convention field */
    private static final String FIXED_CONVENTION_FIELD = "fixedLegConvention";
    /** The inflation node type field */
    private static final String INFLATION_NODE_TYPE_FIELD = "inflationNodeType";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ZeroCouponInflationNode object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(TENOR_FIELD, object.getTenor().toFormattedString());
      message.add(INFLATION_CONVENTION_FIELD, object.getInflationLegConvention());
      message.add(FIXED_CONVENTION_FIELD, object.getFixedLegConvention());
      message.add(INFLATION_NODE_TYPE_FIELD, object.getInflationNodeType().name());
      message.add(CURVE_MAPPER_ID_FIELD, object.getCurveNodeIdMapperName());
      if (object.getName() != null) {
        message.add(NAME_FIELD, object.getName());
      }
      return message;
    }

    @Override
    public ZeroCouponInflationNode buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Tenor tenor = Tenor.parse(message.getString(TENOR_FIELD));
      final ExternalId inflationConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(INFLATION_CONVENTION_FIELD));
      final ExternalId fixedConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(FIXED_CONVENTION_FIELD));
      final InflationNodeType inflationNodeType = InflationNodeType.valueOf(message.getString(INFLATION_NODE_TYPE_FIELD));
      final String curveNodeIdMapperName = message.getString(CURVE_MAPPER_ID_FIELD);
      if (message.hasField(NAME_FIELD)) {
        final String name = message.getString(NAME_FIELD);
        return new ZeroCouponInflationNode(tenor, inflationConvention, fixedConvention, inflationNodeType, curveNodeIdMapperName, name);
      }
      return new ZeroCouponInflationNode(tenor, inflationConvention, fixedConvention, inflationNodeType, curveNodeIdMapperName);
    }
  }

  /**
   * Fudge builder for {@link CalendarSwapNode}
   */
  @FudgeBuilderFor(CalendarSwapNode.class)
  public static final class CalendarSwapNodeBuilder implements FudgeBuilder<CalendarSwapNode> {

    /** The tenor field */
    private static final String START_TENOR_FIELD = "startTenor";
    /** The date set name field */
    private static final String DATESET_NAME = "dateSetName";
    /** The start date number field */
    private static final String START_DATE_NUMBER_FIELD = "startDateNumber";
    /** The end date number */
    private static final String END_DATE_NUMBER_FIELD = "endDateNumber";
    /** The swap convention field */
    private static final String SWAP_CONVENTION_FIELD = "swapConvention";
    /** use fixings field */
    private static final String USE_FIXINGS_FIELD = "useFixings";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CalendarSwapNode object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(START_TENOR_FIELD, object.getStartTenor().toFormattedString());
      message.add(DATESET_NAME, object.getDateSetName());
      message.add(START_DATE_NUMBER_FIELD, object.getStartDateNumber());
      message.add(END_DATE_NUMBER_FIELD, object.getEndDateNumber());
      message.add(CURVE_MAPPER_ID_FIELD, object.getCurveNodeIdMapperName());
      message.add(SWAP_CONVENTION_FIELD, object.getSwapConvention());
      message.add(USE_FIXINGS_FIELD, object.isUseFixings() ? "true" : "false");
      if (object.getName() != null) {
        message.add(NAME_FIELD, object.getName());
      }
      return message;
    }

    @Override
    public CalendarSwapNode buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Tenor tenor = Tenor.parse(message.getString(START_TENOR_FIELD));
      final ExternalId swapConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(SWAP_CONVENTION_FIELD));
      final String curveNodeIdMapperName = message.getString(CURVE_MAPPER_ID_FIELD);
      final String dateSetname = message.getString(DATESET_NAME);
      final int startDateNumber = message.getInt(START_DATE_NUMBER_FIELD);
      final int endDateNumber = message.getInt(END_DATE_NUMBER_FIELD);
      final Boolean useFixings = Boolean.valueOf(message.getString(USE_FIXINGS_FIELD));
      if (message.hasField(NAME_FIELD)) {
        final String name = message.getString(NAME_FIELD);
        return new CalendarSwapNode(dateSetname, tenor, startDateNumber, endDateNumber, swapConvention, useFixings, curveNodeIdMapperName, name);
      }
      return new CalendarSwapNode(dateSetname, tenor, startDateNumber, endDateNumber, swapConvention, useFixings, curveNodeIdMapperName);
    }

  }
}
