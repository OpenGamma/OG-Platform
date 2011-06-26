/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.calculator;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.forex.definition.ForexDefinition;
import com.opengamma.financial.forex.definition.ForexOptionSingleBarrierDefinition;
import com.opengamma.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.financial.forex.definition.ForexSwapDefinition;

/**
 * Tests the visitor of Forex definitions.
 */
public class ForexDefinitionVisitorTest {

  private static final ForexDefinition FX_DEFINITION = ForexInstrumentsDescriptionDataSet.createForexDefinition();
  private static final ForexSwapDefinition FX_SWAP_DEFINITION = ForexInstrumentsDescriptionDataSet.createForexSwapDefinition();
  private static final ForexOptionVanillaDefinition FX_OPTION_DEFINITION = ForexInstrumentsDescriptionDataSet.createForexOptionVanillaDefinition();
  private static final ForexOptionSingleBarrierDefinition FX_SINGLE_BARRIER_OPTION_DEFINITION = ForexInstrumentsDescriptionDataSet.createForexOptionSingleBarrierDefinition();

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
    assertEquals(FX_SINGLE_BARRIER_OPTION_DEFINITION.accept(VISITOR, o), "ForexOptionSingleBarrier2");
    assertEquals(FX_SINGLE_BARRIER_OPTION_DEFINITION.accept(VISITOR), "ForexOptionSingleBarrier1");
  }

  private static class MyVisitor<T, U> implements ForexDefinitionVisitor<T, String> {

    @Override
    public String visit(final ForexConverter<?> definition, final T data) {
      return null;
    }

    @Override
    public String visit(final ForexConverter<?> definition) {
      return null;
    }

    @Override
    public String visitForexDefinition(final ForexDefinition fx, final T data) {
      return "Forex2";
    }

    @Override
    public String visitForexDefinition(final ForexDefinition fx) {
      return "Forex1";
    }

    @Override
    public String visitForexSwapDefinition(final ForexSwapDefinition fx, final T data) {
      return "ForexSwap2";
    }

    @Override
    public String visitForexSwapDefinition(final ForexSwapDefinition fx) {
      return "ForexSwap1";
    }

    @Override
    public String visitForexOptionVanillaDefinition(final ForexOptionVanillaDefinition fx, final T data) {
      return "ForexOptionVanilla2";
    }

    @Override
    public String visitForexOptionVanillaDefinition(final ForexOptionVanillaDefinition fx) {
      return "ForexOptionVanilla1";
    }

    @Override
    public String visitForexOptionSingleBarrierDefiniton(final ForexOptionSingleBarrierDefinition fx, final T data) {
      return "ForexOptionSingleBarrier2";
    }

    @Override
    public String visitForexOptionSingleBarrierDefiniton(final ForexOptionSingleBarrierDefinition fx) {
      return "ForexOptionSingleBarrier1";
    }

  }

}
