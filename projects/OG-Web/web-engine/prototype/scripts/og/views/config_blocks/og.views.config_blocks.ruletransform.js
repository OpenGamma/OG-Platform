/*
 * Copyright 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.config_blocks.RuleTransform',
    dependencies: ['og.common.util.ui.Form'],
    obj: function () {
        var module = this, Block = og.common.util.ui.Block;
        var RuleTransform = function (config) {
            var block = this, id = og.common.id('ruletransform'), data_index = config.index, form = config.form,
                value = config.value, data = config.data, $rules, $rule, extras = {
                    id: id, class_value: (data && data[0]) ||
                        'com.opengamma.engine.function.resolver.IdentityResolutionRuleTransform'
                };
            var processor = function (data) {
                if (!$('#' + id).length) return;
                var indices = data_index.split('.'), last = indices.pop(), result = {};
                $('#' + id + ' .og-js-res-rule').each(function (idx, el) {
                    var $el = $(el), key = $el.find('.og-js-key').val(),
                        val = $(el).find('.og-js-val').val() || null;
                    if (key) result[key] = val === 'null' ? null : val;
                });
                result[0] = $('#' + id + ' .og-js-class').val();
                indices.reduce(function (acc, level) {
                    return acc[level] && typeof acc[level] === 'object' ? acc[level] : (acc[level] = {});
                }, data)[last] = result;
            };
            var render_rule = function (key, value) {
                var $el = $rule.clone();
                $el.find('.og-js-key').val(key);
                $el.find('.og-js-val').val(value);
                $rules.append($el);
            };
            form.Block.call(block, {extras: extras, processor: processor}); // assign a Block instance to this (block)
            block.on('form:load', function () {
                $rules = $('#' + id + ' .og-js-res-rule-holder');
                $rule = $('#' + id + ' .og-js-res-rule').remove();
                if (data) Object.keys(data).forEach(function (rule) {if (rule !== '0') render_rule(rule, data[rule]);});
            }).on('click', '#' + id + ' .og-js-add-rule', function () {
                return render_rule('', null), false;
            }).on('click', '#' + id + ' .og-js-rem-rule', function (event) {
                return $(event.target).parents('.og-js-res-rule:first').remove(), false;
            });
        };
        RuleTransform.prototype = new Block(null, { // inherit Block prototype
            module: 'og.views.forms.view-definition-resolution-rule-transform-fields_tash'
        });
        return RuleTransform;
    }
});