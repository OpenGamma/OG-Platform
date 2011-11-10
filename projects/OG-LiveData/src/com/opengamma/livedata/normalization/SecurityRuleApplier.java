/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import org.fudgemsg.MutableFudgeMsg;

import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.util.ArgumentChecker;

/**
 * Multiplies the value of a {@code Double} field by a security-dependent value.
 */
public class SecurityRuleApplier implements NormalizationRule {

  private final SecurityRuleProvider _ruleProvider;
  
  public SecurityRuleApplier(SecurityRuleProvider ruleProvider) {
    ArgumentChecker.notNull(ruleProvider, "ruleProvider");
    _ruleProvider = ruleProvider;
  }
  
  @Override
  public MutableFudgeMsg apply(MutableFudgeMsg msg, String securityUniqueId, FieldHistoryStore fieldHistory) {
    try {
      NormalizationRule rule = _ruleProvider.getRule(securityUniqueId);
      if (rule == null) {
        return msg;
      }
      return rule.apply(msg, securityUniqueId, fieldHistory);
    } catch (Exception e) {
      // Interpret an exception as a rejection of the message
      return null;
    }
  }

}
