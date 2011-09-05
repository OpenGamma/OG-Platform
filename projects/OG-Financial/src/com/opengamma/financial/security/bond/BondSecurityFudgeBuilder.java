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
  public static final String ISSUER_NAME_KEY = "issuerName";
  /** Field name. */
  public static final String ISSUER_TYPE_KEY = "issuerType";
  /** Field name. */
  public static final String ISSUER_DOMICILE_KEY = "issuerDomicile";
  /** Field name. */
  public static final String MARKET_KEY = "market";
  /** Field name. */
  public static final String CURRENCY_KEY = "currency";
  /** Field name. */
  public static final String YIELD_CONVENTION_KEY = "yieldConvention";
  /** Field name. */
  public static final String GUARANTEE_TYPE_KEY = "guaranteeType";
  /** Field name. */
  public static final String LAST_TRADE_DATE_KEY = "lastTradeDate";
  /** Field name. */
  public static final String COUPON_TYPE_KEY = "couponType";
  /** Field name. */
  public static final String COUPON_RATE_KEY = "couponRate";
  /** Field name. */
  public static final String COUPON_FREQUENCY_KEY = "couponFrequency";
  /** Field name. */
  public static final String DAY_COUNT_CONVENTION_KEY = "dayCountConvention";
  /** Field name. */
  public static final String BUSINESS_DAY_CONVENTION_KEY = "businessDayConvention";
  /** Field name. */
  public static final String ANNOUNCEMENT_DATE_KEY = "announcementDate";
  /** Field name. */
  public static final String INTEREST_ACCRUAL_DATE_KEY = "interestAccrualDate";
  /** Field name. */
  public static final String SETTLEMENT_DATE_KEY = "settlementDate";
  /** Field name. */
  public static final String FIRST_COUPON_DATE_KEY = "firstCouponDate";
  /** Field name. */
  public static final String ISSUANCE_PRICE_KEY = "issuancePrice";
  /** Field name. */
  public static final String TOTAL_AMOUNT_ISSUED_KEY = "totalAmountIssued";
  /** Field name. */
  public static final String MINIMUM_AMOUNT_KEY = "minimumAmount";
  /** Field name. */
  public static final String MINIMUM_INCREMENT_KEY = "minimumIncrement";
  /** Field name. */
  public static final String PAR_AMOUNT_KEY = "parAmount";
  /** Field name. */
  public static final String REDEMPTION_VALUE_KEY = "redemptionValue";

  public static void toFudgeMsg(FudgeSerializer serializer, BondSecurity object, final MutableFudgeMsg msg) {
    FinancialSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, ISSUER_NAME_KEY, object.getIssuerName());
    addToMessage(msg, ISSUER_TYPE_KEY, object.getIssuerType());
    addToMessage(msg, ISSUER_DOMICILE_KEY, object.getIssuerDomicile());
    addToMessage(msg, MARKET_KEY, object.getMarket());
    addToMessage(msg, CURRENCY_KEY, object.getCurrency());
    addToMessage(msg, YIELD_CONVENTION_KEY, object.getYieldConvention());
    addToMessage(msg, GUARANTEE_TYPE_KEY, object.getGuaranteeType());
    addToMessage(msg, LAST_TRADE_DATE_KEY, ExpiryFudgeBuilder.toFudgeMsg(serializer, object.getLastTradeDate()));
    addToMessage(msg, COUPON_TYPE_KEY, object.getCouponType());
    addToMessage(msg, COUPON_RATE_KEY, object.getCouponRate());
    addToMessage(msg, COUPON_FREQUENCY_KEY, object.getCouponFrequency());
    addToMessage(msg, DAY_COUNT_CONVENTION_KEY, object.getDayCount());
    addToMessage(msg, BUSINESS_DAY_CONVENTION_KEY, object.getBusinessDayConvention());
    addToMessage(msg, ANNOUNCEMENT_DATE_KEY, ZonedDateTimeFudgeBuilder.toFudgeMsg(serializer, object.getAnnouncementDate()));
    addToMessage(msg, INTEREST_ACCRUAL_DATE_KEY, ZonedDateTimeFudgeBuilder.toFudgeMsg(serializer, object.getInterestAccrualDate()));
    addToMessage(msg, SETTLEMENT_DATE_KEY, ZonedDateTimeFudgeBuilder.toFudgeMsg(serializer, object.getSettlementDate()));
    addToMessage(msg, FIRST_COUPON_DATE_KEY, ZonedDateTimeFudgeBuilder.toFudgeMsg(serializer, object.getFirstCouponDate()));
    addToMessage(msg, ISSUANCE_PRICE_KEY, object.getIssuancePrice());
    addToMessage(msg, TOTAL_AMOUNT_ISSUED_KEY, object.getTotalAmountIssued());
    addToMessage(msg, MINIMUM_AMOUNT_KEY, object.getMinimumAmount());
    addToMessage(msg, MINIMUM_INCREMENT_KEY, object.getMinimumIncrement());
    addToMessage(msg, PAR_AMOUNT_KEY, object.getParAmount());
    addToMessage(msg, REDEMPTION_VALUE_KEY, object.getRedemptionValue());
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, BondSecurity object) {
    FinancialSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setIssuerName(msg.getString(ISSUER_NAME_KEY));
    object.setIssuerType(msg.getString(ISSUER_TYPE_KEY));
    object.setIssuerDomicile(msg.getString(ISSUER_DOMICILE_KEY));
    object.setMarket(msg.getString(MARKET_KEY));
    object.setCurrency(msg.getValue(Currency.class, CURRENCY_KEY));
    object.setYieldConvention(msg.getValue(YieldConvention.class, YIELD_CONVENTION_KEY));
    object.setGuaranteeType(msg.getString(GUARANTEE_TYPE_KEY));
    object.setLastTradeDate(ExpiryFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(LAST_TRADE_DATE_KEY)));
    object.setCouponType(msg.getString(COUPON_TYPE_KEY));
    object.setCouponRate(msg.getDouble(COUPON_RATE_KEY));
    object.setCouponFrequency(msg.getValue(Frequency.class, COUPON_FREQUENCY_KEY));
    object.setDayCount(msg.getValue(DayCount.class, DAY_COUNT_CONVENTION_KEY));
    object.setBusinessDayConvention(msg.getValue(BusinessDayConvention.class, BUSINESS_DAY_CONVENTION_KEY));
    object.setAnnouncementDate(ZonedDateTimeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(ANNOUNCEMENT_DATE_KEY)));
    object.setInterestAccrualDate(ZonedDateTimeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(INTEREST_ACCRUAL_DATE_KEY)));
    object.setSettlementDate(ZonedDateTimeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(SETTLEMENT_DATE_KEY)));
    object.setFirstCouponDate(ZonedDateTimeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(FIRST_COUPON_DATE_KEY)));
    object.setIssuancePrice(msg.getDouble(ISSUANCE_PRICE_KEY));
    object.setTotalAmountIssued(msg.getDouble(TOTAL_AMOUNT_ISSUED_KEY));
    object.setMinimumAmount(msg.getDouble(MINIMUM_AMOUNT_KEY));
    object.setMinimumIncrement(msg.getDouble(MINIMUM_INCREMENT_KEY));
    object.setParAmount(msg.getDouble(PAR_AMOUNT_KEY));
    object.setRedemptionValue(msg.getDouble(REDEMPTION_VALUE_KEY));
  }

}
