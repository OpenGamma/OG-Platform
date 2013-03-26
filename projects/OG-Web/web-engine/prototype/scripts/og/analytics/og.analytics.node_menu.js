/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.node_menu',
    dependencies: [],
    obj: function () {
        var module = this;
        return function (event, cell) {
            var grid = this, state = grid.state, items, expanded = state.nodes[cell.row],
                expand_deep, expand_globally, collapse_deep, collapse_globally, dry = true,
                rows = grid.meta.viewport.rows.reduce(function (acc, row) {return acc[row] = null, acc;}, {});
            var loading = function (row) {
                var $target = grid.elements.fixed_body.find('.node[data-row=' + row + ']');
                if (!$target.length) return;
                if ($target.is('.loading')) return; else $target.removeClass('expand collapse').addClass('loading');
            };
            expand_deep = {name: 'Expand (deep)', handler: function () {
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
            }};
            items = (expanded ? [collapse_deep, collapse_globally, {}, expand_deep, expand_globally]
                : [expand_deep, expand_globally, {}, collapse_deep, collapse_globally]).map(function (item) {
                    if (item.handler) item.disabled = !item.handler(); // check changed flags & enable relevant options
                    return item;
                });
            dry = false;
            return og.common.util.ui.contextmenu({zindex: 4, items: items}, event, cell);
        };
    }
});