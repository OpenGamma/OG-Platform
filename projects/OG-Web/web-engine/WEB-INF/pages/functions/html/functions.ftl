<#escape x as x?html>
<@page title="Functions">


<#-- SECTION Function search -->
<@section title="Function search" if=searchRequest??>
  <@form method="GET" action="${uris.functions()}">
  <p>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="${searchRequest.name}" /></@rowin>
    <@rowin label="Parameterized">
      <select name="parameterized">
        <option value="" <#if searchRequest.parameterized = ''>selected</#if>></option>
        <option value="Y" <#if searchRequest.parameterized = 'Y'>selected</#if>>Y</option>
        <option value="N" <#if searchRequest.parameterized = 'N'>selected</#if>>N</option>
      </select>
    </@rowin>
    
    <@rowin><input type="submit" value="Search" /></@rowin>
  </p>
  </@form>

<#-- SUBSECTION Function results -->
<#if searchResult??>
<@subsection title="Results">
  <@table items=searchResult.functions paging=paging empty="No functions" headers=["Function name", "Fully qualified type", "Parameterized instances"]; item>
      <#if item.parameterized>
      <td><a href="${uris.parameterziedFunction(item.simpleName)}">${item.simpleName}</a></td>
      <#else>
      <td>${item.simpleName}</td>
      </#if>
      
      <td>${item.fullyQualifiedName}</td>
      <td>
      <#if item.parameterized>
      ${item.parameters?size}
      </#if>
      </td>

  </@table>
</@subsection>
</#if>
</@section>


<#-- SECTION Add function >
<@section title="Add function">
  <@form method="POST" action="${uris.functions()}">
  <p>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="" /></@rowin>
    <@rowin label="Scheme type"><input type="text" size="30" maxlength="80" name="idscheme" value="" /></@rowin>
    <@rowin label="Scheme id"><input type="text" size="30" maxlength="80" name="idvalue" value="" /></@rowin>
    <@rowin label="Region type"><input type="text" size="30" maxlength="80" name="regionscheme" value="" /></@rowin>
    <@rowin label="Region id"><input type="text" size="30" maxlength="80" name="regionvalue" value="" /></@rowin>
    <@rowin><input type="submit" value="Add" /></@rowin>
  </p>
  </@form>
</@section-->


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
