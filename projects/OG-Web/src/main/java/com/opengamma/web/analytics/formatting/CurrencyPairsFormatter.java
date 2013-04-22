/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;

/* package */ class CurrencyPairsFormatter extends AbstractFormatter<CurrencyPairs> {

  private static final String DATA = "data";
  private static final String LABEL = "label";
  private static final String CURRENCY_PAIRS = "Currency Pairs";

  /* package */ CurrencyPairsFormatter() {
    super(CurrencyPairs.class);
    addFormatter(new Formatter<CurrencyPairs>(Format.EXPANDED) {
      @Override
      Object format(CurrencyPairs pairs, ValueSpecification valueSpec, Object inlineKey) {
        return formatExpanded(pairs);
      }
    });
  }

  private Map<String, Object> formatExpanded(CurrencyPairs currencyPairs) {
    Set<CurrencyPair> pairs = currencyPairs.getPairs();
    List<String> pairNames = Lists.newArrayListWithCapacity(pairs.size());
    for (CurrencyPair pair : pairs) {
      pairNames.add(pair.getName());
    }
    Collections.sort(pairNames);
    return ImmutableMap.of(DATA, pairNames, LABEL, CURRENCY_PAIRS);
  }

  @Override
  public Object formatCell(CurrencyPairs pairs, ValueSpecification valueSpec, Object inlineKey) {
    return "Currency Pairs (" + pairs.getPairs().size() + ")";
  }

  @Override
  public DataType getDataType() {
    return DataType.VECTOR;
  }
}
