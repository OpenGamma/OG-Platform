/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Set;
import java.util.regex.Pattern;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.google.common.collect.Sets;
import com.opengamma.util.money.Currency;

/**
 *
 */
@FudgeBuilderFor(YieldCurveDataSelector.class)
public class YieldCurveDataSelectorFudgeBuilder implements FudgeBuilder<YieldCurveDataSelector> {

  private static final String CALC_CONFIGS = "calculationConfigurationNames";
  private static final String NAMES = "names";
  private static final String CURRENCIES = "currencies";
  private static final String NAME_MATCH_PATTERN = "nameMatchPattern";
  private static final String NAME_LIKE_PATTERN = "nameLikePattern";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, YieldCurveDataSelector selector) {
    MutableFudgeMsg msg = serializer.newMessage();
    Set<String> calcConfigNames = selector.getCalcConfigNames();
    if (calcConfigNames != null) {
      MutableFudgeMsg calcConfigsMsg = serializer.newMessage();
      for (String calcConfigName : calcConfigNames) {
        serializer.addToMessage(calcConfigsMsg, null, null, calcConfigName);
      }
      serializer.addToMessage(msg, CALC_CONFIGS, null, calcConfigsMsg);
    }
    if (selector.getNames() != null && !selector.getNames().isEmpty()) {
      MutableFudgeMsg namesMsg = serializer.newMessage();
      for (String name : selector.getNames()) {
        serializer.addToMessage(namesMsg, null, null, name);
      }
      serializer.addToMessage(msg, NAMES, null, namesMsg);
    }
    if (selector.getCurrencies() != null && !selector.getCurrencies().isEmpty()) {
      MutableFudgeMsg currenciesMsg = serializer.newMessage();
      for (Currency currency : selector.getCurrencies()) {
        serializer.addToMessage(currenciesMsg, null, null, currency.getCode());
      }
      serializer.addToMessage(msg, CURRENCIES, null, currenciesMsg);
    }
    if (selector.getNameMatchPattern() != null) {
      serializer.addToMessage(msg, NAME_MATCH_PATTERN, null, selector.getNameMatchPattern().pattern());
    }
    if (selector.getNameLikePattern() != null) {
      serializer.addToMessage(msg, NAME_LIKE_PATTERN, null, selector.getNameLikePattern().pattern());
    }
    return msg;
  }

  @Override
  public YieldCurveDataSelector buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    Set<String> calcConfigNames;
    if (msg.hasField(CALC_CONFIGS)) {
      calcConfigNames = Sets.newHashSet();
      FudgeMsg calcConfigsMsg = msg.getMessage(CALC_CONFIGS);
      for (FudgeField field : calcConfigsMsg) {
        calcConfigNames.add(deserializer.fieldValueToObject(String.class, field));
      }
    } else {
      calcConfigNames = null;
    }
    FudgeField namesField = msg.getByName(NAMES);
    Set<String> names;
    if (namesField != null) {
      FudgeMsg namesMsg = (FudgeMsg) namesField.getValue();
      names = Sets.newHashSet();
      for (FudgeField field : namesMsg) {
        names.add(deserializer.fieldValueToObject(String.class, field));
      }
    } else {
      names = null;
    }

    FudgeField currenciesField = msg.getByName(CURRENCIES);
    Set<Currency> currencies;
    if (currenciesField != null) {
      FudgeMsg currenciesMsg = (FudgeMsg) currenciesField.getValue();
      currencies = Sets.newHashSet();
      for (FudgeField field : currenciesMsg) {
        currencies.add(Currency.of(deserializer.fieldValueToObject(String.class, field)));
      }
    } else {
      currencies = null;
    }

    Pattern nameMatchPattern;
    FudgeField namePatternField = msg.getByName(NAME_MATCH_PATTERN);
    if (namePatternField != null) {
      String regex = deserializer.fieldValueToObject(String.class, namePatternField);
      nameMatchPattern = Pattern.compile(regex);
    } else {
      nameMatchPattern = null;
    }

    Pattern nameLikePattern;
    FudgeField nameLikeField = msg.getByName(NAME_LIKE_PATTERN);
    if (nameLikeField != null) {
      String regex = deserializer.fieldValueToObject(String.class, nameLikeField);
      nameLikePattern = Pattern.compile(regex);
    } else {
      nameLikePattern = null;
    }
    return new YieldCurveDataSelector(calcConfigNames, names, currencies, nameMatchPattern, nameLikePattern);
  }
}
