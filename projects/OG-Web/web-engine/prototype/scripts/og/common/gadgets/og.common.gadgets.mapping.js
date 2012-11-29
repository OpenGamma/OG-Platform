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
            'Depgraph': 'Dependency Graph',
            'Surface': 'Surface',
            'Timeseries': 'Time Series',
            'ExpandedPositions': 'Position'
        };
        return mapping = {
            gadgets: ['Depgraph', 'Data', 'Surface', 'Curve', 'Timeseries', 'ExpandedPositions'],
            panel_preference: {
                'south'      : [0, 2, 4, 3, 1, 5],
                'dock-north' : [2, 4, 3, 1, 0, 5],
                'dock-center': [2, 4, 3, 1, 0, 5],
                'dock-south' : [2, 4, 3, 1, 0, 5],
                'new-window' : [2, 4, 3, 1, 0, 5],
                'inplace'    : [2, 4, 3, 1, 0, 5]
            },
            options: function (cell, grid, panel, override) {
                var gadget_type, source = $.extend({}, grid.source), gadget_options,
                    override_gadget = override || '';
                gadget_type = override_gadget.length ? override_gadget : mapping.type(cell, panel);
                gadget_options = {
                    gadget: 'og.common.gadgets.' + gadget_type,
                    options: {source: source, child: true},
                    row_name: cell.row_name,
                    col_name: cell.col_name,
                    gadget_name: gadget_names[gadget_type],
                    gadget_type: gadget_type,
                    data_type: cell.type
                };
                if (gadget_type === 'Data' || gadget_type === 'Curve' || gadget_type === 'Surface')
                    $.extend(gadget_options.options, {col: cell.col, row: cell.row});
                if (gadget_type === 'ExpandedPositions') {
                    $.extend(gadget_options.options, {
                        col: cell.col, row: cell.row,
                        id: cell.value.positionId, editable: false, external_links: true, child: true
                    });
                }
                if (gadget_type === 'Depgraph') $.extend(source, {depgraph: true, col: cell.col, row: cell.row});
                if (gadget_type === 'Timeseries') $.extend(gadget_options.options,
                    {menu: false, datapoints_link: false, col: cell.col, row: cell.row});
                return gadget_options;
            },
            type: function (cell, panel) {
                var order = mapping.panel_preference[panel || 'new-window'],
                    type_map = mapping.data_type_map[cell.type], i, k;
                for (i = 0; i < order.length; i++)
                    for (k = 0; k < type_map.length; k++)
                        if (order[i] === type_map[k]) return mapping.gadgets[order[i]];
            },
            available_types : function (data_type){
                var types_array = mapping.data_type_map[data_type], i,  types = {gadgets:[]};
                for (i = 0; i < types_array.length; i++){
                    var current = mapping.gadgets[types_array[i]],
                    gadget = {name: current, gadget_name: gadget_names[current]};
                    types.gadgets.push(gadget);
                }
                return types;
            },
            depgraph_blacklist: ["DOUBLE", "PRIMITIVE", "TENOR", "UNKNOWN"],
            data_type_map: {
                CURVE                   : [1, 3],
                DOUBLE                  : [0],
                LABELLED_MATRIX_1D      : [0, 1],
                LABELLED_MATRIX_2D      : [0, 1],
                LABELLED_MATRIX_3D      : [0, 1],
                PRIMITIVE               : [5],
                SURFACE_DATA            : [2, 1, 0],
                UNPLOTTABLE_SURFACE_DATA: [1],
                TENOR                   : [0],
                TIME_SERIES             : [4, 1],
                UNKNOWN                 : [0],
                VECTOR                  : [0, 1]
            }
        };
    }
});
