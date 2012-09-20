/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata;

import static com.opengamma.bbg.BloombergConstants.VALID_EQUITY_TYPES;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.referencedata.ReferenceData;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetRequest;
import com.opengamma.bbg.referencedata.ReferenceDataProviderGetResult;
import com.opengamma.bbg.util.BloombergDomainIdentifierResolver;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.livedata.resolver.AbstractResolver;
import com.opengamma.livedata.resolver.JmsTopicNameResolveRequest;
import com.opengamma.livedata.resolver.JmsTopicNameResolver;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 */
public class BloombergJmsTopicNameResolver extends AbstractResolver<JmsTopicNameResolveRequest, String> implements JmsTopicNameResolver {
  
  private static final Logger s_logger = LoggerFactory
  .getLogger(BloombergJmsTopicNameResolver.class);
 
  private final ReferenceDataProvider _referenceDataProvider;
  
  private static final Set<String> BBG_VALID_EQUITY_TYPES = VALID_EQUITY_TYPES;
  
  public BloombergJmsTopicNameResolver(ReferenceDataProvider referenceDataProvider) {
    ArgumentChecker.notNull(referenceDataProvider, "Reference Data Provider");
    _referenceDataProvider = referenceDataProvider;
  }

  /**
   * @return the referenceDataProvider
   */
  public ReferenceDataProvider getReferenceDataProvider() {
    return _referenceDataProvider;
  }

  @Override
  public Map<JmsTopicNameResolveRequest, String> resolve(Collection<JmsTopicNameResolveRequest> requests) {
    Map<JmsTopicNameResolveRequest, String> returnValue = new HashMap<JmsTopicNameResolveRequest, String>();

    Map<String, Collection<JmsTopicNameResolveRequest>> lookupKey2Requests = new HashMap<String, Collection<JmsTopicNameResolveRequest>>();
    
    for (JmsTopicNameResolveRequest request : requests) {
      ExternalId marketDataUniqueId = request.getMarketDataUniqueId();
      if (!marketDataUniqueId.getScheme().equals(ExternalSchemes.BLOOMBERG_BUID)) {
        s_logger.info("No Bloomberg BUID found, was given: " + marketDataUniqueId);
        returnValue.put(request, null);
        continue;
      }
      
      String lookupKey = BloombergDomainIdentifierResolver.toBloombergKey(marketDataUniqueId);
      
      Collection<JmsTopicNameResolveRequest> requestsForLookupKey = lookupKey2Requests.get(lookupKey);
      if (requestsForLookupKey == null) {
        requestsForLookupKey = new ArrayList<JmsTopicNameResolveRequest>();
        lookupKey2Requests.put(lookupKey, requestsForLookupKey);
      }
      requestsForLookupKey.add(request);
    }
    
    if (!lookupKey2Requests.keySet().isEmpty()) {
      ReferenceDataProviderGetRequest rdRequest = ReferenceDataProviderGetRequest.createGet(
          lookupKey2Requests.keySet(), BloombergConstants.JMS_TOPIC_NAME_RESOLVER_FIELDS, true);
      ReferenceDataProviderGetResult referenceData = _referenceDataProvider.getReferenceData(rdRequest);
      
      for (Map.Entry<String, Collection<JmsTopicNameResolveRequest>> entry : lookupKey2Requests.entrySet()) {
        String lookupKey = entry.getKey();
        ReferenceData result = referenceData.getReferenceDataOrNull(lookupKey);
        
        for (JmsTopicNameResolveRequest request : entry.getValue()) {
          String jmsTopicName = getJmsTopicName(request, result);
          returnValue.put(request, jmsTopicName);
        }
      }
    }
    
    return returnValue;
  }
  
  private String getJmsTopicName(JmsTopicNameResolveRequest request, ReferenceData result) {
    if (result == null) {
      s_logger.info("No reference data available for {}", request);
      return null;
    } else if (result.isIdentifierError()) {
      s_logger.info("Failed to retrieve reference data for {}: {}", request, result.getErrors());
      return null;
    }
    FudgeMsg resultFields = result.getFieldValues();
    
    final String prefix = "LiveData" + SEPARATOR + "Bloomberg" + SEPARATOR;
    final String suffix = request.getNormalizationRule().getJmsTopicSuffix();
    final String bbgUniqueId = request.getMarketDataUniqueId().getValue();
    final String defaultTopicName = prefix + bbgUniqueId + suffix;
    
    String bbgSecurityType = resultFields.getString(BloombergConstants.FIELD_SECURITY_TYPE);
    if (bbgSecurityType == null) {
      return defaultTopicName;      
    
    } else if (BBG_VALID_EQUITY_TYPES.contains(bbgSecurityType)) {
      
      String bbgExchange = resultFields.getString(BloombergConstants.FIELD_PRIMARY_EXCHANGE_NAME);
      String bbgTicker = resultFields.getString(BloombergConstants.FIELD_TICKER);
      
      if (bbgExchange == null || bbgTicker == null) {
        return defaultTopicName;
      }
      
      return prefix + "Equity" + SEPARATOR + bbgExchange + SEPARATOR + bbgTicker + suffix;
      
    } else if (bbgSecurityType.equals(BloombergConstants.BLOOMBERG_US_DOMESTIC_BOND_SECURITY_TYPE)) {
      
      String issuer = resultFields.getString(BloombergConstants.FIELD_ISSUER);
      String cusip = resultFields.getString(BloombergConstants.FIELD_ID_CUSIP);
      
      if (issuer == null || cusip == null) {
        return defaultTopicName;         
      }
      
      return prefix + "Bond" + SEPARATOR + issuer + SEPARATOR + cusip + suffix;
      
    } else if (bbgSecurityType.equals(BloombergConstants.BLOOMBERG_GLOBAL_BOND_SECURITY_TYPE)) {
      
      String issuer = resultFields.getString(BloombergConstants.FIELD_ISSUER);
      String isin = resultFields.getString(BloombergConstants.FIELD_ID_ISIN);
      
      if (issuer == null || isin == null) {
        return defaultTopicName;         
      }
      
      return prefix + "Bond" + SEPARATOR + issuer + SEPARATOR + isin + suffix;
      
    } else if (bbgSecurityType.equals(BloombergConstants.BLOOMBERG_EQUITY_OPTION_SECURITY_TYPE)) {
      
      String underlyingTicker = resultFields.getString(BloombergConstants.FIELD_OPT_UNDL_TICKER);
      String ticker = resultFields.getString(BloombergConstants.FIELD_TICKER);
      
      if (underlyingTicker == null || ticker == null) {
        return defaultTopicName;         
      }
      
      return prefix + "EquityOption" + SEPARATOR + underlyingTicker + SEPARATOR + ticker + suffix;
                                                
    } else {
      return defaultTopicName;
    }
  }
  
}
