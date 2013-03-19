/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CurrencyPairsConfigDocumentLoaderTest {

  @Test
  public void readPairs() throws IOException {
    CurrencyPairsConfigDocumentLoader loader = new CurrencyPairsConfigDocumentLoader(null, null, null);
    String eurUsdStr = "EUR/USD";
    String gbpUsdStr = "GBP/USD";
    String usdCadStr = "USD/CAD";
    CurrencyPair eurUsd = CurrencyPair.parse(eurUsdStr);
    CurrencyPair gbpUsd = CurrencyPair.parse(gbpUsdStr);
    CurrencyPair usdCad = CurrencyPair.parse(usdCadStr);
    BufferedReader reader = new BufferedReader(new StringReader(eurUsdStr + "\n" + gbpUsdStr + "\n" + usdCadStr));
    ImmutableSet<CurrencyPair> pairs = ImmutableSet.of(eurUsd, gbpUsd, usdCad);
    AssertJUnit.assertEquals(pairs, loader.readPairs(reader));
  }

}
