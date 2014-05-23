<#escape x as x?html>
<@page title="Position - ${position.name}" jquery=true aceXmlEditor=true>

<@section css="info" if=deleted>
  <p>This position has been deleted</p>
</@section>


<#-- SECTION Position output -->
<@section title="Position">
  <p>
    <@rowout label="Name">${position.name}</@rowout>
    <@rowout label="Reference">${position.uniqueId.value}, version ${position.uniqueId.version}</@rowout>
  </p>

<#-- SUBSECTION Main data -->
<@subsection title="Amount">
    <@rowout label="Quantity">${position.quantity}</@rowout>
<#list position.securityLink.externalId.externalIds as item>
    <@rowout label="Security">${item.scheme.name} - ${item.value} <a href="${securityUris.securities(item)}">view</a></@rowout>
</#list>
<#if position.securityLink.externalId.externalIds?size != 1>
    <@rowout label="Best match"><a href="${securityUris.securities(position.securityLink)}">best matching securities</a></@rowout>
</#if>
</@subsection>

<#-- SUBSECTION Trades -->
<@subsection title="Trades">
  <@table items=position.trades empty="No trades" headers=["Reference","Quantity","Date","Security","Counterparty"]; item>
      <td>${item.uniqueId.value}</td>
      <td>${item.quantity}</td>
      <td>${item.tradeDate}</td>
      <td>
<#list item.securityLink.externalId.externalIds as id>
${id.scheme.name} - ${id.value},
</#list>
<a href="${securityUris.securities(item.securityLink)}">view</a>
      </td>
      <td>${item.counterpartyExternalId}</td>
  </@table>
</@subsection>

<#-- SUBSECTION Xml -->
<@subsection title="Xml">
  <div id="ace-xml-editor"></div>
<#noescape><@xmlEditorScript  xmlValue="${positionXml!''}" readOnly=true></@xmlEditorScript></#noescape>
</@subsection>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.positionVersions()}">All versions</a><br />
    <a href="${uris.position()}">Latest version - ${latestPosition.uniqueId.version}</a><br />
    <a href="${uris.positions()}">Position search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
