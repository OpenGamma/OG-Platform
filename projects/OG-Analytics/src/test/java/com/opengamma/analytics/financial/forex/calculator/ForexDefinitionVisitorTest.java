/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.calculator;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexNonDeliverableForwardDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexNonDeliverableOptionDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionDigitalDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionSingleBarrierDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexSwapDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitorAdapter;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the visitor of Forex definitions.
 */
@Test(groups = TestGroup.UNIT)
public class ForexDefinitionVisitorTest {

  private static final ForexDefinition FX_DEFINITION = ForexInstrumentsDescriptionDataSet.createForexDefinition();
  private static final ForexSwapDefinition FX_SWAP_DEFINITION = ForexInstrumentsDescriptionDataSet.createForexSwapDefinition();
  private static final ForexOptionVanillaDefinition FX_OPTION_DEFINITION = ForexInstrumentsDescriptionDataSet.createForexOptionVanillaDefinition();
  private static final ForexOptionSingleBarrierDefinition FX_SINGLE_BARRIER_OPTION_DEFINITION = ForexInstrumentsDescriptionDataSet
      .createForexOptionSingleBarrierDefinition();
  private static final ForexNonDeliverableForwardDefinition NDF_DEFINITION = ForexInstrumentsDescriptionDataSet.createForexNonDeliverableForwardDefinition();
  private static final ForexNonDeliverableOptionDefinition NDO_DEFINITION = ForexInstrumentsDescriptionDataSet.createForexNonDeliverableOptionDefinition();
  private static final ForexOptionDigitalDefinition FX_OPTION_DIGITAL_DEFINITION = ForexInstrumentsDescriptionDataSet.createForexOptionDigitalDefinition();

  @SuppressWarnings("synthetic-access")
  private static final MyVisitor<Object> VISITOR = new MyVisitor<>();

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
    assertEquals(NDF_DEFINITION.accept(VISITOR), "ForexNonDeliverableForwardDefinition1");
    assertEquals(NDF_DEFINITION.accept(VISITOR, o), "ForexNonDeliverableForwardDefinition2");
    assertEquals(NDO_DEFINITION.accept(VISITOR), "ForexNonDeliverableOptionDefinition1");
    assertEquals(NDO_DEFINITION.accept(VISITOR, o), "ForexNonDeliverableOptionDefinition2");
    assertEquals(FX_OPTION_DIGITAL_DEFINITION.accept(VISITOR), "ForexOptionDigital1");
    assertEquals(FX_OPTION_DIGITAL_DEFINITION.accept(VISITOR, o), "ForexOptionDigital2");
  }

  private static class MyVisitor<T> extends InstrumentDefinitionVisitorAdapter<T, String> {

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

    @Override
    public String visitForexNonDeliverableForwardDefinition(final ForexNonDeliverableForwardDefinition ndf, final T data) {
      return "ForexNonDeliverableForwardDefinition2";
    }

    @Override
    public String visitForexNonDeliverableForwardDefinition(final ForexNonDeliverableForwardDefinition ndf) {
      return "ForexNonDeliverableForwardDefinition1";
    }

    @Override
    public String visitForexNonDeliverableOptionDefinition(final ForexNonDeliverableOptionDefinition ndo, final T data) {
      return "ForexNonDeliverableOptionDefinition2";
    }

    @Override
    public String visitForexNonDeliverableOptionDefinition(final ForexNonDeliverableOptionDefinition ndo) {
      return "ForexNonDeliverableOptionDefinition1";
    }

    @Override
    public String visitForexOptionDigitalDefinition(final ForexOptionDigitalDefinition fx, final T data) {
      return "ForexOptionDigital2";
    }

    @Override
    public String visitForexOptionDigitalDefinition(final ForexOptionDigitalDefinition fx) {
      return "ForexOptionDigital1";
    }

  }

}
