<#escape x as x?html>
<@page title="Conventions" jquery=true aceXmlEditor=true>

<#-- SECTION Convention search -->
<@section title="Convention search" if=searchRequest??>
  <@form method="GET" action="${uris.conventions()}">
  <p>
  	<@rowin label="Type">
      <select name="type">
      	<option value="" <#if type = ''>selected</#if>></option>
      	<#list conventionDescriptionMap?keys as key><option value="${key}"<#if type = '${key}'> selected</#if>>${conventionDescriptionMap[key]}</option></#list>
      </select>
    </@rowin>
    <@rowin label="Name"><input type="text" size="30" name="name" value="${searchRequest.name}" /></@rowin>
    <@rowin label="Identifier"><input type="text" size="50" name="identifier" value="${searchRequest.externalIdValue}" /></@rowin>
    <@rowin><input type="submit" value="Search" /></@rowin>
  </p>
  </@form>

<#-- SUBSECTION Convention results -->
<#if searchResult??>
<@subsection title="Results">
  <@table items=searchResult.documents paging=paging empty="No convention found" headers=["Name","Reference","Version valid from","Actions"]; item>
      <td><a href="${uris.convention(item.uniqueId)}">${item.name}</a></td>
      <td>${item.uniqueId.value}</td>
      <td>${item.versionFromInstant}</td>
      <td><a href="${uris.convention(item.uniqueId)}">View</a></td>
  </@table>
</@subsection>
</#if>
</@section>

<#-- SECTION Add convention -->
<@section title="Add convention">
  <@form method="POST" action="${uris.conventions()}" id="addForm">
  <p>
    <@rowin label="Name"><input type="text" size="30" name="name" value="" /></@rowin>
    <@rowin label="Type">
      <select name="type">
        <option value=""></option>
        <#list conventionDescriptionMap?keys as key><option value="${key}">${conventionDescriptionMap[key]}</option></#list>
      </select>
    </@rowin>
    <@rowin>
      <div id="ace-xml-editor"></div>
    </@rowin>
    <input type="hidden" name="conventionxml" id="convention-xml"/>
    <@rowin><input type="submit" value="Add" /></@rowin>
    
    <#noescape><@xmlEditorScript formId="addForm" inputId="convention-xml"></@xmlEditorScript></#noescape>  
  </p>
  </@form>
</@section>

<#-- SECTION Links -->
<@section title="Links">
  <p>
  	<a href="${uris.conventions()}">Convention home</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>