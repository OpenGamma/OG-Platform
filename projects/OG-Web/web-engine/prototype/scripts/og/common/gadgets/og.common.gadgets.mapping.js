/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.mapping',
    dependencies: [],
    obj: function () {
        var mapping, gadget_names = {
            'Curve': 'Curve',
            'Data': 'Data',
            'Log': 'Log',
            'Depgraph': 'Dependency Graph',
            'Surface': 'Surface',
            'Timeseries': 'Time Series',
            'Histogram' : 'Histogram',
            'ExpandedPositions': 'Position'
        };
        return mapping = {
            gadgets: ['Depgraph', 'Data', 'Surface', 'Curve', 'Timeseries', 'ExpandedPositions', 'Log', 'Histogram'],
            panel_preference: {
                'south'      : [0, 2, 4, 3, 1, 5, 6],
                'dock-north' : [2, 4, 3, 1, 0, 5, 6],
                'dock-center': [2, 4, 3, 1, 0, 5, 6],
                'dock-south' : [2, 4, 3, 1, 0, 5, 6],
                'new-window' : [2, 4, 3, 1, 0, 5, 6],
                'inplace'    : [2, 4, 3, 1, 0, 5, 6]
            },
            options: function (cell, grid, panel) {
                var gadget_type = mapping.type(cell, panel), source = $.extend({}, grid.source), gadget_options;
                gadget_options = {
                    gadget: 'og.common.gadgets.' + gadget_type,
                    options: {
                        source: source, child: true, col: cell.col, row: cell.row,
                        menu: false, datapoints_link: false, /* ONLY RELEVANT FOR TIMESERIES (be wary) */
                        id: cell.value.positionId, editable: false, external_links: true /* ONLY EXPANDED POSITIONS */
                    },
                    row_name: cell.row_name, col_name: cell.col_name,
                    gadget_name: gadget_names[gadget_type],
                    gadget_type: gadget_type,
                    data_type: cell.type
                };
                return gadget_options;
            },
            type: function (cell, panel) {
                if (cell.value.logLevel === 'ERROR') return 'Log';
                var order = mapping.panel_preference[panel || 'new-window'],
                    type_map = mapping.data_type_map[cell.type], i, k;
                for (i = 0; i < order.length; i++)
                    for (k = 0; k < type_map.length; k++)
                        if (order[i] === type_map[k]) return mapping.gadgets[order[i]];
            },
            available_types: function (data_type, depgraph) {
                var types_array = mapping.data_type_map[data_type], i, types = {gadgets:[]}, current, gadget;
                for (i = 0; i < types_array.length; i++){
                    current = mapping.gadgets[types_array[i]];
                    if (depgraph && current === 'Depgraph') continue;
                    gadget = {name: current, gadget_name: gadget_names[current]};
                    types.gadgets.push(gadget);
                }
                return types;
            },
            depgraph_blacklist: ['DOUBLE', 'PRIMITIVE', 'TENOR', 'UNKNOWN'],
            data_type_map: {
                CURVE                   : [1, 3, 6],
                DOUBLE                  : [0, 6],
                LABELLED_MATRIX_1D      : [0, 1, 6],
                LABELLED_MATRIX_2D      : [0, 1, 6],
                LABELLED_MATRIX_3D      : [0, 1, 6],
                PRIMITIVE               : [5, 6],
                SURFACE_DATA            : [2, 1, 0, 6],
                UNPLOTTABLE_SURFACE_DATA: [1, 6],
                TENOR                   : [0, 6],
                TIME_SERIES             : [4, 1, 6],
                UNKNOWN                 : [0, 6],
                VECTOR                  : [0, 1, 6],
                HISTOGRAM               : [5, 1, 6]
            }
        };
    }
});
