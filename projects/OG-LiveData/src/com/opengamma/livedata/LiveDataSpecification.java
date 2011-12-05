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
 * Specifies the live data that is desired, and in what format.
 * <p>
 * Live data represents updating data, typically from a market.
 * This class defines the data required and the format.
 */
@PublicAPI
public class LiveDataSpecification {

  private static final String NORMALIZATION_RULE_SET_ID_FIELD_NAME = "NormalizationRuleSetId";
  private static final String DOMAIN_SPECIFIC_IDS_FIELD_NAME = "DomainSpecificIdentifiers";

  /**
   * The external identifier bundle describing the desired data, such as the ticker.
   */
  private final ExternalIdBundle _externalIdBundle;
  /**
   * The format that the data should be sent to the client.
   */
  private final String _normalizationRuleSetId;

  /**
   * Creates an instance by copying another instance.
   * 
   * @param specificationToCopy  the original to copy, not null
   */
  public LiveDataSpecification(LiveDataSpecification specificationToCopy) {
    this(specificationToCopy.getNormalizationRuleSetId(), specificationToCopy.getIdentifiers());
  }

  /**
   * Creates an instance from a set of external identifiers.
   * 
   * @param normalizationRuleSetId  the rule defining the data format to return, not null
   * @param externalIds  the external identifiers defining the data to fetch, not null
   */
  public LiveDataSpecification(String normalizationRuleSetId, ExternalId... externalIds) {
    this(normalizationRuleSetId, ExternalIdBundle.of(externalIds));
  }

  /**
   * Creates an instance from a set of external identifiers.
   * 
   * @param normalizationRuleSetId  the rule defining the data format to return, not null
   * @param externalIds  the external identifiers defining the data to fetch, not null
   */
  public LiveDataSpecification(String normalizationRuleSetId, Collection<ExternalId> externalIds) {
    this(normalizationRuleSetId, ExternalIdBundle.of(externalIds));
  }

  /**
   * Creates an instance from a single external identifier.
   * 
   * @param normalizationRuleSetId  the rule defining the data format to return, not null
   * @param externalId  the external identifiers defining the data to fetch, not null
   */
  public LiveDataSpecification(String normalizationRuleSetId, ExternalId externalId) {
    this(normalizationRuleSetId, ExternalIdBundle.of(externalId));
  }

  /**
   * Creates an instance from an external identifier bundle.
   * 
   * @param normalizationRuleSetId  the rule defining the data format to return, not null
   * @param bundle  the external identifier bundle defining the data to fetch, not null
   */
  public LiveDataSpecification(String normalizationRuleSetId, ExternalIdBundle bundle) {
    ArgumentChecker.notNull(normalizationRuleSetId, "normalizationRuleSetId");
    ArgumentChecker.notNull(bundle, "bundle");
    _externalIdBundle = bundle;
    _normalizationRuleSetId = normalizationRuleSetId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the format that the data should be sent to the client.
   * 
   * @return the data format, not null
   */
  public String getNormalizationRuleSetId() {
    return _normalizationRuleSetId;
  }

  /**
   * Gets the external identifier bundle specifying the data to obtain.
   * 
   * @return the bundle, not null
   */
  public ExternalIdBundle getIdentifiers() {
    return _externalIdBundle;
  }

  /**
   * Gets the value of an identifier by scheme.
   * 
   * @param scheme  the scheme to query, null returns null
   * @return the value for the scheme, null if not found
   */
  public String getIdentifier(ExternalScheme scheme) {
    return _externalIdBundle.getValue(scheme);
  }

  //-------------------------------------------------------------------------
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

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof LiveDataSpecification) {
      LiveDataSpecification other = (LiveDataSpecification) obj;
      return _externalIdBundle.equals(other._externalIdBundle) && _normalizationRuleSetId.equals(other._normalizationRuleSetId);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _externalIdBundle.hashCode() ^ _normalizationRuleSetId.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append("LiveDataSpecification[");
    buf.append(_externalIdBundle.toString());
    buf.append("]");
    return buf.toString();
  }

}
