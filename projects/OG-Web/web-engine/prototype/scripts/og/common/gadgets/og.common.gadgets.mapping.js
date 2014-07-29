/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.mapping',
    dependencies: [],
    obj: function () {
        var module = this, mapping, gadget_names = {
            'Curve': 'Curve',
            'Data': 'Data',
            'Log': 'Log',
            'Depgraph': 'Explain Value',
            'Surface': 'Surface',
            'Timeseries': 'Time Series',
            'Histogram' : 'Histogram',
            'ExpandedPositions': 'Position',
            'Text' : 'Text'
        };
        var available_types = function (data_type, depgraph, gadget_type) {
            var types_array = mapping.data_type_map[data_type], i, current, gadget,
                types = {simple_gadgets: [], complex_gadgets: []};
            for (i = 0; i < types_array.length; i++) {
                current = mapping.gadgets[types_array[i]];
                if (depgraph && current === 'Depgraph') {
                    continue;
                }
                gadget = {name: current, gadget_name: gadget_names[current]};
                if (current === gadget_type) {
                    gadget.default_type = true;
                }
                !is_complex(current) ? types.complex_gadgets.push(gadget) : types.simple_gadgets.push(gadget);
            }
            return types;
        };
        var is_complex = function (name) {return ~[0, 6, 8].indexOf(mapping.gadgets.indexOf(name)); }; // if not simple
        var options = function (cell, grid, panel, req) {
            var gadget_type = mapping.type(cell, panel), source = $.extend({}, grid.source), gadget_options;
            gadget_options = {
                gadget: 'og.common.gadgets.' + gadget_type,
                options: {source: source, child: true, type: cell.type, row: cell.row, col: cell.col,
                    menu: false, datapoints_link: false, /* ONLY RELEVANT FOR TIMESERIES (be wary) */
                    value: cell.value.v, editable: false, external_links: true /* ONLY EXPANDED POSITIONS */},
                row_name: cell.row_name,
                col_name: cell.col_name,
                gadget_name: gadget_names[gadget_type],
                gadget_type: gadget_type,
                data_type: cell.type
            };
            if (req) {
                gadget_options.options.req = req.valueRequirement;
                gadget_options.options.colset = req.columnSet;
            }
            return gadget_options;
        };
        var type = function (cell, panel) {
            var order = mapping.panel_preference[panel || 'new-window'], i, k,
                type_map = mapping.data_type_map[cell.type];
            if (!type_map) {
                throw new Error(module.name + ': no type information available for ' + cell.type);
            }
            for (i = 0; i < order.length; i++) {
                for (k = 0; k < type_map.length; k++) {
                    if (order[i] === type_map[k]) {
                        return mapping.gadgets[order[i]];
                    }
                }
            }
        };
        return mapping = {
            data_type_map: {
                CURVE                   : [3, 1, 6, 0],
                DOUBLE                  : [0, 6],
                FUNGIBLE_TRADE          : [5],
                LABELLED_MATRIX_1D      : [0, 1, 6],
                LABELLED_MATRIX_2D      : [0, 1, 6],
                LABELLED_MATRIX_3D      : [0, 1, 6],
                MATRIX_2D               : [0, 1, 6],
                OTC_TRADE               : [5],
                POSITION                : [5],
                STRING                  : [0, 8, 6],
                SURFACE_DATA            : [2, 1, 0, 6],
                UNPLOTTABLE_SURFACE_DATA: [1, 6, 0],
                TENOR                   : [0, 6],
                TIME_SERIES             : [4, 1, 7, 6, 0],
                UNKNOWN                 : [0, 6],
                VECTOR                  : [0, 1, 6]
            },
            depgraph_blacklist: ['DOUBLE', 'STRING', 'TENOR', 'UNKNOWN'],
            gadgets: [
                'Depgraph', 'Data', 'Surface', 'Curve', 'Timeseries', 'ExpandedPositions', 'Log', 'Histogram', 'Text'
            ],
            panel_preference: {
                'south'      : [0, 2, 4, 3, 1, 5, 6, 8],
                'dock-north' : [2, 4, 3, 1, 0, 5, 6, 8],
                'dock-center': [2, 4, 3, 1, 0, 5, 6, 8],
                'dock-south' : [2, 4, 3, 1, 0, 5, 6, 8],
                'new-window' : [2, 4, 3, 1, 0, 5, 6, 8],
                'inplace'    : [2, 4, 3, 1, 0, 5, 8, 6]
            },
            available_types: available_types,
            is_complex: is_complex,
            options: options,
            type: type
        };
    }
});
