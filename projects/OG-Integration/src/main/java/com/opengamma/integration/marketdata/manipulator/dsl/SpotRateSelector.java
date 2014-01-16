/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Set;

import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.SelectorResolver;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * Selects values with a requirement name of {@link ValueRequirementNames#SPOT_RATE} and a target type of
 * {@link CurrencyPair} or {@link UnorderedCurrencyPair}.
 * TODO ImmutableBean
 */
/* package */ class SpotRateSelector implements DistinctMarketDataSelector {

  /** Calc configs to which this selector will apply, null will match any config. */
  private final Set<String> _calcConfigNames;
  private final Set<CurrencyPair> _currencyPairs;
  //private final Set<UnorderedCurrencyPair> _unorderedCurrencyPairs = Sets.newHashSet();

  /* package */ SpotRateSelector(Set<String> calcConfigNames, Set<CurrencyPair> currencyPairs) {
    _calcConfigNames = calcConfigNames;
    _currencyPairs = ArgumentChecker.notEmpty(currencyPairs, "currencyPair");
    /*for (CurrencyPair currencyPair : currencyPairs) {
      _unorderedCurrencyPairs.add(UnorderedCurrencyPair.of(currencyPair.getBase(), currencyPair.getCounter()));
    }*/
    // TODO sanity check currency pairs. ordered and unordered sets should be the same size
    // otherwise it means a pair and its inverse are both included which can't be done
  }

  /* package */ Set<CurrencyPair> getCurrencyPairs() {
    return _currencyPairs;
  }

  @Override
  public boolean hasSelectionsDefined() {
    return true;
  }

  // TODO this can match the same value twice - once when the pair is ordered, once when unordered
  @Override
  public DistinctMarketDataSelector findMatchingSelector(ValueSpecification valueSpecification,
                                                         String calcConfigName,
                                                         SelectorResolver resolver) {
    if (_calcConfigNames != null && !_calcConfigNames.contains(calcConfigName)) {
      return null;
    }
    if (!ValueRequirementNames.SPOT_RATE.equals(valueSpecification.getValueName())) {
      return null;
    }
    ComputationTargetType targetType = valueSpecification.getTargetSpecification().getType();
    String idValue = valueSpecification.getTargetSpecification().getUniqueId().getValue();
    /*if (targetType.equals(ComputationTargetType.UNORDERED_CURRENCY_PAIR)) {
      UnorderedCurrencyPair unorderedCurrencyPair = UnorderedCurrencyPair.parse(idValue);
      if (!_unorderedCurrencyPairs.contains(unorderedCurrencyPair)) {
        return null;
      }
    } else */if (targetType.equals(CurrencyPair.TYPE)) {
      CurrencyPair currencyPair = CurrencyPair.parse(idValue);
      if (!_currencyPairs.contains(currencyPair) && !_currencyPairs.contains(currencyPair.inverse())) {
        return null;
      }
    } else {
      return null;
    }
    return this;
  }
}
