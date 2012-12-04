/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.util.ui.Attributes',
    dependencies: ['og.common.util.ui.Form'],
    obj: function () {
        var module = this, Block = og.common.util.ui.Block, add_list = '.og-attributes-add-list',
        attribute = '<li><div class="og-del og-js-rem"></div>{KEY} = {VALUE}</li>';
        var Attributes = function (config) {
            var block = this, attr, form = config.form;
            attr = {module: 'og.views.forms.attributes_tash'};
            form.Block.call(block, attr);
            block.on('form:load', function () {              
                $(add_list).find('.og-js-rem').on('click').click(function (e) {$(e.target).parent().remove();});    
            }).on('click', '.og-js-add-attribute', function (e) {
                    e.preventDefault();
                    var $group = $(e.target).parent();
                    if (!$group.find('[name=attr_key]').val() || !$group.find('[name=attr_val]').val()) return;
                    $(add_list).prepend(attribute
                        .replace('{KEY}', $group.find('[name=attr_key]').val())
                        .replace('{VALUE}', $group.find('[name=attr_val]').val())
                    );
                    $group.find('[name^=attr]').val('');
                    block.load();
            });
        };
        Attributes.prototype = new Block(); // inherit Block prototype
        return Attributes;
    }
});