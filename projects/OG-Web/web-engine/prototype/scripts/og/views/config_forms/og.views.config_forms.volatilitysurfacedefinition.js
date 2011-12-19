/*
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.config_forms.volatilitysurfacedefinition',
    dependencies: [
        'og.api',
        'og.common.util.ui'
    ],
    obj: function () {
        var module = this, Form = og.common.util.ui.Form,
            INDX = '<INDEX>',
            type_map = [
                ['0',                                           Form.type.STR],
                ['currency',                                    Form.type.STR],
                ['name',                                        Form.type.STR],
                [['target', '0'].join('.'),                     Form.type.STR],
                [['target', '0', INDX].join('.'),               Form.type.STR],
                [['target', 'Scheme'].join('.'),                Form.type.STR],
                [['target', 'Value'].join('.'),                 Form.type.STR],
                [['target', 'currency'].join('.'),              Form.type.STR],
                [['target', 'currency1'].join('.'),             Form.type.STR],
                [['target', 'currency2'].join('.'),             Form.type.STR],
                [['xs', INDX].join('.'),                        Form.type.BYT],
                [['xs', INDX, '0'].join('.'),                   Form.type.STR],
                [['xs', INDX, 'date'].join('.'),                Form.type.STR],
                [['xs', INDX, 'tenor'].join('.'),               Form.type.STR],
                [['ys', INDX].join('.'),                        Form.type.DBL],
                [['ys', INDX, '0', INDX].join('.'),             Form.type.STR],
                [['ys', INDX, 'first', '0', INDX].join('.'),    Form.type.STR],
                [['ys', INDX, 'first', 'value'].join('.'),      Form.type.BYT],
                [['ys', INDX, '0'].join('.'),                   Form.type.STR],
                [['ys', INDX, 'tenor'].join('.'),               Form.type.STR],
                [['ys', INDX, 'second', '0'].join('.'),         Form.type.STR],
                [['ys', INDX, 'second', '1'].join('.'),         Form.type.STR]
            ].reduce(function (acc, val) {return acc[val[0]] = val[1], acc;}, {});
        return og.views.config_forms['default'].preload({type_map: type_map});
        /* dead code below */
        return function (config) {
            var selector = config.selector,
                form = new Form({
                    module: 'og.views.forms.volatility-surface-definition',
                    type_map: type_map,
                    selector: selector
                });
            form.dom();
        };
    }
});