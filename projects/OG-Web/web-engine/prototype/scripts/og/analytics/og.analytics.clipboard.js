/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.Clipboard',
    dependencies: ['og.analytics.Data'],
    obj: function () {
        var module = this, textarea, node, formatters = {
            CURVE: function (value) {return '**CURVE**';},
            DOUBLE: function (value) {
                return typeof value === 'string' ? value : typeof value.v === 'string' ? value.v : ''
            },
            LABELLED_MATRIX_1D: function (value) {
                return typeof value === 'string' ? value : typeof value.v === 'string' ? value.v : '';
            },
            LABELLED_MATRIX_2D: function (value) {
                return typeof value === 'string' ? value : typeof value.v === 'string' ? value.v : ''
            },
            PRIMITIVE: function (value) {
                return typeof value === 'string' ? value : typeof value.v === 'string' ? value.v : ''
            },
            SURFACE_DATA: function (value) {return '** SURFACE DATA **';},
            TIME_SERIES: function (value) {return '**TIME SERIES**';},
            UNKNOWN: function (value) {
                var type = value.t;
                return value && formatters[type] ? formatters[type](value) : value && value.v || '';
            }
        };
        var constructor = function (grid) {
            var clipboard = this;
            clipboard.data = clipboard.selection = null;
            clipboard.dataman = new og.analytics.Data(grid.source, true).on('data', data_handler, clipboard);
            clipboard.grid = grid.on('select', function (selection) {clipboard.viewport(selection);});
        };
        var data_handler = function (data) {
            var clipboard = this, grid = clipboard.grid, lcv, index = 0, selection = clipboard.selection,
                rows = selection.rows.length, cols = selection.cols.length, row;
            clipboard.data = [];
            while (rows--) for (clipboard.data.push(row = []), lcv = 0; lcv < cols; lcv += 1)
                row.push({value: data[index++], type: clipboard.selection.type[lcv]});
            if (!grid.selector.copyable) grid.selector.render();
        };
        var format = function (cell) {
            if (typeof cell.value === 'undefined') return '';
            if (formatters[cell.type]) return formatters[cell.type](cell.value);
            og.dev.warn(module.name + ': no formatter for ' + cell.type, cell);
            return typeof cell.value.v === 'string' ? cell.value.v : '';
        };
        var same_viewport = function (one, two) {
            if ((!one || !two) && one !== two) return false; // if either viewport is null
            return one.rows.join('|') === two.rows.join('|') && one.cols.join('|') === two.cols.join('|');
        };
        var select = function (text) {textarea.val(text).focus().select();};
        constructor.prototype.clear = function () {
            var clipboard = this;
            if (clipboard.selection) clipboard.dataman.viewport(clipboard.selection = clipboard.data = null);
        };
        constructor.prototype.has = function (selection) {
            var clipboard = this, grid = clipboard.grid;
            clipboard.viewport(selection);
            return !!selection && !!(clipboard.data || (clipboard.data = grid.range(selection)));
        };
        constructor.prototype.select = function () {
            var clipboard = this, data = clipboard.data;
            if (!data || !data.length) return og.dev.warn(module.name + ': no data to select'), select('');
            select(data.map(function (r) {return r.map(function (c) {return format(c);}).join('\t');}).join('\n'));
        };
        constructor.prototype.viewport = function (selection) {
            var clipboard = this, grid = clipboard.grid, grid_data, data_viewport = clipboard.dataman.meta.viewport;
            if (selection === null) return clipboard.dataman.viewport(clipboard.selection = clipboard.data = null);
            grid_data = grid.range(selection);
            if (same_viewport(clipboard.selection, selection)) if (same_viewport(selection, data_viewport))
                return grid_data ? (clipboard.dataman.viewport(null), clipboard.data = grid_data) : null;
            return (clipboard.selection = selection) && grid_data ?
                (clipboard.dataman.viewport(null), clipboard.data = grid_data)
                    : (clipboard.dataman.viewport({rows: selection.rows, cols: selection.cols}), clipboard.data = null);
        };
        $(function () {
            node = (textarea = $('<textarea />').appendTo('body')
                .css({position: 'absolute', top: '-500px', left: '-500px', width: '100px', height: '100px'}))[0];
        });
        return constructor;
    }
});
