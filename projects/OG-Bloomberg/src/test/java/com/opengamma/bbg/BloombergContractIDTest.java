/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link BloombergContractID} 
 */
@Test(groups = TestGroup.UNIT)
public class BloombergContractIDTest {
  
  private static final Integer[] YEARS = {1, 11, 2011};
  
  private static final BloombergContractID BBG_CONTRACT = new BloombergContractID("ED", "comdty");
  
  private static final OptionType[] OPTION_TYPES = {OptionType.CALL, OptionType.PUT};
  
  public void toFutureExternalId() {
    for (Integer year : YEARS) {
      assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDF1 COMDTY"), BBG_CONTRACT.toFutureExternalId(1, year));
      assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDG1 COMDTY"), BBG_CONTRACT.toFutureExternalId(2, year));
      assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDH1 COMDTY"), BBG_CONTRACT.toFutureExternalId(3, year));
      assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDJ1 COMDTY"), BBG_CONTRACT.toFutureExternalId(4, year));
      assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDK1 COMDTY"), BBG_CONTRACT.toFutureExternalId(5, year));
      assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDM1 COMDTY"), BBG_CONTRACT.toFutureExternalId(6, year));
      assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDN1 COMDTY"), BBG_CONTRACT.toFutureExternalId(7, year));
      assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDQ1 COMDTY"), BBG_CONTRACT.toFutureExternalId(8, year));
      assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDU1 COMDTY"), BBG_CONTRACT.toFutureExternalId(9, year));
      assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDV1 COMDTY"), BBG_CONTRACT.toFutureExternalId(10, year));
      assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDX1 COMDTY"), BBG_CONTRACT.toFutureExternalId(11, year));
      assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDZ1 COMDTY"), BBG_CONTRACT.toFutureExternalId(12, year));
    }
  }
  
  public void toOptionExternalId() {
    for (OptionType optionType : OPTION_TYPES) {
      if (optionType == OptionType.CALL) {
        assertEquals(ExternalSchemes.bloombergTickerSecurityId("ED 01/2011 C1.234 COMDTY"), BBG_CONTRACT.toOptionExternalId(1, 2011, 1.234, optionType));
        assertEquals(ExternalSchemes.bloombergTickerSecurityId("ED 02/2011 C1.234 COMDTY"), BBG_CONTRACT.toOptionExternalId(2, 2011, 1.234, optionType));
        assertEquals(ExternalSchemes.bloombergTickerSecurityId("ED 03/2011 C1.234 COMDTY"), BBG_CONTRACT.toOptionExternalId(3, 2011, 1.234, optionType));
        assertEquals(ExternalSchemes.bloombergTickerSecurityId("ED 04/2011 C1.234 COMDTY"), BBG_CONTRACT.toOptionExternalId(4, 2011, 1.234, optionType));
        assertEquals(ExternalSchemes.bloombergTickerSecurityId("ED 05/2011 C1.234 COMDTY"), BBG_CONTRACT.toOptionExternalId(5, 2011, 1.234, optionType));
        assertEquals(ExternalSchemes.bloombergTickerSecurityId("ED 06/2011 C1.234 COMDTY"), BBG_CONTRACT.toOptionExternalId(6, 2011, 1.234, optionType));
        assertEquals(ExternalSchemes.bloombergTickerSecurityId("ED 07/2011 C1.234 COMDTY"), BBG_CONTRACT.toOptionExternalId(7, 2011, 1.234, optionType));
        assertEquals(ExternalSchemes.bloombergTickerSecurityId("ED 08/2011 C1.234 COMDTY"), BBG_CONTRACT.toOptionExternalId(8, 2011, 1.234, optionType));
        assertEquals(ExternalSchemes.bloombergTickerSecurityId("ED 09/2011 C1.234 COMDTY"), BBG_CONTRACT.toOptionExternalId(9, 2011, 1.234, optionType));
        assertEquals(ExternalSchemes.bloombergTickerSecurityId("ED 10/2011 C1.234 COMDTY"), BBG_CONTRACT.toOptionExternalId(10, 2011, 1.234, optionType));
        assertEquals(ExternalSchemes.bloombergTickerSecurityId("ED 11/2011 C1.234 COMDTY"), BBG_CONTRACT.toOptionExternalId(11, 2011, 1.234, optionType));
        assertEquals(ExternalSchemes.bloombergTickerSecurityId("ED 12/2011 C1.234 COMDTY"), BBG_CONTRACT.toOptionExternalId(12, 2011, 1.234, optionType));
      } else {
        assertEquals(ExternalSchemes.bloombergTickerSecurityId("ED 01/2011 P1.234 COMDTY"), BBG_CONTRACT.toOptionExternalId(1, 2011, 1.234, optionType));
        assertEquals(ExternalSchemes.bloombergTickerSecurityId("ED 02/2011 P1.234 COMDTY"), BBG_CONTRACT.toOptionExternalId(2, 2011, 1.234, optionType));
        assertEquals(ExternalSchemes.bloombergTickerSecurityId("ED 03/2011 P1.234 COMDTY"), BBG_CONTRACT.toOptionExternalId(3, 2011, 1.234, optionType));
        assertEquals(ExternalSchemes.bloombergTickerSecurityId("ED 04/2011 P1.234 COMDTY"), BBG_CONTRACT.toOptionExternalId(4, 2011, 1.234, optionType));
        assertEquals(ExternalSchemes.bloombergTickerSecurityId("ED 05/2011 P1.234 COMDTY"), BBG_CONTRACT.toOptionExternalId(5, 2011, 1.234, optionType));
        assertEquals(ExternalSchemes.bloombergTickerSecurityId("ED 06/2011 P1.234 COMDTY"), BBG_CONTRACT.toOptionExternalId(6, 2011, 1.234, optionType));
        assertEquals(ExternalSchemes.bloombergTickerSecurityId("ED 07/2011 P1.234 COMDTY"), BBG_CONTRACT.toOptionExternalId(7, 2011, 1.234, optionType));
        assertEquals(ExternalSchemes.bloombergTickerSecurityId("ED 08/2011 P1.234 COMDTY"), BBG_CONTRACT.toOptionExternalId(8, 2011, 1.234, optionType));
        assertEquals(ExternalSchemes.bloombergTickerSecurityId("ED 09/2011 P1.234 COMDTY"), BBG_CONTRACT.toOptionExternalId(9, 2011, 1.234, optionType));
        assertEquals(ExternalSchemes.bloombergTickerSecurityId("ED 10/2011 P1.234 COMDTY"), BBG_CONTRACT.toOptionExternalId(10, 2011, 1.234, optionType));
        assertEquals(ExternalSchemes.bloombergTickerSecurityId("ED 11/2011 P1.234 COMDTY"), BBG_CONTRACT.toOptionExternalId(11, 2011, 1.234, optionType));
        assertEquals(ExternalSchemes.bloombergTickerSecurityId("ED 12/2011 P1.234 COMDTY"), BBG_CONTRACT.toOptionExternalId(12, 2011, 1.234, optionType));
      }
    }
  }
  
