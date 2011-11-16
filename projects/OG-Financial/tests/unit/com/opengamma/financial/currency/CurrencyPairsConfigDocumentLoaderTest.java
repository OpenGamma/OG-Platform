package com.opengamma.financial.currency;

import com.google.common.collect.ImmutableSet;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class CurrencyPairsConfigDocumentLoaderTest {

  @Test
  public void readPairs() throws IOException {
    CurrencyPairsConfigDocumentLoader loader = new CurrencyPairsConfigDocumentLoader(null, null, null);
    String eurUsdStr = "EUR/USD";
    String gbpUsdStr = "GBP/USD";
    String usdCadStr = "USD/CAD";
    CurrencyPair eurUsd = CurrencyPair.of(eurUsdStr);
    CurrencyPair gbpUsd = CurrencyPair.of(gbpUsdStr);
    CurrencyPair usdCad = CurrencyPair.of(usdCadStr);
    BufferedReader reader = new BufferedReader(new StringReader(eurUsdStr + "\n" + gbpUsdStr + "\n" + usdCadStr));
    ImmutableSet<CurrencyPair> pairs = ImmutableSet.of(eurUsd, gbpUsd, usdCad);
    AssertJUnit.assertEquals(pairs, loader.readPairs(reader));
  }
}
