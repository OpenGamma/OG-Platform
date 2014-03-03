<#escape x as x?html>
<@page title="LegalEntities" jquery=true aceXmlEditor=true>

<#-- SECTION LegalEntity search -->
<@section title="LegalEntity search" if=searchRequest??>
  <@form method="GET" action="${uris.legalEntities()}">
  <p>
    <@rowin label="Name"><input type="text" size="30" name="name" value="${searchRequest.name}" /></@rowin>
    <@rowin><input type="submit" value="Search" /></@rowin>
  </p>
  </@form>

<#-- SUBSECTION LegalEntity results -->
<#if searchResult??>
<@subsection title="Results">
  <@table items=searchResult.documents paging=paging empty="No legal entity found" headers=["Name","Reference","Version valid from","Actions"]; item>
      <td><a href="${uris.legalEntity(item.uniqueId)}">${item.name}</a></td>
      <td>${item.uniqueId.value}</td>
      <td>${item.versionFromInstant}</td>
      <td><a href="${uris.legalEntity(item.uniqueId)}">View</a></td>
  </@table>
</@subsection>
</#if>
</@section>

<#-- SECTION Add legal entity -->
<@section title="Add legal entity">
  <@form method="POST" action="${uris.legalEntities()}" id="addForm">
  <p>
    <@rowin label="Name"><input type="text" size="30" name="name" value="" /></@rowin>
    <@rowin>
      <div id="ace-xml-editor"></div>
    </@rowin>
    <input type="hidden" name="legalEntityXML" id="legalEntity-xml"/>
    <@rowin><input type="submit" value="Add" /></@rowin>
    
    <#noescape><@xmlEditorScript formId="addForm" inputId="legalEntity-xml"></@xmlEditorScript></#noescape>  
  </p>
  </@form>
</@section>

<#-- SECTION Links -->
<@section title="Links">
  <p>
  	<a href="${uris.legalEntities()}">LegalEntity home</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>