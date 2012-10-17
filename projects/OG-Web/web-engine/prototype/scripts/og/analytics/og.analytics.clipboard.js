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
            DOUBLE: function (value) {return value && value.v || '';},
            LABELLED_MATRIX_1D: function (value) {return value && value.v || '';},
            PRIMITIVE: function (value) {return value.v || value;},
            TIME_SERIES: function (value) {return '**TIME SERIES**';},
            UNKNOWN: function (value) {
                var type = value.t;
                return value && formatters[type] ? formatters[type](value) : value && value.v || '';
            }
        };
        var constructor = function (grid) {
            var clipboard = this;
            clipboard.data = null;
            clipboard.dataman = null;
            clipboard.grid = grid;
            clipboard.signature = null;
        };
        var format = function (cell) {
            if (formatters[cell.type]) return formatters[cell.type](cell.value);
            og.dev.warn(module.name + ': no formatter for ' + cell.type, cell);
            return typeof cell.value.v === 'string' ? cell.value.v : '';
        };
        var select = function (text) {textarea.val(text).focus().select();};
        constructor.prototype.clear = function () {
            var clipboard = this;
            if (clipboard.dataman) try {clipboard.dataman.kill(), clipboard.dataman = null;} catch (error) {}
        };
        constructor.prototype.has = function (selection) {
            var clipboard = this, grid = clipboard.grid, grid_data, signature, dataman;
            if (!selection) return false;
            grid_data = selection.rows.length === 1 && selection.cols.length === 1 ? grid.cell(selection)
                : grid.range(selection);
            signature = selection.rows.join('|') + '-' + selection.cols.join('|');
            if (!grid_data && signature !== clipboard.signature) clipboard.data = null;
            clipboard.signature = signature;
            if (grid_data && clipboard.dataman) clipboard.clear();
            if (!grid_data && !clipboard.dataman) {
                clipboard.data = null;
                clipboard.dataman = dataman = new og.analytics.Data(grid.source)
                    .viewport({rows: selection.rows, cols: selection.cols})
                    .on('data', function (data) {
                        if (dataman.signature !== clipboard.signature) return clipboard.data = null, clipboard.clear();
                        var lcv, index = 0, rows = selection.rows.length, cols = selection.cols.length, row;
                        clipboard.data = [];
                        while (rows--) {
                            for (clipboard.data.push(row = []), lcv = 0; lcv < cols; lcv += 1)
                                row.push({value: data[index++], type: selection.type[lcv]});
                        }
                    });
                dataman.signature = signature;
            }
            return grid_data ? (clipboard.data = grid_data) && true : !!clipboard.data;;
        };
        constructor.prototype.select = function () {
            var clipboard = this;
            if (!clipboard.data) return og.dev.warn(module.name + ': no data to select');
            if (!$.isArray(clipboard.data)) return select(format(clipboard.data));
            select(clipboard.data.map(function (row) {
                return row.map(function (cell) {return format(cell);}).join('\t');
            }).join('\n'));
        };
        $(function () {
            node = (textarea = $('<textarea />').appendTo('body')
                .css({position: 'absolute', top: '-500px', left: '-500px', width: '100px', height: '100px'}))[0];
        });
        return constructor;
    }
});
