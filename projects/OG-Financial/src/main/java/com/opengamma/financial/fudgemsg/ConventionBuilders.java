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

import com.opengamma.analytics.financial.interestrate.CompoundingType;
import com.opengamma.financial.convention.CMSLegConvention;
import com.opengamma.financial.convention.CompoundingIborLegConvention;
import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.InterestRateFutureConvention;
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
  private static final String NAME_FIELD = "name";
  private static final String EXTERNAL_ID_BUNDLE_FIELD = "externalIdBundles";
  private static final String UNIQUE_ID_FIELD = "uniqueId";

  private ConventionBuilders() {
  }

  /**
   * Fudge builder for CMS leg conventions.
   */
  @FudgeBuilderFor(CMSLegConvention.class)
  public static class CMSLegConventionBuilder implements FudgeBuilder<CMSLegConvention> {
    private static final String SWAP_INDEX_ID_FIELD = "swapIndexConvention";
    private static final String PAYMENT_TENOR_FIELD = "paymentTenor";
    private static final String ADVANCE_FIXING_FIELD = "advanceFixing";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CMSLegConvention object) {
      final MutableFudgeMsg message = serializer.newMessage();
      FudgeSerializer.addClassHeader(message, CMSLegConvention.class);
      serializer.addToMessage(message, SWAP_INDEX_ID_FIELD, null, object.getSwapIndexConvention());
      serializer.addToMessage(message, PAYMENT_TENOR_FIELD, null, object.getPaymentTenor());
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
      final Tenor paymentTenor = deserializer.fieldValueToObject(Tenor.class, message.getByName(PAYMENT_TENOR_FIELD));
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
    private static final String SWAP_INDEX_ID_FIELD = "swapIndexConvention";
    private static final String PAYMENT_TENOR_FIELD = "paymentTenor";
    private static final String COMPOUNDING_TYPE_FIELD = "compoundingType";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CompoundingIborLegConvention object) {
      final MutableFudgeMsg message = serializer.newMessage();
      FudgeSerializer.addClassHeader(message, CompoundingIborLegConvention.class);
      serializer.addToMessage(message, SWAP_INDEX_ID_FIELD, null, object.getIborIndexConvention());
      serializer.addToMessage(message, PAYMENT_TENOR_FIELD, null, object.getPaymentTenor());
      message.add(COMPOUNDING_TYPE_FIELD, object.getCompoundingType().name());
      message.add(NAME_FIELD, object.getName());
      serializer.addToMessage(message, EXTERNAL_ID_BUNDLE_FIELD, null, object.getExternalIdBundle());
      serializer.addToMessage(message, UNIQUE_ID_FIELD, null, object.getUniqueId());
      return message;
    }

    @Override
    public CompoundingIborLegConvention buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_FIELD);
      final ExternalIdBundle externalIdBundle = deserializer.fieldValueToObject(ExternalIdBundle.class, message.getByName(EXTERNAL_ID_BUNDLE_FIELD));
      final ExternalId swapIndexConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(SWAP_INDEX_ID_FIELD));
      final Tenor paymentTenor = deserializer.fieldValueToObject(Tenor.class, message.getByName(PAYMENT_TENOR_FIELD));
      final CompoundingType compoundingType = CompoundingType.valueOf(message.getString(COMPOUNDING_TYPE_FIELD));
      final UniqueId uniqueId = deserializer.fieldValueToObject(UniqueId.class, message.getByName(UNIQUE_ID_FIELD));
      final CompoundingIborLegConvention convention = new CompoundingIborLegConvention(name, externalIdBundle, swapIndexConvention, paymentTenor, compoundingType);
      convention.setUniqueId(uniqueId);
      return convention;
    }
  }

  /**
   * Fudge builder for deposit conventions.
   */
  @FudgeBuilderFor(DepositConvention.class)
  public static class DepositConventionBuilder implements FudgeBuilder<DepositConvention> {
    private static final String DAY_COUNT_FIELD = "dayCount";
    private static final String BUSINESS_DAY_CONVENTION_FIELD = "businessDayConvention";
    private static final String DAYS_TO_SETTLE_FIELD = "daysToSettle";
    private static final String IS_EOM_FIELD = "isEOM";
    private static final String CURRENCY_FIELD = "currency";
    private static final String REGION_FIELD = "region";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final DepositConvention object) {
      final MutableFudgeMsg message = serializer.newMessage();
      FudgeSerializer.addClassHeader(message, DepositConvention.class);
      message.add(DAY_COUNT_FIELD, object.getDayCount().getConventionName());
      message.add(BUSINESS_DAY_CONVENTION_FIELD, object.getBusinessDayConvention().getConventionName());
      message.add(DAYS_TO_SETTLE_FIELD, object.getDaysToSettle());
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
      final int daysToSettle = message.getInt(DAYS_TO_SETTLE_FIELD);
      final boolean isEOM = message.getBoolean(IS_EOM_FIELD);
      final Currency currency = Currency.of(message.getString(CURRENCY_FIELD));
      final ExternalId regionCalendar = deserializer.fieldValueToObject(ExternalId.class, message.getByName(REGION_FIELD));
      final UniqueId uniqueId = deserializer.fieldValueToObject(UniqueId.class, message.getByName(UNIQUE_ID_FIELD));
      final DepositConvention convention = new DepositConvention(name, externalIdBundle, dayCount, businessDayConvention, daysToSettle, isEOM, currency, regionCalendar);
      convention.setUniqueId(uniqueId);
      return convention;
    }

  }

  /**
   * Fudge builder for exchange-traded interest rate futures.
   */
  @FudgeBuilderFor(InterestRateFutureConvention.class)
  public static class InterestRateFutureConventionBuilder implements FudgeBuilder<InterestRateFutureConvention> {
    private static final String EXPIRY_CONVENTION_FIELD = "expiryConvention";
    private static final String EXCHANGE_CALENDAR_FIELD = "exchangeCalendar";
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
      final UniqueId uniqueId = deserializer.fieldValueToObject(UniqueId.class, message.getByName(UNIQUE_ID_FIELD));
      final InterestRateFutureConvention convention = new InterestRateFutureConvention(name, externalIdBundle, expiryConvention, exchangeCalendar, indexConvention);
      convention.setUniqueId(uniqueId);
      return convention;
    }
  }
}
