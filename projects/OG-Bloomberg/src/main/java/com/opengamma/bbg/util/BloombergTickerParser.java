/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.bbg.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;

/**
 * <p>
 * Base class for Bloomberg ticker parsing.  The rationale for having this parsing is to be able to extract instrument
 * indicatives from a Bloomberg ticker string or {@link com.opengamma.id.ExternalId}, without looking up the instrument via
 * Bloomberg security field loading.  This allows the caller to avoid expending Bloomberg security field lookup quota.
 * </p>
 * <p>
 * This class is abstract, with subclasses representing different instrument types.  The idiom for using the classes is to
 * create a parser instance around the Bloomberg ticker, and then call various getters on the instance to read the indicatives.
 * </p>
 * @author noah@opengamma
 */
public abstract class BloombergTickerParser { 
  //------------ FIELDS ------------
  private ExternalId _identifier;
  
  
  
  // ------------ METHODS ------------
  // -------- CONSTRUCTORS --------
  /**
   * Create a parser
   * @param value a legal Bloomberg ticker, as string
   */
  public BloombergTickerParser(String value) {
    init(value);
    _identifier = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, value);
  }
  
  /**
   * Create a parser
   * @param identifier a legal Bloomberg ticker, with {@link com.opengamma.id.ExternalScheme} 
   * of {@link com.opengamma.core.id.ExternalSchemes#BLOOMBERG_TICKER}
   */
  public BloombergTickerParser(ExternalId identifier) {
    if (identifier.isNotScheme(ExternalSchemes.BLOOMBERG_TICKER)) {
      throw new OpenGammaRuntimeException("Must be BLOOMBERG_TICKER identifier scheme: " + identifier);
    }
    init(identifier.getValue());
    _identifier = identifier;
  }
   
  private void init(String value) {
    Matcher matcher = Pattern.compile(getPatternString()).matcher(value);
    if (!matcher.matches()) {
      throw new OpenGammaRuntimeException("Not a legal Bloomberg ticker for instrument type: " + value);
    }
    parse(matcher);
  }
  
  
  //-------- PROPERTIES --------
  /**
   * Returns the wrapped {@link com.opengamma.id.ExternalId} being parsed
   * @return the wrapped {@link com.opengamma.id.ExternalId} being parsed
   */
  public ExternalId getIdentifier() {
    return _identifier;
  }
  
  
  // -------- ABSTRACT METHODS --------
  /**
   * Abstract subclass method that should provide a regex for parsing the Bloomberg ticker for the given instrument type.
   * This regex should use groups, so that the created {@link java.util.regex.Matcher} can access indicatives easily.
   * @return a regex for parsing the Bloomberg ticker for the given instrument type
   */
  protected abstract String getPatternString();
  
  /**
   * Abstract subclass method for performing actual parsing.  The matcher will be setup with the regex from {@link #getPatternString}. 
   * @param matcher the matcher used for parsing, setup with the regex from {@link #getPatternString}
   */
  protected abstract void parse(Matcher matcher);
}
