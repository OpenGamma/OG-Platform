<#escape x as x?html>
<@page title="Portfolio node - ${node.name}">

<@section css="info" if=deleted>
  <p>This portfolio has been deleted</p>
</@section>


<#-- SECTION Node output -->
<@section title="Portfolio node">
  <p>
    <@rowout label="Portfolio"><a href="${uris.portfolio()}">${portfolio.name}</a></@rowout>
<#if parentNode?has_content>
    <@rowout label="Parent node"><a href="${uris.node(parentNode)}">${parentNode.name}</a></@rowout>
</#if>
    <@rowout label="Name">${node.name}</@rowout>
    <@rowout label="Reference">${node.uniqueId.value}, version ${node.uniqueId.version}</@rowout>
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
  <@table items=positions empty="No positions" headers=["Name","Reference","Quantity","Actions",""]; item>
      <td><a href="${positionUris.position(item)}">${item.name}</a></td>
      <td>${item.uniqueId.value}</td>
      <td>${item.quantity}</td>
      <td><a href="${positionUris.position(item)}">View</a></td>
      <td><@form method="DELETE" action="${uris.nodePosition(item.uniqueId)}"><input type="submit" value="Delete" /></@form></td>
  </@table>
</@subsection>
</@section>


<#-- SECTION Update node -->
<@section title="Update node" if=!deleted>
  <@form method="PUT" action="${uris.node()}">
  <p>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="${node.name}" /></@rowin>
    <@rowin><input type="submit" value="Update" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Delete portfolio -->
<@section title="Delete node" if=!deleted>
  <@form method="DELETE" action="${uris.node()}">
  <p>
    <@rowin><input type="submit" value="Delete" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Add node -->
<@section title="Add child node" if=!deleted>
  <@form method="POST" action="${uris.node()}">
  <p>
    <@rowin label="Name"><input type="text" size="30" maxlength="80" name="name" value="" /></@rowin>
    <@rowin><input type="submit" value="Add" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Add position -->
<@section title="Add position" if=!deleted>
  <@form method="POST" action="${uris.nodePositions(node)}">
  <p>
    <@rowin label="Position URL"><input type="text" size="60" maxlength="255" name="positionurl" value="" /></@rowin>
    <@rowin><input type="submit" value="Add" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
<#if parentNode?has_content>
    <a href="${uris.node(parentNode)}">Parent node - ${parentNode.name}</a><br />
</#if>
    <a href="${uris.portfolio()}">Portfolio - ${portfolio.name}</a><br />
    <a href="${uris.portfolios()}">Portfolio search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
