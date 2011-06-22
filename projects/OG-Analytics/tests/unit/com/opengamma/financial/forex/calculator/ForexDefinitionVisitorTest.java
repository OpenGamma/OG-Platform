/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.calculator;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.forex.definition.ForexDefinition;
import com.opengamma.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.financial.forex.definition.ForexSwapDefinition;

/**
 * Tests the visitor of Forex definitions.
 */
public class ForexDefinitionVisitorTest {

  private static final ForexDefinition FX_DEFINITION = ForexInstrumentsDescriptionDataSets.createForexDefinition();
  private static final ForexSwapDefinition FX_SWAP_DEFINITION = ForexInstrumentsDescriptionDataSets.createForexSwapDefinition();
  private static final ForexOptionVanillaDefinition FX_OPTION_DEFINITION = ForexInstrumentsDescriptionDataSets.createForexOptionVanillaDefinition();

  @SuppressWarnings("synthetic-access")
  private static final MyVisitor<Object, String> VISITOR = new MyVisitor<Object, String>();

  @Test
  public void testVisitor() {
    final Object o = "G";
    assertEquals(FX_DEFINITION.accept(VISITOR), "Forex1");
    assertEquals(FX_DEFINITION.accept(VISITOR, o), "Forex2");
    assertEquals(FX_SWAP_DEFINITION.accept(VISITOR), "ForexSwap1");
    assertEquals(FX_SWAP_DEFINITION.accept(VISITOR, o), "ForexSwap2");
    assertEquals(FX_OPTION_DEFINITION.accept(VISITOR), "ForexOptionVanilla1");
    assertEquals(FX_OPTION_DEFINITION.accept(VISITOR, o), "ForexOptionVanilla2");
  }

  private static class MyVisitor<T, U> implements ForexDefinitionVisitor<T, String> {

    @Override
    public String visit(ForexConverter<?> definition, T data) {
      return null;
    }

    @Override
    public String visit(ForexConverter<?> definition) {
      return null;
    }

    @Override
    public String visitForexDefinition(ForexDefinition fx, T data) {
      return "Forex2";
    }

    @Override
    public String visitForexDefinition(ForexDefinition fx) {
      return "Forex1";
    }

    @Override
    public String visitForexSwapDefinition(ForexSwapDefinition fx, T data) {
      return "ForexSwap2";
    }

    @Override
    public String visitForexSwapDefinition(ForexSwapDefinition fx) {
      return "ForexSwap1";
    }

    @Override
    public String visitForexOptionVanillaDefinition(ForexOptionVanillaDefinition fx, T data) {
      return "ForexOptionVanilla2";
    }

    @Override
    public String visitForexOptionVanillaDefinition(ForexOptionVanillaDefinition fx) {
      return "ForexOptionVanilla1";
    }

  }

}
