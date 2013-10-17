/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.test;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.testng.annotations.Test;

import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.ResourceUtils;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class IRSwapTradeParserTest {
  
  private static final Logger s_logger = LoggerFactory.getLogger(IRSwapTradeParserTest.class);
  
  public void test() throws Exception {
    IRSwapTradeParser tradeParser = new IRSwapTradeParser();
    Resource resource = ResourceUtils.createResource("classpath:com/opengamma/financial/analytics/test/Trades14Oct.csv"); 
    List<IRSwapSecurity> trades = tradeParser.parseCSVFile(resource.getURL());
    for (IRSwapSecurity irSwapSecurity : trades) {
      SwapSecurity swapSecurity = irSwapSecurity.getSwapSecurity();
      Double ersPV = irSwapSecurity.getRawInput().getDouble("ERS PV");
    }
    s_logger.warn("Got {} trades", trades.size());
  }

}
