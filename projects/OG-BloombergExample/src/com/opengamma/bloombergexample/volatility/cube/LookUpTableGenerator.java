/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bloombergexample.volatility.cube;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.time.calendar.Period;

import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.marketdatasnapshot.VolatilityPoint;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;

/**
 * 
 */
public class LookUpTableGenerator {
  
  private static final String TICKER_FILE = "VolatilityCubeIdentifierLookupTable.csv";

  @Test
  public void generate() {
    final InputStream is = getClass().getResourceAsStream(TICKER_FILE);
    final CSVReader csvReader = new CSVReader(new InputStreamReader(is), CSVParser.DEFAULT_SEPARATOR,
        CSVParser.DEFAULT_QUOTE_CHARACTER, CSVParser.DEFAULT_ESCAPE_CHARACTER, 1);
    
    String[] nextLine;
    try {
      while ((nextLine = csvReader.readNext()) != null) {
        //TODO: are these the right way round (copied from VolatilityCubeConfigPopulator)
        final String currencyIso = nextLine[1];
        final String expiry = nextLine[2];
        final String expiryUnit = nextLine[3];
        final String swapPeriod = nextLine[4];
        final String swapPeriodUnit = nextLine[5];
        final String payOrReceive = nextLine[6];
        final String relativeStrike = nextLine[7];
        final String ticker = nextLine[8];

        if (ticker != null) {
          final Currency currency = Currency.of(currencyIso);
          final Tenor swapTenor = new Tenor(Period.parse("P" + swapPeriod + swapPeriodUnit));
          final Tenor optionExpiry = new Tenor(Period.parse("P" + expiry + expiryUnit));
          double sign;
          if ("PY".equals(payOrReceive)) {
            sign = -1;
          } else if ("RC".equals(payOrReceive)) {
            sign = 1;
          } else {
            throw new IllegalArgumentException();
          }

          final Double relativeStrikeRaw = Double.valueOf(relativeStrike);
          final double normalizedStrike = relativeStrikeRaw > 10 ? relativeStrikeRaw : relativeStrikeRaw * 100;
          Double relativeStrikeBps = sign * normalizedStrike;
          if (relativeStrikeBps == -0.0) {
            //Apparently the volatilities should be the same, so lets avoid fudge pains
            relativeStrikeBps = 0.0;
          }
          final VolatilityPoint point = new VolatilityPoint(swapTenor, optionExpiry, relativeStrikeBps);
          System.err.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n", 
              nextLine[0], currencyIso, expiry, expiryUnit, swapPeriod, swapPeriodUnit, payOrReceive, relativeStrike, getSyntheticId(currency, optionExpiry, swapTenor, relativeStrikeBps));

//          final ExternalId identifier = getIdentifier(ticker + " Curncy");
//
//          final ObjectsPair<Currency, VolatilityPoint> key = Pair.of(currency, point);
//          if (_idsByPoint.containsKey(key)) {
//            _idsByPoint.get(key).add(identifier);
//          } else {
//            _idsByPoint.put(key, Sets.newHashSet(identifier));
//          }
        }
      }
      csvReader.close();
    } catch (final IOException e) {
      throw new OpenGammaRuntimeException("Unable to read from " + TICKER_FILE, e);
    }
  }

  private String getSyntheticId(Currency currency, Tenor optionExpiry, Tenor swapTenor, Double relativeStrikeBps) {
    String relativeStrike = null;
    if (relativeStrikeBps < 0) {
      relativeStrike = "N" + Math.abs(relativeStrikeBps);
    } else {
      relativeStrike = "P" + Math.abs(relativeStrikeBps);
    }
    return String.format("%sSWAPTIONVOL%s%s%s", currency.getCode(), 
        formatPeriod(optionExpiry.getPeriod()), formatPeriod(swapTenor.getPeriod()), relativeStrike);
  }
  
  private String formatPeriod(final Period period) {
    String periodStr = period.toString();
    if (periodStr.startsWith("P")) {
      periodStr = periodStr.substring(1);
    }
    return periodStr;
  }
}
