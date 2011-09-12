/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.bond;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.security.FinancialSecurityFudgeBuilder;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.ExpiryFudgeBuilder;
import com.opengamma.util.time.ZonedDateTimeFudgeBuilder;

/**
 * A Fudge builder for {@code BondSecurity}.
 */
public class BondSecurityFudgeBuilder extends AbstractFudgeBuilder {

  /** Field name. */
  public static final String ISSUER_NAME_FIELD_NAME = "issuerName";
  /** Field name. */
  public static final String ISSUER_TYPE_FIELD_NAME = "issuerType";
  /** Field name. */
  public static final String ISSUER_DOMICILE_FIELD_NAME = "issuerDomicile";
  /** Field name. */
  public static final String MARKET_FIELD_NAME = "market";
  /** Field name. */
  public static final String CURRENCY_FIELD_NAME = "currency";
  /** Field name. */
  public static final String YIELD_CONVENTION_FIELD_NAME = "yieldConvention";
  /** Field name. */
  public static final String GUARANTEE_TYPE_FIELD_NAME = "guaranteeType";
  /** Field name. */
  public static final String LAST_TRADE_DATE_FIELD_NAME = "lastTradeDate";
  /** Field name. */
  public static final String COUPON_TYPE_FIELD_NAME = "couponType";
  /** Field name. */
  public static final String COUPON_RATE_FIELD_NAME = "couponRate";
  /** Field name. */
  public static final String COUPON_FREQUENCY_FIELD_NAME = "couponFrequency";
  /** Field name. */
  public static final String DAY_COUNT_CONVENTION_FIELD_NAME = "dayCountConvention";
  /** Field name. */
  public static final String BUSINESS_DAY_CONVENTION_FIELD_NAME = "businessDayConvention";
  /** Field name. */
  public static final String ANNOUNCEMENT_DATE_FIELD_NAME = "announcementDate";
  /** Field name. */
  public static final String INTEREST_ACCRUAL_DATE_FIELD_NAME = "interestAccrualDate";
  /** Field name. */
  public static final String SETTLEMENT_DATE_FIELD_NAME = "settlementDate";
  /** Field name. */
  public static final String FIRST_COUPON_DATE_FIELD_NAME = "firstCouponDate";
  /** Field name. */
  public static final String ISSUANCE_PRICE_FIELD_NAME = "issuancePrice";
  /** Field name. */
  public static final String TOTAL_AMOUNT_ISSUED_FIELD_NAME = "totalAmountIssued";
  /** Field name. */
  public static final String MINIMUM_AMOUNT_FIELD_NAME = "minimumAmount";
  /** Field name. */
  public static final String MINIMUM_INCREMENT_FIELD_NAME = "minimumIncrement";
  /** Field name. */
  public static final String PAR_AMOUNT_FIELD_NAME = "parAmount";
  /** Field name. */
  public static final String REDEMPTION_VALUE_FIELD_NAME = "redemptionValue";

