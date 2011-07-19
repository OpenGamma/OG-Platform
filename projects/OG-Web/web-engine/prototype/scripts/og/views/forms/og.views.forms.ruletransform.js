/*
 * @copyright 2011 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.forms.RuleTransform',
    dependencies: ['og.common.util.ui.Form'],
    obj: function () {
        var module = this, id_count = 0, prefix = 'ruletransform_widget_';
        return function (config) {
            var data_index = config.index, form = config.form, value = config.value, block_options = {
                module: 'og.views.forms.view-definition-resolution-rule-transform-fields'
            };
            return new form.Block(block_options);
        };
    }
});