/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.config;

import static com.opengamma.bbg.BloombergConstants.BBG_BASE_METAL_TYPE;
import static com.opengamma.bbg.BloombergConstants.BBG_COAL;
import static com.opengamma.bbg.BloombergConstants.BBG_CRUDE_OIL;
import static com.opengamma.bbg.BloombergConstants.BBG_CURRENCY_TYPE;
import static com.opengamma.bbg.BloombergConstants.BBG_ELECTRICITY;
import static com.opengamma.bbg.BloombergConstants.BBG_FOODSTUFF;
import static com.opengamma.bbg.BloombergConstants.BBG_LIVESTOCK;
import static com.opengamma.bbg.BloombergConstants.BBG_PRECIOUS_METAL_TYPE;
import static com.opengamma.bbg.BloombergConstants.BBG_REFINED_PRODUCTS;
import static com.opengamma.bbg.BloombergConstants.BBG_SOY;
import static com.opengamma.bbg.BloombergConstants.BBG_STOCK_FUTURE_TYPE;
import static com.opengamma.bbg.BloombergConstants.BBG_WHEAT;
import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_BOND_FUTURE_TYPE;
import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_CURRENCY_TYPE;
import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_EQUITY_INDEX_TYPE;
import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_EQUITY_OPTION_SECURITY_TYPE;
import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_FINANCIAL_COMMODITY_OPTION_TYPE;
import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_INTEREST_RATE_TYPE;
import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_NON_DELIVERABLE_IRS_SWAP_TYPE;
import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_PHYSICAL_INDEX_FUTURE_TYPE;
import static com.opengamma.bbg.loader.SecurityType.AGRICULTURE_FUTURE;
import static com.opengamma.bbg.loader.SecurityType.BASIS_SWAP;
import static com.opengamma.bbg.loader.SecurityType.BILL;
import static com.opengamma.bbg.loader.SecurityType.BOND;
import static com.opengamma.bbg.loader.SecurityType.BOND_FUTURE;
import static com.opengamma.bbg.loader.SecurityType.BOND_FUTURE_OPTION;
import static com.opengamma.bbg.loader.SecurityType.COMMODITY_FUTURE_OPTION;
import static com.opengamma.bbg.loader.SecurityType.CREDIT_DEFAULT_SWAP;
import static com.opengamma.bbg.loader.SecurityType.ENERGY_FUTURE;
import static com.opengamma.bbg.loader.SecurityType.EQUITY;
import static com.opengamma.bbg.loader.SecurityType.EQUITY_DIVIDEND_FUTURE;
import static com.opengamma.bbg.loader.SecurityType.EQUITY_FUTURE;
import static com.opengamma.bbg.loader.SecurityType.EQUITY_INDEX;
import static com.opengamma.bbg.loader.SecurityType.EQUITY_INDEX_FUTURE_OPTION;
import static com.opengamma.bbg.loader.SecurityType.EQUITY_INDEX_OPTION;
import static com.opengamma.bbg.loader.SecurityType.EQUITY_OPTION;
import static com.opengamma.bbg.loader.SecurityType.FORWARD_CROSS;
import static com.opengamma.bbg.loader.SecurityType.FRA;
import static com.opengamma.bbg.loader.SecurityType.FX_FORWARD;
import static com.opengamma.bbg.loader.SecurityType.FX_FUTURE;
import static com.opengamma.bbg.loader.SecurityType.FX_FUTURE_OPTION;
import static com.opengamma.bbg.loader.SecurityType.INDEX_FUTURE;
import static com.opengamma.bbg.loader.SecurityType.INTEREST_RATE_FUTURE;
import static com.opengamma.bbg.loader.SecurityType.IR_FUTURE_OPTION;
import static com.opengamma.bbg.loader.SecurityType.METAL_FUTURE;
import static com.opengamma.bbg.loader.SecurityType.RATE;
import static com.opengamma.bbg.loader.SecurityType.SPOT_RATE;
import static com.opengamma.bbg.loader.SecurityType.SWAP;
import static com.opengamma.bbg.loader.SecurityType.VOLATILITY_QUOTE;

import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.ArgumentChecker;

/**
 * Loads config master with default bloomberg security types definition
 */
public class BloombergSecurityTypeDefinitionLoader {

  private static final BloombergSecurityTypeDefinition DEFAULT_CONFIG = buildDefaultConfig();
  private final ConfigMaster _configMaster;
  private final String _configName;

  public BloombergSecurityTypeDefinitionLoader(final ConfigMaster configMaster, final String configName) {
    ArgumentChecker.notNull(configMaster, "config master");
    ArgumentChecker.notNull(configName, "config name");
    _configMaster = configMaster;
    _configName = configName;
  }