  public void toFutureOptionExternalId() {
    for (Integer year : YEARS) {
      for (OptionType optionType : OPTION_TYPES) {
        if (optionType == OptionType.CALL) {
          assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDF1C 1.234 COMDTY"), BBG_CONTRACT.toFutureOptionExternalId(1, year, 1.234, optionType));
          assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDG1C 1.234 COMDTY"), BBG_CONTRACT.toFutureOptionExternalId(2, year, 1.234, optionType));
          assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDH1C 1.234 COMDTY"), BBG_CONTRACT.toFutureOptionExternalId(3, year, 1.234, optionType));
          assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDJ1C 1.234 COMDTY"), BBG_CONTRACT.toFutureOptionExternalId(4, year, 1.234, optionType));
          assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDK1C 1.234 COMDTY"), BBG_CONTRACT.toFutureOptionExternalId(5, year, 1.234, optionType));
          assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDM1C 1.234 COMDTY"), BBG_CONTRACT.toFutureOptionExternalId(6, year, 1.234, optionType));
          assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDN1C 1.234 COMDTY"), BBG_CONTRACT.toFutureOptionExternalId(7, year, 1.234, optionType));
          assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDQ1C 1.234 COMDTY"), BBG_CONTRACT.toFutureOptionExternalId(8, year, 1.234, optionType));
          assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDU1C 1.234 COMDTY"), BBG_CONTRACT.toFutureOptionExternalId(9, year, 1.234, optionType));
          assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDV1C 1.234 COMDTY"), BBG_CONTRACT.toFutureOptionExternalId(10, year, 1.234, optionType));
          assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDX1C 1.234 COMDTY"), BBG_CONTRACT.toFutureOptionExternalId(11, year, 1.234, optionType));
          assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDZ1C 1.234 COMDTY"), BBG_CONTRACT.toFutureOptionExternalId(12, year, 1.234, optionType));
        } else {
          assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDF1P 1.234 COMDTY"), BBG_CONTRACT.toFutureOptionExternalId(1, year, 1.234, optionType));
          assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDG1P 1.234 COMDTY"), BBG_CONTRACT.toFutureOptionExternalId(2, year, 1.234, optionType));
          assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDH1P 1.234 COMDTY"), BBG_CONTRACT.toFutureOptionExternalId(3, year, 1.234, optionType));
          assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDJ1P 1.234 COMDTY"), BBG_CONTRACT.toFutureOptionExternalId(4, year, 1.234, optionType));
          assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDK1P 1.234 COMDTY"), BBG_CONTRACT.toFutureOptionExternalId(5, year, 1.234, optionType));
          assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDM1P 1.234 COMDTY"), BBG_CONTRACT.toFutureOptionExternalId(6, year, 1.234, optionType));
          assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDN1P 1.234 COMDTY"), BBG_CONTRACT.toFutureOptionExternalId(7, year, 1.234, optionType));
          assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDQ1P 1.234 COMDTY"), BBG_CONTRACT.toFutureOptionExternalId(8, year, 1.234, optionType));
          assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDU1P 1.234 COMDTY"), BBG_CONTRACT.toFutureOptionExternalId(9, year, 1.234, optionType));
          assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDV1P 1.234 COMDTY"), BBG_CONTRACT.toFutureOptionExternalId(10, year, 1.234, optionType));
          assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDX1P 1.234 COMDTY"), BBG_CONTRACT.toFutureOptionExternalId(11, year, 1.234, optionType));
          assertEquals(ExternalSchemes.bloombergTickerSecurityId("EDZ1P 1.234 COMDTY"), BBG_CONTRACT.toFutureOptionExternalId(12, year, 1.234, optionType));
        }
      }
    }
  }
  
  public void padding() {
    final BloombergContractID id = new BloombergContractID("S", "Comdty");
    assertEquals(id.getContractCode(), "S ");
    assertEquals(id.getMarketSector(), "Comdty");
    final BloombergContractID idGC = new BloombergContractID("GC", "Comdty");
    assertEquals(idGC.getContractCode(), "GC");
    assertEquals(idGC.getMarketSector(), "Comdty");
  }

}
