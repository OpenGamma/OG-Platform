/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.exchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Standard provider of exchange data based on a CSV file.
 */
public class DefaultExchangeDataProvider implements ExchangeDataProvider {

  private static final String EXCHANGE_ISO_FILE = "ISO10383MIC.csv";
  private Map<String, Exchange> _exchangeMap = new HashMap<String, Exchange>();

  /**
   * Creates an instance.
   */
  public DefaultExchangeDataProvider() {
    loadExchangeData();
  }

  /**
   * Loads the exchange data
   * 
   * @param filename  the filename, not null
   */
  private void loadExchangeData() {
    InputStream is = getClass().getResourceAsStream(EXCHANGE_ISO_FILE);
    if (is == null) {
      throw new OpenGammaRuntimeException("Unable to locate " + EXCHANGE_ISO_FILE);
    }
    CSVReader exchangeIsoReader = new CSVReader(new InputStreamReader(is), CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, CSVParser.DEFAULT_ESCAPE_CHARACTER, 1);
    String [] nextLine;
    try {
      while ((nextLine = exchangeIsoReader.readNext()) != null) {
        String micCode = nextLine[0];
        String description = nextLine[1];
        String countryCode = nextLine[2];
        String country = nextLine[3];
        String city = nextLine[4];
        String acr = nextLine[5];
        String status = nextLine[7];
        if (StringUtils.isNotBlank(micCode) && StringUtils.isNotBlank(countryCode)) {
          Exchange exchange = new Exchange();
          exchange.setMic(micCode);
          exchange.setDescription(description);
          exchange.setCountryCode(countryCode);
          exchange.setCountry(country);
          exchange.setCity(city);
          exchange.setAcr(acr);
          exchange.setStatus(status);
          _exchangeMap.put(micCode, exchange);
        }
      }
      exchangeIsoReader.close();
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Unable to read from " + EXCHANGE_ISO_FILE, e);
    }
  }

  @Override
  public Exchange getExchange(String micCode) {
    return _exchangeMap.get(micCode);
  }

}
