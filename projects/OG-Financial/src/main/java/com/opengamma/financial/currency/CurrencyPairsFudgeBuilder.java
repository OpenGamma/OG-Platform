/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.id.UniqueId;

/**
 * Fudge builder for {@link CurrencyPairs}.
 */
@FudgeBuilderFor(CurrencyPairs.class)
public class CurrencyPairsFudgeBuilder implements FudgeBuilder<CurrencyPairs> {

  /**
   * Field name for the unique ID
   */
  public static final String UNIQUE_ID_FIELD_NAME = "uniqueId";
  /**
   * Field name for the set of currency pairs
   */
  public static final String CURRENCY_PAIRS_FIELD_NAME = "currencyPairs";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, CurrencyPairs object) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, UNIQUE_ID_FIELD_NAME, null, object.getUniqueId());
    Set<CurrencyPair> pairs = object.getPairs();
    // sort the names so it's more obvious when messages are equal
    Set<String> pairNames = new TreeSet<>();
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
      pairs.add(CurrencyPair.parse(pairName));
    }
    CurrencyPairs currencyPairs = CurrencyPairs.of(pairs);
    FudgeField uniqueIdField = message.getByName(UNIQUE_ID_FIELD_NAME);
    if (uniqueIdField != null) {
      currencyPairs.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueIdField));      
    }
    return currencyPairs;
  }

}
