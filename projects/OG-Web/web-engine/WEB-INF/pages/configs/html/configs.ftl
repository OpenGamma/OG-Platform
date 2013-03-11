<#escape x as x?html>
<@page title="Configurations">


<#-- SECTION Config search -->
<@section title="Configuration search" if=searchRequest??>
  <@form method="GET" action="${uris.configs()}">
  <p>
  	<@rowin label="Type">
      <select name="type">
      	<option value="" <#if type = ''>selected</#if>></option>
      	<#list typeMap?keys as key>
    		<option value="${key}" <#if type = '${key}'>selected</#if>>${key}</option>
		</#list>
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
  <@form method="POST" action="${uris.configs()}">
  <p>
    <@rowin label="Name"><input type="text" size="30" name="name" value="" /></@rowin>
    <@rowin label="Type"><input type="text" size="30" name="type" value="" /></@rowin>
    <@rowin label="Configuration (XML)">
      <div style="border:1px solid black;padding:2px;"><textarea rows="30" cols="80" name="configxml" id="xmltextarea"></textarea></div>
    </@rowin>
    <@rowin><input type="submit" value="Add" /></@rowin>
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