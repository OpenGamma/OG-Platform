/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.financial.generator.SecurityGenerator;
import com.opengamma.id.ExternalId;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.util.function.BiFunction;
import com.opengamma.util.money.Currency;

/**
 * Utility for generating a portfolio of securities.
 */
public class PortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {

  protected void configureChain(final SecurityGenerator<?> securityGenerator) {
    super.configureChain(securityGenerator);
    securityGenerator.setCurrencyCurveName("DEFAULT");
    securityGenerator.setPreferredScheme(ExternalSchemes.BLOOMBERG_TICKER);
    securityGenerator.setSpotRateIdentifier(new BiFunction<Currency, Currency, ExternalId>() {
      @Override
      public ExternalId apply(Currency a, Currency b) {
        return ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, a.getCode() + b.getCode() + " Curncy");
      }
    });
  }

  public static void main(final String[] args) { // CSIGNORE
    AbstractTool<IntegrationToolContext> tool = new AbstractTool<IntegrationToolContext>() {
      private final PortfolioGeneratorTool _instance = new PortfolioGeneratorTool();
      @Override
      protected Options createOptions(boolean mandatoryConfigResource) {
        final Options options = super.createOptions(mandatoryConfigResource);
        _instance.createOptions(options);
        return options;
      }
      @Override
      protected void doRun() throws Exception {
        final CommandLine commandLine = getCommandLine();
        _instance.run(getToolContext(), commandLine);
      }
      @Override
      protected Class<?> getEntryPointClass() {
        return PortfolioGeneratorTool.class;
      }
    };
    tool.invokeAndTerminate(args);
  }

}
