/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * Parses a bundle XML file into a bundle manager.
 */
public class BundleParser {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BundleParser.class);

  /** The bundle element tag name. */
  private static final String BUNDLE_ELEMENT = "bundle";
  /** The fragment element tag name. */
  private static final String FRAGMENT_ELEMENT = "fragment";
  /** The ID attribute name. */
  private static final String ID_ATTR = "id";

  /**
   * The URI provider for fragment references.
   */
  private final UriProvider _fragmentUriProvider;
  /**
   * The base path.
   */
  private final String _basePath;
  /**
   * The bundle manager to populate.
   */
  private final BundleManager _bundleManager = new BundleManager();
  /**
   * The cache of elements.
   */
  private final Map<String, Element> _elementsByIdMap = new HashMap<String, Element>(); 

  /**
   * Creates a parser
   * 
   * @param fragmentUriProvider  the URI provider for fragments, not null
   * @param basePath  the base path, not null
   */
  public BundleParser(UriProvider fragmentUriProvider, String basePath) {
    ArgumentChecker.notNull(fragmentUriProvider, "fragmentUriProvider");
    ArgumentChecker.notNull(basePath, "basePath");
    _fragmentUriProvider = fragmentUriProvider;
    _basePath = basePath.startsWith("/") ? basePath : "/" + basePath;
  }

  //-------------------------------------------------------------------------
  /**
   * Parses the XML file, returning the bundle manager.
   * 
   * @param xmlStream the XML input stream, not null
   * @return the parsed bundle manager, not null
   */
  public BundleManager parse(InputStream xmlStream) {
    ArgumentChecker.notNull(xmlStream, "xml inputstream");
    DocumentBuilder builder = getDocumentBuilder();
    if (builder != null) {
      try {
        Document document = builder.parse(xmlStream);
        processXMLDocument(document);
      } catch (SAXException ex) {
        throw new OpenGammaRuntimeException("unable to parse xml file", ex);
      } catch (IOException ex) {
        throw new OpenGammaRuntimeException("unable to read xml file", ex);
      }
    }
    return _bundleManager;
  }

  private void processXMLDocument(Document document) {
    buildAllElements(document);
    for (Element element : _elementsByIdMap.values()) {
      addToManager(element);
    }
  }

  private void addToManager(Element element) {
    String idAttr = element.getAttribute(ID_ATTR);
    Bundle bundle = new Bundle(idAttr);
    NodeList childNodes = element.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element childElement = (Element) node;
        if (childElement.getNodeName().equals(BUNDLE_ELEMENT)) {
          processRefBundle(bundle, childElement);
        }
        if (childElement.getNodeName().equals(FRAGMENT_ELEMENT)) {
          processFragment(bundle, childElement);
        } 
      }
    }
    _bundleManager.addBundle(bundle);
  }

  private void processFragment(Bundle bundle, Element element) {
    String fragment = element.getTextContent();
    if (isValidFragment(fragment)) {
      bundle.addChildNode(createBundleFragment(fragment));
    }
  }

  private boolean isValidFragment(String fragment) {
    if (StringUtils.isNotBlank(fragment)) {
      return true;
    }
    throw new OpenGammaRuntimeException("invalid fragment value while parsing bundle xml file");
  }

  private BundleNode createBundleFragment(String fragment) {
    URI fragmentUri = getFragmentUriProvider().getUri(fragment);
    String fragmentPath = getBasePath() + fragment;
    return new Fragment(fragmentUri, fragmentPath);
  }

  private void processRefBundle(Bundle bundle, Element element) {
    String idRef = element.getAttribute("idref");
    if (isValidIdRef(idRef)) {
      Bundle refBundle = _bundleManager.getBundle(idRef);
      if (refBundle == null) {
        Element refElement = _elementsByIdMap.get(idRef);
        // this can cause infinite loop if we have circular reference
        addToManager(refElement);
        refBundle = _bundleManager.getBundle(idRef);
      }
      bundle.addChildNode(refBundle);
    }
  }

  private boolean isValidIdRef(String idRef) {
    if (StringUtils.isNotBlank(idRef) && idRefExists(idRef)) {
      return true;
    }
    throw new OpenGammaRuntimeException(" invalid idref ["  + idRef + "]");
  }

  private boolean idRefExists(String idRef) {
    return _elementsByIdMap.get(idRef) != null;
  }

  private void buildAllElements(Document document) {
    Element rootElement = document.getDocumentElement();
    if (isValidRootElement(rootElement)) {
      rootElement.normalize();
      NodeList childNodes = rootElement.getChildNodes();
      for (int i = 0; i < childNodes.getLength(); i++) {
        Node node = childNodes.item(i);
        if (node.getNodeType() == Node.ELEMENT_NODE) {
          Element element = (Element) node;
          if (isValidBundleElement(element)) {
            String idAttr = element.getAttribute(ID_ATTR);
            if (_elementsByIdMap.get(idAttr) == null) {
              _elementsByIdMap.put(idAttr, element);
            } else {
              throw new OpenGammaRuntimeException("parsing bundle XML : duplicate id attribute in " + node.getNodeName());
            }
          }
        }
      }
    }
  }

  private boolean isValidRootElement(Element rootElement) {
    if (rootElement.getNodeName().equals("uiResourceConfig")) {
      return true;
    }
    throw new OpenGammaRuntimeException("parsing bundle XML : invalid root element " + rootElement.getNodeName());
  }

  private boolean isValidBundleElement(Element element) {
    return isBundleElement(element) && hasChildren(element) && hasValidId(element);
  }

  private boolean hasValidId(Element element) {
    if (element.hasAttribute(ID_ATTR) && StringUtils.isNotBlank(element.getAttribute(ID_ATTR))) {
      return true;
    } 
    throw new OpenGammaRuntimeException("parsing bundle XML : bundle element needs id attribute");
  }

  private boolean hasChildren(Element element) {
    if (element.hasChildNodes()) {
      return true;
    } 
    throw new OpenGammaRuntimeException("parsing bundle XML : missing children elements in bundle");
  }

  private boolean isBundleElement(Element element) {
    if (element.getNodeName().equals(BUNDLE_ELEMENT)) {
      return true;
    } 
    throw new OpenGammaRuntimeException("parsing bundle XML : element not a bundle");
  }

  private DocumentBuilder getDocumentBuilder() {
    DocumentBuilder builder = null;
    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    try {
      builder = builderFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      s_logger.warn("Unable to create a DOM parser", e);
    }
    return builder;
  }
  
  private UriProvider getFragmentUriProvider() {
    return _fragmentUriProvider;
  }
  
  private String getBasePath() {
    return _basePath;
  }

}
