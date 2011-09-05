/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.equity;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.FinancialSecurityFudgeBuilder;
import com.opengamma.id.ExternalIdFudgeBuilder;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.ZonedDateTimeFudgeBuilder;

/**
 * A Fudge builder for {@code EquityVarianceSwapSecurity}.
 */
@FudgeBuilderFor(EquityVarianceSwapSecurity.class)
public class EquityVarianceSwapSecurityFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<EquityVarianceSwapSecurity> {

  /** Field name. */
  public static final String SPOT_UNDERLYING_IDENTIFIER_KEY = "spotUnderlyingIdentifier";
  /** Field name. */
  public static final String CURRENCY_KEY = "currency";
  /** Field name. */
  public static final String STRIKE_KEY = "strike";
  /** Field name. */
  public static final String NOTIONAL_KEY = "notional";
  /** Field name. */
  public static final String PARAMETERIZED_AS_VARIANCE_KEY = "parameterizedAsVariance";
  /** Field name. */
  public static final String ANNUALIZATION_FACTOR_KEY = "annualizationFactor";
  /** Field name. */
  public static final String FIRST_OBSERVATION_DATE_KEY = "firstObservationDate";
  /** Field name. */
  public static final String LAST_OBSERVATION_DATE_KEY = "lastObservationDate";
  /** Field name. */
  public static final String SETTLEMENT_DATE_KEY = "settlementDate";
  /** Field name. */
  public static final String REGION_KEY = "region";
  /** Field name. */
  public static final String OBSERVATION_FREQUENCY_KEY = "observationFrequency";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, EquityVarianceSwapSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    EquityVarianceSwapSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(FudgeSerializer serializer, EquityVarianceSwapSecurity object, final MutableFudgeMsg msg) {
    FinancialSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    addToMessage(msg, SPOT_UNDERLYING_IDENTIFIER_KEY, ExternalIdFudgeBuilder.toFudgeMsg(serializer, object.getSpotUnderlyingId()));
    addToMessage(msg, CURRENCY_KEY, object.getCurrency());
    addToMessage(msg, STRIKE_KEY, object.getStrike());
    addToMessage(msg, NOTIONAL_KEY, object.getNotional());
    addToMessage(msg, PARAMETERIZED_AS_VARIANCE_KEY, object.isParameterizedAsVariance());
    addToMessage(msg, ANNUALIZATION_FACTOR_KEY, object.getAnnualizationFactor());
    addToMessage(msg, FIRST_OBSERVATION_DATE_KEY, ZonedDateTimeFudgeBuilder.toFudgeMsg(serializer, object.getFirstObservationDate()));
    addToMessage(msg, LAST_OBSERVATION_DATE_KEY, ZonedDateTimeFudgeBuilder.toFudgeMsg(serializer, object.getLastObservationDate()));
    addToMessage(msg, SETTLEMENT_DATE_KEY, ZonedDateTimeFudgeBuilder.toFudgeMsg(serializer, object.getSettlementDate()));
    addToMessage(msg, REGION_KEY, ExternalIdFudgeBuilder.toFudgeMsg(serializer, object.getRegionId()));
    addToMessage(msg, OBSERVATION_FREQUENCY_KEY, object.getObservationFrequency());
  }

  @Override
  public EquityVarianceSwapSecurity buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    EquityVarianceSwapSecurity object = new EquityVarianceSwapSecurity();
    EquityVarianceSwapSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg, EquityVarianceSwapSecurity object) {
    FinancialSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    object.setSpotUnderlyingId(ExternalIdFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(SPOT_UNDERLYING_IDENTIFIER_KEY)));
    object.setCurrency(msg.getValue(Currency.class, CURRENCY_KEY));
    object.setStrike(msg.getDouble(STRIKE_KEY));
    object.setNotional(msg.getDouble(NOTIONAL_KEY));
    object.setParameterizedAsVariance(msg.getBoolean(PARAMETERIZED_AS_VARIANCE_KEY));
    object.setAnnualizationFactor(msg.getDouble(ANNUALIZATION_FACTOR_KEY));
    object.setFirstObservationDate(ZonedDateTimeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(FIRST_OBSERVATION_DATE_KEY)));
    object.setLastObservationDate(ZonedDateTimeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(LAST_OBSERVATION_DATE_KEY)));
    object.setSettlementDate(ZonedDateTimeFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(SETTLEMENT_DATE_KEY)));
    object.setRegionId(ExternalIdFudgeBuilder.fromFudgeMsg(deserializer, msg.getMessage(REGION_KEY)));
    object.setObservationFrequency(msg.getValue(Frequency.class, OBSERVATION_FREQUENCY_KEY));
  }

}