  public BloombergSecurityTypeDefinitionLoader(final ConfigMaster configMaster) {
    this(configMaster, BloombergSecurityTypeDefinition.DEFAULT_CONFIG_NAME);
  }

  private static BloombergSecurityTypeDefinition buildDefaultConfig() {
    final BloombergSecurityTypeDefinition definition = new BloombergSecurityTypeDefinition();

    definition.addSecurityType("Common Stock", EQUITY);
    definition.addSecurityType("Preference", EQUITY);
    definition.addSecurityType("ADR", EQUITY);
    definition.addSecurityType("Open-End Fund", EQUITY);
    definition.addSecurityType("Closed-End Fund", EQUITY);
    definition.addSecurityType("ETP", EQUITY);
    definition.addSecurityType("REIT", EQUITY);
    definition.addSecurityType("Tracking Stk", EQUITY);
    definition.addSecurityType("Unit", EQUITY);
    definition.addSecurityType("Right", EQUITY);
    definition.addSecurityType("Ltd Part", EQUITY);
    definition.addSecurityType("NY Reg Shrs", EQUITY);
    definition.addSecurityType("PUBLIC", EQUITY);
    definition.addSecurityType("Equity WRT", EQUITY);

    definition.addSecurityType(BBG_WHEAT, AGRICULTURE_FUTURE);
    definition.addSecurityType(BBG_SOY, AGRICULTURE_FUTURE);
    definition.addSecurityType(BBG_LIVESTOCK, AGRICULTURE_FUTURE);
    definition.addSecurityType(BBG_FOODSTUFF, AGRICULTURE_FUTURE);

    definition.addSecurityType(BLOOMBERG_BOND_FUTURE_TYPE, BOND_FUTURE);

    definition.addSecurityType(BBG_REFINED_PRODUCTS, ENERGY_FUTURE);
    definition.addSecurityType(BBG_ELECTRICITY, ENERGY_FUTURE);
    definition.addSecurityType(BBG_COAL, ENERGY_FUTURE);
    definition.addSecurityType(BBG_CRUDE_OIL, ENERGY_FUTURE);

    definition.addSecurityType(BBG_STOCK_FUTURE_TYPE, EQUITY_DIVIDEND_FUTURE);

    definition.addSecurityType(BBG_CURRENCY_TYPE, FX_FUTURE);

    definition.addSecurityType(BLOOMBERG_EQUITY_INDEX_TYPE, INDEX_FUTURE);

    definition.addSecurityType(BLOOMBERG_INTEREST_RATE_TYPE, INTEREST_RATE_FUTURE);

    definition.addSecurityType(BBG_PRECIOUS_METAL_TYPE, METAL_FUTURE);
    definition.addSecurityType(BBG_BASE_METAL_TYPE, METAL_FUTURE);

    definition.addSecurityType(BLOOMBERG_EQUITY_INDEX_TYPE, EQUITY_FUTURE);
    definition.addSecurityType(BLOOMBERG_EQUITY_INDEX_TYPE, EQUITY_FUTURE);

    definition.addSecurityType(BLOOMBERG_EQUITY_OPTION_SECURITY_TYPE, EQUITY_OPTION);

    definition.addSecurityType("Equity Index Spot Options", EQUITY_INDEX_OPTION);
    definition.addSecurityType("Equity Volatility Index Option", EQUITY_INDEX_OPTION);

    definition.addSecurityType("Equity Index", EQUITY_INDEX_FUTURE_OPTION);

    definition.addSecurityType(BLOOMBERG_INTEREST_RATE_TYPE, IR_FUTURE_OPTION);
    definition.addSecurityType(BLOOMBERG_FINANCIAL_COMMODITY_OPTION_TYPE, IR_FUTURE_OPTION);

    definition.addSecurityType(BLOOMBERG_BOND_FUTURE_TYPE, BOND_FUTURE_OPTION);

    definition.addSecurityType(BBG_PRECIOUS_METAL_TYPE, COMMODITY_FUTURE_OPTION);
    definition.addSecurityType(BBG_BASE_METAL_TYPE, COMMODITY_FUTURE_OPTION);
    definition.addSecurityType(BBG_REFINED_PRODUCTS, COMMODITY_FUTURE_OPTION);
    definition.addSecurityType(BBG_ELECTRICITY, COMMODITY_FUTURE_OPTION);
    definition.addSecurityType(BBG_COAL, COMMODITY_FUTURE_OPTION);
    definition.addSecurityType(BBG_CRUDE_OIL, COMMODITY_FUTURE_OPTION);
    definition.addSecurityType(BBG_WHEAT, COMMODITY_FUTURE_OPTION);
    definition.addSecurityType(BBG_SOY, COMMODITY_FUTURE_OPTION);
    definition.addSecurityType(BBG_FOODSTUFF, COMMODITY_FUTURE_OPTION);
    definition.addSecurityType(BBG_LIVESTOCK, COMMODITY_FUTURE_OPTION);

    definition.addSecurityType(BLOOMBERG_CURRENCY_TYPE, FX_FUTURE_OPTION);
    definition.addSecurityType(BLOOMBERG_PHYSICAL_INDEX_FUTURE_TYPE, EQUITY_FUTURE);

    definition.addSecurityType("SWAP", SWAP);
    definition.addSecurityType("OVERNIGHT INDEXED SWAP", SWAP);
    definition.addSecurityType("FWD SWAP", SWAP);
    definition.addSecurityType("NDF SWAP", SWAP);
    definition.addSecurityType("ONSHORE SWAP", SWAP);
    definition.addSecurityType(BLOOMBERG_NON_DELIVERABLE_IRS_SWAP_TYPE, SWAP);

    definition.addSecurityType("BASIS SWAP", BASIS_SWAP);

    definition.addSecurityType("Prvt CMO FLT", BOND);
    definition.addSecurityType("EURO MTN", BOND);
    definition.addSecurityType("EURO-ZONE", BOND);
    definition.addSecurityType("CF", BOND);
    definition.addSecurityType("ABS Other", BOND);
    definition.addSecurityType("EURO NON-DOLLAR", BOND);
    definition.addSecurityType("CMBS", BOND);
    definition.addSecurityType("ABS Auto", BOND);
    definition.addSecurityType("PRIV PLACEMENT", BOND);
    definition.addSecurityType("GLOBAL", BOND);
    definition.addSecurityType("EURO-DOLLAR", BOND);
    definition.addSecurityType("YANKEE", BOND);
    definition.addSecurityType("US DOMESTIC", BOND);
    definition.addSecurityType("ABS Card", BOND);
    definition.addSecurityType("Prvt CMO Other", BOND);
    definition.addSecurityType("SN", BOND);
    definition.addSecurityType("Agncy ABS Other", BOND);
    definition.addSecurityType("US GOVERNMENT", BOND);
    definition.addSecurityType("UK GILT STOCK", BOND);
    definition.addSecurityType("CANADIAN", BOND);
    definition.addSecurityType("DOMESTIC", BOND);

    definition.addSecurityType("BANK BILL", BILL);

    definition.addSecurityType(BLOOMBERG_EQUITY_INDEX_TYPE, EQUITY_INDEX);

    definition.addSecurityType("FORWARD CROSS", FORWARD_CROSS);

    definition.addSecurityType("FRA", FRA);

    definition.addSecurityType("DEPOSIT", RATE);
    definition.addSecurityType("Index", RATE);

    definition.addSecurityType("Physical commodity spot.", SPOT_RATE);
    definition.addSecurityType("SPOT", SPOT_RATE);
    definition.addSecurityType("CROSS", SPOT_RATE);
    definition.addSecurityType("CD", SPOT_RATE);

    definition.addSecurityType("OPTION VOLATILITY", VOLATILITY_QUOTE);
    definition.addSecurityType("SWAPTION VOLATILITY", VOLATILITY_QUOTE);

    definition.addSecurityType("FORWARD", FX_FORWARD);
    definition.addSecurityType("ONSHORE FORWARD", FX_FORWARD);
    definition.addSecurityType("NON-DELIVERABLE FORWARD", FX_FORWARD);

    definition.addSecurityType("CREDIT DEFAULT SWAP", CREDIT_DEFAULT_SWAP);

    return definition;
  }

  public ConfigMaster getConfigMaster() {
    return _configMaster;
  }

  /**
   * Runs the tool.
   */
  public void run() {
    final ConfigItem<BloombergSecurityTypeDefinition> configItem = ConfigItem.of(DEFAULT_CONFIG, _configName);

    final ConfigSearchRequest<BloombergSecurityTypeDefinition> req = new ConfigSearchRequest<BloombergSecurityTypeDefinition>();
    req.setType(BloombergSecurityTypeDefinition.class);
    req.setName(configItem.getName());
    final ConfigDocument configDocument = new ConfigDocument(configItem);
    final ConfigSearchResult<BloombergSecurityTypeDefinition> res = getConfigMaster().search(req);
    if (res.getDocuments().isEmpty()) {
      getConfigMaster().add(configDocument);
    } else {
      configDocument.setUniqueId(res.getDocuments().get(0).getUniqueId());
      getConfigMaster().update(configDocument);
    }
  }

}
