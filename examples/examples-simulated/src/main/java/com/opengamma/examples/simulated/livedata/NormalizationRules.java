/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.examples.simulated.livedata;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.livedata.normalization.FieldHistoryUpdater;
import com.opengamma.livedata.normalization.FieldNameChange;
import com.opengamma.livedata.normalization.MarketValueCalculator;
import com.opengamma.livedata.normalization.NormalizationRule;
import com.opengamma.livedata.normalization.NormalizationRuleSet;
import com.opengamma.livedata.normalization.StandardRules;

/**
 * Produces the normalization ruleset for the Simulated supplied data.
 */
public final class NormalizationRules {

  private NormalizationRules() {
  }

  public static NormalizationRuleSet getMarketValueNormalization() {
    final List<NormalizationRule> rules = new ArrayList<NormalizationRule>();
    
    rules.add(new FieldNameChange("LAST_PRICE", MarketDataRequirementNames.LAST));
    rules.add(new FieldNameChange("BID", MarketDataRequirementNames.BID));
    rules.add(new FieldNameChange("ASK", MarketDataRequirementNames.ASK));
    rules.add(new FieldNameChange("PX_SETTLE", MarketDataRequirementNames.SETTLE_PRICE));
    rules.add(new FieldNameChange("VOLUME", MarketDataRequirementNames.VOLUME));
    rules.add(new FieldNameChange("OPT_IMPLIED_VOLATILITY_BID_RT", MarketDataRequirementNames.BID_IMPLIED_VOLATILITY));
    rules.add(new FieldNameChange("OPT_IMPLIED_VOLATILITY_ASK_RT", MarketDataRequirementNames.ASK_IMPLIED_VOLATILITY));
    rules.add(new FieldNameChange("OPT_IMPLIED_VOLATILITY_LAST_RT", MarketDataRequirementNames.LAST_IMPLIED_VOLATILITY));
    rules.add(new FieldNameChange("OPT_IMPLIED_VOLATILITY_MID_RT", MarketDataRequirementNames.MID_IMPLIED_VOLATILITY));
    rules.add(new FieldNameChange("YLD_CNV_MID", MarketDataRequirementNames.YIELD_CONVENTION_MID));
    rules.add(new FieldNameChange("YLD_YTM_MID", MarketDataRequirementNames.YIELD_YIELD_TO_MATURITY_MID));
    rules.add(new FieldNameChange("PX_DIRTY_MID", MarketDataRequirementNames.DIRTY_PRICE_MID));
    
    rules.add(new MarketValueCalculator());
    rules.add(new FieldHistoryUpdater());
    return new NormalizationRuleSet(StandardRules.getOpenGammaRuleSetId(), "", rules);
  }

}
