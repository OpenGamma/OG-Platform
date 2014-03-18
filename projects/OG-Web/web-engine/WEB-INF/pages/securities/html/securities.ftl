<#escape x as x?html>
<@page title="Securities" jquery=true aceXmlEditor=true>


<#-- SECTION Security search -->
<@section title="Security search" if=searchRequest??>
  <@form method="GET" action="${uris.securities()}">
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
    <@rowin label="Type">
      <select name="type">
        <option value="" <#if searchRequest.securityType = ''>selected</#if>></option>
        <#list description2type?keys as key>
        <option value="${description2type[key]}" <#if searchRequest.securityType = '${description2type[key]}'>selected</#if>>${key}</option>
        </#list>
      </select>
    </@rowin>
    <@rowin label="Identifier"><input type="text" size="30" maxlength="80" name="identifier" value="${searchRequest.externalIdValue}" /></@rowin>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="${searchRequest.name}" /></@rowin>
    <@rowin><input type="submit" value="Search" /></@rowin>
  </p>
  </@form>

<#-- SUBSECTION Security results -->
<#if searchResult??>
<@subsection title="Results">
  <@table items=searchResult.documents paging=paging empty="No securities" headers=["Name","Reference","Version valid from","Actions"]; item>
      <td><a href="${uris.security(item.security)}">${item.security.name}</a></td>
      <td>${item.uniqueId.value}</td>
      <td>${item.versionFromInstant}</td>
      <td><a href="${uris.security(item.security)}">View</a></td>
  </@table>
</@subsection>
</#if>
</@section>


<#-- SECTION Add security -->
<@section title="Add security by XML">
  <@form method="POST" action="${uris.securities()}" id="addSecurityForm">
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
    <@rowin>
      <div id="ace-xml-editor"></div>
    </@rowin>
    <@rowin><input type="hidden" name="securityXml" id="security-xml"/></@rowin>
    <input type="hidden" name="type" value="xml"/>
    <@rowin><input type="submit" value="Add" /></@rowin>
  </p>
  
  <#noescape><@xmlEditorScript formId="addSecurityForm" inputId="security-xml"></@xmlEditorScript></#noescape>  
  </@form>
</@section>

<#-- SECTION Load and Add security -->
<@section title="Load securities by ID">
  <@form method="POST" action="${uris.securities()}">
  <p> 
    <@rowin label="Scheme type">
      <select name="idscheme">
        <option value="BLOOMBERG_TICKER">Bloomberg Ticker</option>
        <option value="BLOOMBERG_TCM">Bloomberg Ticker/Coupon/Maturity</option>
        <option value="BLOOMBERG_BUID">Bloomberg BUID</option>
        <option value="CUSIP">CUSIP</option>
        <option value="ISIN">ISIN</option>
        <option value="RIC">RIC</option>
        <option value="SEDOL1">SEDOL</option>
      </select>
    </@rowin>
    <@rowin label="Identifiers">
      <textarea name="idvalue" cols="35" rows="10"></textarea>
    </@rowin>
    <input type="hidden" name="type" value="id"/>
    <@rowin><input type="submit" value="Add" /></@rowin>
  </p>
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
