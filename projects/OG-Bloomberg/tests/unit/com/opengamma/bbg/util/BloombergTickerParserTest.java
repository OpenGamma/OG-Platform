/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.util;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * Test bloomberg ticker validation
 */
public class BloombergTickerParserTest {

  @Test
  public void equity() throws Exception {
    // Stock
    AssertJUnit.assertTrue(BloombergDataUtils.isValidBloombergTicker("VNT VC Equity"));
    AssertJUnit.assertTrue(BloombergDataUtils.isValidBloombergTicker("VNT VC Equity".toUpperCase()));
    AssertJUnit.assertTrue(BloombergDataUtils.isValidBloombergTicker("VNT VC Equity".toLowerCase()));
    AssertJUnit.assertTrue(BloombergDataUtils.isValidBloombergTicker("VNT Equity"));
    
    // Dividend stock future
    AssertJUnit.assertTrue(BloombergDataUtils.isValidBloombergTicker("GNZDX=H2 US Equity"));
    AssertJUnit.assertTrue(BloombergDataUtils.isValidBloombergTicker("GNZDX=H2 Equity"));
    
    // ETP
    AssertJUnit.assertTrue(BloombergDataUtils.isValidBloombergTicker("QQQ* MM Equity"));
    
    // Equity Option
    AssertJUnit.assertTrue(BloombergDataUtils.isValidBloombergTicker("QRN 12/22/11 P1.5 Equity"));
    
    // I.R. Fut WRT
    AssertJUnit.assertTrue(BloombergDataUtils.isValidBloombergTicker("7586G Equity"));
    
    // Sec Lending
    AssertJUnit.assertTrue(BloombergDataUtils.isValidBloombergTicker("ABNL/S Equity"));
    
    // Unit
    AssertJUnit.assertTrue(BloombergDataUtils.isValidBloombergTicker("AW-U Equity"));
  }
}
