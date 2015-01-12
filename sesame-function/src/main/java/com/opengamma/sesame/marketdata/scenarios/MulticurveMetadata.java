/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import com.opengamma.DataNotFoundException;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexDeposit;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.core.link.ConventionLink;
import com.opengamma.core.link.SecurityLink;
import com.opengamma.financial.analytics.curve.ConverterUtils;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IborCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.OvernightCurveTypeConfiguration;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Metadata for a multicurve bundle, used to decide whether to apply a scenario perturbation.
 * <p>
 * Metadata can be constructed from an existing multicurve bundle or from the configuration used
 * to build one.
 * <p>
 * This is not intended to be general purpose for a multicurve, it is specifically intended for
 * use in the scenario framework.
 */
public final class MulticurveMetadata {

  private static final Logger s_logger = LoggerFactory.getLogger(MulticurveMetadata.class);

  private final Set<String> _curveNames;
  private final SetMultimap<Currency, String> _curveNamesByCurrency;
  private final Map<IndexDeposit, String> _curveNamesByIndex;
  private final Map<String, String> _curveNamesByIndexName;

  private MulticurveMetadata(Set<String> curveNames,
                             SetMultimap<Currency, String> curveNamesByCurrency,
                             Map<IndexDeposit, String> curveNamesByIndex,
                             Map<String, String> curveNamesByIndexName) {
    _curveNames = curveNames;
    _curveNamesByCurrency = curveNamesByCurrency;
    _curveNamesByIndex = curveNamesByIndex;
    _curveNamesByIndexName = curveNamesByIndexName;
  }

  /**
   * Creates multicurve metadata from the configuration used to build the multicurve bundle.
   *
   * @param config the configuration for building the multicurve bundle
   */
  public static MulticurveMetadata forConfiguration(CurveConstructionConfiguration config) {
    ArgumentChecker.notNull(config, "config");

    ImmutableSetMultimap.Builder<Currency, String> currencyBuilder = ImmutableSetMultimap.builder();
    ImmutableSet.Builder<String> nameBuilder = ImmutableSet.builder();
    ImmutableMap.Builder<IndexDeposit, String> indexBuilder = ImmutableMap.builder();
    ImmutableMap.Builder<String, String> indexNameBuilder = ImmutableMap.builder();

    for (CurveGroupConfiguration groupConfig : config.getCurveGroups()) {
      Map<String, List<? extends CurveTypeConfiguration>> curveMap = groupConfig.getTypesForCurves();

      for (Map.Entry<String, List<? extends CurveTypeConfiguration>> entry : curveMap.entrySet()) {
        String curveName = entry.getKey();
        nameBuilder.add(curveName);
        List<? extends CurveTypeConfiguration> curveConfigs = entry.getValue();

        for (Currency currency : currenciesForConfigs(curveConfigs)) {
          currencyBuilder.put(currency, curveName);
        }
        // it doesn't make sense for one curve to have multiple indices but the object model allows it
        for (IndexDeposit index : indicesForConfigs(curveConfigs)) {
          indexBuilder.put(index, curveName);
          indexNameBuilder.put(index.getName(), curveName);
        }
      }
    }
    return new MulticurveMetadata(
        nameBuilder.build(),
        currencyBuilder.build(),
        indexBuilder.build(),
        indexNameBuilder.build());
  }

  /**
   * Creates multicurve metadata from an existing multicurve bundle.
   *
   * @param multicurve a multicurve bundle
   */
  public static MulticurveMetadata forMulticurve(MulticurveBundle multicurve) {
    ArgumentChecker.notNull(multicurve, "multicurve");

    MulticurveProviderDiscount curves = multicurve.getMulticurveProvider();
    ImmutableSetMultimap.Builder<Currency, String> currencyBuilder = ImmutableSetMultimap.builder();
    ImmutableMap.Builder<IndexDeposit, String> indexBuilder = ImmutableMap.builder();
    ImmutableMap.Builder<String, String> indexNameBuilder = ImmutableMap.builder();

    for (Map.Entry<Currency, YieldAndDiscountCurve> entry : curves.getDiscountingCurves().entrySet()) {
      currencyBuilder.put(entry.getKey(), entry.getValue().getName());
    }
    for (Map.Entry<IndexON, YieldAndDiscountCurve> entry : curves.getForwardONCurves().entrySet()) {
      IndexON index = entry.getKey();
      String curveName = entry.getValue().getName();
      currencyBuilder.put(index.getCurrency(), curveName);
      indexBuilder.put(index, curveName);
      indexNameBuilder.put(index.getName(), curveName);
    }
    for (Map.Entry<IborIndex, YieldAndDiscountCurve> entry : curves.getForwardIborCurves().entrySet()) {
      IborIndex index = entry.getKey();
      String curveName = entry.getValue().getName();
      currencyBuilder.put(index.getCurrency(), curveName);
      indexBuilder.put(index, curveName);
      indexNameBuilder.put(index.getName(), curveName);
    }
    return new MulticurveMetadata(
        ImmutableSet.copyOf(curves.getAllNames()),
        currencyBuilder.build(),
        indexBuilder.build(),
        indexNameBuilder.build());
  }

