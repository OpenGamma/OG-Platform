<#escape x as x?html>
<@page title="Snapshots" jquery=true aceXmlEditor=true>

<#-- SECTION Snapshot search -->
<@section title="Snapshot search" if=searchRequest??>
  <@form method="GET" action="${uris.snapshots()}">
  <p>
  	<@rowin label="Type">
      <select name="type">
      	<option value="" <#if type = ''>selected</#if>></option>
      	<#list snapshotDescriptionMap?keys as key><option value="${key}"<#if type = '${key}'> selected</#if>>${snapshotDescriptionMap[key]}</option></#list>
      </select>
    </@rowin>
    <@rowin label="Name"><input type="text" size="30" name="name" value="${searchRequest.name}" /></@rowin>
    <@rowin><input type="submit" value="Search" /></@rowin>
  </p>
  </@form>

<#-- SUBSECTION Snapshot results -->
<#if searchResult??>
<@subsection title="Results">
  <@table items=searchResult.documents paging=paging empty="No snapshot found" headers=["Name","Reference","Version valid from","Actions"]; item>
      <td><a href="${uris.snapshot(item.uniqueId)}">${item.name}</a></td>
      <td>${item.uniqueId.value}</td>
      <td>${item.versionFromInstant}</td>
      <td><a href="${uris.snapshot(item.uniqueId)}">View</a></td>
  </@table>
</@subsection>
</#if>
</@section>

<#-- SECTION Add snapshot -->
<@section title="Add snapshot">
  <@form method="POST" action="${uris.snapshots()}" id="addForm">
  <p>
    <@rowin>
      <div id="ace-xml-editor"></div>
    </@rowin>
    <input type="hidden" name="snapshotxml" id="snapshot-xml"/>
    <@rowin><input type="submit" value="Add" /></@rowin>
    
    <#noescape><@xmlEditorScript formId="addForm" inputId="snapshot-xml"></@xmlEditorScript></#noescape>  
  </p>
  </@form>
</@section>

<#-- SECTION Links -->
<@section title="Links">
  <p>
  	<a href="${uris.snapshots()}">Snapshot home</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>