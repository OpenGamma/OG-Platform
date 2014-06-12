<#escape x as x?html>
<@page title="Configurations" jquery=true aceXmlEditor=true>


<#-- SECTION Config search -->
<@section title="Configuration search" if=searchRequest??>
  <@form method="GET" action="${uris.configs()}">
  <p>
  	<@rowin label="Type">
      <select name="type">
      	<option value="" <#if type = ''>selected</#if>></option>
      	<#list configDescriptionMap?keys as key><option value="${key}"<#if type = '${key}'> selected</#if>>${configDescriptionMap[key]}</option></#list>
      </select>
    </@rowin>
    <@rowin label="Name"><input type="text" size="30" name="name" value="${searchRequest.name}" /></@rowin>
    <@rowin><input type="submit" value="Search" /></@rowin>
  </p>
  </@form>

<#-- SUBSECTION Config results -->
<#if searchResult??>
<@subsection title="Results">
  <@table items=searchResult.documents paging=paging empty="No configuration found" headers=["Name","Reference","Version valid from","Actions"]; item>
      <td><a href="${uris.config(item.uniqueId)}">${item.name}</a></td>
      <td>${item.uniqueId.value}</td>
      <td>${item.versionFromInstant}</td>
      <td><a href="${uris.config(item.uniqueId)}">View</a></td>
  </@table>
</@subsection>
</#if>
</@section>

<#-- SECTION Add config -->
<@section title="Add config">
  <@form method="POST" action="${uris.configs()}" id="addConfigForm">
  <p>
    <@rowin label="Name"><input type="text" size="30" name="name" value="" /></@rowin>
    <@rowin label="Type">
      <select name="type">
        <option value=""></option>
        <#list configDescriptionMap?keys as key><option value="${key}">${configDescriptionMap[key]}</option></#list>
      </select>
    </@rowin>
    
    <@rowin label="Configuration (XML)">
      <div id="ace-xml-editor"></div>
    </@rowin>
    <@rowin><input type="hidden" name="configXML" id="config-xml"/></@rowin>
    <@rowin><input type="submit" value="Add" /></@rowin>
    
    <#noescape><@xmlEditorScript formId="addConfigForm" inputId="config-xml"></@xmlEditorScript></#noescape>  
  </p>
  </@form>
</@section>

<#-- SECTION Links -->
<@section title="Links">
  <p>
  	<a href="${uris.configs()}">Configuration home</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>