  /**
   * Returns all currencies for the curve configurations.
   *
   * @param curveConfigs curve type configurations
   * @return all currencies in the curve type configurations
   */
  private static Set<Currency> currenciesForConfigs(List<? extends CurveTypeConfiguration> curveConfigs) {
    Set<Currency> currencies = new HashSet<>();

    for (CurveTypeConfiguration curveConfig : curveConfigs) {
      if (curveConfig instanceof DiscountingCurveTypeConfiguration) {
        try {
          currencies.add(Currency.of(((DiscountingCurveTypeConfiguration) curveConfig).getReference()));
        } catch (IllegalArgumentException e) {
          s_logger.warn("Failed to parse curve config reference as currency. Config: {}", curveConfig);
        }
      } else if (curveConfig instanceof IborCurveTypeConfiguration) {
        try {
          IborIndex index = createIborIndex((IborCurveTypeConfiguration) curveConfig);
          currencies.add(index.getCurrency());
        } catch (DataNotFoundException e) {
          s_logger.warn("Unable to resolve index ID '{}' for curve type config {}",
                        ((IborCurveTypeConfiguration) curveConfig).getConvention(), curveConfig);
        }
      } else if (curveConfig instanceof OvernightCurveTypeConfiguration) {
        try {
          IndexON index = createOvernightIndex((OvernightCurveTypeConfiguration) curveConfig);
          currencies.add(index.getCurrency());
        } catch (DataNotFoundException e) {
          s_logger.warn("Unable to resolve index ID '{}' for curve type config {}",
                        ((OvernightCurveTypeConfiguration) curveConfig).getConvention(), curveConfig);
        }
      } else {
        s_logger.warn("Curve type config of unexpected type: " + curveConfig.getClass().getName());
      }
    }
    return currencies;
  }

  // it doesn't make sense for one curve to have multiple indices but the object model allows it
  private static Set<IndexDeposit> indicesForConfigs(List<? extends CurveTypeConfiguration> curveConfigs) {
    Set<IndexDeposit> indices = new HashSet<>();

    for (CurveTypeConfiguration curveConfig : curveConfigs) {
      if (curveConfig instanceof IborCurveTypeConfiguration) {
        try {
          IborIndex index = createIborIndex((IborCurveTypeConfiguration) curveConfig);
          indices.add(index);
        } catch (DataNotFoundException e) {
          s_logger.warn("Unable to resolve index ID '{}' for curve type config {}",
                        ((IborCurveTypeConfiguration) curveConfig).getConvention(), curveConfig);
        }
      } else if (curveConfig instanceof OvernightCurveTypeConfiguration) {
        try {
          IndexON index = createOvernightIndex((OvernightCurveTypeConfiguration) curveConfig);
          indices.add(index);
        } catch (DataNotFoundException e) {
          s_logger.warn("Unable to resolve index ID '{}' for curve type config {}",
                        ((OvernightCurveTypeConfiguration) curveConfig).getConvention(), curveConfig);
        }
      }
    }
    return indices;
  }

  private static IndexON createOvernightIndex(OvernightCurveTypeConfiguration type) {
    OvernightIndex index = SecurityLink.resolvable(type.getConvention().toBundle(), OvernightIndex.class).resolve();
    OvernightIndexConvention indexConvention =
        ConventionLink.resolvable(index.getConventionId(), OvernightIndexConvention.class).resolve();
    return ConverterUtils.indexON(index.getName(), indexConvention);
  }

  private static IborIndex createIborIndex(IborCurveTypeConfiguration type) {
    com.opengamma.financial.security.index.IborIndex indexSecurity =
        SecurityLink.resolvable(type.getConvention(), com.opengamma.financial.security.index.IborIndex.class).resolve();

    IborIndexConvention indexConvention =
        ConventionLink.resolvable(indexSecurity.getConventionId(), IborIndexConvention.class).resolve();

    return ConverterUtils.indexIbor(indexSecurity.getName(), indexConvention, indexSecurity.getTenor());
  }

  /**
   * @return the names of all curves in the multicurve bundle
   */
  public Set<String> getCurveNames() {
    return _curveNames;
  }

  /**
   * Returns the curve names in the multicurve bundle, keyed by the curve currency.
   * <p>
   * For discounting curves this is the curve currency. For IBOR or overnight forward curves it is the
   * currency taken from the security representing the curve's index.
   *
   * @return the curve names in the multicurve bundle, keyed by the curve currency
   */
  public SetMultimap<Currency, String> getCurveNamesByCurrency() {
    return _curveNamesByCurrency;
  }

  /**
   * Returns the curve names in the multicurve bundle, keyed by the curve index.
   *
   * @return the curve names in the multicurve bundle, keyed by the curve index
   */
  public Map<IndexDeposit, String> getCurveNamesByIndex() {
    return _curveNamesByIndex;
  }

  /**
   * Returns the curve names in the multicurve bundle, keyed by the curve index name.
   *
   * @return the curve names in the multicurve bundle, keyed by the curve index name
   */
  public Map<String, String> getCurveNamesByIndexName() {
    return _curveNamesByIndexName;
  }
}
