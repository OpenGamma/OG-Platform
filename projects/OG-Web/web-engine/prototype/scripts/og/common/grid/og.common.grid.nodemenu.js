/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.grid.NodeMenu',
    dependencies: [],
    obj: function () {
        var module = this, has = 'hasOwnProperty';
        var NodeMenu = function (grid, cell, event) {
            var menu = this;
            menu.grid = grid; menu.event = event; menu.cell = cell;
        };
        NodeMenu.prototype.display = function () {
            var menu = this;
            return og.common.util.ui.contextmenu({zindex: 5, items: menu.items()}, menu.event, menu.cell);
        };
        NodeMenu.prototype.items = function () {
            var menu = this, grid = menu.grid, event = menu.event, cell = menu.cell, dry = true,
                state = grid.state, items, expanded = state.nodes[cell.row],
                expand_deep, expand_globally, collapse_deep, collapse_globally, new_portfolio,
                rows = grid.meta.viewport.rows.reduce(function (acc, row) {return acc[row] = null, acc;}, {});
            var loading = function (row) {
                var $target = grid.elements.fixed_body.find('.node[data-row=' + row + ']');
                if (!$target.length) return;
                if ($target.is('.loading')) return; else $target.removeClass('expand collapse').addClass('loading');
            };
            if (!grid.state.nodes[has](cell.row)) return [];
            /*expand_deep = {name: 'Expand (deep)', handler: function () {
                var range = state.nodes.ranges.filter(function (range) {return range[0] === cell.row;})[0],
                    indent, max_indent, nodes, changed;
                indent = state.nodes.indent[cell.row];
                nodes = state.nodes.all.filter(function (node) {return node >= range[0] && node <= range[1];});
                max_indent = Math.max.apply(null, nodes.map(function (node) {return state.nodes.indent[node];}));
                max_indent = max_indent - (max_indent === indent ? 0 : max_indent ? 1 : 0);
                if (!dry && (cell.row in rows)) loading(cell.row);
                nodes.forEach(function (node) {
                    if (state.nodes.indent[node] > max_indent) return;
                    if (!state.nodes[node]) changed = true;
                    if (!dry) state.nodes[node] = true;
                });
                if (dry) return changed;
                grid.resize().selector.clear();
            }};
            expand_globally = {name: 'Expand globally at this level', handler: function () {
                var indent = state.nodes.indent[cell.row], max_indent = state.nodes.max_indent, changed;
                max_indent = max_indent - (max_indent === indent ? 0 : max_indent ? 1 : 0);
                state.nodes.all.forEach(function (node) {
                    var current = state.nodes.indent[node];
                    if (current < indent || current > max_indent) return;
                    if (!dry && (node in rows) && !state.nodes[node]) loading(node);
                    if (!state.nodes[node]) changed = true;
                    if (!dry) state.nodes[node] = true;
                });
                if (dry) return changed;
                grid.resize().selector.clear();
            }};
            collapse_deep = {name: 'Collapse (deep)', handler: function () {
                var range = state.nodes.ranges.filter(function (range) {return range[0] === cell.row;})[0], changed,
                    nodes = state.nodes.all.filter(function (node) {return node >= range[0] && node <= range[1];});
                if (!dry && (cell.row in rows)) loading(cell.row);
                nodes.forEach(function (node) {
                    if (state.nodes[node]) changed = true;
                    if (!dry) state.nodes[node] = false;
                });
                if (dry) return changed;
                grid.resize().selector.clear();
            }};
            collapse_globally = {name: 'Collapse globally at this level', handler: function () {
                var indent = state.nodes.indent[cell.row], changed;
                state.nodes.all.forEach(function (node) {
                    if (state.nodes.indent[node] < indent) return;
                    if (!dry && (node in rows) && state.nodes[node]) loading(node);
                    if (state.nodes[node]) changed = true;
                    if (!dry) state.nodes[node] = false;
                });
                if (dry) return changed;
                grid.resize().selector.clear();
            }};*/
            new_portfolio = {name: 'Create a new portfolio containing this position', handler: function () {
                var position = cell.value.v['positionId'], portfolio, win = window.open();
                og.api.rest.portfolios.put({name: cell.value.v['name'] + ' - ' + og.common.util.date(new Date)})
                    .pipe(function (result) {
                        if (result.error) return null;
                        return og.api.rest.portfolios.get({id: result.meta.id});
                    })
                    .pipe(function (result) {
                        if (!result || result.error) return null;
                        portfolio = result.data.template_data;
                        return og.api.rest.portfolios
                            .put({id: portfolio.object_id, node: portfolio.node, position: position});
                    })
                    .pipe(function (result) {
                        if (!result || result.error) alert(result.error || 'An error occurred creating this portfolio');
                        win.location.href = 'admin.ftl#/portfolios/' + portfolio.object_id;
                    });
            }};
            /*items = (expanded ? [collapse_deep, collapse_globally, {}, expand_deep, expand_globally]
                : [expand_deep, expand_globally, {}, collapse_deep, collapse_globally]).map(function (item) {
                    if (item.handler) item.disabled = !item.handler(); // check changed flags & enable relevant options
                    return item;
                });*/
            items = [];
            if (!grid.source.aggregators.length && cell.value.v[has]('positionId')) // no new portfolio option in
                items.push({}, new_portfolio);                                      // aggregated views
            return (dry = false), items;
        };
        return NodeMenu;
    }
});