/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import java.util.Collection;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMessageFactory;
import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.id.DomainSpecificIdentifier;
import com.opengamma.id.DomainSpecificIdentifiers;
import com.opengamma.id.IdentificationDomain;
import com.opengamma.util.ArgumentChecker;

/**
 * Specifies what data you want, in what format.
 * Used on the client side.
 *
 * @author pietari
 */
public class LiveDataSpecification {
  
  private final static String NORMALIZATION_RULE_SET_ID_FIELD_NAME = "NormalizationRuleSetId";
  private final static String DOMAIN_SPECIFIC_IDS_FIELD_NAME = "DomainSpecificIdentifiers";
  
  /** A set of IDs for a single ticker **/
  private final DomainSpecificIdentifiers _domainSpecificIdentifiers;
  
  /** What format it should be sent to the client **/
  private final String _normalizationRuleSetId;
  
  public LiveDataSpecification(LiveDataSpecification source) {
    this(source.getNormalizationRuleSetId(), source.getIdentifiers());        
  }
  
  public LiveDataSpecification(String normalizationRuleSetId, DomainSpecificIdentifier... identifiers) {
    this(normalizationRuleSetId, new DomainSpecificIdentifiers(identifiers));
  }
  
  public LiveDataSpecification(String normalizationRuleSetId, Collection<? extends DomainSpecificIdentifier> identifiers) {
    this(normalizationRuleSetId, new DomainSpecificIdentifiers(identifiers));
  }
  
  public LiveDataSpecification(String normalizationRuleSetId, DomainSpecificIdentifier identifier) {
    this(normalizationRuleSetId, new DomainSpecificIdentifiers(identifier));
  }
  
  public LiveDataSpecification(String normalizationRuleSetId, DomainSpecificIdentifiers domainSpecificIdentifiers) {
    ArgumentChecker.checkNotNull(normalizationRuleSetId, "Client data format");
    ArgumentChecker.checkNotNull(domainSpecificIdentifiers, "Identifiers");
    _domainSpecificIdentifiers = domainSpecificIdentifiers;
    _normalizationRuleSetId = normalizationRuleSetId;
  }
  
  public String getNormalizationRuleSetId() {
    return _normalizationRuleSetId;
  }

  public DomainSpecificIdentifiers getIdentifiers() {
    return _domainSpecificIdentifiers;
  }
  
  public String getIdentifier(IdentificationDomain domain) {
    return _domainSpecificIdentifiers.getIdentifier(domain);
  }
  
  public static LiveDataSpecification fromFudgeMsg(FudgeFieldContainer fudgeMsg) {
    String normalizationRuleSetId = fudgeMsg.getString(NORMALIZATION_RULE_SET_ID_FIELD_NAME);
    DomainSpecificIdentifiers ids = new DomainSpecificIdentifiers(fudgeMsg.getMessage(DOMAIN_SPECIFIC_IDS_FIELD_NAME));
    return new LiveDataSpecification(normalizationRuleSetId, ids);    
  }
  
  public FudgeFieldContainer toFudgeMsg(FudgeMessageFactory fudgeMessageFactory) {
    ArgumentChecker.checkNotNull(fudgeMessageFactory, "Fudge Context");
    MutableFudgeFieldContainer msg = fudgeMessageFactory.newMessage();
    msg.add(NORMALIZATION_RULE_SET_ID_FIELD_NAME, _normalizationRuleSetId);
    msg.add(DOMAIN_SPECIFIC_IDS_FIELD_NAME, _domainSpecificIdentifiers.toFudgeMsg(fudgeMessageFactory));
    return msg;
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
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    LiveDataSpecification other = (LiveDataSpecification) obj;
    if (_domainSpecificIdentifiers == null) {
      if (other._domainSpecificIdentifiers != null)
        return false;
    } else if (!_domainSpecificIdentifiers
        .equals(other._domainSpecificIdentifiers))
      return false;
    if (_normalizationRuleSetId == null) {
      if (other._normalizationRuleSetId != null)
        return false;
    } else if (!_normalizationRuleSetId.equals(other._normalizationRuleSetId))
      return false;
    return true;
  }

}
