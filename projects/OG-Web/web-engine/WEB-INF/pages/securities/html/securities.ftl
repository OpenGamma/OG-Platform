<#escape x as x?html>
<@page title="Securities">


<#-- SECTION Security search -->
<@section title="Security search" if=searchRequest??>
  <@form method="GET" action="${uris.securities()}">
  <p>
    <@rowin label="Type">
      <select name="type">
        <option value="" <#if searchRequest.securityType = ''>selected</#if>></option>
        <#list securityTypes as key>
          <option value="${key}" <#if searchRequest.securityType = '${key}'>selected</#if>>${key}</option>
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
<@section title="Add securities">
  <@form method="POST" action="${uris.securities()}" id="addSecurityForm">
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
    
    <@subsection title="Security Bean XML">
      <@rowin label="">
        <div id="security-xml-editor"></div>
      </@rowin>
    </@subsection>
    
    <@rowin><input type="hidden" name="securityxml" id="securityxml"/></@rowin>
    <@rowin><input type="submit" value="Add" /></@rowin>
<script type="text/javascript">
var editor = ace.edit("security-xml-editor")
editor.getSession().setMode('ace/mode/xml')
$("#security-xml-editor").show()

$("#addSecurityForm").submit( function(eventObj) {
  $("#securityxml").val(editor.getSession().getValue())
  return true
})
</script>
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
