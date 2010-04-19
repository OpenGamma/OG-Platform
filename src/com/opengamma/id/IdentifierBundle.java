/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.fudgemsg.FudgeMessageFactory;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;

import com.opengamma.util.ArgumentChecker;

/**
 * A bundle of identifiers.
 *
 * @author kirk
 */
public class IdentifierBundle implements Serializable {

  /**
   * Fudge message key for the identifier set.
   */
  public static final String ID_FUDGE_FIELD_NAME = "ID";

  /**
   * The set of identifiers.
   */
  private final Set<Identifier> _identifiers;
  /**
   * The cached hash code.
   */
  private final int _hashCode;

  /**
   * Creates an empty bundle.
   */
  public IdentifierBundle() {
    _identifiers = Collections.emptySet();
    _hashCode = calcHashCode();
  }

  /**
   * Creates a bundle from a single identifier.
   * @param identifier  the identifier, null returns an empty bundle
   */
  public IdentifierBundle(Identifier identifier) {
    if (identifier == null) {
      _identifiers = Collections.emptySet();
    } else {
      _identifiers = Collections.singleton(identifier);
    }
    _hashCode = calcHashCode();
  }

  /**
   * Creates a bundle from an array of identifiers.
   * @param identifiers  the array of identifiers, null returns an empty bundle
   */
  public IdentifierBundle(Identifier... identifiers) {
    if ((identifiers == null) || (identifiers.length == 0)) {
      _identifiers = Collections.emptySet();
    } else {
      _identifiers = new HashSet<Identifier>(Arrays.asList(identifiers));
    }
    _hashCode = calcHashCode();
  }

  /**
   * Creates a bundle from a collection of identifiers.
   * @param identifiers  the collection of identifiers, null returns an empty bundle
   */
  public IdentifierBundle(Collection<? extends Identifier> identifiers) {
    if (identifiers == null) {
      _identifiers = Collections.emptySet();
    } else {
      _identifiers = new HashSet<Identifier>(identifiers);
    }
    _hashCode = calcHashCode();
  }

  /**
   * Constructs an identifier from a Fudge message.
   * @param fudgeMsg  the fudge message, not null
   */
  public IdentifierBundle(FudgeFieldContainer fudgeMsg) {
    Set<Identifier> identifiers = new HashSet<Identifier>();
    for (FudgeField field : fudgeMsg.getAllByName(ID_FUDGE_FIELD_NAME)) {
      if (!(field.getValue() instanceof FudgeFieldContainer)) {
        throw new IllegalArgumentException("Message provider has field named " + ID_FUDGE_FIELD_NAME + " which doesn't contain a sub-Message");
      }
      Identifier identifier = new Identifier((FudgeFieldContainer)field.getValue());
      identifiers.add(identifier);
    }
    _identifiers = identifiers;
    _hashCode = calcHashCode();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the collection of identifiers in the bundle.
   * @return the identifier collection, never null
   */
  public Collection<Identifier> getIdentifiers() {
    return Collections.unmodifiableSet(_identifiers);
  }

  /**
   * Gets the standalone identifier for the specified scheme.
   * @param scheme  the scheme to query, null returns null
   * @return the standalone identifier, null if not found
   */
  public String getIdentifier(IdentificationScheme scheme) {
    for (Identifier identifier : _identifiers) {
      if (ObjectUtils.equals(scheme, identifier.getDomain())) {
        return identifier.getValue();
      }
    }
    return null;
  }

  /**
   * Gets the number of identifiers in the bundle.
   * @return the bundle size, zero or greater
   */
  public int size() {
    return _identifiers.size();
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    IdentifierBundle other = (IdentifierBundle) obj;
    if (!ObjectUtils.equals(_identifiers, other._identifiers)) {
      return false;
    }
    return true;
  }

  protected int calcHashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((_identifiers == null) ? 0 : _identifiers.hashCode());
    return result;
  }

  @Override
  public int hashCode() {
    return _hashCode;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getClass().getSimpleName()).append("[");
    List<String> idsAsText = new ArrayList<String>();
    for (Identifier identifier : _identifiers) {
      idsAsText.add(identifier.getDomain().getDomainName() + ":" + identifier.getValue());
    }
    sb.append(StringUtils.join(idsAsText, ", "));
    sb.append("]");
    return sb.toString();
  }

  //-------------------------------------------------------------------------
  public FudgeFieldContainer toFudgeMsg(FudgeMessageFactory fudgeMessageFactory) {
    ArgumentChecker.checkNotNull(fudgeMessageFactory, "Fudge Context");
    MutableFudgeFieldContainer msg = fudgeMessageFactory.newMessage();
    for (Identifier identifier: getIdentifiers()) {
      msg.add(ID_FUDGE_FIELD_NAME, identifier.toFudgeMsg(fudgeMessageFactory));
    }
    return msg;
  }

}
