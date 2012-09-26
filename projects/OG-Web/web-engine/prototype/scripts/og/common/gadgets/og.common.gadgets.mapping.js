/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.gadgets.mapping',
    dependencies: [],
    obj: function () {
        return {
            gadgets: ['depgraph', 'data', 'surface', 'curve', 'timeseries'],
            panel_preference: {
                'south'      : [0, 2, 4, 3, 1],
                'dock-north' : [2, 4, 3, 1, 0],
                'dock-center': [2, 4, 3, 1, 0],
                'dock-south' : [2, 4, 3, 1, 0],
                'new-window' : [2, 4, 3, 1, 0]
            },
            typemap: {
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
