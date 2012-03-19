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
            var id = prefix + id_count++,
                data_index = config.index, form = config.form, value = config.value,
                data = config.data, block_options, $rules, $rule, render_rule;
            render_rule = function (key, value) {
                var $el = $rule.clone();
                $el.find('.og-js-key').val(key);
                $el.find('.og-js-val').val(value);
                $rules.append($el);
            };
            block_options = {
                module: 'og.views.forms.view-definition-resolution-rule-transform-fields',
                extras: {
                    id: id,
                    class_value: (data && data[0]) ||
                        // default value
                        'com.opengamma.engine.function.resolver.IdentityResolutionRuleTransform'
                },
                processor: function (data) {
                    if (!$('#' + id).length) return;
                    var indices = data_index.split('.'), last = indices.pop(), result = {};
                    $('#' + id + ' .og-js-res-rule').each(function (idx, el) {
                        var $el = $(el),
                            key = $el.find('.og-js-key').val(),
                            val = $(el).find('.og-js-val').val() || null;
                        if (key) result[key] = val === 'null' ? null : val;
                    });
                    result[0] = $('#' + id + ' .og-js-class').val();
                    indices.reduce(function (acc, level) {
                        return acc[level] && typeof acc[level] === 'object' ? acc[level] : (acc[level] = {});
                    }, data)[last] = result;
                },
                handlers: [
                    {type: 'form:load', handler: function () {
                        var rule;
                        $rules = $('#' + id + ' .og-js-res-rule-holder');
                        $rule = $('#' + id + ' .og-js-res-rule').remove();
                        for (rule in data) if (rule !== '0') render_rule(rule, data[rule]);
                    }},
                    {type: 'click', selector: '#' + id + ' .og-js-add-rule', handler: function () {
                        render_rule('', null);
                        return false;
                    }},
                    {type: 'click', selector: '#' + id + ' .og-js-rem-rule', handler: function (e) {
                        $(e.target).parents('.og-js-res-rule:first').remove();
                        return false;
                    }}
                ]
            };
            return new form.Block(block_options);
        };
    }
});