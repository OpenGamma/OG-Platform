/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.bbg.util.ReferenceDataProviderUtils;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Maps Bloomberg security type and futures category to subclasses of {@link ManageableSecurity}
 */
public class BloombergSecurityTypeResolver implements SecurityTypeResolver {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergSecurityTypeResolver.class);

  private static final Map<String, SecurityType> s_futureTypes = Maps.newConcurrentMap();
  static {
    addValidTypes(s_futureTypes, AgricultureFutureLoader.VALID_FUTURE_CATEGORIES, SecurityType.AGRICULTURE_FUTURE);
    addValidTypes(s_futureTypes, BondFutureLoader.VALID_FUTURE_CATEGORIES, SecurityType.BOND_FUTURE);
    addValidTypes(s_futureTypes, EnergyFutureLoader.VALID_FUTURE_CATEGORIES, SecurityType.ENERGY_FUTURE);
    addValidTypes(s_futureTypes, EquityDividendFutureLoader.VALID_FUTURE_CATEGORIES, SecurityType.EQUITY_DIVIDEND_FUTURE);
    addValidTypes(s_futureTypes, FXFutureLoader.VALID_FUTURE_CATEGORIES, SecurityType.FX_FUTURE);
    addValidTypes(s_futureTypes, IndexFutureLoader.VALID_FUTURE_CATEGORIES, SecurityType.INDEX_FUTURE);
    addValidTypes(s_futureTypes, InterestRateFutureLoader.VALID_FUTURE_CATEGORIES, SecurityType.INTEREST_RATE_FUTURE);
    addValidTypes(s_futureTypes, MetalFutureLoader.VALID_FUTURE_CATEGORIES, SecurityType.METAL_FUTURE);
    //types for EquityFutureSecurity
    addValidTypes(s_futureTypes, EquityFutureLoader.VALID_SECURITY_TYPES, SecurityType.EQUITY_FUTURE);

  }

  private static final Map<String, SecurityType> s_optionTypes = Maps.newConcurrentMap();
  static {
    addValidTypes(s_optionTypes, EquityOptionLoader.VALID_SECURITY_TYPES, SecurityType.EQUITY_OPTION);
    addValidTypes(s_optionTypes, EquityIndexOptionLoader.VALID_SECURITY_TYPES, SecurityType.EQUITY_INDEX_OPTION);
    addValidTypes(s_optionTypes, EquityIndexFutureOptionLoader.VALID_SECURITY_TYPES, SecurityType.EQUITY_INDEX_FUTURE_OPTION);
    addValidTypes(s_optionTypes, IRFutureOptionLoader.VALID_SECURITY_TYPES, SecurityType.IR_FUTURE_OPTION);
    addValidTypes(s_optionTypes, BondFutureOptionLoader.VALID_SECURITY_TYPES, SecurityType.BOND_FUTURE_OPTION);
    addValidTypes(s_optionTypes, CommodityFutureOptionLoader.VALID_SECURITY_TYPES, SecurityType.COMMODITY_FUTURE_OPTION);
    addValidTypes(s_optionTypes, FxFutureOptionLoader.VALID_SECURITY_TYPES, SecurityType.FX_FUTURE_OPTION);
  }

  private static final Map<String, SecurityType> s_swapTypes = Maps.newConcurrentMap();
  static {
    addValidTypes(s_swapTypes, NonLoadedSecurityTypes.VALID_SWAP_SECURITY_TYPES, SecurityType.SWAP);
    addValidTypes(s_swapTypes, NonLoadedSecurityTypes.VALID_BASIS_SWAP_SECURITY_TYPES, SecurityType.BASIS_SWAP);
    addValidTypes(s_swapTypes, NonLoadedSecurityTypes.VALID_INFLATION_SWAP_TYPES, SecurityType.INFLATION_SWAP);
  }

  private static final Map<String, SecurityType> s_miscTypes = Maps.newConcurrentMap();
  static {
    addValidTypes(s_miscTypes, EquityLoader.VALID_SECURITY_TYPES, SecurityType.EQUITY);
    addValidTypes(s_miscTypes, BondLoader.VALID_SECURITY_TYPES, SecurityType.BOND);
    addValidTypes(s_miscTypes, NonLoadedSecurityTypes.VALID_EQUITY_INDEX_SECURITY_TYPES, SecurityType.EQUITY_INDEX);
    addValidTypes(s_miscTypes, NonLoadedSecurityTypes.VALID_FORWARD_CROSS_SECURITY_TYPES, SecurityType.FORWARD_CROSS);
    addValidTypes(s_miscTypes, NonLoadedSecurityTypes.VALID_FRA_SECURITY_TYPES, SecurityType.FRA);
    addValidTypes(s_miscTypes, IndexLoader.VALID_SECURITY_TYPES, SecurityType.INDEX);
    addValidTypes(s_miscTypes, NonLoadedSecurityTypes.VALID_RATE_TYPES, SecurityType.RATE);
    addValidTypes(s_miscTypes, NonLoadedSecurityTypes.VALID_SPOT_RATE_TYPES, SecurityType.SPOT_RATE);
    addValidTypes(s_miscTypes, NonLoadedSecurityTypes.VALID_VOLATILITY_QUOTE_TYPES, SecurityType.VOLATILITY_QUOTE);
    addValidTypes(s_miscTypes, NonLoadedSecurityTypes.VALID_FX_FORWARD_TYPES, SecurityType.FX_FORWARD);
  }

  private static final Map<String, SecurityType> s_type2Types = Maps.newConcurrentMap();
  static {
    addValidTypes(s_type2Types, BillLoader.VALID_SECURITY_TYPES2, SecurityType.BILL);
  }

  //private static final Map<String, SecurityType> s_cdsTypes = Maps.newConcurrentMap();
  static {
    addValidTypes(s_swapTypes, NonLoadedSecurityTypes.VALID_CDS_TYPES, SecurityType.CREDIT_DEFAULT_SWAP);
  }

  private static final Set<String> BBG_FIELDS = Sets.newHashSet(BloombergConstants.FIELD_SECURITY_TYPE, BloombergConstants.FIELD_FUTURES_CATEGORY, BloombergConstants.FIELD_NAME,
      BloombergConstants.FIELD_SECURITY_TYPE2);

  private final ReferenceDataProvider _referenceDataProvider;

  /**
   * Creates a BloombergSecurityTypeResolver
   * 
   * @param referenceDataProvider the reference data provider, not null
   */
  public BloombergSecurityTypeResolver(final ReferenceDataProvider referenceDataProvider) {
    ArgumentChecker.notNull(referenceDataProvider, "referenceDataProvider");
    _referenceDataProvider = referenceDataProvider;
  }

  @Override
  public Map<ExternalIdBundle, SecurityType> getSecurityType(final Collection<ExternalIdBundle> identifiers) {
    ArgumentChecker.notNull(identifiers, "identifiers");

    final Map<ExternalIdBundle, SecurityType> result = Maps.newHashMap();

    final BiMap<String, ExternalIdBundle> bundle2Bbgkey = BloombergDataUtils.convertToBloombergBuidKeys(identifiers, _referenceDataProvider);

    final Map<String, FudgeMsg> securityTypeResult = ReferenceDataProviderUtils.getFields(bundle2Bbgkey.keySet(), BBG_FIELDS, _referenceDataProvider);

    for (final ExternalIdBundle identifierBundle : identifiers) {
      final String bbgKey = bundle2Bbgkey.inverse().get(identifierBundle);
      if (bbgKey != null) {
        final FudgeMsg fudgeMsg = securityTypeResult.get(bbgKey);
        if (fudgeMsg != null) {
          final String bbgSecurityType = fudgeMsg.getString(BloombergConstants.FIELD_SECURITY_TYPE);
          final String bbgSecurityType2 = fudgeMsg.getString(BloombergConstants.FIELD_SECURITY_TYPE2);
          final String futureCategory = fudgeMsg.getString(BloombergConstants.FIELD_FUTURES_CATEGORY);

          SecurityType securityType = null;
          if (bbgSecurityType != null) {
            if (bbgSecurityType.toUpperCase().contains(" FUTURE")) {
              s_logger.debug("s_futureTypes {}", s_futureTypes);
              securityType = s_futureTypes.get(futureCategory);
            } else if (bbgSecurityType.toUpperCase().contains(" OPTION")) {

              // Special case handling for EQUITY_INDEX_DIVIDEND_FUTURE_OPTION which we
              // are otherwise unable to distinguish from EQUITY_INDEX_FUTURE_OPTION
              final String name = fudgeMsg.getString(BloombergConstants.FIELD_NAME);
              securityType = isEquityIndexFutureDividendOption(futureCategory, name) ?
                  SecurityType.EQUITY_INDEX_DIVIDEND_FUTURE_OPTION :
                    s_optionTypes.get(futureCategory);
            } else if (bbgSecurityType.toUpperCase().endsWith("SWAP")) {
              securityType = s_swapTypes.get(bbgSecurityType);
            } else if (bbgSecurityType2 != null && bbgSecurityType2.toUpperCase().contains("BILL")) {
              securityType = s_type2Types.get(bbgSecurityType2);
            } else {
              securityType = s_miscTypes.get(bbgSecurityType);
            }
          }

          if (securityType != null) {
            result.put(identifierBundle, securityType);
          } else {
            s_logger.warn("Unknown security type of {} for {}", bbgSecurityType, identifierBundle);
          }
        }
      }
    }
    return result;
  }

  private boolean isEquityIndexFutureDividendOption(final String futureCategory, final String name) {
    return name != null && "Equity Index".equals(futureCategory) && name.toUpperCase().contains("DIVIDEND");
  }

  private static void addValidTypes(final Map<String, SecurityType> sFuturetypes, final Set<String> validSecurityTypes, final SecurityType securityType) {
    for (final String bbgSecType : validSecurityTypes) {
      sFuturetypes.put(bbgSecType, securityType);
    }
  }

  public ReferenceDataProvider getReferenceDataProvider() {
    return _referenceDataProvider;
  }

}
