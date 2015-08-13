/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.sesame.trade.fpml;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.joda.beans.PropertyDefinition;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opengamma.util.ArgumentChecker;

/**
 * A single element in the tree structure of XML.
 * <p>
 * This class is a minimal, lightweight representation of an element in the XML tree.
 * The element has a name, attributes, and either content or children.
 * <p>
 * Note that this representation does not express all XML features.
 * No support is provided for processing instructions, comments, mixed content or namespaces.
 * In addition, it is not possible to determine the difference between empty content and no children.
 */
public final class XmlElement {

  /**
   * The element name.
   */
  @PropertyDefinition(validate = "notNull")
  private final String _name;
  /**
   * The attributes.
   */
  @PropertyDefinition(validate = "notNull")
  private final ImmutableMap<String, String> _attributes;
  /**
   * The element content.
   */
  @PropertyDefinition(validate = "notNull")
  private final String _content;
  /**
   * The child nodes.
   */
  @PropertyDefinition(validate = "notNull")
  private final ImmutableList<XmlElement> _children;

  //-------------------------------------------------------------------------
  /**
   * Obtains an instance with content and no attributes.
   * <p>
   * Returns an element representing XML with content, but no children.
   * 
   * @param name  the element name, not empty
   * @param content  the content, empty if the element has no content
   * @return the element
   */
  public static XmlElement ofContent(String name, String content) {
    return ofContent(name, ImmutableMap.<String, String>of(), content);
  }

  /**
   * Obtains an instance with content and attributes.
   * <p>
   * Returns an element representing XML with content and attributes but no children.
   * 
   * @param name  the element name, not empty
   * @param attributes  the attributes, empty if the element has no attributes
   * @param content  the content, empty if the element has no content
   * @return the element
   */
  public static XmlElement ofContent(String name, Map<String, String> attributes, String content) {
    return new XmlElement(name, ImmutableMap.copyOf(attributes), content, ImmutableList.<XmlElement>of());
  }

  /**
   * Obtains an instance with children and no attributes.
   * <p>
   * Returns an element representing XML with children, but no content.
   * 
   * @param name  the element name, not empty
   * @param children  the children, empty if the element has no children
   * @return the element
   */
  public static XmlElement ofChildren(String name, List<XmlElement> children) {
    return ofChildren(name, ImmutableMap.<String, String>of(), children);
  }

