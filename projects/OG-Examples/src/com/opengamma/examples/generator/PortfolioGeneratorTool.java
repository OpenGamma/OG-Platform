/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.generator;

import org.apache.commons.cli.Options;

import com.opengamma.examples.tool.AbstractExampleTool;

/**
 * Utility for generating a portfolio of securities.
 */
public class PortfolioGeneratorTool extends com.opengamma.financial.generator.PortfolioGeneratorTool {

  /**
   * Default portfolio size used by sub-classes.
   */
  protected static final int PORTFOLIO_SIZE = 200;

  public static void main(final String[] args) { // CSIGNORE
    (new AbstractExampleTool() {

      private final PortfolioGeneratorTool _instance = new PortfolioGeneratorTool();

      @Override
      protected Options createOptions(boolean mandatoryConfigArg) {
        final Options options = super.createOptions(mandatoryConfigArg);
        _instance.createOptions(options);
        return options;
      }

      @Override
      protected void doRun() throws Exception {
        _instance.run(getToolContext(), getCommandLine());
      }

      @Override
      protected Class<?> getEntryPointClass() {
        return PortfolioGeneratorTool.class;
      }

    }).initAndRun(args);
    System.exit(0);
  }

}
