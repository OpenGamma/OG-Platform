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
            clipboard.data = null;
            clipboard.dataman = new og.analytics.Data(grid.source, true /* bypass type check */)
                .on('data', function (data) {
                    var lcv, index = 0,
                        rows = clipboard.selection.rows.length,
                        cols = clipboard.selection.cols.length, row;
                    clipboard.data = [];
                    while (rows--) for (clipboard.data.push(row = []), lcv = 0; lcv < cols; lcv += 1)
                        row.push({value: data[index++], type: clipboard.selection.type[lcv]});
                    // if (!grid.selector.copyable) grid.selector.render();
                });
            clipboard.grid = grid;
            clipboard.selection = null;
            grid.on('select', function (selection) {clipboard.viewport(selection);});
        };
        var format = function (cell) {
            if (typeof cell.value === 'undefined') return '';
            if (formatters[cell.type]) return formatters[cell.type](cell.value);
            og.dev.warn(module.name + ': no formatter for ' + cell.type, cell);
            return typeof cell.value.v === 'string' ? cell.value.v : '';
        };
        var same_viewport = function (one, two) {
            return one.rows.join('|') === two.rows.join('|') && one.cols.join('|') === two.cols.join('|');
        };
        var select = function (text) {textarea.val(text).focus().select();};
        constructor.prototype.clear = function () {
            var clipboard = this;
            if (clipboard.selection) clipboard.dataman.viewport(null), clipboard.selection = clipboard.data = null;
        };
        constructor.prototype.has = function (selection) {
            if (!selection) return false;
            var clipboard = this, grid = clipboard.grid;
            return !!(clipboard.data || (clipboard.data = grid.range(selection)));
        };
        constructor.prototype.select = function () {
            var clipboard = this;
            if (!clipboard.data) return og.dev.warn(module.name + ': no data to select');
            if (!$.isArray(clipboard.data)) return select(format(clipboard.data));
            select(clipboard.data.map(function (row) {
                return row.map(function (cell) {return format(cell);}).join('\t');
            }).join('\n'));
        };
        constructor.prototype.viewport = function (selection) {
            var clipboard = this, grid = clipboard.grid;
            if (clipboard.selection && selection && same_viewport(clipboard.selection, selection)) return;
            clipboard.selection = selection;
            clipboard.data = null;
            if (grid.range(selection)) return;
            clipboard.dataman.viewport(selection);
        };
        $(function () {
            node = (textarea = $('<textarea />').appendTo('body')
                .css({position: 'absolute', top: '-500px', left: '-500px', width: '100px', height: '100px'}))[0];
        });
        return constructor;
    }
});
