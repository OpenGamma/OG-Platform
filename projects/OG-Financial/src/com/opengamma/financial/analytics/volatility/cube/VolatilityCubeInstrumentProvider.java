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
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import javax.time.calendar.Period;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.marketdatasnapshot.VolatilityPoint;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.analytics.volatility.surface.BloombergSwaptionVolatilitySurfaceInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.SurfaceInstrumentProvider;
import com.opengamma.id.Identifier;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Generates instrument codes for volatilities given points.
 */
public final class VolatilityCubeInstrumentProvider {

  //TODO: other ATM surfaces
  private static final Currency ATM_INSTRUMENT_PROVIDER_CURRENCY = Currency.USD;
  private static final SurfaceInstrumentProvider<Tenor, Tenor> ATM_INSTRUMENT_PROVIDER = 
    new BloombergSwaptionVolatilitySurfaceInstrumentProvider("US", "SV", true, false, " Curncy");
  
  /**
   * Generates Bloomberg codes for volatilities given points.
   */
  public static final VolatilityCubeInstrumentProvider BLOOMBERG = new VolatilityCubeInstrumentProvider();

  private static final String TICKER_FILE = "VolatilityCubeIdentifierLookupTable.csv";

  private final HashMap<ObjectsPair<Currency, VolatilityPoint>, Set<Identifier>> _idsByPoint;
  private final HashMap<ObjectsPair<Currency, Identifier>, VolatilityPoint> _pointsById;

  private VolatilityCubeInstrumentProvider() {
    //TODO not here

    _idsByPoint = new HashMap<ObjectsPair<Currency, VolatilityPoint>, Set<Identifier>>();
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
        String payOrReceive = nextLine[6];
        String relativeStrike = nextLine[7];
        String ticker = nextLine[8];

        if (ticker != null) {
          Currency currency = Currency.of(currencyIso);
          Tenor swapTenor = new Tenor(Period.parse("P" + swapPeriod + swapPeriodUnit));
          Tenor optionExpiry = new Tenor(Period.parse("P" + expiry + expiryUnit));
          double sign;
          if ("PY".equals(payOrReceive)) {
            sign = -1;
          } else if ("RC".equals(payOrReceive)) {
            sign = 1;
          } else {
            throw new IllegalArgumentException();
          }

          Double relativeStrikeRaw = Double.valueOf(relativeStrike);
          if (relativeStrikeRaw == 0.0 && currency.equals(ATM_INSTRUMENT_PROVIDER_CURRENCY)) {
            continue; // We use ATM_INSTRUMENT_PROVIDER for these
          }
          double normalizedStrike = relativeStrikeRaw > 10 ? relativeStrikeRaw : relativeStrikeRaw * 100;
          Double relativeStrikeBps = sign * normalizedStrike;
          VolatilityPoint point = new VolatilityPoint(swapTenor, optionExpiry, relativeStrikeBps);

          Identifier identifier = getIdentifier(ticker + " Curncy");

          ObjectsPair<Currency, VolatilityPoint> key = Pair.of(currency, point);
          if (_idsByPoint.containsKey(key)) {
            _idsByPoint.get(key).add(identifier);
          } else {
            _idsByPoint.put(key, Sets.newHashSet(identifier));
          }

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

  public Set<Identifier> getInstruments(Currency currency, VolatilityPoint point) {
    if (point.getRelativeStrike() == 0 && currency.equals(ATM_INSTRUMENT_PROVIDER_CURRENCY)) {
      return Sets.newHashSet(ATM_INSTRUMENT_PROVIDER.getInstrument(point.getSwapTenor(), point.getOptionExpiry()));
    } else {
      return _idsByPoint.get(Pair.of(currency, point));
    }
  }

  public Set<Currency> getAllCurrencies() {
    HashSet<Currency> ret = new HashSet<Currency>();
    for (Entry<ObjectsPair<Currency, VolatilityPoint>, Set<Identifier>> entry : _idsByPoint.entrySet()) {
      ret.add(entry.getKey().first);
    }
    return ret;
  }
  
  public Set<VolatilityPoint> getAllPoints(Currency currency) {
    HashSet<VolatilityPoint> ret = new HashSet<VolatilityPoint>();
    for (Entry<ObjectsPair<Currency, VolatilityPoint>, Set<Identifier>> entry : _idsByPoint.entrySet()) {
      if (entry.getKey().first.equals(currency)) {
        ret.add(entry.getKey().second);
      }
    }
    return ret;
  }
}
