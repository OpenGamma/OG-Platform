/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import static com.opengamma.financial.InMemoryRegionRepository.ISO_COUNTRY_2;
import static com.opengamma.financial.InMemoryRegionRepository.ISO_CURRENCY_3;
import static com.opengamma.financial.InMemoryRegionRepository.TYPE_COLUMN;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;

/**
 * Define parameters for a search for a region.  This will commonly just be an Indentifier, but could be a complex
 * set of field 'predicates' (key/value pairs logically ANDed).  Currently this class will not support both predicate
 * and identifier searches in the same request.
 */
public class RegionSearchRequest {
  private String _hierarchy;
  private Map<String, Object> _predicates = new HashMap<String, Object>();
  private boolean _root;
  private boolean _graphIncluded;
  private IdentifierBundle _identifiers;
    
  public RegionSearchRequest(String hierarchy) {
    _hierarchy = hierarchy;
  }
  
  public RegionSearchRequest(String hierarchy, String key, Object value) {
    _hierarchy = hierarchy;
    addPredicate(key, value);
  }
  
  public RegionSearchRequest(String hierarchy, IdentifierBundle identifiers) {
    _hierarchy = hierarchy;
    _identifiers = identifiers;
  }

  public RegionSearchRequest(String hierarchy, Identifier identifier) {
    _hierarchy = hierarchy;
    _identifiers = IdentifierBundle.of(identifier);
  }
  
  public void addPredicate(String key, Object value) {
    if (_identifiers != null) {
      throw new OpenGammaRuntimeException("Not Implmented: Currently you can only search by identifier bundle OR predicates on fields, not both");
    }
    _predicates.put(key, value);
  }
  
  public void setType(RegionType type) {
    _predicates.put(TYPE_COLUMN, type);
  }
  
  public void setCountryISO2(String countryISO2) {
    _predicates.put(ISO_COUNTRY_2, countryISO2);
  }
  
  public void setCurrencyISO3(String currencyISO3) {
    _predicates.put(ISO_CURRENCY_3, currencyISO3);
  }
  
  public void setIdentifiers(IdentifierBundle identifiers) {
    if (_predicates.size() > 0) {
      throw new OpenGammaRuntimeException("Not Implmented: Currently you can only search by identifier bundle OR predicates on fields, not both");
    }
    _identifiers = identifiers;
  }
  
  public void setRootRequest(boolean getRootNode) {
    _root = true;
  }
  
  public void setGraphIncluded(boolean graphIncluded) {
    _graphIncluded = graphIncluded;
  }
  
  public Map<String, Object> getPredicates() {
    return _predicates;
  }
  
  public IdentifierBundle getIdentifierBundle() {
    return _identifiers;
  }
  
  public String getHierarchy() {
    return _hierarchy;
  }
  
  public boolean isRootRequest() {
    return _root;
  }
  
  public boolean isGraphIncluded() {
    return _graphIncluded;
  }
}
