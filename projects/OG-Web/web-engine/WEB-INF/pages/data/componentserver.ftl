<#-- This file exists in each web area (engine/sharedmasters/marketdata) -->
<#-- Please ensure changes are propagated to all copies -->
<#escape x as x?html>
<@page title="Available RESTful components">

<#-- SECTION Exchange output -->
<@section title="Component server">
  <p>
    <@rowout label="Base URI">${componentServer.uri}</@rowout>
  </p>

<#-- SUBSECTION Main data -->
<#list infosByType.keySet() as key>
<@subsection title="${key.simpleName}">
    <@rowout label="Type">${key.name}</@rowout>
<#assign temp = infosByType.get(key) />
  <@table items=temp empty="No components" headers=["Classifier","Uri","Attributes"]; item>
      <td>${item.classifier}</td>
      <td><a href="${item.uri}">${item.uri}</a></td>
      <td>
<#list item.attributes?keys as attrKey>
      ${attrKey} = ${item.attributes[attrKey]}<#if key_has_next><br /></#if>
</#list>
      </td>
  </@table>
</@subsection>
</#list>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="components.xml">View as XML</a><br />
    <a href="components.fudge">View as Fudge</a><br />
    <a href="${uris.home()}">Return home</a><br />
<#if security.isPermitted('WebAbout:view')>
    <a href="${uris.about()}">View information about the system</a><br />
</#if>
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
