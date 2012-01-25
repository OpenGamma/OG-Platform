/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.examples.portfolioloader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.util.tuple.Triple;

/**
 * Abstract class for importing data from various 3rd party file formats
 */
public abstract class PortfolioLoader {
 
  private SheetReader _sheet;         // The spreadsheet from which to import
     
  public PortfolioLoader(SheetReader sheet) {
    _sheet = sheet;
  }
  
  public abstract Triple<ManageableTrade, ManageablePosition, ManageableSecurity> loadNext();

  public Triple<Collection<ManageableTrade>, Collection<ManageablePosition>, Collection<ManageableSecurity>> loadAll() {
    Map<String, ManageableSecurity> securities = new HashMap<String, ManageableSecurity>();
    List<ManageablePosition> positions = new ArrayList<ManageablePosition>();
    List<ManageableTrade> trades = new ArrayList<ManageableTrade>();
    
    Triple<ManageableTrade, ManageablePosition, ManageableSecurity> next;
    
    while ((next = loadNext()) != null) {
      
      ManageableSecurity newSecurity = next.getThird();
      ManageablePosition newPosition = next.getSecond();
      ManageableTrade newTrade = next.getFirst();
      
      // Check if this security has already been loaded
      if (newSecurity != null) {
        ManageableSecurity origSecurity = securities.get(newSecurity.getName());
        if (origSecurity == null) {
          // No - add it
          securities.put(newSecurity.getName(), newSecurity);
        } else if (origSecurity != newSecurity) {
          // Yes - don't duplicate, but instead set trade/position links to the original security
          if (newTrade != null) {
            newTrade.setSecurityLink(ManageableSecurityLink.of(origSecurity));
          }
          if (newPosition != null) {
            newPosition.setSecurityLink(ManageableSecurityLink.of(origSecurity));
          }
        }
      }

      // add the associated position, if not already in the collection
      if (newPosition != null) {
        if (!positions.contains(newPosition)) {
          positions.add(newPosition);
        }
      }
      
      // add the associated trade
      if (newTrade != null) { // && !trades.contains(newTrade)) {
        trades.add(newTrade);
      }
    }
    
    return new Triple<Collection<ManageableTrade>, Collection<ManageablePosition>, Collection<ManageableSecurity>>(trades, positions, securities.values());
  }
  
  public SheetReader getSheet() {
    return _sheet;
  }

  public void setSheet(SheetReader sheet) {
    _sheet = sheet;
  }

}
