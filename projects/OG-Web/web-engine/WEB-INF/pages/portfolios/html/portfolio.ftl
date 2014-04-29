<#escape x as x?html>
<@page title="Portfolio - ${portfolio.name}">

<@section css="info" if=deleted>
  <p>This portfolio has been deleted</p>
</@section>


<#-- SECTION Portfolio output -->
<@section title="Portfolio">
  <p>
    <@rowout label="Name">${portfolio.name}</@rowout>
    <@rowout label="Reference">${portfolio.uniqueId.value}, version ${portfolio.uniqueId.version}</@rowout>
  </p>

<#-- SUBSECTION Child nodes -->
<@subsection title="Child nodes">
  <@table items=childNodes empty="No child nodes" headers=["Name","Reference","Actions"]; item>
      <td><a href="${uris.node(item)}">${item.name}</a></td>
      <td>${item.uniqueId.value}</td>
      <td><a href="${uris.node(item)}">View</a></td>
  </@table>
</@subsection>

<#-- SUBSECTION Positions -->
<@subsection title="Positions">
  <@table items=positions empty="No positions" headers=["Name","Reference","Quantity","Actions"]; item>
      <td><a href="${positionUris.position(item)}">${item.name}</a></td>
      <td>${item.uniqueId.value}</td>
      <td>${item.quantity}</td>
      <td><a href="${positionUris.position(item)}">View</a></td>
  </@table>
</@subsection>
</@section>


<#-- SECTION Update portfolio -->
<@section title="Update portfolio" if=!deleted && userSecurity.isPermitted('PortfolioMaster:edit:update')>
  <@form method="PUT" action="${uris.portfolio()}">
  <p>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="${portfolio.name}" /></@rowin>
    <@rowin label="Hidden"><input type="checkbox" name="hidden" value="true" <#if portfolioDoc.visibility = 'HIDDEN'>checked="true"</#if> /></@rowin>
    <@rowin><input type="submit" value="Update" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Delete portfolio -->
<@section title="Delete portfolio" if=!deleted && userSecurity.isPermitted('PortfolioMaster:edit:update')>
  <@form method="DELETE" action="${uris.portfolio()}">
  <p>
    <@rowin><input type="submit" value="Delete" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Add node -->
<@section title="Add child node" if=!deleted && userSecurity.isPermitted('PortfolioMaster:edit:update')>
  <@form method="POST" action="${uris.node(portfolio.rootNode)}">
  <p>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="" /></@rowin>
    <@rowin><input type="submit" value="Add" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Add position -->
<@section title="Add position" if=!deleted && userSecurity.isPermitted('PortfolioMaster:edit:update')>
  <@form method="POST" action="${uris.nodePositions(portfolio.rootNode)}">
  <p>
    <@rowin label="Position URL"><input type="text" size="60" maxlength="255" name="positionurl" value="" /></@rowin>
    <@rowin><input type="submit" value="Add" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.portfolios()}">Portfolio search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
