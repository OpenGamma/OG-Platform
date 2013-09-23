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
import org.joda.beans.PropertyDefinition;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.interestrate.CompoundingType;
import com.opengamma.financial.convention.CMSLegConvention;
import com.opengamma.financial.convention.CompoundingIborLegConvention;
import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.ConventionDocument;
import com.opengamma.financial.convention.DeliverablePriceQuotedSwapFutureConvention;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.FXForwardAndSwapConvention;
import com.opengamma.financial.convention.FXSpotConvention;
import com.opengamma.financial.convention.FederalFundsFutureConvention;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.InflationLegConvention;
import com.opengamma.financial.convention.InterestRateFutureConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.PriceIndexConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapConvention;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.SwapIndexConvention;
import com.opengamma.financial.convention.VanillaIborLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Fudge builders for {@link Convention} classes
 */
public final class ConventionBuilders {
  /** The name field */
  private static final String NAME_FIELD = "name";
  /** The external id bundle field */
  private static final String EXTERNAL_ID_BUNDLE_FIELD = "externalIdBundles";
  /** The unique id field */
  private static final String UNIQUE_ID_FIELD = "uniqueId";

  private ConventionBuilders() {
  }

  /**
   * Fudge builder for CMS leg conventions.
   */
  @FudgeBuilderFor(CMSLegConvention.class)
  public static class CMSLegConventionBuilder implements FudgeBuilder<CMSLegConvention> {
    /** The swap index convention field */
    private static final String SWAP_INDEX_ID_FIELD = "swapIndexConvention";
    /** The payment tenor field */
    private static final String PAYMENT_TENOR_FIELD = "paymentTenor";
    /** The advance fixing field */
    private static final String ADVANCE_FIXING_FIELD = "advanceFixing";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CMSLegConvention object) {
      final MutableFudgeMsg message = serializer.newMessage();
      FudgeSerializer.addClassHeader(message, CMSLegConvention.class);
      serializer.addToMessage(message, SWAP_INDEX_ID_FIELD, null, object.getSwapIndexConvention());
      message.add(PAYMENT_TENOR_FIELD, object.getPaymentTenor().getPeriod().toString());
      message.add(ADVANCE_FIXING_FIELD, object.isIsAdvanceFixing());
      message.add(NAME_FIELD, object.getName());
      serializer.addToMessage(message, EXTERNAL_ID_BUNDLE_FIELD, null, object.getExternalIdBundle());
      serializer.addToMessage(message, UNIQUE_ID_FIELD, null, object.getUniqueId());
      return message;
    }

