/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.livedata.resolver.JmsTopicNameResolver;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author pietari
 */
public class NormalizationRuleSet implements Serializable {
  
  private static final FudgeContext FUDGE_CONTEXT = FudgeContext.GLOBAL_DEFAULT;
  
  private final String _id;
  private final String _jmsTopicSuffix;
  private final List<NormalizationRule> _rules;
  
  /** Useful for tests */
  public NormalizationRuleSet(String id) {
    this(id, id, Collections.<NormalizationRule>emptyList()); 
  }
  
  public NormalizationRuleSet(String id, 
      String jmsTopicSuffix,
      List<NormalizationRule> rules) {
    ArgumentChecker.checkNotNull(id, "Rule set ID");
    ArgumentChecker.checkNotNull(jmsTopicSuffix, "Jms Topic Suffix");
    ArgumentChecker.checkNotNull(rules, "StandardRules");
    _id = id;
    
    if (!jmsTopicSuffix.isEmpty() && !jmsTopicSuffix.startsWith(JmsTopicNameResolver.SEPARATOR)) {
      _jmsTopicSuffix = JmsTopicNameResolver.SEPARATOR + jmsTopicSuffix;
    } else {
      _jmsTopicSuffix = jmsTopicSuffix;
    }
    
    _rules = new ArrayList<NormalizationRule>(rules);    
  }
  
  public FudgeFieldContainer getNormalizedMessage(FudgeFieldContainer msg) {
    MutableFudgeFieldContainer normalizedMsg = FUDGE_CONTEXT.newMessage(msg);
    for (NormalizationRule rule : _rules) {
      rule.apply(normalizedMsg);      
    }
    return normalizedMsg;
  }
  
  public String getId() {
    return _id;
  }
  
  /**
   * Return value, if non-empty, will always start with {@link JmsTopicNameResolver#SEPARATOR}.
   * However, an empty string is also a possibility.  
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
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    NormalizationRuleSet other = (NormalizationRuleSet) obj;
    if (_id == null) {
      if (other._id != null)
        return false;
    } else if (!_id.equals(other._id))
      return false;
    return true;
  }
  
}
