/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.bean;

import java.util.Map;

import org.joda.beans.Bean;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.AbstractDocument;
import com.opengamma.util.JodaBeanSerialization;
import com.opengamma.util.ZipUtils;

/**
 * Provides access to the fields necessary to insert into {@code DbBeanMaster}.
 * <p>
 * Actual masters will normally create an instance as an anonymous inner class.
 * 
 * @param <D>  the type of the document
 * @param <V>  the type of the value
 */
public abstract class BeanMasterCallback<D extends AbstractDocument, V extends Bean> {

  /**
   * Gets the SQL table prefix.
   * @return the SQL table prefix, not null
   */
  protected abstract String getSqlTablePrefix();

  /**
   * Gets the name of this master.
   * @return the master name, not null
   */
  protected abstract String getMasterName();

  /**
   * Gets the root type.
   * @return the root class type, not null
   */
  protected abstract Class<V> getRootType();

  //-------------------------------------------------------------------------
  /**
   * Gets the name of this master.
   * @param value  the value to create with, not null
   * @return the created document, not null
   */
  protected abstract D createDocument(V value);

  /**
   * Gets the name.
   * @param value  the bean to extract from, not null
   * @return the name, not null
   */
  protected abstract String getName(V value);

  /**
   * Gets the bundle.
   * @param value  the bean to extract from, not null
   * @return the bundle, not null
   */
  protected abstract ExternalIdBundle getExternalIdBundle(V value);

  /**
   * Gets the attributes.
   * @param value  the bean to extract from, not null
   * @return the attributes, not null
   */
  protected abstract Map<String, String> getAttributes(V value);

  /**
   * Gets the indexed properties.
   * @param value  the bean to extract from, not null
   * @return the search properties, not null
   */
  protected abstract Map<String, String> getIndexedProperties(V value);

  /**
   * Gets the type character used to identify the main type.
   * @param value  the bean to extract from, not null
   * @return the main type, not null
   */
  protected abstract char getMainType(V value);

  /**
   * Gets the sub type.
   * @param value  the bean to extract from, not null
   * @return the sub type, not null
   */
  protected abstract String getSubType(V value);

  /**
   * Gets the actual Java type, typically the short class name.
   * @param value  the bean to extract from, not null
   * @return the document, not null
   */
  protected String getActualType(V value) {
    return value.getClass().getSimpleName();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the packed data.
   * @param value  the bean to extract from, not null
   * @return the document, not null
   */
  protected byte[] getPackedData(V value) {
    String xml = JodaBeanSerialization.serializer(false).xmlWriter().write(value);
    return ZipUtils.deflateString(xml);
  }

  /**
   * Parses the packed data.
   * @param data  the packed data, not null
   * @return the value, not null
   */
  protected V parsePackedData(final byte[] data) {
    String xml = ZipUtils.inflateString(data);
    return JodaBeanSerialization.deserializer().xmlReader().read(xml, getRootType());
  }

}
