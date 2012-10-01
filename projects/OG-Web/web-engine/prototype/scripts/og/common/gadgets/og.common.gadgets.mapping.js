/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.mapping',
    dependencies: [],
    obj: function () {
        var module = this, mapping;
        return mapping = {
            gadgets: ['Depgraph', 'Data', 'Surface', 'Curve', 'Timeseries'],
            panel_preference: {
                'south'      : [0, 2, 4, 3, 1],
                'dock-north' : [2, 4, 3, 1, 0],
                'dock-center': [2, 4, 3, 1, 0],
                'dock-south' : [2, 4, 3, 1, 0],
                'new-window' : [2, 4, 3, 1, 0]
            },
            options: function (cell, grid, panel) {
                var type = mapping.type(cell, panel), source = $.extend({}, grid.source), options = {
                    'Depgraph': function (cell) {
                        source.depgraph = true;
                        source.col = cell.col;
                        source.row = cell.row;
                        return {
                            gadget: 'og.common.gadgets.Depgraph',
                            options: {child: true, source: source},
                            row_name: cell.row_name,
                            col_name: cell.col_name,
                            type: 'Dependency Graph'
                        }
                    }
                };
                if (!options[type]) return og.dev.warn(type + ' does not exist in ' + module.name);
                return options[type](cell);
            },
            type : function (cell, panel) {
                var order = mapping.panel_preference[panel || 'new-window'],
                    type_map = mapping.type_map[cell.type], i, k;
                for (i = 0; i < order.length; i++) for (k = 0; k < type_map.length; k++) if (order[i] === type_map[k])
                    return mapping.gadgets[order[i]];
            },
            type_map: {
                CURVE             : [1, 3],
                DOUBLE            : [0],
                LABELLED_MATRIX_1D: [0, 1],
                LABELLED_MATRIX_2D: [0, 1, 2, 3],
                LABELLED_MATRIX_3D: [0, 1],
                PRIMITIVE         : [0],
                SURFACE_DATA      : [2, 1],
                TENOR             : [0],
                TIME_SERIES       : [4, 1],
                UNKNOWN           : [0]
            }
        }
    }
});
