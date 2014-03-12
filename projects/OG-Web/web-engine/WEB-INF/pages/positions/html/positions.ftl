<#escape x as x?html>
<@page title="Positions" jquery=true aceXmlEditor=true>

<#-- SECTION Position search -->
<@section title="Position search" if=searchRequest??>
  <@form method="GET" action="${uris.positions()}">
  <p>
    <#if uniqueIdSchemes?exists>
      <@rowin label="UniqueId Scheme">
        <select name="uniqueIdScheme">
          <option value="" <#if searchRequest.uniqueIdScheme = ''>selected</#if>></option>
          <#list uniqueIdSchemes as uniqueIdScheme>
            <option value="${uniqueIdScheme}" <#if searchRequest.uniqueIdScheme = '${uniqueIdScheme}'>selected</#if>>${uniqueIdScheme}</option>
          </#list>
        </select>
      </@rowin>
    </#if>  
    <@rowin label="Min quantity"><input type="text" size="10" maxlength="12" name="minquantity" value="${searchRequest.minQuantity}" /></@rowin>
    <@rowin label="Max quantity"><input type="text" size="10" maxlength="12" name="maxquantity" value="${searchRequest.maxQuantity}" /></@rowin>
    <@rowin label="Identifier"><input type="text" size="30" maxlength="80" name="identifier" value="${searchRequest.securityIdValue}" /></@rowin>
    <@rowin><input type="submit" value="Search" /></@rowin>
  </p>
  </@form>

<#-- SUBSECTION Position results -->
<#if searchResult??>
<@subsection title="Results">
  <@table items=searchResult.documents paging=paging empty="No positions" headers=["Name","Reference","Version valid from","Quantity","Trades","Actions"]; item>
      <td><a href="${uris.position(item.position)}">${item.position.name}</a></td>
      <td>${item.position.uniqueId.value}</td>
      <td>${item.versionFromInstant}</td>
      <td>${item.position.quantity}</td>
      <td>${item.position.trades?size}</td>
      <td><a href="${uris.position(item.position)}">View</a></td>
  </@table>
</@subsection>
</#if>
</@section>


<#-- SECTION Add position -->
<@section title="Add position">
  <@form method="POST" action="${uris.positions()}">
  <p>
    <#if uniqueIdSchemes?exists>
    <@rowin label="UniqueId Scheme">
      <select name="uniqueIdScheme">
        <option value="" <#if searchRequest.uniqueIdScheme = ''>selected</#if>></option>
        <#list uniqueIdSchemes as uniqueIdScheme>
        <option value="${uniqueIdScheme}" <#if searchRequest.uniqueIdScheme = '${uniqueIdScheme}'>selected</#if>>${uniqueIdScheme}</option>
        </#list>
      </select>
    </@rowin>
    </#if>  
    <@rowin label="Quantity"><input type="text" size="10" maxlength="12" name="quantity" value="" /></@rowin>
    <@rowin label="Scheme type">
      <select name="idscheme">
       <#list externalSchemes?keys as key> 
          <option value="${key}">${externalSchemes[key]}</option>
       </#list>
      </select>
    </@rowin>
    <@rowin label="Identifier"><input type="text" size="30" maxlength="80" name="idvalue" value="" /></@rowin>
    <@rowin><input type="submit" value="Add" /></@rowin>
  </p>
  </@form>
</@section>

<#-- SECTION Add position by XML-->
<@section title="Add position by XML">
  <@form method="POST" action="${uris.positions()}" id="addPositionForm">
  <p>
    <#if uniqueIdSchemes?exists>
    <@rowin label="UniqueId Scheme">
      <select name="uniqueIdScheme">
        <option value="" <#if searchRequest.uniqueIdScheme = ''>selected</#if>></option>
        <#list uniqueIdSchemes as uniqueIdScheme>
        <option value="${uniqueIdScheme}" <#if searchRequest.uniqueIdScheme = '${uniqueIdScheme}'>selected</#if>>${uniqueIdScheme}</option>
        </#list>
      </select>
    </@rowin>
    </#if> 
    <@rowin label="Value">
      <div id="ace-xml-editor"></div>
    </@rowin>
    <input type="hidden" name="positionXml" id="position-xml"/>
    <input type="hidden" name="type" value="xml"/>
    <@rowin><input type="submit" value="Add" /></@rowin>
  </p>
  <#noescape><@xmlEditorScript formId="addPositionForm" inputId="position-xml" xmlValue="${positionXml!''}"></@xmlEditorScript></#noescape> 
  </@form>
</@section>

<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
