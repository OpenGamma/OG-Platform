/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata.normalization;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.normalization.BloombergRateClassifier;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.livedata.normalization.NormalizationRule;
import com.opengamma.livedata.normalization.SecurityRuleProvider;
import com.opengamma.livedata.normalization.UnitChange;

/**
 * Provider of normalization rules for Bloomberg rates, taking into account the security type.
 */
public class BloombergRateRuleProvider implements SecurityRuleProvider {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergRateRuleProvider.class);

  private static final Set<String> FIELDS = Sets.newHashSet(MarketDataRequirementNames.MARKET_VALUE, MarketDataRequirementNames.DIRTY_PRICE_MID);
  private static final NormalizationRule RULE_10 = new UnitChange(FIELDS, 0.1);
  private static final NormalizationRule RULE_100 = new UnitChange(FIELDS, 0.01);
  private static final NormalizationRule RULE_1000 = new UnitChange(FIELDS, 0.001);
  private static final NormalizationRule RULE_10000 = new UnitChange(FIELDS, 0.0001);
  private static final NormalizationRule RULE_100000 = new UnitChange(FIELDS, 0.00001);
  private static final NormalizationRule RULE_1000000 = new UnitChange(FIELDS, 0.000001);

  private final BloombergRateClassifier _classifier;
  
  /**
   * A flag to control whether unknown security types cause normalization to fail (the default), or whether the value
   * will be passed through unnormalized. This may be controlled externally at runtime.
   */
  private volatile boolean _failUnknownSecurityType = true;
  
  public BloombergRateRuleProvider(BloombergRateClassifier classifier) {
    _classifier = classifier;
  }
  
  public boolean isFailUnknownSecurityType() {
    return _failUnknownSecurityType;
  }
  
  public void setFailUnknownSecurityType(boolean failUnknownSecurityType) {
    _failUnknownSecurityType = failUnknownSecurityType;
  }

  @Override
  public NormalizationRule getRule(String securityUniqueId) {
    Integer normalizationFactor = _classifier.getNormalizationFactor(securityUniqueId);
    if (normalizationFactor == null) {
      if (_failUnknownSecurityType) {
        throw new OpenGammaRuntimeException("Unable to determine security type for " + securityUniqueId);
      } else {
        s_logger.warn("Unable to determine normalization factor for " + securityUniqueId + ". Its market value will be unnormalized.");
        return null;
      }
    }
    switch (normalizationFactor) {
      case 1:
        return null;
      case 10:
        return RULE_10;
      case 100:
        return RULE_100;
      case 1000:
        return RULE_1000;
      case 10000:
        return RULE_10000;
      case 100000:
        return RULE_100000;
      case 1000000:
        return RULE_1000000;
      default:
        throw new OpenGammaRuntimeException("Unexpected normalization factor: " + normalizationFactor);
    }
  }
  
}
