/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.generator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.financial.generator.SecurityGenerator;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.util.function.BiFunction;
import com.opengamma.util.money.Currency;

/**
 * Utility for generating a portfolio of securities with OG_SYNTHETIC_TICKER tickers.
 */
public class SyntheticPortfolioGeneratorTool extends AbstractPortfolioGeneratorTool {

  @Override
  protected void configureChain(final SecurityGenerator<?> securityGenerator) {
    super.configureChain(securityGenerator);
    securityGenerator.setCurrencyCurveName("SECONDARY");
    securityGenerator.setCurveCalculationConfig(Currency.CHF, "DefaultTwoCurveCHFConfig");
    securityGenerator.setCurveCalculationConfig(Currency.EUR, "DefaultTwoCurveEURConfig");
    securityGenerator.setCurveCalculationConfig(Currency.GBP, "DefaultTwoCurveGBPConfig");
    securityGenerator.setCurveCalculationConfig(Currency.JPY, "DefaultTwoCurveJPYConfig");
    securityGenerator.setCurveCalculationConfig(Currency.USD, "DefaultTwoCurveUSDConfig");
    securityGenerator.setPreferredScheme(ExternalSchemes.OG_SYNTHETIC_TICKER);
    securityGenerator.setSpotRateIdentifier(new BiFunction<Currency, Currency, ExternalId>() {
      @Override
      public ExternalId apply(Currency a, Currency b) {
        return ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, a.getCode() + b.getCode());
      }
    });
  }

  public static void main(final String[] args) { // CSIGNORE
    AbstractTool<ToolContext> tool = new AbstractTool<ToolContext>() {
      private final SyntheticPortfolioGeneratorTool _instance = new SyntheticPortfolioGeneratorTool();
      @Override
      protected Options createOptions(boolean mandatoryConfigArg) {
        final Options options = super.createOptions(mandatoryConfigArg);
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
        return SyntheticPortfolioGeneratorTool.class;
      }
    };
    tool.invokeAndTerminate(args);
  }

}
