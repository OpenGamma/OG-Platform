/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import static org.testng.Assert.assertEquals;


import java.math.BigDecimal;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.ExtId;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.FixingIndex;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.FixingIndex.RateType;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.FraTrade;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.IdWrapper;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class FraTradeSecurityExtractorTest {
  
  @Test
  public void testExtractSecurities() {
    FraTrade fra = createBasicFra();
    FraTradeSecurityExtractor fraExtractor = new FraTradeSecurityExtractor(fra);
    ManageableSecurity[] extractSecurities = fraExtractor.extractSecurities();
    assertEquals(1, extractSecurities.length, "One fra expected");
    assertEquals(FRASecurity.class, extractSecurities[0].getClass(), "Expected instance of FraSecurity");
    
  }

  @Test(expectedExceptions={OpenGammaRuntimeException.class})
  public void testExtractSecuritiesBadPaymentDate() {
    FraTrade fra = createBasicFra();
    fra.setPaymentDate(fra.getEffectiveDate().plusDays(1));
    FraTradeSecurityExtractor fraExtractor = new FraTradeSecurityExtractor(fra);
    //should throw:
    fraExtractor.extractSecurities();
  }

  @Test
  public void testExtractSecuritiesWithCalendarInfo() {
    //should work, but will print a warning
    FraTrade fra = createBasicFra();
    fra.setDayCount("Modified Following");
    fra.setBusinessDayConvention("Actual/365");
    FraTradeSecurityExtractor fraExtractor = new FraTradeSecurityExtractor(fra);
    ManageableSecurity[] extractSecurities = fraExtractor.extractSecurities();
    assertEquals(1, extractSecurities.length, "One fra expected");
    assertEquals(FRASecurity.class, extractSecurities[0].getClass(), "Expected instance of FraSecurity");
  }
  
  /**
   * @return a fra with some fields set
   */
  private FraTrade createBasicFra(){
    FraTrade fra = new FraTrade();

    IdWrapper tradeId = createExternalId("IdFromExternalSystem", "External");
    IdWrapper regionId = createExternalId("IdFromExternalSystem", "External");
    IdWrapper counterparty = createExternalId("GOLDMAN", "Cpty");
    
    fra.setExternalSystemId(tradeId);
    fra.setTradeDate(LocalDate.of(2013, 1, 21));
    fra.setCounterparty(counterparty);
    fra.setPayFixed(true);
    fra.setRegionId(regionId);
    fra.setEffectiveDate(LocalDate.of(2013, 1, 23));
    fra.setPaymentDate(LocalDate.of(2013, 1, 23));
    fra.setFixingDate(LocalDate.of(2013, 2, 21));
    fra.setTerminationDate(LocalDate.of(2013, 5, 23));
    fra.setCurrency(Currency.USD);
    fra.setNotional(BigDecimal.valueOf(1000000));
    fra.setRate(BigDecimal.valueOf(105.25));
    
    FixingIndex fixingIndex = new FixingIndex();
    fixingIndex.setIndex(createExternalId("US0003M Curncy", "BLOOMBERG_TICKER").getExternalId());
    fixingIndex.setRateType(RateType.IBOR);
    fra.setFixingIndex(fixingIndex);
    
    return fra;
  }

  private IdWrapper createExternalId(String id, String scheme) {
    ExtId tradeExtId = new ExtId();
    tradeExtId.setId(id);
    tradeExtId.setScheme(scheme);
    IdWrapper tradeId = new IdWrapper();
    tradeId.setExternalId(tradeExtId);
    return tradeId;
  }

}
