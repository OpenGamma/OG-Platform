/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bloombergexample.tool;


import static com.opengamma.bloombergexample.loader.PortfolioLoaderHelper.getWithException;
import static com.opengamma.bloombergexample.loader.PortfolioLoaderHelper.normaliseHeaders;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.bloombergexample.loader.ExampleEquityPortfolioLoader;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.GICSCode;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Abstract class for tools that sets up a tool context.
 */
public abstract class AbstractExampleTool extends AbstractTool {

  /**
   * Example configuration for tools.
   */
  public static final String TOOLCONTEXT_EXAMPLE_PROPERTIES = "classpath:toolcontext/toolcontext-example.properties";
  
  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractExampleTool.class);

  @Override
  public boolean initAndRun(String[] args) {
    return super.initAndRun(args, TOOLCONTEXT_EXAMPLE_PROPERTIES, null);
  }

  protected Collection<EquitySecurity> readEquitySecurities() {
    Collection<EquitySecurity> equities = new ArrayList<EquitySecurity>();
    InputStream inputStream = ExampleEquityPortfolioLoader.class.getResourceAsStream("example-equity.csv");
    try {
      if (inputStream != null) {
        CSVReader csvReader = new CSVReader(new InputStreamReader(inputStream));

        String[] headers = csvReader.readNext();
        normaliseHeaders(headers);

        String[] line;
        int rowIndex = 1;
        while ((line = csvReader.readNext()) != null) {
          Map<String, String> equityDetails = new HashMap<String, String>();
          for (int i = 0; i < headers.length; i++) {
            if (i >= line.length) {
              // Run out of headers for this line
              break;
            }
            equityDetails.put(headers[i], line[i]);
          }
          try {
            equities.add(parseEquity(equityDetails));
          } catch (Exception e) {
            s_logger.warn("Skipped row " + rowIndex + " because of an error", e);
          }
          rowIndex++;
        }
      }
    } catch (FileNotFoundException ex) {
      throw new OpenGammaRuntimeException("File '" + inputStream + "' could not be found");
    } catch (IOException ex) {
      throw new OpenGammaRuntimeException("An error occurred while reading file '" + inputStream + "'");
    }

    StringBuilder sb = new StringBuilder();
    sb.append("Parsed ").append(equities.size()).append(" equities:\n");
    for (EquitySecurity equity : equities) {
      sb.append("\t").append(equity.getName()).append("\n");
    }
    s_logger.info(sb.toString());

    return equities;
  }

  protected EquitySecurity parseEquity(Map<String, String> equityDetails) {
    String companyName = getWithException(equityDetails, "companyname");
    String currency = getWithException(equityDetails, "currency");
    String exchange = getWithException(equityDetails, "exchange");
    String exchangeCode = getWithException(equityDetails, "exchangecode");
    String gicsCode = getWithException(equityDetails, "giscode");
    String isin = getWithException(equityDetails, "isin");
    String cusip = getWithException(equityDetails, "cusip");
    String ticker = getWithException(equityDetails, "ticker");

    return createEquitySecurity(companyName, Currency.of(currency), exchange, exchangeCode, gicsCode,
      ExternalId.of(SecurityUtils.ISIN, isin),
      ExternalId.of(SecurityUtils.CUSIP, cusip),
      ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, ticker));
  }

  protected EquitySecurity createEquitySecurity(String companyName, Currency currency, String exchange, String exchangeCode, String gicsCode, ExternalId... identifiers) {
    EquitySecurity equitySecurity = new EquitySecurity(exchange, exchangeCode, companyName, currency);
    equitySecurity.setGicsCode(GICSCode.of(gicsCode));
    equitySecurity.setExternalIdBundle(ExternalIdBundle.of(identifiers));
    equitySecurity.setName(companyName);
    return equitySecurity;
  }

}
