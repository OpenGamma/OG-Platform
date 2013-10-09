/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for working with identifiers.
 * <p>
 * This class is a thread-safe static utility class.
 */
public final class IdUtils {

  /**
   * Restricted constructor.
   */
  private IdUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Sets the unique identifier of an object if it implements {@code MutableUniqueIdentifiable}.
   * <p>
   * This provides uniform access to objects that support having their unique identifier
   * updated after construction.
   * <p>
   * For example, code in the database layer will need to update the unique identifier
   * when the object is stored.
   *
   * @param object  the object to set into
   * @param uniqueId  the unique identifier to set, may be null
   */
  public static void setInto(Object object, UniqueId uniqueId) {
    if (object instanceof MutableUniqueIdentifiable) {
      ((MutableUniqueIdentifiable) object).setUniqueId(uniqueId);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Converts a list of {@code UniqueId} or {@code ObjectId} to a list of strings.
   *
   * @param ids  the ids to convert, null returns empty list
   * @return the string list, not null
   */
  public static List<String> toStringList(Iterable<? extends ObjectIdentifiable> ids) {
    List<String> strs = new ArrayList<String>();
    if (ids != null) {
      for (ObjectIdentifiable obj : ids) {
        if (obj instanceof UniqueId) {
          strs.add(obj.toString());
        } else {
          strs.add(obj.getObjectId().toString());
        }
      }
    }
    return strs;
  }

  /**
   * Converts a list of strings to a list of {@code UniqueId}.
   *
   * @param uniqueIdStrs  the identifiers to convert, null returns empty list
   * @return the list of unique identifiers, not null
   */
  public static List<UniqueId> parseUniqueIds(Iterable<String> uniqueIdStrs) {
    List<UniqueId> uniqueIds = new ArrayList<UniqueId>();
    if (uniqueIdStrs != null) {
      for (String uniqueIdStr : uniqueIdStrs) {
        uniqueIds.add(UniqueId.parse(uniqueIdStr));
      }
    }
    return uniqueIds;
  }

  /**
   * Converts a list of strings to a list of {@code ObjectId}.
   *
   * @param objectIdStrs  the identifiers to convert, null returns empty list
   * @return the list of unique identifiers, not null
   */
  public static List<ObjectId> parseObjectIds(Iterable<String> objectIdStrs) {
    List<ObjectId> objectIds = new ArrayList<ObjectId>();
    if (objectIdStrs != null) {
      for (String objectIdStr : objectIdStrs) {
        objectIds.add(ObjectId.parse(objectIdStr));
      }
    }
    return objectIds;
  }

}
