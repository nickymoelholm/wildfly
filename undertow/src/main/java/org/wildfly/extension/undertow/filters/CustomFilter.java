/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.wildfly.extension.undertow.filters;

import io.undertow.server.HttpHandler;

import java.util.Arrays;
import java.util.Collection;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;

/**
 * @author <a href="mailto:moelholm@gmail.com">Nicky Moelholm</a>
 */
public class CustomFilter extends Filter {

    public static final CustomFilter INSTANCE = new CustomFilter();

    private static final AttributeDefinition CLASS = new SimpleAttributeDefinitionBuilder("class", ModelType.STRING)
            .setAllowNull(false).setAllowExpression(true).build();

    private static final AttributeDefinition MODULE = new SimpleAttributeDefinitionBuilder("module", ModelType.STRING)
            .setAllowNull(false).setAllowExpression(true).build();

    private CustomFilter() {
        super("custom");
    }

    @Override
    public Collection<AttributeDefinition> getAttributes() {
        return Arrays.asList(CLASS, MODULE);
    }

    @Override
    public Class<? extends HttpHandler> getHandlerClass(OperationContext context, ModelNode model) throws OperationFailedException {
        String className = CLASS.resolveModelAttribute(context, model).asString();
        String moduleName = MODULE.resolveModelAttribute(context, model).asString();
        try {
            ModuleIdentifier moduleId = ModuleIdentifier.create(moduleName);
            Module module = Module.getCallerModuleLoader().loadModule(moduleId);
            return  module.getClassLoader().loadClass(className)
                    .asSubclass(HttpHandler.class);
        } catch (Exception e) {
            throw new OperationFailedException(e, model);
        }
    }

}