  public static void toFudgeMsg(FudgeSerializer serializer, BondSecurity object, final MutableFudgeMsg msg) {
    FinancialSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, ISSUER_NAME_FIELD_NAME, object.getIssuerName());
    addToMessage(msg, ISSUER_TYPE_FIELD_NAME, object.getIssuerType());
    addToMessage(msg, ISSUER_DOMICILE_FIELD_NAME, object.getIssuerDomicile());
    addToMessage(msg, MARKET_FIELD_NAME, object.getMarket());
    addToMessage(msg, CURRENCY_FIELD_NAME, object.getCurrency());
    addToMessage(msg, YIELD_CONVENTION_FIELD_NAME, object.getYieldConvention());
    addToMessage(msg, GUARANTEE_TYPE_FIELD_NAME, object.getGuaranteeType());
    addToMessage(msg, LAST_TRADE_DATE_FIELD_NAME, ExpiryFudgeBuilder.toFudgeMsg(serializer, object.getLastTradeDate()));
    addToMessage(msg, COUPON_TYPE_FIELD_NAME, object.getCouponType());
    addToMessage(msg, COUPON_RATE_FIELD_NAME, object.getCouponRate());
    addToMessage(msg, COUPON_FREQUENCY_FIELD_NAME, object.getCouponFrequency());
    addToMessage(msg, DAY_COUNT_CONVENTION_FIELD_NAME, object.getDayCount());
    addToMessage(msg, BUSINESS_DAY_CONVENTION_FIELD_NAME, object.getBusinessDayConvention());
    addToMessage(msg, ANNOUNCEMENT_DATE_FIELD_NAME, ZonedDateTimeFudgeBuilder.toFudgeMsg(serializer, object.getAnnouncementDate()));
    addToMessage(msg, INTEREST_ACCRUAL_DATE_FIELD_NAME, ZonedDateTimeFudgeBuilder.toFudgeMsg(serializer, object.getInterestAccrualDate()));
    addToMessage(msg, SETTLEMENT_DATE_FIELD_NAME, ZonedDateTimeFudgeBuilder.toFudgeMsg(serializer, object.getSettlementDate()));
    addToMessage(msg, FIRST_COUPON_DATE_FIELD_NAME, ZonedDateTimeFudgeBuilder.toFudgeMsg(serializer, object.getFirstCouponDate()));
    addToMessage(msg, ISSUANCE_PRICE_FIELD_NAME, object.getIssuancePrice());
    addToMessage(msg, TOTAL_AMOUNT_ISSUED_FIELD_NAME, object.getTotalAmountIssued());
    addToMessage(msg, MINIMUM_AMOUNT_FIELD_NAME, object.getMinimumAmount());
    addToMessage(msg, MINIMUM_INCREMENT_FIELD_NAME, object.getMinimumIncrement());
    addToMessage(msg, PAR_AMOUNT_FIELD_NAME, object.getParAmount());
    addToMessage(msg, REDEMPTION_VALUE_FIELD_NAME, object.getRedemptionValue());
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, BondSecurity object) {
    FinancialSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setIssuerName(msg.getString(ISSUER_NAME_FIELD_NAME));
    object.setIssuerType(msg.getString(ISSUER_TYPE_FIELD_NAME));
    object.setIssuerDomicile(msg.getString(ISSUER_DOMICILE_FIELD_NAME));
    object.setMarket(msg.getString(MARKET_FIELD_NAME));
    object.setCurrency(msg.getValue(Currency.class, CURRENCY_FIELD_NAME));
    object.setYieldConvention(msg.getValue(YieldConvention.class, YIELD_CONVENTION_FIELD_NAME));
    object.setGuaranteeType(msg.getString(GUARANTEE_TYPE_FIELD_NAME));
    object.setLastTradeDate(ExpiryFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(LAST_TRADE_DATE_FIELD_NAME)));
    object.setCouponType(msg.getString(COUPON_TYPE_FIELD_NAME));
    object.setCouponRate(msg.getDouble(COUPON_RATE_FIELD_NAME));
    object.setCouponFrequency(msg.getValue(Frequency.class, COUPON_FREQUENCY_FIELD_NAME));
    object.setDayCount(msg.getValue(DayCount.class, DAY_COUNT_CONVENTION_FIELD_NAME));
    object.setBusinessDayConvention(msg.getValue(BusinessDayConvention.class, BUSINESS_DAY_CONVENTION_FIELD_NAME));
    object.setAnnouncementDate(ZonedDateTimeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(ANNOUNCEMENT_DATE_FIELD_NAME)));
    object.setInterestAccrualDate(ZonedDateTimeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(INTEREST_ACCRUAL_DATE_FIELD_NAME)));
    object.setSettlementDate(ZonedDateTimeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(SETTLEMENT_DATE_FIELD_NAME)));
    object.setFirstCouponDate(ZonedDateTimeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(FIRST_COUPON_DATE_FIELD_NAME)));
    object.setIssuancePrice(msg.getDouble(ISSUANCE_PRICE_FIELD_NAME));
    object.setTotalAmountIssued(msg.getDouble(TOTAL_AMOUNT_ISSUED_FIELD_NAME));
    object.setMinimumAmount(msg.getDouble(MINIMUM_AMOUNT_FIELD_NAME));
    object.setMinimumIncrement(msg.getDouble(MINIMUM_INCREMENT_FIELD_NAME));
    object.setParAmount(msg.getDouble(PAR_AMOUNT_FIELD_NAME));
    object.setRedemptionValue(msg.getDouble(REDEMPTION_VALUE_FIELD_NAME));
  }

}
