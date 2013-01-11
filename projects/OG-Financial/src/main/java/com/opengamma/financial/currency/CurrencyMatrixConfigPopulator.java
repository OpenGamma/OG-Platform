/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.util.money.Currency;

/**
 * Loads a default currency matrix into the configuration database.
 */
public class CurrencyMatrixConfigPopulator {

  private static final Logger s_logger = LoggerFactory.getLogger(CurrencyMatrixConfigPopulator.class);

  /**
   * Bloomberg currency matrix config name
   */
  public static final String BLOOMBERG_LIVE_DATA = "BloombergLiveData";

  /**
   * Synthetic currency matrix config name
   */
  public static final String SYNTHETIC_LIVE_DATA = "SyntheticLiveData";

  public static ConfigMaster populateCurrencyMatrixConfigMaster(final ConfigMaster cfgMaster) {
    final CurrencyPairs currencies = new MasterConfigSource(cfgMaster).getSingle(CurrencyPairs.class, CurrencyPairs.DEFAULT_CURRENCY_PAIRS, VersionCorrection.LATEST);
    storeCurrencyMatrix(cfgMaster, BLOOMBERG_LIVE_DATA, createBloombergConversionMatrix(currencies));
    storeCurrencyMatrix(cfgMaster, SYNTHETIC_LIVE_DATA, createSyntheticConversionMatrix(currencies));
    return cfgMaster;
  }

  private static void storeCurrencyMatrix(final ConfigMaster cfgMaster, final String name, final CurrencyMatrix currencyMatrix) {
    final ConfigItem<CurrencyMatrix> doc = ConfigItem.of(currencyMatrix, name, CurrencyMatrix.class);
    doc.setName(name);
    ConfigMasterUtils.storeByName(cfgMaster, doc);
  }

  public static CurrencyMatrix createBloombergConversionMatrix(final CurrencyPairs currencies) {
    final SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    for (final CurrencyPair pair : currencies.getPairs()) {
      matrix.setLiveData(pair.getCounter(), pair.getBase(),
          new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER.toString(), pair.getBase().getCode() +
              pair.getCounter().getCode() + " Curncy")));
    }
    dumpMatrix(matrix);
    return matrix;
  }

  public static CurrencyMatrix createSyntheticConversionMatrix(final CurrencyPairs currencies) {
    final SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    final Currency commonCross = Currency.USD;
    for (final CurrencyPair pair : currencies.getPairs()) {
      if (commonCross.equals(pair.getBase()) || commonCross.equals(pair.getCounter())) {
        matrix.setLiveData(pair.getCounter(), pair.getBase(),
            new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.PRIMITIVE, ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER.getName(), pair.getBase().getCode() +
                pair.getCounter().getCode())));
      }
    }
    for (final CurrencyPair pair : currencies.getPairs()) {
      if (!commonCross.equals(pair.getBase()) && !commonCross.equals(pair.getCounter())) {
        matrix.setCrossConversion(pair.getCounter(), pair.getBase(), commonCross);
      }
    }
    dumpMatrix(matrix);
    return matrix;

  }

  public static void dumpMatrix(final CurrencyMatrix matrix) {
    final StringBuilder sb = new StringBuilder();
    sb.append('\n');
    for (final Currency x : matrix.getTargetCurrencies()) {
      sb.append('\t').append(x.getCode());
    }
    for (final Currency y : matrix.getSourceCurrencies()) {
      sb.append('\n').append(y.getCode());
      for (final Currency x : matrix.getTargetCurrencies()) {
        sb.append('\t').append(matrix.getConversion(y, x));
      }
    }
    s_logger.debug("Currency matrix = {}", sb);
  }

}
