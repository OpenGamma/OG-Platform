/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.util.ArgumentChecker;

/**
 * Multiplies the value of a {@code Double} field by a security-dependent value.
 */
public class SecurityRuleApplier implements NormalizationRule {
  private static final Logger s_logger = LoggerFactory.getLogger(SecurityRuleApplier.class);
  
  private final SecurityRuleProvider _ruleProvider;
  
  public SecurityRuleApplier(SecurityRuleProvider ruleProvider) {
    ArgumentChecker.notNull(ruleProvider, "ruleProvider");
    _ruleProvider = ruleProvider;
  }
  
  @Override
  public MutableFudgeMsg apply(MutableFudgeMsg msg, String securityUniqueId, FieldHistoryStore fieldHistory) {
    NormalizationRule rule;
    try {
      rule = _ruleProvider.getRule(securityUniqueId);
      if (rule == null) {
        return msg;
      }
    } catch (Exception e) {
      s_logger.warn("Failed to get normalization rule for security id {} : {}", securityUniqueId, e.getMessage());
      return null;
    }
    
    try {
      return rule.apply(msg, securityUniqueId, fieldHistory);
    } catch (Exception e) {
      s_logger.debug("Rule {} rejected message with exception {}", rule.toString(), e.getMessage());
      // Interpret an exception as a rejection of the message
      return null;
    }
  }

}
