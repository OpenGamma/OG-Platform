<#escape x as x?html>
<@page title="Securities">


<#-- SECTION Security search -->
<@section title="Security search" if=searchRequest??>
  <@form method="GET" action="${uris.securities()}">
  <p>
    <@rowin label="Type">
      <select name="type">
        <option value="" <#if searchRequest.securityType = ''>selected</#if>></option>
        <option value="BOND" <#if searchRequest.securityType = 'BOND'>selected</#if>>Bond</option>
        <option value="CASH" <#if searchRequest.securityType = 'CASH'>selected</#if>>Cash</option>
        <option value="EQUITY" <#if searchRequest.securityType = 'EQUITY'>selected</#if>>Equity</option>
        <option value="FRA" <#if searchRequest.securityType = 'FRA'>selected</#if>>FRA</option>
        <option value="FUTURE" <#if searchRequest.securityType = 'FUTURE'>selected</#if>>Future</option>
        <option value="EQUITY_OPTION" <#if searchRequest.securityType = 'EQUITY_OPTION'>selected</#if>>Option</option>
        <option value="SWAP" <#if searchRequest.securityType = 'SWAP'>selected</#if>>Swap</option>
      </select>
    </@rowin>
    <@rowin label="Identifier"><input type="text" size="30" maxlength="80" name="identifier" value="${searchRequest.identifierValue}" /></@rowin>
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
    <@rowin label="Identifiers"></@rowin>
    <@rowin><textarea name="idvalue" cols="35" rows="10"></textarea></@rowin>
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
