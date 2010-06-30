/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import java.util.HashMap;
import java.util.Map;

import javax.time.calendar.LocalDate;

import org.apache.commons.collections.BidiMap;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * 
 *
 * @author jim
 */
public interface ExchangeRepository {
  public static final IdentificationScheme ISO_MIC = new IdentificationScheme("ISO_MIC");
  public static final IdentificationScheme COPP_CLARK_NAME = new IdentificationScheme("COPP_CLARK_NAME");
  public static final IdentificationScheme COPP_CLARK_CENTER_ID = new IdentificationScheme("COPP_CLARK_CENTER_ID");
  
  public Exchange resolveExchange(LocalDate asOf, IdentifierBundle identifier);
  public Exchange resolveExchange(LocalDate asOf, Identifier identifier);
  public Exchange resolveExchange(LocalDate asOf, UniqueIdentifier identifier);
}
