/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.util.amount.SurfaceValue;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
/* package */final class AnalyticsResultBuilder {

  private AnalyticsResultBuilder() {
  }

  @FudgeBuilderFor(InterestRateCurveSensitivity.class)
  public static final class PresentValueSensitivityBuilder extends AbstractFudgeBuilder<InterestRateCurveSensitivity> {
    private static final String CURVE_FIELD_NAME = "CurveName";
    private static final String PAIR_FIELD_NAME = "Pair";
    private static final String SENSITIVITIES_FIELD_NAME = "Sensitivities";

    @Override
    public InterestRateCurveSensitivity buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final List<FudgeField> curveNameFields = message.getAllByName(CURVE_FIELD_NAME);
      final List<FudgeField> sensitivitiesFields = message.getAllByName(SENSITIVITIES_FIELD_NAME);
      final Map<String, List<DoublesPair>> data = new HashMap<String, List<DoublesPair>>();
      final int n = curveNameFields.size();
      if (sensitivitiesFields.size() != n) {
        throw new OpenGammaRuntimeException("Sensitivities list not same size as names list");
      }
      for (int i = 0; i < n; i++) {
        final FudgeField nameField = curveNameFields.get(i);
        final String name = deserializer.fieldValueToObject(String.class, nameField);
        final FudgeField listField = sensitivitiesFields.get(i);
        final List<DoublesPair> pairs = new ArrayList<DoublesPair>();
        final List<FudgeField> pairsField = ((FudgeMsg) listField.getValue()).getAllByName(PAIR_FIELD_NAME);
        for (final FudgeField pair : pairsField) {
          pairs.add((DoublesPair) deserializer.fieldValueToObject(Pair.class, pair));
        }
        data.put(name, pairs);
      }
      return new InterestRateCurveSensitivity(data);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final InterestRateCurveSensitivity object) {
      final Map<String, List<DoublesPair>> data = object.getSensitivities();
      for (final Map.Entry<String, List<DoublesPair>> entry : data.entrySet()) {
        message.add(CURVE_FIELD_NAME, null, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(entry.getKey()), entry.getKey().getClass()));
        final MutableFudgeMsg perPairMessage = serializer.newMessage();
        for (final DoublesPair pair : entry.getValue()) {
          perPairMessage.add(PAIR_FIELD_NAME, null, FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(pair), pair.getClass()));
        }
        message.add(SENSITIVITIES_FIELD_NAME, null, perPairMessage);
      }
    }
  }

  @FudgeBuilderFor(MultipleCurrencyInterestRateCurveSensitivity.class)
  public static final class MultipleCurrencyInterestRateCurveSensitivityBuilder extends AbstractFudgeBuilder<MultipleCurrencyInterestRateCurveSensitivity> {
    private static final String CURRENCY_FIELD_NAME = "Currencies";
    private static final String SENSITIVITIES_FIELD_NAME = "Sensitivities";

    @Override
    public MultipleCurrencyInterestRateCurveSensitivity buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final List<FudgeField> currencyNameFields = message.getAllByName(CURRENCY_FIELD_NAME);
      final List<FudgeField> sensitivitiesFields = message.getAllByName(SENSITIVITIES_FIELD_NAME);
      MultipleCurrencyInterestRateCurveSensitivity result = null;
      for (int i = 0; i < currencyNameFields.size(); i++) {
        final Currency ccy = deserializer.fieldValueToObject(Currency.class, currencyNameFields.get(i));
        final InterestRateCurveSensitivity sensitivity = deserializer.fieldValueToObject(InterestRateCurveSensitivity.class, sensitivitiesFields.get(i));
        if (result == null) {
          result = MultipleCurrencyInterestRateCurveSensitivity.of(ccy, sensitivity);
        } else {
          result = result.plus(ccy, sensitivity);
        }
      }
      return result;
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final MultipleCurrencyInterestRateCurveSensitivity object) {
      final Set<Currency> currencies = object.getCurrencies();
      for (final Currency ccy : currencies) {
        serializer.addToMessage(message, CURRENCY_FIELD_NAME, null, ccy);
        serializer.addToMessage(message, SENSITIVITIES_FIELD_NAME, null, object.getSensitivity(ccy));
      }
    }
  }

  @FudgeBuilderFor(PresentValueForexBlackVolatilitySensitivity.class)
  public static final class PresentValueForexBlackVolatilitySensitivityBuilder extends AbstractFudgeBuilder<PresentValueForexBlackVolatilitySensitivity> {
    private static final String FIRST_CURRENCY_FIELD_NAME = "firstCurrency";
    private static final String SECOND_CURRENCY_FIELD_NAME = "secondCurrency";
    private static final String TIME_FIELD_NAME = "time";
    private static final String STRIKE_FIELD_NAME = "strike";
    private static final String VEGA_FIELD_NAME = "vega";
    private static final String VEGA_ENTRY_NAME = "vegaEntry";

    @Override
    public PresentValueForexBlackVolatilitySensitivity buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Currency firstCurrency = Currency.of(message.getString(FIRST_CURRENCY_FIELD_NAME));
      final Currency secondCurrency = Currency.of(message.getString(SECOND_CURRENCY_FIELD_NAME));
      final SurfaceValue vega = new SurfaceValue();
      final List<FudgeField> vegaEntryFields = message.getAllByName(VEGA_ENTRY_NAME);
      for (int i = 0; i < vegaEntryFields.size(); i++) {
        final double time = ((FudgeMsg) vegaEntryFields.get(i).getValue()).getDouble(TIME_FIELD_NAME);
        final double strike = ((FudgeMsg) vegaEntryFields.get(i).getValue()).getDouble(STRIKE_FIELD_NAME);
        final Double vegaValue = ((FudgeMsg) vegaEntryFields.get(i).getValue()).getDouble(VEGA_FIELD_NAME);
        vega.add(DoublesPair.of(time, strike), vegaValue);
      }
      return new PresentValueForexBlackVolatilitySensitivity(firstCurrency, secondCurrency, vega);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final PresentValueForexBlackVolatilitySensitivity object) {
      final Pair<Currency, Currency> currencies = object.getCurrencyPair();
      message.add(FIRST_CURRENCY_FIELD_NAME, currencies.getFirst().getCode());
      message.add(SECOND_CURRENCY_FIELD_NAME, currencies.getSecond().getCode());
      final SurfaceValue vegas = object.getVega();
      final HashMap<DoublesPair, Double> map = vegas.getMap();
      for (final Map.Entry<DoublesPair, Double> entry : map.entrySet()) {
        final MutableFudgeMsg perEntryMessage = serializer.newMessage();
        perEntryMessage.add(TIME_FIELD_NAME, entry.getKey().first);
        perEntryMessage.add(STRIKE_FIELD_NAME, entry.getKey().second);
        perEntryMessage.add(VEGA_FIELD_NAME, entry.getValue());
        message.add(VEGA_ENTRY_NAME, null, perEntryMessage);
      }
    }
  }
}
