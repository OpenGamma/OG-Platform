/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.opengamma.livedata.resolver.JmsTopicNameResolver;
import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * An ordered set of normalization rules.
 */
public class NormalizationRuleSet {
  
  private static final Logger s_logger = LoggerFactory.getLogger(NormalizationRuleSet.class);
  
  private final String _id;
  private final String _jmsTopicSuffix;
  private final List<NormalizationRule> _rules;
  
  /* Useful for tests */
  public NormalizationRuleSet(String id) {
    this(id, id, Collections.<NormalizationRule>emptyList()); 
  }
  
  /* Also useful for tests */
  public NormalizationRuleSet(String id, NormalizationRule... rules) {
    this(id, id, Lists.newArrayList(rules));
  }
  
  public NormalizationRuleSet(String id, 
      String jmsTopicSuffix,
      List<NormalizationRule> rules) {
    ArgumentChecker.notNull(id, "Rule set ID");
    ArgumentChecker.notNull(jmsTopicSuffix, "Jms Topic Suffix");
    ArgumentChecker.notNull(rules, "StandardRules");
    _id = id;
    
    if (!jmsTopicSuffix.isEmpty() && !jmsTopicSuffix.startsWith(JmsTopicNameResolver.SEPARATOR)) {
      _jmsTopicSuffix = JmsTopicNameResolver.SEPARATOR + jmsTopicSuffix;
    } else {
      _jmsTopicSuffix = jmsTopicSuffix;
    }
    
    _rules = new ArrayList<NormalizationRule>(rules);    
  }
  
  /**
   * Gets a normalized message.
   * This is done by applying the set of normalization rules
   * to the raw message. 
   * 
   * @param msg message received from underlying market data API in its native format.
   * @param securityUniqueId  the data provider's unique ID of the security, not null
   * @param fieldHistory history of field values  
   * @return the normalized message. Null if one of the normalization rules
   * rejected the message.
   */
  public FudgeMsg getNormalizedMessage(FudgeMsg msg, String securityUniqueId, FieldHistoryStore fieldHistory) {
    MutableFudgeMsg normalizedMsg = OpenGammaFudgeContext.getInstance().newMessage(msg);
    for (NormalizationRule rule : _rules) {
      normalizedMsg = rule.apply(normalizedMsg, securityUniqueId, fieldHistory);
      if (normalizedMsg == null) {
        // One of the rules rejected the message entirely.
        s_logger.debug("Rule {} in rule set {} rejected message {}", new Object[] {rule, getId(), normalizedMsg});
        break;
      }
    }
    s_logger.debug("Applying rule set {} to message {} produced normalized message {}", new Object[] {getId(), msg, normalizedMsg});
    return normalizedMsg;
  }
  
  /**
   * Gets the ID of this normalization rule set.
   * 
   * @return the ID of this normalization rule set.
   */
  public String getId() {
    return _id;
  }
  
  /**
   * Gets the Jms topic suffix of this normalization rule set.
   * <p>
   * The return value, if non-empty, will always start with {@link JmsTopicNameResolver#SEPARATOR}.
   * However, an empty string is also a possibility.
   * 
   * @return the JMS topic suffix
   */
  public String getJmsTopicSuffix() {
    return _jmsTopicSuffix;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_id == null) ? 0 : _id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    NormalizationRuleSet other = (NormalizationRuleSet) obj;
    if (_id == null) {
      if (other._id != null) {
        return false;
      }
    } else if (!_id.equals(other._id)) {
      return false;
    }
    return true;
  }
  
}
