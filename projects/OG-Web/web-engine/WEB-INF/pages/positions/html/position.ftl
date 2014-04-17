<#escape x as x?html>
<@page title="Position - ${position.name}" jquery=true aceXmlEditor=true>

<@section css="info" if=deleted>
  <p>This position has been deleted</p>
</@section>


<#-- SECTION Position output -->
<@section title="Position">
  <p>
    <@rowout label="Name">${position.name}</@rowout>
    <@rowout label="Reference">${position.uniqueId.value}, version ${position.uniqueId.version}, <a href="${uris.positionVersions()}">view history</a></@rowout>
    <#if position.providerId?has_content>
        <@rowout label="Provider ID">${position.providerId}</@rowout>
    </#if>
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

<#-- SUBSECTION Attributes -->
<@subsection title="Attributes">
  <@table items=attributes?keys empty="No attributes" headers=["Attribute Name","Value"]; item>
      <td>${item}</td>
      <td>${attributes[item]}</td>
  </@table>
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
</@section>


<#-- SECTION Update position -->
<@section title="Update position" if=!deleted>
  <@form method="PUT" action="${uris.position()}" id="updatePositionForm">
  <p>
    <@rowin>
      <div id="ace-xml-editor"></div>
    </@rowin>
    <input type="hidden" name="positionXml" id="position-xml"/>
    <@rowin><input type="submit" value="Update" /></@rowin>
<#noescape><@xmlEditorScript formId="updatePositionForm" inputId="position-xml" xmlValue="${positionXml!''}"></@xmlEditorScript></#noescape>
  </p>
  </@form>
</@section>


<#-- SECTION Delete position -->
<@section title="Delete position" if=!deleted>
  <@form method="DELETE" action="${uris.position()}">
  <p>
    <@rowin><input type="submit" value="Delete" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.positionVersions()}">History of this position</a><br />
    <a href="${uris.positions()}">Position search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