    @Override
    public CMSLegConvention buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_FIELD);
      final ExternalIdBundle externalIdBundle = deserializer.fieldValueToObject(ExternalIdBundle.class, message.getByName(EXTERNAL_ID_BUNDLE_FIELD));
      final ExternalId swapIndexConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(SWAP_INDEX_ID_FIELD));
      final Tenor paymentTenor = Tenor.of(Period.parse(message.getString(PAYMENT_TENOR_FIELD)));
      final boolean isAdvanceFixing = message.getBoolean(ADVANCE_FIXING_FIELD);
      final UniqueId uniqueId = deserializer.fieldValueToObject(UniqueId.class, message.getByName(UNIQUE_ID_FIELD));
      final CMSLegConvention convention = new CMSLegConvention(name, externalIdBundle, swapIndexConvention, paymentTenor, isAdvanceFixing);
      convention.setUniqueId(uniqueId);
      return convention;
    }

  }

  /**
   * Fudge builder for compounding ibor leg conventions.
   */
  @FudgeBuilderFor(CompoundingIborLegConvention.class)
  public static class CompoundingIborLegConventionBuilder implements FudgeBuilder<CompoundingIborLegConvention> {
    /** The ibor index convention field */
    private static final String IBOR_INDEX_ID_FIELD = "iborIndexConvention";
    /** The payment tenor field */
    private static final String PAYMENT_TENOR_FIELD = "paymentTenor";
    /** The compounding type field */
    private static final String COMPOUNDING_TYPE_FIELD = "compoundingType";
    /** The compounding tenor field */
    private static final String COMPOSITION_TENOR_FIELD = "compositionTenor";
    /** The stub type for the compounding within one coupon field */
    private static final String STUB_TYPE_COMPOUND_FIELD = "stubTypeCompound";
    /** The exchange notional field */
    private static final String EXCHANGE_NOTIONAL_FIELD = "exchangeNotional";
    /** The settlement days field */
    private static final String SETTLEMENT_DAYS_FIELD = "settlementDays";
    /** The payment lag field */
    private static final String PAYMENT_LAG_FIELD = "paymentLag";
    /** The EOM field */
    private static final String IS_EOM_FIELD = "isEOM";
    /** The stub type for the coupons in the leg field */
    private static final String STUB_TYPE_LEG_FIELD = "stubTypeLeg";

    /**
     * The stub type.
     */
    @PropertyDefinition(validate = "notNull")
    private StubType _stubTypeLeg;

    /**
     * The payment lag in days.
     */
    @PropertyDefinition
    private int _paymentLag;

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CompoundingIborLegConvention object) {
      final MutableFudgeMsg message = serializer.newMessage();
      FudgeSerializer.addClassHeader(message, CompoundingIborLegConvention.class);
      serializer.addToMessage(message, IBOR_INDEX_ID_FIELD, null, object.getIborIndexConvention());
      message.add(PAYMENT_TENOR_FIELD, object.getPaymentTenor().getPeriod().toString());
      message.add(COMPOUNDING_TYPE_FIELD, object.getCompoundingType().name());
      message.add(STUB_TYPE_COMPOUND_FIELD, object.getStubTypeCompound().name());
      message.add(COMPOSITION_TENOR_FIELD, object.getCompositionTenor().getPeriod().toString());
      message.add(SETTLEMENT_DAYS_FIELD, object.getSettlementDays());
      message.add(IS_EOM_FIELD, object.isIsEOM());
      message.add(STUB_TYPE_LEG_FIELD, object.getStubTypeLeg().name());
      message.add(EXCHANGE_NOTIONAL_FIELD, object.isIsExchangeNotional());
      message.add(PAYMENT_LAG_FIELD, object.getPaymentLag());
      message.add(NAME_FIELD, object.getName());
      serializer.addToMessage(message, EXTERNAL_ID_BUNDLE_FIELD, null, object.getExternalIdBundle());
      serializer.addToMessage(message, UNIQUE_ID_FIELD, null, object.getUniqueId());
      return message;
    }

    @Override
    public CompoundingIborLegConvention buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_FIELD);
      final ExternalIdBundle externalIdBundle = deserializer.fieldValueToObject(ExternalIdBundle.class, message.getByName(EXTERNAL_ID_BUNDLE_FIELD));
      final ExternalId swapIndexConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(IBOR_INDEX_ID_FIELD));
      final Tenor paymentTenor = Tenor.of(Period.parse(message.getString(PAYMENT_TENOR_FIELD)));
      final CompoundingType compoundingType = CompoundingType.valueOf(message.getString(COMPOUNDING_TYPE_FIELD));
      final Tenor compositionTenor = Tenor.of(Period.parse(message.getString(COMPOSITION_TENOR_FIELD)));
      final StubType stubTypeCompound = StubType.valueOf(message.getString(STUB_TYPE_COMPOUND_FIELD));
      final int settlementDays = message.getInt(SETTLEMENT_DAYS_FIELD);
      final boolean isEOM = message.getBoolean(IS_EOM_FIELD);
      final StubType stubTypeLeg = StubType.valueOf(message.getString(STUB_TYPE_LEG_FIELD));
      final boolean exchangeNotional = message.getBoolean(EXCHANGE_NOTIONAL_FIELD);
      final int paymentLag = message.getInt(PAYMENT_LAG_FIELD);
      final CompoundingIborLegConvention convention = new CompoundingIborLegConvention(name, externalIdBundle, swapIndexConvention, paymentTenor, 
          compoundingType, compositionTenor, stubTypeCompound, settlementDays, isEOM, stubTypeLeg, exchangeNotional, paymentLag);
      final FudgeField uniqueIdMsg = message.getByName(UNIQUE_ID_FIELD);
      if (uniqueIdMsg != null) {
        convention.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueIdMsg));
      }
      return convention;
    }
  }

  /**
   * Fudge builder for deposit conventions.
   */
  @FudgeBuilderFor(DepositConvention.class)
  public static class DepositConventionBuilder implements FudgeBuilder<DepositConvention> {
    /** The day-count field */
    private static final String DAY_COUNT_FIELD = "dayCount";
    /** The business day convention field */
    private static final String BUSINESS_DAY_CONVENTION_FIELD = "businessDayConvention";
    /** The settlement days field */
    private static final String SETTLEMENT_DAYS_FIELD = "settlementDays";
    /** The EOM field */
    private static final String IS_EOM_FIELD = "isEOM";
    /** The currency field */
    private static final String CURRENCY_FIELD = "currency";
    /** The region field */
    private static final String REGION_FIELD = "region";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final DepositConvention object) {
      final MutableFudgeMsg message = serializer.newMessage();
      FudgeSerializer.addClassHeader(message, DepositConvention.class);
      message.add(DAY_COUNT_FIELD, object.getDayCount().getConventionName());
      message.add(BUSINESS_DAY_CONVENTION_FIELD, object.getBusinessDayConvention().getConventionName());
      message.add(SETTLEMENT_DAYS_FIELD, object.getSettlementDays());
      message.add(IS_EOM_FIELD, object.isIsEOM());
      message.add(CURRENCY_FIELD, object.getCurrency().getCode());
      serializer.addToMessage(message, REGION_FIELD, null, object.getRegionCalendar());
      message.add(NAME_FIELD, object.getName());
      serializer.addToMessage(message, EXTERNAL_ID_BUNDLE_FIELD, null, object.getExternalIdBundle());
      serializer.addToMessage(message, UNIQUE_ID_FIELD, null, object.getUniqueId());
      return message;
    }

    @Override
    public DepositConvention buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_FIELD);
      final ExternalIdBundle externalIdBundle = deserializer.fieldValueToObject(ExternalIdBundle.class, message.getByName(EXTERNAL_ID_BUNDLE_FIELD));
      final DayCount dayCount = DayCountFactory.INSTANCE.getDayCount(message.getString(DAY_COUNT_FIELD));
      final BusinessDayConvention businessDayConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention(message.getString(BUSINESS_DAY_CONVENTION_FIELD));
      final int settlementDays = message.getInt(SETTLEMENT_DAYS_FIELD);
      final boolean isEOM = message.getBoolean(IS_EOM_FIELD);
      final Currency currency = Currency.of(message.getString(CURRENCY_FIELD));
      final ExternalId regionCalendar = deserializer.fieldValueToObject(ExternalId.class, message.getByName(REGION_FIELD));
      final DepositConvention convention = new DepositConvention(name, externalIdBundle, dayCount, businessDayConvention, settlementDays, isEOM, currency, regionCalendar);
      final FudgeField uniqueIdMsg = message.getByName(UNIQUE_ID_FIELD);
      if (uniqueIdMsg != null) {
        convention.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueIdMsg));
      }
      return convention;
    }

  }

  /**
   * Fudge builder for FX forward and swap conventions.
   */
  @FudgeBuilderFor(FXForwardAndSwapConvention.class)
  public static class FXForwardAndSwapConventionBuilder implements FudgeBuilder<FXForwardAndSwapConvention> {
    /** The spot convention field */
    private static final String SPOT_CONVENTION_FIELD = "spotConvention";
    /** The business day convention field */
    private static final String BUSINESS_DAY_CONVENTION_FIELD = "businessDayConvention";
    /** The EOM field */
    private static final String IS_EOM_FIELD = "isEOM";
    /** The settlement region field */
    private static final String SETTLEMENT_REGION_FIELD = "settlementRegion";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final FXForwardAndSwapConvention object) {
      final MutableFudgeMsg message = serializer.newMessage();
      FudgeSerializer.addClassHeader(message, FXForwardAndSwapConvention.class);
      serializer.addToMessage(message, SPOT_CONVENTION_FIELD, null, object.getSpotConvention());
      message.add(BUSINESS_DAY_CONVENTION_FIELD, object.getBusinessDayConvention().getConventionName());
      message.add(IS_EOM_FIELD, object.isIsEOM());
      serializer.addToMessage(message, SETTLEMENT_REGION_FIELD, null, object.getSettlementRegion());
      message.add(NAME_FIELD, object.getName());
      serializer.addToMessage(message, EXTERNAL_ID_BUNDLE_FIELD, null, object.getExternalIdBundle());
      serializer.addToMessage(message, UNIQUE_ID_FIELD, null, object.getUniqueId());
      return message;
    }

    @Override
    public FXForwardAndSwapConvention buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_FIELD);
      final ExternalIdBundle externalIdBundle = deserializer.fieldValueToObject(ExternalIdBundle.class, message.getByName(EXTERNAL_ID_BUNDLE_FIELD));
      final ExternalId spotConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(SPOT_CONVENTION_FIELD));
      final BusinessDayConvention businessDayConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention(message.getString(BUSINESS_DAY_CONVENTION_FIELD));
      final boolean isEOM = message.getBoolean(IS_EOM_FIELD);
      final ExternalId settlementRegion = deserializer.fieldValueToObject(ExternalId.class, message.getByName(SETTLEMENT_REGION_FIELD));
      final FXForwardAndSwapConvention convention = new FXForwardAndSwapConvention(name, externalIdBundle, spotConvention, businessDayConvention, isEOM, settlementRegion);
      final FudgeField uniqueIdMsg = message.getByName(UNIQUE_ID_FIELD);
      if (uniqueIdMsg != null) {
        convention.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueIdMsg));
      }
      return convention;
    }
  }

  /**
   * Fudge builder for FX spot conventions.
   */
  @FudgeBuilderFor(FXSpotConvention.class)
  public static class FXSpotConventionBuilder implements FudgeBuilder<FXSpotConvention> {
    /** The settlement days field */
    private static final String SETTLEMENT_DAYS_FIELD = "settlementDays";
    /** The settlement region field */
    private static final String SETTLEMENT_REGION_FIELD = "settlementRegion";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final FXSpotConvention object) {
      final MutableFudgeMsg message = serializer.newMessage();
      FudgeSerializer.addClassHeader(message, FXSpotConvention.class);
      message.add(SETTLEMENT_DAYS_FIELD, object.getSettlementDays());
      if (object.getSettlementRegion() != null) {
        serializer.addToMessage(message, SETTLEMENT_REGION_FIELD, null, object.getSettlementRegion());
      }
      message.add(NAME_FIELD, object.getName());
      serializer.addToMessage(message, EXTERNAL_ID_BUNDLE_FIELD, null, object.getExternalIdBundle());
      serializer.addToMessage(message, UNIQUE_ID_FIELD, null, object.getUniqueId());
      return message;
    }

    @Override
    public FXSpotConvention buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_FIELD);
      final ExternalIdBundle externalIdBundle = deserializer.fieldValueToObject(ExternalIdBundle.class, message.getByName(EXTERNAL_ID_BUNDLE_FIELD));
      final int settlementDays = message.getInt(SETTLEMENT_DAYS_FIELD);
      ExternalId settlementRegion;
      if (message.hasField(SETTLEMENT_REGION_FIELD)) {
        settlementRegion = deserializer.fieldValueToObject(ExternalId.class, message.getByName(SETTLEMENT_REGION_FIELD));
      } else {
        settlementRegion = null;
      }
      final FXSpotConvention convention = new FXSpotConvention(name, externalIdBundle, settlementDays, settlementRegion);
      final FudgeField uniqueIdMsg = message.getByName(UNIQUE_ID_FIELD);
      if (uniqueIdMsg != null) {
        convention.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueIdMsg));
      }
      return convention;
    }
  }

  /**
   * Fudge builder for ibor index conventions.
   */
  @FudgeBuilderFor(IborIndexConvention.class)
  public static class IborIndexConventionBuilder implements FudgeBuilder<IborIndexConvention> {
    /** The day-count field */
    private static final String DAY_COUNT_FIELD = "dayCount";
    /** The business day convention field */
    private static final String BUSINESS_DAY_CONVENTION_FIELD = "businessDayConvention";
    /** The settlement days field */
    private static final String SETTLEMENT_DAYS_FIELD = "settlementDays";
    /** The EOM field */
    private static final String IS_EOM_FIELD = "isEOM";
    /** The currency field */
    private static final String CURRENCY_FIELD = "currency";
    /** The fixing time field */
    private static final String FIXING_TIME_FIELD = "fixingTime";
    /** The fixing time zone field */
    private static final String FIXING_TIME_ZONE_FIELD = "fixingTimeZone";
    /** The fixing calendar field */
    private static final String FIXING_CALENDAR_FIELD = "fixingCalendar";
    /** The region field */
    private static final String REGION_FIELD = "region";
    /** The fixing page field */
    private static final String FIXING_PAGE_FIELD = "fixingPage";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final IborIndexConvention object) {
      final MutableFudgeMsg message = serializer.newMessage();
      FudgeSerializer.addClassHeader(message, IborIndexConvention.class);
      message.add(DAY_COUNT_FIELD, object.getDayCount().getConventionName());
      message.add(BUSINESS_DAY_CONVENTION_FIELD, object.getBusinessDayConvention().getConventionName());
      message.add(SETTLEMENT_DAYS_FIELD, object.getSettlementDays());
      message.add(IS_EOM_FIELD, object.isIsEOM());
      message.add(CURRENCY_FIELD, object.getCurrency().getCode());
      message.add(FIXING_TIME_FIELD, object.getFixingTime().toString());
      message.add(FIXING_TIME_ZONE_FIELD, object.getFixingTimeZone());
      serializer.addToMessage(message, FIXING_CALENDAR_FIELD, null, object.getFixingCalendar());
      serializer.addToMessage(message, REGION_FIELD, null, object.getRegionCalendar());
      message.add(FIXING_PAGE_FIELD, object.getFixingPage());
      message.add(NAME_FIELD, object.getName());
      serializer.addToMessage(message, EXTERNAL_ID_BUNDLE_FIELD, null, object.getExternalIdBundle());
      serializer.addToMessage(message, UNIQUE_ID_FIELD, null, object.getUniqueId());
      return message;
    }

    @Override
    public IborIndexConvention buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_FIELD);
      final ExternalIdBundle externalIdBundle = deserializer.fieldValueToObject(ExternalIdBundle.class, message.getByName(EXTERNAL_ID_BUNDLE_FIELD));
      final DayCount dayCount = DayCountFactory.INSTANCE.getDayCount(message.getString(DAY_COUNT_FIELD));
      final BusinessDayConvention businessDayConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention(message.getString(BUSINESS_DAY_CONVENTION_FIELD));
      final int settlementDays = message.getInt(SETTLEMENT_DAYS_FIELD);
      final boolean isEOM = message.getBoolean(IS_EOM_FIELD);
      final Currency currency = Currency.of(message.getString(CURRENCY_FIELD));
      final LocalTime fixingTime = LocalTime.parse(message.getString(FIXING_TIME_FIELD));
      final String fixingTimeZone = message.getString(FIXING_TIME_ZONE_FIELD);
      final ExternalId fixingCalendar = deserializer.fieldValueToObject(ExternalId.class, message.getByName(FIXING_CALENDAR_FIELD));
      final ExternalId regionCalendar = deserializer.fieldValueToObject(ExternalId.class, message.getByName(REGION_FIELD));
      final String fixingPage = message.getString(FIXING_PAGE_FIELD);
      final IborIndexConvention convention = new IborIndexConvention(name, externalIdBundle, dayCount, businessDayConvention, settlementDays, isEOM, currency,
          fixingTime, fixingTimeZone, fixingCalendar, regionCalendar, fixingPage);
      final FudgeField uniqueIdMsg = message.getByName(UNIQUE_ID_FIELD);
      if (uniqueIdMsg != null) {
        convention.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueIdMsg));
      }
      return convention;
    }
  }

  /**
   * Fudge builder for exchange-traded interest rate futures.
   */
  @FudgeBuilderFor(InterestRateFutureConvention.class)
  public static class InterestRateFutureConventionBuilder implements FudgeBuilder<InterestRateFutureConvention> {
    /** The expiry convention field */
    private static final String EXPIRY_CONVENTION_FIELD = "expiryConvention";
    /** The exchange calendar field */
    private static final String EXCHANGE_CALENDAR_FIELD = "exchangeCalendar";
    /** The index convention field */
    private static final String INDEX_CONVENTION_FIELD = "indexConvention";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final InterestRateFutureConvention object) {
      final MutableFudgeMsg message = serializer.newMessage();
      FudgeSerializer.addClassHeader(message, InterestRateFutureConvention.class);
      serializer.addToMessage(message, EXPIRY_CONVENTION_FIELD, null, object.getExpiryConvention());
      serializer.addToMessage(message, EXCHANGE_CALENDAR_FIELD, null, object.getExchangeCalendar());
      serializer.addToMessage(message, INDEX_CONVENTION_FIELD, null, object.getIndexConvention());
      message.add(NAME_FIELD, object.getName());
      serializer.addToMessage(message, EXTERNAL_ID_BUNDLE_FIELD, null, object.getExternalIdBundle());
      serializer.addToMessage(message, UNIQUE_ID_FIELD, null, object.getUniqueId());
      return message;
    }

    @Override
    public InterestRateFutureConvention buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_FIELD);
      final ExternalIdBundle externalIdBundle = deserializer.fieldValueToObject(ExternalIdBundle.class, message.getByName(EXTERNAL_ID_BUNDLE_FIELD));
      final ExternalId expiryConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(EXPIRY_CONVENTION_FIELD));
      final ExternalId exchangeCalendar = deserializer.fieldValueToObject(ExternalId.class, message.getByName(EXCHANGE_CALENDAR_FIELD));
      final ExternalId indexConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(INDEX_CONVENTION_FIELD));
      final InterestRateFutureConvention convention = new InterestRateFutureConvention(name, externalIdBundle, expiryConvention, exchangeCalendar, indexConvention);
      final FudgeField uniqueIdMsg = message.getByName(UNIQUE_ID_FIELD);
      if (uniqueIdMsg != null) {
        convention.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueIdMsg));
      }
      return convention;
    }
  }

  /**
   * Fudge builder for Federal fund futures.
   */
  @FudgeBuilderFor(FederalFundsFutureConvention.class)
  public static class FederalFundsFutureConventionBuilder implements FudgeBuilder<FederalFundsFutureConvention> {
    /** The expiry convention field */
    private static final String EXPIRY_CONVENTION_FIELD = "expiryConvention";
    /** The exchange calendar field */
    private static final String EXCHANGE_CALENDAR_FIELD = "exchangeCalendar";
    /** The index convention field */
    private static final String INDEX_CONVENTION_FIELD = "indexConvention";
    /** The notional */
    private static final String NOTIONAL_FIELD = "notional";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final FederalFundsFutureConvention object) {
      final MutableFudgeMsg message = serializer.newMessage();
      FudgeSerializer.addClassHeader(message, FederalFundsFutureConvention.class);
      serializer.addToMessage(message, EXPIRY_CONVENTION_FIELD, null, object.getExpiryConvention());
      serializer.addToMessage(message, EXCHANGE_CALENDAR_FIELD, null, object.getExchangeCalendar());
      serializer.addToMessage(message, INDEX_CONVENTION_FIELD, null, object.getIndexConvention());
      serializer.addToMessage(message, NOTIONAL_FIELD, null, object.getNotional());
      message.add(NAME_FIELD, object.getName());
      serializer.addToMessage(message, EXTERNAL_ID_BUNDLE_FIELD, null, object.getExternalIdBundle());
      serializer.addToMessage(message, UNIQUE_ID_FIELD, null, object.getUniqueId());
      return message;
    }

    @Override
    public FederalFundsFutureConvention buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_FIELD);
      final ExternalIdBundle externalIdBundle = deserializer.fieldValueToObject(ExternalIdBundle.class, message.getByName(EXTERNAL_ID_BUNDLE_FIELD));
      final ExternalId expiryConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(EXPIRY_CONVENTION_FIELD));
      final ExternalId exchangeCalendar = deserializer.fieldValueToObject(ExternalId.class, message.getByName(EXCHANGE_CALENDAR_FIELD));
      final ExternalId indexConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(INDEX_CONVENTION_FIELD));
      final double notional = message.getDouble(NOTIONAL_FIELD);
      final FederalFundsFutureConvention convention = new FederalFundsFutureConvention(name, externalIdBundle, expiryConvention, exchangeCalendar, indexConvention, notional);
      final FudgeField uniqueIdMsg = message.getByName(UNIQUE_ID_FIELD);
      if (uniqueIdMsg != null) {
        convention.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueIdMsg));
      }
      return convention;
    }
  }

  /**
   * Fudge builder for Federal fund futures.
   */
  @FudgeBuilderFor(DeliverablePriceQuotedSwapFutureConvention.class)
  public static class DeliverablPriceQuotedSwapFutureConventionBuilder implements FudgeBuilder<DeliverablePriceQuotedSwapFutureConvention> {
    /** The expiry convention field */
    private static final String EXPIRY_CONVENTION_FIELD = "expiryConvention";
    /** The exchange calendar field */
    private static final String EXCHANGE_CALENDAR_FIELD = "exchangeCalendar";
    /** The index convention field */
    private static final String SWAP_CONVENTION_FIELD = "swapConvention";
    /** The notional */
    private static final String NOTIONAL_FIELD = "notional";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final DeliverablePriceQuotedSwapFutureConvention object) {
      final MutableFudgeMsg message = serializer.newMessage();
      FudgeSerializer.addClassHeader(message, DeliverablePriceQuotedSwapFutureConvention.class);
      serializer.addToMessage(message, EXPIRY_CONVENTION_FIELD, null, object.getExpiryConvention());
      serializer.addToMessage(message, EXCHANGE_CALENDAR_FIELD, null, object.getExchangeCalendar());
      serializer.addToMessage(message, SWAP_CONVENTION_FIELD, null, object.getSwapConvention());
      serializer.addToMessage(message, NOTIONAL_FIELD, null, object.getNotional());
      message.add(NAME_FIELD, object.getName());
      serializer.addToMessage(message, EXTERNAL_ID_BUNDLE_FIELD, null, object.getExternalIdBundle());
      serializer.addToMessage(message, UNIQUE_ID_FIELD, null, object.getUniqueId());
      return message;
    }

    @Override
    public DeliverablePriceQuotedSwapFutureConvention buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_FIELD);
      final ExternalIdBundle externalIdBundle = deserializer.fieldValueToObject(ExternalIdBundle.class, message.getByName(EXTERNAL_ID_BUNDLE_FIELD));
      final ExternalId expiryConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(EXPIRY_CONVENTION_FIELD));
      final ExternalId exchangeCalendar = deserializer.fieldValueToObject(ExternalId.class, message.getByName(EXCHANGE_CALENDAR_FIELD));
      final ExternalId swapConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(SWAP_CONVENTION_FIELD));
      final double notional = message.getDouble(NOTIONAL_FIELD);
      final DeliverablePriceQuotedSwapFutureConvention convention = new DeliverablePriceQuotedSwapFutureConvention(name, externalIdBundle,
          expiryConvention, exchangeCalendar, swapConvention, notional);
      final FudgeField uniqueIdMsg = message.getByName(UNIQUE_ID_FIELD);
      if (uniqueIdMsg != null) {
        convention.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueIdMsg));
      }
      return convention;
    }
  }

  /**
   * Fudge builder for OIS swap leg conventions.
   */
  @FudgeBuilderFor(OISLegConvention.class)
  public static class OISLegConventionBuilder implements FudgeBuilder<OISLegConvention> {
    /** The overnight index convention field */
    private static final String OVERNIGHT_INDEX_CONVENTION_FIELD = "oisIndexConvention";
    /** The payment tenor field */
    private static final String PAYMENT_TENOR_FIELD = "paymentTenor";
    /** The payment delay field */
    private static final String PAYMENT_LAG_FIELD = "paymentLag";
    /** The settlement days field */
    private static final String SETTLEMENT_DAYS_FIELD = "settlementDays";
    /** The business day convention field */
    private static final String BUSINESS_DAY_CONVENTION_FIELD = "businessDayConvention";
    /** The EOM field */
    private static final String IS_EOM_FIELD = "isEOM";
    /** The stub type field */
    private static final String STUB_TYPE_FIELD = "stubType";
    /** The exchange notional field */
    private static final String EXCHANGE_NOTIONAL_FIELD = "exchangeNotional";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final OISLegConvention object) {
      final MutableFudgeMsg message = serializer.newMessage();
      FudgeSerializer.addClassHeader(message, OISLegConvention.class);
      serializer.addToMessage(message, OVERNIGHT_INDEX_CONVENTION_FIELD, null, object.getOvernightIndexConvention());
      message.add(PAYMENT_TENOR_FIELD, object.getPaymentTenor().getPeriod().toString());
      message.add(PAYMENT_LAG_FIELD, object.getPaymentLag());
      message.add(SETTLEMENT_DAYS_FIELD, object.getSettlementDays());
      message.add(BUSINESS_DAY_CONVENTION_FIELD, object.getBusinessDayConvention().getConventionName());
      message.add(IS_EOM_FIELD, object.isIsEOM());
      message.add(STUB_TYPE_FIELD, object.getStubType().name());
      message.add(EXCHANGE_NOTIONAL_FIELD, object.isIsExchangeNotional());
      message.add(NAME_FIELD, object.getName());
      serializer.addToMessage(message, EXTERNAL_ID_BUNDLE_FIELD, null, object.getExternalIdBundle());
      serializer.addToMessage(message, UNIQUE_ID_FIELD, null, object.getUniqueId());
      return message;
    }

    @Override
    public OISLegConvention buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_FIELD);
      final ExternalIdBundle externalIdBundle = deserializer.fieldValueToObject(ExternalIdBundle.class, message.getByName(EXTERNAL_ID_BUNDLE_FIELD));
      final ExternalId overnightIndexConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(OVERNIGHT_INDEX_CONVENTION_FIELD));
      final Tenor paymentTenor = Tenor.of(Period.parse(message.getString(PAYMENT_TENOR_FIELD)));
      final int paymentLag = message.getInt(PAYMENT_LAG_FIELD);
      final int settlementDays = message.getInt(SETTLEMENT_DAYS_FIELD);
      final BusinessDayConvention businessDayConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention(message.getString(BUSINESS_DAY_CONVENTION_FIELD));
      final boolean isEOM = message.getBoolean(IS_EOM_FIELD);
      final StubType stubType = StubType.valueOf(message.getString(STUB_TYPE_FIELD));
      final boolean exchangeNotional = message.getBoolean(EXCHANGE_NOTIONAL_FIELD);
      final OISLegConvention convention = new OISLegConvention(name, externalIdBundle, overnightIndexConvention, paymentTenor,
          businessDayConvention, settlementDays, isEOM, stubType, exchangeNotional, paymentLag);
      final FudgeField uniqueIdMsg = message.getByName(UNIQUE_ID_FIELD);
      if (uniqueIdMsg != null) {
        convention.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueIdMsg));
      }
      return convention;
    }
  }

  /**
   * Fudge builder for OIS swap leg conventions.
   */
  @FudgeBuilderFor(OvernightIndexConvention.class)
  public static class OvernightIndexConventionBuilder implements FudgeBuilder<OvernightIndexConvention> {
    /** The day-count field */
    private static final String DAY_COUNT_FIELD = "dayCount";
    /** The publication lag field */
    private static final String PUBLICATION_LAG_FIELD = "publicationLag";
    /** The currency field */
    private static final String CURRENCY_FIELD = "currency";
    /** The region field */
    private static final String REGION_FIELD = "region";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final OvernightIndexConvention object) {
      final MutableFudgeMsg message = serializer.newMessage();
      FudgeSerializer.addClassHeader(message, OvernightIndexConvention.class);
      message.add(DAY_COUNT_FIELD, object.getDayCount());
      message.add(PUBLICATION_LAG_FIELD, object.getPublicationLag());
      message.add(CURRENCY_FIELD, object.getCurrency().getCode());
      serializer.addToMessage(message, REGION_FIELD, null, object.getRegionCalendar());
      message.add(NAME_FIELD, object.getName());
      serializer.addToMessage(message, EXTERNAL_ID_BUNDLE_FIELD, null, object.getExternalIdBundle());
      serializer.addToMessage(message, UNIQUE_ID_FIELD, null, object.getUniqueId());
      return message;
    }

    @Override
    public OvernightIndexConvention buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_FIELD);
      final ExternalIdBundle externalIdBundle = deserializer.fieldValueToObject(ExternalIdBundle.class, message.getByName(EXTERNAL_ID_BUNDLE_FIELD));
      final DayCount dayCount = DayCountFactory.INSTANCE.getDayCount(message.getString(DAY_COUNT_FIELD));
      final int publicationLag = message.getInt(PUBLICATION_LAG_FIELD);
      final Currency currency = Currency.of(message.getString(CURRENCY_FIELD));
      final ExternalId region = deserializer.fieldValueToObject(ExternalId.class, message.getByName(REGION_FIELD));
      final OvernightIndexConvention convention = new OvernightIndexConvention(name, externalIdBundle, dayCount, publicationLag, currency, region);
      final FudgeField uniqueIdMsg = message.getByName(UNIQUE_ID_FIELD);
      if (uniqueIdMsg != null) {
        convention.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueIdMsg));
      }
      return convention;
    }
  }

  /**
   * Fudge builder for price index conventions.
   */
  @FudgeBuilderFor(PriceIndexConvention.class)
  public static class PriceIndexConventionBuilder implements FudgeBuilder<PriceIndexConvention> {
    /** The currency field */
    private static final String CURRENCY_FIELD = "currency";
    /** The region field */
    private static final String REGION_FIELD = "region";
    /** The price index id field */
    private static final String PRICE_INDEX_ID_FIELD = "priceIndexId";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final PriceIndexConvention object) {
      final MutableFudgeMsg message = serializer.newMessage();
      FudgeSerializer.addClassHeader(message, PriceIndexConvention.class);
      message.add(CURRENCY_FIELD, object.getCurrency().getCode());
      serializer.addToMessage(message, REGION_FIELD, null, object.getRegion());
      serializer.addToMessage(message, PRICE_INDEX_ID_FIELD, null, object.getPriceIndexId());
      message.add(NAME_FIELD, object.getName());
      serializer.addToMessage(message, EXTERNAL_ID_BUNDLE_FIELD, null, object.getExternalIdBundle());
      serializer.addToMessage(message, UNIQUE_ID_FIELD, null, object.getUniqueId());
      return message;
    }

    @Override
    public PriceIndexConvention buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_FIELD);
      final ExternalIdBundle externalIdBundle = deserializer.fieldValueToObject(ExternalIdBundle.class, message.getByName(EXTERNAL_ID_BUNDLE_FIELD));
      final Currency currency = Currency.of(message.getString(CURRENCY_FIELD));
      final ExternalId region = deserializer.fieldValueToObject(ExternalId.class, message.getByName(REGION_FIELD));
      final ExternalId priceIndexId = deserializer.fieldValueToObject(ExternalId.class, message.getByName(PRICE_INDEX_ID_FIELD));
      final PriceIndexConvention convention = new PriceIndexConvention(name, externalIdBundle, currency, region, priceIndexId);
      final FudgeField uniqueIdMsg = message.getByName(UNIQUE_ID_FIELD);
      if (uniqueIdMsg != null) {
        convention.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueIdMsg));
      }
      return convention;
    }

  }

  /**
   * Fudge builder for swap conventions.
   */
  @FudgeBuilderFor(SwapConvention.class)
  public static class SwapConventionBuilder implements FudgeBuilder<SwapConvention> {
    /** The pay leg field */
    private static final String PAY_LEG_FIELD = "payLeg";
    /** The receive leg field */
    private static final String RECEIVE_LEG_FIELD = "receiveLeg";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final SwapConvention object) {
      final MutableFudgeMsg message = serializer.newMessage();
      FudgeSerializer.addClassHeader(message, SwapConvention.class);
      serializer.addToMessage(message, PAY_LEG_FIELD, null, object.getPayLegConvention());
      serializer.addToMessage(message, RECEIVE_LEG_FIELD, null, object.getReceiveLegConvention());
      message.add(NAME_FIELD, object.getName());
      serializer.addToMessage(message, EXTERNAL_ID_BUNDLE_FIELD, null, object.getExternalIdBundle());
      serializer.addToMessage(message, UNIQUE_ID_FIELD, null, object.getUniqueId());
      return message;
    }

    @Override
    public SwapConvention buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_FIELD);
      final ExternalIdBundle externalIdBundle = deserializer.fieldValueToObject(ExternalIdBundle.class, message.getByName(EXTERNAL_ID_BUNDLE_FIELD));
      final ExternalId payLegConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(PAY_LEG_FIELD));
      final ExternalId receiveLegConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(RECEIVE_LEG_FIELD));
      final SwapConvention convention = new SwapConvention(name, externalIdBundle, payLegConvention, receiveLegConvention);
      final FudgeField uniqueIdMsg = message.getByName(UNIQUE_ID_FIELD);
      if (uniqueIdMsg != null) {
        convention.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueIdMsg));
      }
      return convention;
    }
  }

  /**
   * Fudge builder for swap fixed leg conventions.
   */
  @FudgeBuilderFor(SwapFixedLegConvention.class)
  public static class SwapFixedLegConventionBuilder implements FudgeBuilder<SwapFixedLegConvention> {
    /** The payment tenor field */
    private static final String PAYMENT_TENOR = "paymentTenor";
    /** The day-count field */
    private static final String DAY_COUNT_FIELD = "dayCount";
    /** The business day convention field */
    private static final String BUSINESS_DAY_CONVENTION_FIELD = "businessDayConvention";
    /** The settlement days field */
    private static final String SETTLEMENT_DAYS_FIELD = "settlementDays";
    /** The EOM field */
    private static final String IS_EOM_FIELD = "isEOM";
    /** The currency field */
    private static final String CURRENCY_FIELD = "currency";
    /** The region field */
    private static final String REGION_FIELD = "region";
    /** The stub type field */
    private static final String STUB_TYPE_FIELD = "stubType";
    /** The exchange notional field */
    private static final String EXCHANGE_NOTIONAL_FIELD = "exchangeNotional";
    /** The payment lag field */
    private static final String PAYMENT_LAG_FIELD = "paymentLag";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final SwapFixedLegConvention object) {
      final MutableFudgeMsg message = serializer.newMessage();
      FudgeSerializer.addClassHeader(message, SwapFixedLegConvention.class);
      message.add(PAYMENT_TENOR, object.getPaymentTenor().getPeriod().toString());
      message.add(DAY_COUNT_FIELD, object.getDayCount().getConventionName());
      message.add(BUSINESS_DAY_CONVENTION_FIELD, object.getBusinessDayConvention().getConventionName());
      message.add(SETTLEMENT_DAYS_FIELD, object.getSettlementDays());
      message.add(IS_EOM_FIELD, object.isIsEOM());
      message.add(CURRENCY_FIELD, object.getCurrency().getCode());
      serializer.addToMessage(message, REGION_FIELD, null, object.getRegionCalendar());
      message.add(STUB_TYPE_FIELD, object.getStubType().name());
      message.add(EXCHANGE_NOTIONAL_FIELD, object.isIsExchangeNotional());
      message.add(PAYMENT_LAG_FIELD, object.getPaymentLag());
      message.add(NAME_FIELD, object.getName());
      serializer.addToMessage(message, EXTERNAL_ID_BUNDLE_FIELD, null, object.getExternalIdBundle());
      serializer.addToMessage(message, UNIQUE_ID_FIELD, null, object.getUniqueId());
      return message;
    }

    @Override
    public SwapFixedLegConvention buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_FIELD);
      final ExternalIdBundle externalIdBundle = deserializer.fieldValueToObject(ExternalIdBundle.class, message.getByName(EXTERNAL_ID_BUNDLE_FIELD));
      final Tenor paymentTenor = Tenor.of(Period.parse(message.getString(PAYMENT_TENOR)));
      final DayCount dayCount = DayCountFactory.INSTANCE.getDayCount(message.getString(DAY_COUNT_FIELD));
      final BusinessDayConvention businessDayConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention(message.getString(BUSINESS_DAY_CONVENTION_FIELD));
      final int settlementDays = message.getInt(SETTLEMENT_DAYS_FIELD);
      final boolean isEOM = message.getBoolean(IS_EOM_FIELD);
      final Currency currency = Currency.of(message.getString(CURRENCY_FIELD));
      final ExternalId regionCalendar = deserializer.fieldValueToObject(ExternalId.class, message.getByName(REGION_FIELD));
      final StubType stubType = StubType.valueOf(message.getString(STUB_TYPE_FIELD));
      final boolean exchangeNotional = message.getBoolean(EXCHANGE_NOTIONAL_FIELD);
      final int paymentLag = message.getInt(PAYMENT_LAG_FIELD);
      final SwapFixedLegConvention convention = new SwapFixedLegConvention(name, externalIdBundle, paymentTenor, dayCount, businessDayConvention, currency,
          regionCalendar, settlementDays, isEOM, stubType, exchangeNotional, paymentLag);
      final FudgeField uniqueIdMsg = message.getByName(UNIQUE_ID_FIELD);
      if (uniqueIdMsg != null) {
        convention.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueIdMsg));
      }
      return convention;
    }
  }

  /**
   * Fudge builder for swap index conventions.
   */
  @FudgeBuilderFor(SwapIndexConvention.class)
  public static class SwapIndexConventionBuilder implements FudgeBuilder<SwapIndexConvention> {
    /** The fixing time field */
    private static final String FIXING_TIME_FIELD = "fixingTime";
    /** The swap convention field */
    private static final String SWAP_CONVENTION_FIELD = "swapConvention";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final SwapIndexConvention object) {
      final MutableFudgeMsg message = serializer.newMessage();
      FudgeSerializer.addClassHeader(message, SwapIndexConvention.class);
      message.add(FIXING_TIME_FIELD, object.getFixingTime().toString());
      serializer.addToMessage(message, SWAP_CONVENTION_FIELD, null, object.getSwapConvention());
      message.add(NAME_FIELD, object.getName());
      serializer.addToMessage(message, EXTERNAL_ID_BUNDLE_FIELD, null, object.getExternalIdBundle());
      serializer.addToMessage(message, UNIQUE_ID_FIELD, null, object.getUniqueId());
      return message;
    }

    @Override
    public SwapIndexConvention buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_FIELD);
      final ExternalIdBundle externalIdBundle = deserializer.fieldValueToObject(ExternalIdBundle.class, message.getByName(EXTERNAL_ID_BUNDLE_FIELD));
      final LocalTime fixingTime = LocalTime.parse(message.getString(FIXING_TIME_FIELD));
      final ExternalId receiveLegConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(SWAP_CONVENTION_FIELD));
      final SwapIndexConvention convention = new SwapIndexConvention(name, externalIdBundle, fixingTime, receiveLegConvention);
      final FudgeField uniqueIdMsg = message.getByName(UNIQUE_ID_FIELD);
      if (uniqueIdMsg != null) {
        convention.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueIdMsg));
      }
      return convention;
    }
  }

  /**
   * Fudge builder for vanilla ibor leg conventions.
   */
  @FudgeBuilderFor(VanillaIborLegConvention.class)
  public static class VanillaIborLegConventionBuilder implements FudgeBuilder<VanillaIborLegConvention> {
    /** The ibor index convention field */
    private static final String IBOR_INDEX_CONVENTION_FIELD = "iborIndexConvention";
    /** The advance fixing field */
    private static final String ADVANCE_FIXING_FIELD = "advanceFixing";
    /** The stub type field */
    private static final String STUB_TYPE_FIELD = "stubType";
    /** The interpolator name field */
    private static final String INTERPOLATOR_NAME_FIELD = "interpolatorName";
    /** The reset tenor field */
    private static final String RESET_TENOR_FIELD = "resetTenor";
    /** The EOM field */
    private static final String IS_EOM_FIELD = "isEOM";
    /** The stub type field */
    private static final String SETTLEMENT_DAYS_FIELD = "settlementDays";
    /** The exchange notional field */
    private static final String EXCHANGE_NOTIONAL_FIELD = "exchangeNotional";
    /** The payment lag field */
    private static final String PAYMENT_LAG_FIELD = "paymentLag";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final VanillaIborLegConvention object) {
      final MutableFudgeMsg message = serializer.newMessage();
      FudgeSerializer.addClassHeader(message, VanillaIborLegConvention.class);
      serializer.addToMessage(message, IBOR_INDEX_CONVENTION_FIELD, null, object.getIborIndexConvention());
      message.add(ADVANCE_FIXING_FIELD, object.isIsAdvanceFixing());
      message.add(STUB_TYPE_FIELD, object.getStubType().name());
      message.add(INTERPOLATOR_NAME_FIELD, object.getInterpolationMethod());
      message.add(NAME_FIELD, object.getName());
      message.add(RESET_TENOR_FIELD, object.getResetTenor().getPeriod().toString());
      message.add(IS_EOM_FIELD, object.isIsEOM());
      message.add(SETTLEMENT_DAYS_FIELD, object.getSettlementDays());
      message.add(EXCHANGE_NOTIONAL_FIELD, object.isIsExchangeNotional());
      message.add(PAYMENT_LAG_FIELD, object.getPaymentLag());
      serializer.addToMessage(message, EXTERNAL_ID_BUNDLE_FIELD, null, object.getExternalIdBundle());
      serializer.addToMessage(message, UNIQUE_ID_FIELD, null, object.getUniqueId());
      return message;
    }

    @Override
    public VanillaIborLegConvention buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_FIELD);
      final ExternalIdBundle externalIdBundle = deserializer.fieldValueToObject(ExternalIdBundle.class, message.getByName(EXTERNAL_ID_BUNDLE_FIELD));
      final ExternalId iborIndexConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(IBOR_INDEX_CONVENTION_FIELD));
      final boolean isAdvanceFixing = message.getBoolean(ADVANCE_FIXING_FIELD);
      final StubType stubType = StubType.valueOf(message.getString(STUB_TYPE_FIELD));
      final String interpolatorName = message.getString(INTERPOLATOR_NAME_FIELD);
      final Tenor resetTenor = Tenor.of(Period.parse(message.getString(RESET_TENOR_FIELD)));
      final int settlementDays = message.getInt(SETTLEMENT_DAYS_FIELD);
      final boolean isEOM = message.getBoolean(IS_EOM_FIELD);
      final boolean exchangeNotional = message.getBoolean(EXCHANGE_NOTIONAL_FIELD);
      final int paymentLag = message.getInt(PAYMENT_LAG_FIELD);
      final VanillaIborLegConvention convention = new VanillaIborLegConvention(name, externalIdBundle, iborIndexConvention, isAdvanceFixing, interpolatorName,
          resetTenor, settlementDays, isEOM, stubType, exchangeNotional, paymentLag);
      final FudgeField uniqueIdMsg = message.getByName(UNIQUE_ID_FIELD);
      if (uniqueIdMsg != null) {
        convention.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueIdMsg));
      }
      return convention;
    }
  }

  /**
   * Fudge builder for inflation leg conventions.
   */
  @FudgeBuilderFor(InflationLegConvention.class)
  public static class InflationLegConventionBuilder implements FudgeBuilder<InflationLegConvention> {
    /** The price index field */
    private static final String PRICE_INDEX_FIELD = "priceIndexConvention";
    /** The business day convention field */
    private static final String BUSINESS_DAY_CONVENTION_FIELD = "businessDayConvention";
    /** The day-count field */
    private static final String DAYCOUNT_FIELD = "dayCount";
    /** The EOM field */
    private static final String IS_EOM_FIELD = "isEOM";
    /** The month lag field */
    private static final String MONTH_LAG_FIELD = "monthLag";
    /** The spot lag field */
    private static final String SPOT_LAG_FIELD = "spotLag";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final InflationLegConvention object) {
      final MutableFudgeMsg message = serializer.newMessage();
      FudgeSerializer.addClassHeader(message, InflationLegConvention.class);
      serializer.addToMessage(message, PRICE_INDEX_FIELD, null, object.getPriceIndexConvention());
      message.add(BUSINESS_DAY_CONVENTION_FIELD, object.getBusinessDayConvention().getConventionName());
      message.add(DAYCOUNT_FIELD, object.getDayCount().getConventionName());
      message.add(IS_EOM_FIELD, object.isIsEOM());
      message.add(MONTH_LAG_FIELD, object.getMonthLag());
      message.add(SPOT_LAG_FIELD, object.getSpotLag());
      message.add(NAME_FIELD, object.getName());
      serializer.addToMessage(message, EXTERNAL_ID_BUNDLE_FIELD, null, object.getExternalIdBundle());
      serializer.addToMessage(message, UNIQUE_ID_FIELD, null, object.getUniqueId());
      return message;
    }

    @Override
    public InflationLegConvention buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_FIELD);
      final ExternalIdBundle externalIdBundle = deserializer.fieldValueToObject(ExternalIdBundle.class, message.getByName(EXTERNAL_ID_BUNDLE_FIELD));
      final BusinessDayConvention businessDayConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention(message.getString(BUSINESS_DAY_CONVENTION_FIELD));
      final DayCount dayCount = DayCountFactory.INSTANCE.getDayCount(message.getString(DAYCOUNT_FIELD));
      final boolean isEOM = message.getBoolean(IS_EOM_FIELD);
      final int monthLag = message.getInt(MONTH_LAG_FIELD);
      final int spotLag = message.getInt(SPOT_LAG_FIELD);
      final ExternalId priceIndexConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(PRICE_INDEX_FIELD));
      final InflationLegConvention convention = new InflationLegConvention(name, externalIdBundle, businessDayConvention, dayCount,
          isEOM, monthLag, spotLag, priceIndexConvention);
      final FudgeField uniqueIdMsg = message.getByName(UNIQUE_ID_FIELD);
      if (uniqueIdMsg != null) {
        convention.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueIdMsg));
      }
      return convention;
    }

  }

  /**
   * Fudge builder for convention document.
   */
  @FudgeBuilderFor(ConventionDocument.class)
  public static class ConventionDocumentBuilder implements FudgeBuilder<ConventionDocument> {
    /** The convention field */
    private static final String CONVENTION_FIELD = "convention";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ConventionDocument object) {
      final MutableFudgeMsg message = serializer.newMessage();
      FudgeSerializer.addClassHeader(message, ConventionDocument.class);
      serializer.addToMessageWithClassHeaders(message, CONVENTION_FIELD, null, object.getConvention(), object.getConvention().getClass());
      return message;
    }

    @Override
    public ConventionDocument buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Convention convention = (Convention) deserializer.fieldValueToObject(message.getByName(CONVENTION_FIELD));
      return new ConventionDocument(convention);
    }

  }
}
