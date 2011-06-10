/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.time.calendar.Period;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.marketdatasnapshot.VolatilityPoint;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.id.Identifier;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Generates instrument codes for volatilities given points.
 */
public final class VolatilityCubeInstrumentProvider {

  /**
   * Generates Bloomberg codes for volatilities given points.
   */
  public static final VolatilityCubeInstrumentProvider BLOOMBERG = new VolatilityCubeInstrumentProvider();

  private static final String TICKER_FILE = "VolatilityCubeIdentifierLookupTable.csv";

  private final HashMap<ObjectsPair<Currency, VolatilityPoint>, Identifier> _idsByPoint;
  private final HashMap<ObjectsPair<Currency, Identifier>, VolatilityPoint> _pointsById;

  private VolatilityCubeInstrumentProvider() {
    //TODO not here

    _idsByPoint = new HashMap<ObjectsPair<Currency, VolatilityPoint>, Identifier>();
    _pointsById = new HashMap<ObjectsPair<Currency, Identifier>, VolatilityPoint>();

    InputStream is = getClass().getResourceAsStream(TICKER_FILE);
    if (is == null) {
      throw new OpenGammaRuntimeException("Unable to locate " + TICKER_FILE);
    }
    CSVReader csvReader = new CSVReader(new InputStreamReader(is), CSVParser.DEFAULT_SEPARATOR,
        CSVParser.DEFAULT_QUOTE_CHARACTER, CSVParser.DEFAULT_ESCAPE_CHARACTER, 1);
    String[] nextLine;
    try {
      while ((nextLine = csvReader.readNext()) != null) {
        //TODO: are these the right way round (copied from VolatilityCubeConfigPopulator)
        String currencyIso = nextLine[1];
        String expiry = nextLine[2];
        String expiryUnit = nextLine[3];
        String swapPeriod = nextLine[4];
        String swapPeriodUnit = nextLine[5];
        String payOrReceive = nextLine[6]; //TODO
        String relativeStrike = nextLine[7];
        String ticker = nextLine[8];

        if (ticker != null) {
          Currency currency = Currency.of(currencyIso);
          Tenor swapTenor = new Tenor(Period.parse("P" + swapPeriod + swapPeriodUnit));
          Tenor optionExpiry = new Tenor(Period.parse("P" + expiry + expiryUnit));
          double sign;
          if ("PY".equals(payOrReceive)) {
            sign = -1;
          } else if ("RCV".equals(payOrReceive)) {
            sign = 1;
          } else {
            throw new IllegalArgumentException();
          }

          Double relativeStrikeRaw = Double.valueOf(relativeStrike);
          double normalizedStrike = relativeStrikeRaw > 10 ? relativeStrikeRaw : relativeStrikeRaw * 100;
          Double relativeStrikeBps = sign * normalizedStrike;
          VolatilityPoint point = new VolatilityPoint(swapTenor, optionExpiry, relativeStrikeBps);

          Identifier identifier = getIdentifier(ticker + " Curncy");
          Identifier prev = _idsByPoint.put(Pair.of(currency, point), identifier);
          if (_pointsById.put(Pair.of(currency, identifier), point) != null) {
            throw new IllegalArgumentException();
          }
        }
      }
      csvReader.close();
    } catch (IOException e) {
      throw new OpenGammaRuntimeException("Unable to read from " + TICKER_FILE, e);
    }
  }

  private Identifier getIdentifier(String ticker) {
    return Identifier.of(SecurityUtils.BLOOMBERG_TICKER, ticker);
  }

  public VolatilityPoint getPoint(Currency currency, Identifier instrument) {
    return _pointsById.get(Pair.of(currency, instrument));
  }

  public Identifier getInstrument(Currency currency, VolatilityPoint point) {
    return _idsByPoint.get(Pair.of(currency, point));
  }
}