  /**
   * Obtains an instance with children and attributes.
   * <p>
   * Returns an element representing XML with children and attributes, but no content.
   * 
   * @param name  the element name, not empty
   * @param attributes  the attributes, empty if the element has no attributes
   * @param children  the children, empty if the element has no children
   * @return the element
   */
  public static XmlElement ofChildren(String name, Map<String, String> attributes, List<XmlElement> children) {
    return new XmlElement(name, ImmutableMap.copyOf(attributes), "", ImmutableList.copyOf(children));
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   * 
   * @param name  the element name, not empty
   * @param attributes  the attributes, empty if the element has no attributes
   * @param content  the content, empty if the element has no content
   * @param children  the children, empty if the element has no children
   */
  private XmlElement(
      String name,
      ImmutableMap<String, String> attributes,
      String content,
      ImmutableList<XmlElement> children) {

    this._name = ArgumentChecker.notEmpty(name, "name");
    this._attributes = attributes;
    this._content = ArgumentChecker.notNull(content, "content");
    this._children = children;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the element name.
   * <p>
   * This is the local name of this element.
   * 
   * @return the name
   */
  public String getName() {
    return _name;
  }

  /**
   * Gets an attribute by name.
   * <p>
   * This returns the value of the attribute with the specified local name.
   * An exception is thrown if the attribute does not exist.
   * 
   * @param attrName  the attribute name to find
   * @return the attribute
   * @throws IllegalArgumentException if the attribute name does not exist
   */
  public String getAttribute(String attrName) {
    String attrValue = _attributes.get(attrName);
    if (attrValue == null) {
      throw new IllegalArgumentException("Unknown attribute '" + attrName + "' on element '" + _name + "'");
    }
    return attrValue;
  }

  /**
   * Gets the attributes.
   * <p>
   * This returns all the attributes of this element.
   * 
   * @return the attributes
   */
  public ImmutableMap<String, String> getAttributes() {
    return _attributes;
  }

  /**
   * Checks if the element has content.
   * <p>
   * Content exists if it is non-empty.
   * 
   * @return the content
   */
  public boolean hasContent() {
    return _content.length() > 0;
  }

  /**
   * Gets the element content.
   * <p>
   * If this element has no content, the empty string is returned.
   * 
   * @return the content
   */
  public String getContent() {
    return _content;
  }

  /**
   * Gets a child element by index.
   * 
   * @param index  the index to find
   * @return the child
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  public XmlElement getChild(int index) {
    return _children.get(index);
  }

  /**
   * Gets the child elements.
   * <p>
   * This returns all the children of this element.
   * 
   * @return the children
   */
  public ImmutableList<XmlElement> getChildren() {
    return _children;
  }

  /**
   * Gets the single matching child element with the specified name, or empty if not found.
   * <p>
   * This returns the child element with the specified local name.
   * An exception is thrown if there is more than one matching child.
   * 
   * @param childName  the name to match
   * @return the child matching the name
   * @throws IllegalArgumentException if there is more than one match
   */
  public Optional<XmlElement> getChildOptional(String childName) {
    List<XmlElement> filtered = getChildren(childName);
    switch (filtered.size()) {
      case 0:
        return Optional.absent();
      case 1:
        return Optional.of(filtered.get(0));
      default:
        throw new IllegalArgumentException("Multiple elements found with name '" + childName + "' in element '" + _name + "'");
    }
  }

  /**
   * Gets the single matching child element with the specified name.
   * <p>
   * This returns the child element with the specified local name.
   * An exception is thrown if there is more than one matching child or the child does not exist.
   * 
   * @param childName  the name to match
   * @return the child matching the name
   * @throws IllegalArgumentException if there is more than one match or no matches
   */
  public XmlElement getChildSingle(String childName) {
    List<XmlElement> filtered = getChildren(childName);
    switch (filtered.size()) {
      case 0:
        throw new IllegalArgumentException("Unknown element '" + childName + "' in element '" + _name + "'");
      case 1:
        return filtered.get(0);
      default:
        throw new IllegalArgumentException("Multiple elements '" + childName + "' in element '" + _name + "'");
    }
  }

  /**
   * Gets the first matching child element with the specified name.
   * <p>
   * This returns the first child element with the specified local name.
   * An exception is thrown if the child does not exist.
   * 
   * @param childName  the name to match
   * @return the child matching the name
   * @throws IllegalArgumentException if there is no match
   */
  public XmlElement getChildFirst(String childName) {
    for (XmlElement child : _children) {
      if (child.getName().equals(childName)) {
        return child;
      }
    }
    throw new IllegalArgumentException("Unknown element '" + childName + "' in element '" + _name + "'");
  }

  /**
   * Gets the child elements matching the specified name.
   * <p>
   * This returns all the child elements with the specified local name.
   * 
   * @param childName  the name to match
   * @return the children matching the name
   */
  public ImmutableList<XmlElement> getChildren(String childName) {
    ImmutableList.Builder<XmlElement> builder = ImmutableList.builder();
    for (XmlElement child : _children) {
      if (child.getName().equals(childName)) {
        builder.add(child);
      }
    }
    return builder.build();
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if this element equals another.
   * <p>
   * This compares the entire state of the elements.
   * 
   * @param obj  the other element, null returns false
   * @return true if equal
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof XmlElement) {
      XmlElement other = (XmlElement) obj;
      return _name.equals(other._name) &&
          Objects.equals(_content, other._content) &&
          _attributes.equals(other._attributes) &&
          _children.equals(other._children);
    }
    return false;
  }

  /**
   * Returns a suitable hash code.
   * <p>
   * Note that the has code excludes the children to ensure performance is reasonable in large trees.
   * 
   * @return the hash code
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _name.hashCode();
    result = prime * result + _content.hashCode();
    result = prime * result + _attributes.hashCode();
    return result;
  }

  /**
   * Returns a string summary of the element.
   * <p>
   * The string form includes the attributes and content, but summarizes the child elements.
   * 
   * @return the string form
   */
  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(512);
    buf.append('<').append(_name);
    for (Entry<String, String> entry : _attributes.entrySet()) {
      buf.append(' ').append(entry.getKey()).append('=').append('"').append(entry.getValue()).append('"');
    }
    buf.append('>');
    if (_children.isEmpty()) {
      buf.append(_content);
    } else {
      for (XmlElement child : _children) {
        buf.append(System.lineSeparator()).append(" <").append(child.getName()).append(" ... />");
      }
      buf.append(System.lineSeparator());
    }
    buf.append("</").append(_name).append('>');
    return buf.toString();
  }

}
