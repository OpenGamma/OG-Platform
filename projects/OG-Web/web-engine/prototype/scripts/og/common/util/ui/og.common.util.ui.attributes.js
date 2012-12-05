/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.util.ui.Attributes',
    dependencies: ['og.common.util.ui.Form'],
    obj: function () {
        var module = this, Block = og.common.util.ui.Block, add_list = '.og-attributes-add-list',
        attribute = Handlebars.compile('<li><div class="og-del og-js-rem"></div>{{{key}}} = {{{value}}}</li>');
        var Attributes = function (config) {
            var block = this, id = og.common.id('attributes'), form = config.form;
            form.Block.call(block, {module: 'og.views.forms.attributes_tash', extras: {id: id}});
            block.on('click', '#' + id + ' ' + add_list + ' .og-js-rem', function (event) {
                $(event.target).parent().remove();
            }).on('click', '#' + id + ' .og-js-add-attribute', function (event) {
                event.preventDefault();
                var $group = $(event.target).parent(), key = $group.find('[name=attr_key]').val(),
                    value = $group.find('[name=attr_val]').val();
                if (!key || !value) return;
                $(add_list).prepend(attribute({key: key, value: value}));
                $group.find('[name^=attr]').val('');
            });
        };
        Attributes.prototype = new Block; // inherit Block prototype
        return Attributes;
    }
});