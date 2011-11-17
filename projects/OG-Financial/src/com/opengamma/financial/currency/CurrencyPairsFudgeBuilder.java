/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import java.util.HashSet;
import java.util.Set;

/**
 * Fudge builder for {@link CurrencyPairs}.
 */
@FudgeBuilderFor(CurrencyPairs.class)
public class CurrencyPairsFudgeBuilder implements FudgeBuilder<CurrencyPairs> {
  
  /** Field name for the set of currency pairs */
  public static final String CURRENCY_PAIRS_FIELD_NAME = "currencyPairs";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CurrencyPairs object) {
    MutableFudgeMsg msg = serializer.newMessage();
    Set<CurrencyPair> pairs = object.getPairs();
    HashSet<String> pairNames = new HashSet<String>(pairs.size());
    for (CurrencyPair pair : pairs) {
      pairNames.add(pair.getName());
    }
    serializer.addToMessage(msg, CURRENCY_PAIRS_FIELD_NAME, null, pairNames);
    return msg;
  }

  @SuppressWarnings("unchecked")
  @Override
  public CurrencyPairs buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    FudgeField pairsField = message.getByName(CURRENCY_PAIRS_FIELD_NAME);
    Set<String> pairNames = deserializer.fieldValueToObject(Set.class, pairsField);
    Set<CurrencyPair> pairs = new HashSet<CurrencyPair>(pairNames.size());
    for (String pairName : pairNames) {
      pairs.add(CurrencyPair.of(pairName));
    }
    return new CurrencyPairs(pairs);
  }
}
