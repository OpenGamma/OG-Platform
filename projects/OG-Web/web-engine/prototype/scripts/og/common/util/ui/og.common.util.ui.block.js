/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */

$.register_module({
    name: 'og.common.util.ui.Block',
    dependencies: ['og.api.text'],
    obj: function () {
        var module = this, STALL = 500 /* 500ms */, api_text = og.api.text;
        /** @private */
        var template_gen = function (name, html) {return !name || !html ? false : Handlebars.compile(html);};
        /** @class og.common.util.ui.Block */
        var Block = function (form, config) {
            var block = this, config = block.config = config || {};
            block.children = config.children || [];
            block.form = form;
            if (block.template === null) $.when(config.module ? api_text({module: config.module}) : void 0)
                .then(function (result) {block.template = template_gen(config.module, result);});
        };
        Block.prototype.add = function () {
            var block = this;
            Array.prototype.slice.call(arguments)
                .forEach(function (child) {block.children.push(new Block(block.form, child));});
            return block;
        };
        Block.prototype.html = function (handler) {
            var block = this, template = block.template, total = block.children.length, done = 0, result = [],
                generator = block.config.generator;
            /** @private */
            var internal_handler = function () {
                handler(template ? template($.extend(
                    result.reduce(function (acc, val, idx) {return acc['item_' + idx] = val, acc;}, {}),
                    block.config.extras
                )) : wrap(result.join('')));
            };
            /** @private */
            var wrap = function (html) {
                return block.config.wrap ? Handlebars.compile(block.config.wrap)({html: html}) : html;
            };
            if (block.config.content) return handler(block.config.content), block;
            if (template === null) return setTimeout(function () {block.html(handler);}, STALL), block;
            if (generator) return generator(handler, template), block;
            if (!total) return internal_handler(), block;
            block.children.forEach(function (val, idx) {
                if (val.html)
                    return val.html(function (html) {(result[idx] = html), (total === ++done) && internal_handler();});
                result[idx] = val;
                if (total === ++done) internal_handler();
            });
            return block;
        };
        Block.prototype.load = function () {
            var block = this, form = block.form;
            if (form.events['form:load']) // mimic a form load event
                form.events['form:load'].forEach(function (val) {if (val.origin === block) val.handler();});
            block.children.forEach(function (child) {if (child.load) child.load();});
        };
        Block.prototype.off = function () {
            var block = this;
            return block.form ? block.form.off.apply(block, Array.prototype.slice.call(arguments)) : block;
        };
        Block.prototype.on = function () {
            var block = this;
            return block.form ? block.form.on.apply(block, Array.prototype.slice.call(arguments)) : block;
        };
        Block.prototype.process = function (data, errors) {
            var block = this, process = block.config.procesor;
            block.children.forEach(function (child) {if (child.process) child.process(data, errors);});
            try {if (processor) processor(data);} catch (error) {errors.push(error);}
        };
        Block.prototype.template = null;
        return Block;
    }
});