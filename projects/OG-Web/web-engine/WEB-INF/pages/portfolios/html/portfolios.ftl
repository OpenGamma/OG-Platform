<#escape x as x?html>
<@page title="Portfolios">


<#-- SECTION Portfolio search -->
<@section title="Portfolio search" if=searchRequest??>
  <@form method="GET" action="${uris.portfolios()}">
  <p>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="${searchRequest.name}" /></@rowin>
    <@rowin label="Show Hidden Portfolios"><input type="checkbox" name="includeHidden" value="true" <#if searchRequest.visibility = 'HIDDEN'>checked="true"</#if> /></@rowin>
    <@rowin><input type="submit" value="Search" /></@rowin>
  </p>
  </@form>

<#-- SUBSECTION Portfolio results -->
<#if searchResult??>
<@subsection title="Results">
  <@table items=searchResult.documents paging=paging empty="No portfolios" headers=["Name","Reference","Version valid from","Actions"]; item>
      <td><a href="${uris.portfolio(item.portfolio)}">${item.portfolio.name}</a></td>
      <td>${item.portfolio.uniqueId.value}</td>
      <td>${item.versionFromInstant}</td>
      <td><a href="${uris.portfolio(item.portfolio)}">View</a></td>
  </@table>
</@subsection>
</#if>
</@section>


<#-- SECTION Add portfolio -->
<@section title="Add portfolio" if=userSecurity.isPermitted('PortfolioMaster:edit:add')>
  <@form method="POST" action="${uris.portfolios()}">
  <p>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="" /></@rowin>
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
