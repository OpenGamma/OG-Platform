/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;


import static org.testng.AssertJUnit.fail;

import org.testng.annotations.Test;

/**
 * 
 */
public class InstrumentDefinitionVisitorAdapterTest {

  @Test
  public void testImplemented() {
    final MyVisitor visitor = new MyVisitor();
    for (final InstrumentDefinition<?> definition : TestInstrumentDefinitions.getAllInstruments()) {
      try {
        definition.accept(visitor);
        fail();
      } catch (final UnsupportedOperationException e) {
      }
    }

    for (final InstrumentDefinition<?> definition : TestInstrumentDefinitions.getAllInstruments()) {
      try {
        definition.accept(visitor, "");
        fail();
      } catch (final UnsupportedOperationException e) {
      }
    }

    for (final InstrumentDefinition<?> definition : TestInstrumentDefinitions.getAllInstruments()) {
      try {
        definition.accept(visitor, null);
        fail();
      } catch (final UnsupportedOperationException e) {
      }
    }
  }

  private static class MyVisitor extends InstrumentDefinitionVisitorAdapter<Object, Object> {
    public MyVisitor() {
    }
  }
}
