/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import com.google.common.base.Supplier;
import com.opengamma.util.ArgumentChecker;

/**
 * Represents a set of rules for the format of a {@link UniqueId} from which new identifiers can be generated,
 * or existing ones checked. This is mainly for user-generated objects where information needs to be encoded in the
 * value of the identifiers to distinguish between difference sources of user objects (i.e. where the scheme along is
 * not enough). This makes them much like the URI used to access the resource in a RESTful manner; we should look at
 * merging the two concepts.
 * <p>
 * The rules consist of:
 * <ul>
 * <li>A scheme for the identifiers
 * <li>An optional prefix for the value of the identifiers
 * </ul>
 * No assumptions are made about the format of the scheme or value prefix themselves.
 * <p>
 * This class is immutable and thread-safe.
 */
public class UniqueIdTemplate {

  /**
   * The scheme.
   */
  private final String _scheme;
  /**
   * The value prefix.
   */
  private final String _valuePrefix;

  /**
   * Creates an instance using a scheme.
   * 
   * @param scheme  the scheme to use, not null
   */
  public UniqueIdTemplate(String scheme) {
    this(scheme, null);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the scheme.
   * 
   * @return the scheme
   */
  public String getScheme() {
    return _scheme;
  }

  /**
   * Get the value prefix.
   * 
   * @return  the value prefix
   */
  public String getValuePrefix() {
    return _valuePrefix;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a supplier that acts as a factory for unique identifiers.
   * 
   * @return the supplier, not null
   */
  public Supplier<UniqueId> createSupplier() {
    return new Supplier<UniqueId>() {
      private int _count;

      @Override
      public UniqueId get() {
        return uniqueId(Integer.toString(_count++));
      }
    };
  }

  /**
   * Constructs a new instance.
   * 
   * @param scheme  the scheme of the unique identifier, not null
   * @param valuePrefix  the prefix of the unique identifier value, possibly null to indicate no prefix
   */
  public UniqueIdTemplate(String scheme, String valuePrefix) {
    ArgumentChecker.notNull(scheme, "scheme");
    _scheme = scheme;
    _valuePrefix = valuePrefix;
  }

  /**
   * Creates a new {@link UniqueId} using the rules of this template.
   * 
   * @param valueContent  the content for the {@link UniqueId}'s value (i.e. excluding any prefix), not null
   * @return  a {@link UniqueId} which incorporates <code>valueContent</code> into the rules of this template 
   */
  public UniqueId uniqueId(String valueContent) {
    ArgumentChecker.notNull(valueContent, "valueContent");
    String value = valueContent;
    if (getValuePrefix() != null) {
      value = getValuePrefix() + value;
    }
    return UniqueId.of(getScheme(), value);
  }

  /**
   * Checks whether a given {@link UniqueId} conforms to this template.
   * 
   * @param uniqueId  the unique identifier, not null
   * @return true if the identifier could have been generated from this template
   */
  public boolean conforms(UniqueId uniqueId) {
    if (!getScheme().equals(uniqueId.getScheme())) {
      return false;
    }
    if (getValuePrefix() == null) {
      return true;
    }
    return uniqueId.getValue().startsWith(getValuePrefix());
  }

  /**
   * Extracts the content out of the value of a {@link UniqueId} (i.e. the value minus any prefix). The
   * identifier must conform to this template.
   * 
   * @param uniqueId  the unique identifier, not null
   * @return the value content
   */
  public String extractValueContent(UniqueId uniqueId) {
    if (!conforms(uniqueId)) {
      throw new IllegalArgumentException("The specified UniqueId does not conform to this template");
    }
    int contentPos = getValuePrefix() == null ? 0 : getValuePrefix().length();
    return uniqueId.getValue().substring(contentPos);
  }

}
