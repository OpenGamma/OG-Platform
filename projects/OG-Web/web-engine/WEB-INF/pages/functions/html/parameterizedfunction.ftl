<#escape x as x?html>
<@page title="Functions">


<#-- SECTION Function results -->
<@section title="Function results">
<#if searchResult??>
<@subsection title=searchResult.simpleName>
  <#assign colNames = []>
  <#list searchResult.parameters?first as x>
    <#assign colNames = colNames + [ "Parameter ${x_index}" ] >
  </#list>
  <@table items=searchResult.parameters paging=paging empty="No functions" headers=colNames; item>
      
      <#list item as x>
      <td>${x}</td>
      </#list>
  
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
    <a href="${homeUris.home()}">Home</a> <br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
