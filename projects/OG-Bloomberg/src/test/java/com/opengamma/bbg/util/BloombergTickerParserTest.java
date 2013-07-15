/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.util;

import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BloombergTickerParserTest {

  @Test
  public void equity() throws Exception {
    // Stock
    assertTrue(BloombergDataUtils.isValidBloombergTicker("VNT VC Equity"));
    assertTrue(BloombergDataUtils.isValidBloombergTicker("VNT VC Equity".toUpperCase()));
    assertTrue(BloombergDataUtils.isValidBloombergTicker("VNT VC Equity".toLowerCase()));
    assertTrue(BloombergDataUtils.isValidBloombergTicker("VNT Equity"));
    
    // Dividend stock future
    assertTrue(BloombergDataUtils.isValidBloombergTicker("GNZDX=H2 US Equity"));
    assertTrue(BloombergDataUtils.isValidBloombergTicker("GNZDX=H2 Equity"));
    
    // ETP
    assertTrue(BloombergDataUtils.isValidBloombergTicker("QQQ* MM Equity"));
    
    // Equity Option
    assertTrue(BloombergDataUtils.isValidBloombergTicker("QRN 12/22/11 P1.5 Equity"));
    
    // I.R. Fut WRT
    assertTrue(BloombergDataUtils.isValidBloombergTicker("7586G Equity"));
    
    // Sec Lending
    assertTrue(BloombergDataUtils.isValidBloombergTicker("ABNL/S Equity"));
    
    // Unit
    assertTrue(BloombergDataUtils.isValidBloombergTicker("AW-U Equity"));
  }

}
