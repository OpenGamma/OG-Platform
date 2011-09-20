/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import java.util.Collection;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleFudgeBuilder;
import com.opengamma.id.ExternalScheme;
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
  private final ExternalIdBundle _externalIdBundle;
  
  /** What format it should be sent to the client **/
  private final String _normalizationRuleSetId;
  
  public LiveDataSpecification(LiveDataSpecification source) {
    this(source.getNormalizationRuleSetId(), source.getIdentifiers());        
  }
  
  public LiveDataSpecification(String normalizationRuleSetId, ExternalId... identifiers) {
    this(normalizationRuleSetId, ExternalIdBundle.of(identifiers));
  }
  
  public LiveDataSpecification(String normalizationRuleSetId, Collection<ExternalId> identifiers) {
    this(normalizationRuleSetId, ExternalIdBundle.of(identifiers));
  }
  
  public LiveDataSpecification(String normalizationRuleSetId, ExternalId identifier) {
    this(normalizationRuleSetId, ExternalIdBundle.of(identifier));
  }
  
  public LiveDataSpecification(String normalizationRuleSetId, ExternalIdBundle bundle) {
    ArgumentChecker.notNull(normalizationRuleSetId, "Client data format");
    ArgumentChecker.notNull(bundle, "bundle");
    _externalIdBundle = bundle;
    _normalizationRuleSetId = normalizationRuleSetId;
  }
  
  public String getNormalizationRuleSetId() {
    return _normalizationRuleSetId;
  }

  public ExternalIdBundle getIdentifiers() {
    return _externalIdBundle;
  }
  
  public String getIdentifier(ExternalScheme scheme) {
    return _externalIdBundle.getValue(scheme);
  }
  
  public static LiveDataSpecification fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg fudgeMsg) {
    String normalizationRuleSetId = fudgeMsg.getString(NORMALIZATION_RULE_SET_ID_FIELD_NAME);
    ExternalIdBundle ids = ExternalIdBundleFudgeBuilder.fromFudgeMsg(deserializer, fudgeMsg.getMessage(DOMAIN_SPECIFIC_IDS_FIELD_NAME));
    return new LiveDataSpecification(normalizationRuleSetId, ids);    
  }
  
  public FudgeMsg toFudgeMsg(FudgeSerializer serializer) {
    ArgumentChecker.notNull(serializer, "FudgeSerializer");
    MutableFudgeMsg msg = serializer.newMessage();
    msg.add(NORMALIZATION_RULE_SET_ID_FIELD_NAME, _normalizationRuleSetId);
    msg.add(DOMAIN_SPECIFIC_IDS_FIELD_NAME, ExternalIdBundleFudgeBuilder.toFudgeMsg(serializer, _externalIdBundle));
    return msg;
  }
  
  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("LiveDataSpecification[");
    stringBuilder.append(_externalIdBundle.toString());
    stringBuilder.append("]");
    return stringBuilder.toString(); 
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime
        * result
        + ((_externalIdBundle == null) ? 0
            : _externalIdBundle.hashCode());
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
    if (_externalIdBundle == null) {
      if (other._externalIdBundle != null) {
        return false;
      }
    } else if (!_externalIdBundle.equals(other._externalIdBundle)) {
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
