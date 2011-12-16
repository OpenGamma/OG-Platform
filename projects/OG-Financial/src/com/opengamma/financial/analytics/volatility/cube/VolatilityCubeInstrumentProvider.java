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
import com.opengamma.id.ExternalId;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
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
      new BloombergSwaptionVolatilitySurfaceInstrumentProvider("US", "SV", false, true, " Curncy", MarketDataRequirementNames.MARKET_VALUE);
  private static final SurfaceInstrumentProvider<Tenor, Tenor> ATM_STRIKE_INSTRUMENT_PROVIDER =
      new BloombergSwaptionVolatilitySurfaceInstrumentProvider("US", "FS", false, true, " Curncy", MarketDataRequirementNames.MARKET_VALUE);

  /**
   * Generates Bloomberg codes for volatilities given points.
   */
  public static final VolatilityCubeInstrumentProvider BLOOMBERG = new VolatilityCubeInstrumentProvider();

  private static final String TICKER_FILE = "VolatilityCubeIdentifierLookupTable.csv";

  private final HashMap<ObjectsPair<Currency, VolatilityPoint>, Set<ExternalId>> _idsByPoint;

  private VolatilityCubeInstrumentProvider() {
    //TODO not here
    _idsByPoint = new HashMap<ObjectsPair<Currency, VolatilityPoint>, Set<ExternalId>>();

    final InputStream is = getClass().getResourceAsStream(TICKER_FILE);
    if (is == null) {
      throw new OpenGammaRuntimeException("Unable to locate " + TICKER_FILE);
    }
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

          final ExternalId identifier = getIdentifier(ticker + " Curncy");

          final ObjectsPair<Currency, VolatilityPoint> key = Pair.of(currency, point);
          if (_idsByPoint.containsKey(key)) {
            _idsByPoint.get(key).add(identifier);
          } else {
            _idsByPoint.put(key, Sets.newHashSet(identifier));
          }
        }
      }
      csvReader.close();
    } catch (final IOException e) {
      throw new OpenGammaRuntimeException("Unable to read from " + TICKER_FILE, e);
    }
  }

  private ExternalId getIdentifier(final String ticker) {
    return ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, ticker);
  }

  public Set<ExternalId> getInstruments(final Currency currency, final VolatilityPoint point) {
    if ((point.getRelativeStrike() == 0.0) && currency.equals(ATM_INSTRUMENT_PROVIDER_CURRENCY)) {
      final ExternalId instrument = ATM_INSTRUMENT_PROVIDER.getInstrument(point.getSwapTenor(), point.getOptionExpiry());
      return Sets.newHashSet(instrument);
    } else {
      return _idsByPoint.get(Pair.of(currency, point));
    }
  }

  public Set<Currency> getAllCurrencies() {
    final HashSet<Currency> ret = new HashSet<Currency>();
    for (final Entry<ObjectsPair<Currency, VolatilityPoint>, Set<ExternalId>> entry : _idsByPoint.entrySet()) {
      ret.add(entry.getKey().first);
    }
    return ret;
  }

  public Set<VolatilityPoint> getAllPoints(final Currency currency) {
    final HashSet<VolatilityPoint> ret = new HashSet<VolatilityPoint>();
    for (final Entry<ObjectsPair<Currency, VolatilityPoint>, Set<ExternalId>> entry : _idsByPoint.entrySet()) {
      if (entry.getKey().first.equals(currency)) {
        ret.add(entry.getKey().second);
      }
    }
    return ret;
  }

  public ExternalId getStrikeInstrument(final Currency currency, final VolatilityPoint point) {
    return getStrikeInstrument(currency, point.getSwapTenor(), point.getOptionExpiry());
  }

  public ExternalId getStrikeInstrument(final Currency currency, final Tenor swapTenor, final Tenor optionExpiry) {
    if (currency.equals(ATM_INSTRUMENT_PROVIDER_CURRENCY)) {
      return ATM_STRIKE_INSTRUMENT_PROVIDER.getInstrument(swapTenor, optionExpiry);
    } else {
      //TODO other currencies
      return null;
    }
  }
}
