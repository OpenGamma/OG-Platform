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
import com.opengamma.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.financial.forex.derivative.ForexSwap;

/**
 * Tests the visitor of Forex derivatives.
 */
public class ForexDerivativeVisitorTest {

  private static final Forex FX = ForexInstrumentsDescriptionDataSet.createForex();
  private static final ForexSwap FX_SWAP = ForexInstrumentsDescriptionDataSet.createForexSwap();
  private static final ForexOptionVanilla FX_OPTION = ForexInstrumentsDescriptionDataSet.createForexOptionVanilla();

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
    ForexDerivative[] forexArray = new ForexDerivative[] {FX, FX_SWAP};
    try {
      VISITOR_ABSTRACT.visit(forexArray[0]);
      assertTrue(false);
    } catch (UnsupportedOperationException e) {
      assertTrue(true);
    } catch (Exception e) {
      assertTrue(false);
    }
    try {
      VISITOR_ABSTRACT.visit(forexArray);
      assertTrue(false);
    } catch (UnsupportedOperationException e) {
      assertTrue(true);
    } catch (Exception e) {
      assertTrue(false);
    }
    try {
      VISITOR_ABSTRACT.visit(forexArray, o);
      assertTrue(false);
    } catch (UnsupportedOperationException e) {
      assertTrue(true);
    } catch (Exception e) {
      assertTrue(false);
    }
  }

  private void testException(ForexDerivative fx) {
    try {
      fx.accept(VISITOR_ABSTRACT);
      assertTrue(false);
    } catch (UnsupportedOperationException e) {
      assertTrue(true);
    } catch (Exception e) {
      assertTrue(false);
    }
  }

  private void testException(ForexDerivative fx, Object o) {
    try {
      fx.accept(VISITOR_ABSTRACT, o);
      assertTrue(false);
    } catch (UnsupportedOperationException e) {
      assertTrue(true);
    } catch (Exception e) {
      assertTrue(false);
    }
  }

  private static class MyVisitor<T, U> implements ForexDerivativeVisitor<T, String> {

    @Override
    public String visit(ForexDerivative derivative, T data) {
      return null;
    }

    @Override
    public String visit(ForexDerivative derivative) {
      return null;
    }

    @Override
    public String[] visit(ForexDerivative[] derivative, T data) {
      return null;
    }

    @Override
    public String[] visit(ForexDerivative[] derivative) {
      return null;
    }

    @Override
    public String visitForex(Forex derivative, T data) {
      return "Forex2";
    }

    @Override
    public String visitForex(Forex derivative) {
      return "Forex1";
    }

    @Override
    public String visitForexSwap(ForexSwap derivative, T data) {
      return "ForexSwap2";
    }

    @Override
    public String visitForexSwap(ForexSwap derivative) {
      return "ForexSwap1";
    }

    @Override
    public String visitForexOptionVanilla(ForexOptionVanilla derivative, T data) {
      return "ForexOptionVanilla2";
    }

    @Override
    public String visitForexOptionVanilla(ForexOptionVanilla derivative) {
      return "ForexOptionVanilla1";
    }

  }

  private static class MyAbstractVisitor<T, U> extends AbstractForexDerivativeVisitor<T, String> {

  }

}
