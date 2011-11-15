/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.calculator;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.financial.forex.derivative.ForexNonDeliverableOption;
import com.opengamma.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.financial.forex.derivative.ForexSwap;

/**
 * Tests the visitor of Forex derivatives.
 */
public class ForexDerivativeVisitorTest {

  private static final Forex FX = ForexInstrumentsDescriptionDataSet.createForex();
  private static final ForexSwap FX_SWAP = ForexInstrumentsDescriptionDataSet.createForexSwap();
  private static final ForexOptionVanilla FX_OPTION = ForexInstrumentsDescriptionDataSet.createForexOptionVanilla();
  private static final ForexOptionSingleBarrier FX_OPTION_SINGLE_BARRIER = ForexInstrumentsDescriptionDataSet.createForexOptionSingleBarrier();
  private static final ForexNonDeliverableForward NDF = ForexInstrumentsDescriptionDataSet.createForexNonDeliverableForward();
  private static final ForexNonDeliverableOption NDO = ForexInstrumentsDescriptionDataSet.createForexNonDeliverableOption();

  @SuppressWarnings("synthetic-access")
  private static final MyVisitor<Object, String> VISITOR = new MyVisitor<Object, String>();

  @SuppressWarnings("synthetic-access")
  private static final MyAbstractVisitor<Object, String> VISITOR_ABSTRACT = new MyAbstractVisitor<Object, String>();

  @Test
  public void testVisitor() {
    final Object o = "G";
    assertEquals(FX.accept(VISITOR), "Forex1");
    assertEquals(FX.accept(VISITOR, o), "Forex2");
    assertEquals(FX_SWAP.accept(VISITOR), "ForexSwap1");
    assertEquals(FX_SWAP.accept(VISITOR, o), "ForexSwap2");
    assertEquals(FX_OPTION.accept(VISITOR), "ForexOptionVanilla1");
    assertEquals(FX_OPTION.accept(VISITOR, o), "ForexOptionVanilla2");
    assertEquals(FX_OPTION_SINGLE_BARRIER.accept(VISITOR), "ForexOptionSingleBarrier1");
    assertEquals(FX_OPTION_SINGLE_BARRIER.accept(VISITOR, o), "ForexOptionSingleBarrier2");
    assertEquals(NDF.accept(VISITOR), "ForexNonDeliverableForward1");
    assertEquals(NDF.accept(VISITOR, o), "ForexNonDeliverableForward2");
    assertEquals(NDO.accept(VISITOR), "ForexNonDeliverableOption1");
    assertEquals(NDO.accept(VISITOR, o), "ForexNonDeliverableOption2");
  }

  @Test
  public void testAbstractVisitorException() {
    final Object o = "G";
    testException(FX);
    testException(FX, o);
    testException(FX_SWAP);
    testException(FX_SWAP, o);
    testException(FX_OPTION);
    testException(FX_OPTION, o);
    testException(FX_OPTION_SINGLE_BARRIER);
    testException(FX_OPTION_SINGLE_BARRIER, o);
    testException(NDF);
    testException(NDF, o);
    testException(NDO);
    testException(NDO, o);
    final ForexDerivative[] forexArray = new ForexDerivative[] {FX, FX_SWAP};
    try {
      VISITOR_ABSTRACT.visit(forexArray[0]);
      assertTrue(false);
    } catch (final UnsupportedOperationException e) {
      assertTrue(true);
    } catch (final Exception e) {
      assertTrue(false);
    }
    try {
      VISITOR_ABSTRACT.visit(forexArray);
      assertTrue(false);
    } catch (final UnsupportedOperationException e) {
      assertTrue(true);
    } catch (final Exception e) {
      assertTrue(false);
    }
    try {
      VISITOR_ABSTRACT.visit(forexArray, o);
      assertTrue(false);
    } catch (final UnsupportedOperationException e) {
      assertTrue(true);
    } catch (final Exception e) {
      assertTrue(false);
    }
  }

  private void testException(final ForexDerivative fx) {
    try {
      fx.accept(VISITOR_ABSTRACT);
      assertTrue(false);
    } catch (final UnsupportedOperationException e) {
      assertTrue(true);
    } catch (final Exception e) {
      assertTrue(false);
    }
  }

  private void testException(final ForexDerivative fx, final Object o) {
    try {
      fx.accept(VISITOR_ABSTRACT, o);
      assertTrue(false);
    } catch (final UnsupportedOperationException e) {
      assertTrue(true);
    } catch (final Exception e) {
      assertTrue(false);
    }
  }

  private static class MyVisitor<T, U> implements ForexDerivativeVisitor<T, String> {

    @Override
    public String visit(final ForexDerivative derivative, final T data) {
      return null;
    }

    @Override
    public String visit(final ForexDerivative derivative) {
      return null;
    }

    @Override
    public String[] visit(final ForexDerivative[] derivative, final T data) {
      return null;
    }

    @Override
    public String[] visit(final ForexDerivative[] derivative) {
      return null;
    }

    @Override
    public String visitForex(final Forex derivative, final T data) {
      return "Forex2";
    }

    @Override
    public String visitForex(final Forex derivative) {
      return "Forex1";
    }

    @Override
    public String visitForexSwap(final ForexSwap derivative, final T data) {
      return "ForexSwap2";
    }

    @Override
    public String visitForexSwap(final ForexSwap derivative) {
      return "ForexSwap1";
    }

    @Override
    public String visitForexOptionVanilla(final ForexOptionVanilla derivative, final T data) {
      return "ForexOptionVanilla2";
    }

    @Override
    public String visitForexOptionVanilla(final ForexOptionVanilla derivative) {
      return "ForexOptionVanilla1";
    }

    @Override
    public String visitForexOptionSingleBarrier(final ForexOptionSingleBarrier derivative, final T data) {
      return "ForexOptionSingleBarrier2";
    }

    @Override
    public String visitForexOptionSingleBarrier(final ForexOptionSingleBarrier derivative) {
      return "ForexOptionSingleBarrier1";
    }

    @Override
    public String visitForexNonDeliverableForward(ForexNonDeliverableForward derivative, T data) {
      return "ForexNonDeliverableForward2";
    }

    @Override
    public String visitForexNonDeliverableForward(ForexNonDeliverableForward derivative) {
      return "ForexNonDeliverableForward1";
    }

    @Override
    public String visitForexNonDeliverableOption(ForexNonDeliverableOption derivative, T data) {
      return "ForexNonDeliverableOption2";
    }

    @Override
    public String visitForexNonDeliverableOption(ForexNonDeliverableOption derivative) {
      return "ForexNonDeliverableOption1";
    }

  }

  private static class MyAbstractVisitor<T, U> extends AbstractForexDerivativeVisitor<T, String> {

  }

}
