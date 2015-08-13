/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.sesame.trade.fpml;

import java.io.File;
import java.util.List;

import org.joda.beans.Bean;
import org.joda.beans.ser.JodaBeanSer;
import org.testng.annotations.Test;

import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.opengamma.sesame.trade.TradeWrapper;

/**
 * Loader of trade data in FpML v5.8 format.
 * <p>
 * This handles the subset of FpML necessary to populate the trade model.
 */
@Test
public final class FpmlTradeParserTest {

  public void test_fpmlExamples() {
    File dir = new File("src/test/resources/com/opengamma/sesame/trade/fpml");
    for (File file : dir.listFiles()) {
      if (file.getName().endsWith(".xml")) {
        System.out.println(file);
        ByteSource source = Files.asByteSource(file);
        FpmlTradeParser parser = new FpmlTradeParser(source, "PARTYAUS33");
        List<TradeWrapper<?>> trades = parser.parseTrades();
        TradeWrapper<?> trade = trades.get(0);
        System.out.println(JodaBeanSer.PRETTY.xmlWriter().write((Bean) trade));
//          ((Expandable<?>) trade.getProduct()).expand();
      }
    }
  }

}
