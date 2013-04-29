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

import com.opengamma.financial.convention.Convention;
import com.opengamma.financial.convention.DepositConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;

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
}
