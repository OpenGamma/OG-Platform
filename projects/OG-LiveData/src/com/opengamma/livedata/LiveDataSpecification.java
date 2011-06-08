/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import java.util.Collection;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgFactory;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializationContext;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * Specifies what data you want, in what format.
 */
@PublicAPI
public class LiveDataSpecification {
  
  private static final String NORMALIZATION_RULE_SET_ID_FIELD_NAME = "NormalizationRuleSetId";
  private static final String DOMAIN_SPECIFIC_IDS_FIELD_NAME = "DomainSpecificIdentifiers";
  
  /** A set of IDs for a single ticker **/
  private final IdentifierBundle _domainSpecificIdentifiers;
  
  /** What format it should be sent to the client **/
  private final String _normalizationRuleSetId;
  
  public LiveDataSpecification(LiveDataSpecification source) {
    this(source.getNormalizationRuleSetId(), source.getIdentifiers());        
  }
  
  public LiveDataSpecification(String normalizationRuleSetId, Identifier... identifiers) {
    this(normalizationRuleSetId, IdentifierBundle.of(identifiers));
  }
  
  public LiveDataSpecification(String normalizationRuleSetId, Collection<Identifier> identifiers) {
    this(normalizationRuleSetId, IdentifierBundle.of(identifiers));
  }
  
  public LiveDataSpecification(String normalizationRuleSetId, Identifier identifier) {
    this(normalizationRuleSetId, IdentifierBundle.of(identifier));
  }
  
  public LiveDataSpecification(String normalizationRuleSetId, IdentifierBundle domainSpecificIdentifiers) {
    ArgumentChecker.notNull(normalizationRuleSetId, "Client data format");
    ArgumentChecker.notNull(domainSpecificIdentifiers, "Identifiers");
    _domainSpecificIdentifiers = domainSpecificIdentifiers;
    _normalizationRuleSetId = normalizationRuleSetId;
  }
  
  public String getNormalizationRuleSetId() {
    return _normalizationRuleSetId;
  }

  public IdentifierBundle getIdentifiers() {
    return _domainSpecificIdentifiers;
  }
  
  public String getIdentifier(IdentificationScheme domain) {
    return _domainSpecificIdentifiers.getIdentifier(domain);
  }
  
  public static LiveDataSpecification fromFudgeMsg(FudgeDeserializationContext fudgeContext, FudgeMsg fudgeMsg) {
    String normalizationRuleSetId = fudgeMsg.getString(NORMALIZATION_RULE_SET_ID_FIELD_NAME);
    IdentifierBundle ids = IdentifierBundle.fromFudgeMsg(fudgeContext, fudgeMsg.getMessage(DOMAIN_SPECIFIC_IDS_FIELD_NAME));
    return new LiveDataSpecification(normalizationRuleSetId, ids);    
  }
  
  public FudgeMsg toFudgeMsg(FudgeMsgFactory fudgeMessageFactory) {
    ArgumentChecker.notNull(fudgeMessageFactory, "Fudge Context");
    MutableFudgeMsg msg = fudgeMessageFactory.newMessage();
    msg.add(NORMALIZATION_RULE_SET_ID_FIELD_NAME, _normalizationRuleSetId);
    msg.add(DOMAIN_SPECIFIC_IDS_FIELD_NAME, _domainSpecificIdentifiers.toFudgeMsg(fudgeMessageFactory));
    return msg;
  }
  
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE); 
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime
        * result
        + ((_domainSpecificIdentifiers == null) ? 0
            : _domainSpecificIdentifiers.hashCode());
    result = prime
        * result
        + ((_normalizationRuleSetId == null) ? 0 : _normalizationRuleSetId
            .hashCode());
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
    LiveDataSpecification other = (LiveDataSpecification) obj;
    if (_domainSpecificIdentifiers == null) {
      if (other._domainSpecificIdentifiers != null) {
        return false;
      }
    } else if (!_domainSpecificIdentifiers.equals(other._domainSpecificIdentifiers)) {
      return false;
    }
    if (_normalizationRuleSetId == null) {
      if (other._normalizationRuleSetId != null) {
        return false;
      }
    } else if (!_normalizationRuleSetId.equals(other._normalizationRuleSetId)) {
      return false;
    }
    return true;
  }

}
