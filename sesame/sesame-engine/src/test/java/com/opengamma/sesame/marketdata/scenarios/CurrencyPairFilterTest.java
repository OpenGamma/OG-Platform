/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.sesame.marketdata.FxRateId;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class CurrencyPairFilterTest {

  public void match() {
    CurrencyPair pair = CurrencyPair.parse("EUR/USD");
    CurrencyPairFilter filter = new CurrencyPairFilter(pair);
    Set<? extends MatchDetails> matchDetails = filter.apply(FxRateId.of(pair));

    assertEquals(1, matchDetails.size());
    CurrencyPairMatchDetails currencyPairDetails = (CurrencyPairMatchDetails) matchDetails.iterator().next();
    assertFalse(currencyPairDetails.isInverse());
  }

  public void matchInverse() {
    CurrencyPair pair = CurrencyPair.parse("EUR/USD");
    CurrencyPair inverse = pair.inverse();
    CurrencyPairFilter filter = new CurrencyPairFilter(pair);
    Set<? extends MatchDetails> matchDetails = filter.apply(FxRateId.of(inverse));

    assertEquals(1, matchDetails.size());
    CurrencyPairMatchDetails currencyPairDetails = (CurrencyPairMatchDetails) matchDetails.iterator().next();
    assertTrue(currencyPairDetails.isInverse());
  }

  public void noMatch() {
    CurrencyPair pair = CurrencyPair.parse("EUR/USD");
    CurrencyPairFilter filter = new CurrencyPairFilter(CurrencyPair.parse("EUR/CHF"));
    assertEquals(0, filter.apply(FxRateId.of(pair)).size());
  }

  public void matchWithData() {
    CurrencyPair pair = CurrencyPair.parse("EUR/USD");
    CurrencyPairFilter filter = new CurrencyPairFilter(pair);
    Set<? extends MatchDetails> matchDetails = filter.apply(FxRateId.of(pair), 1.1);

    assertEquals(1, matchDetails.size());
    CurrencyPairMatchDetails currencyPairDetails = (CurrencyPairMatchDetails) matchDetails.iterator().next();
    assertFalse(currencyPairDetails.isInverse());
  }

  public void matchInverseWithData() {
    CurrencyPair pair = CurrencyPair.parse("EUR/USD");
    CurrencyPair inverse = pair.inverse();
    CurrencyPairFilter filter = new CurrencyPairFilter(pair);
    Set<? extends MatchDetails> matchDetails = filter.apply(FxRateId.of(inverse), 1.1);

    assertEquals(1, matchDetails.size());
    CurrencyPairMatchDetails currencyPairDetails = (CurrencyPairMatchDetails) matchDetails.iterator().next();
    assertTrue(currencyPairDetails.isInverse());
  }

  public void noMatchWithData() {
    CurrencyPair pair = CurrencyPair.parse("EUR/USD");
    CurrencyPairFilter filter = new CurrencyPairFilter(CurrencyPair.parse("EUR/CHF"));
    assertEquals(0, filter.apply(FxRateId.of(pair), 1.1).size());
  }
}
