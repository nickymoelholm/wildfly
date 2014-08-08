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

package org.wildfly.extension.undertow.handlers;

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
import org.wildfly.extension.undertow.Constants;
import org.wildfly.extension.undertow.UndertowLogger;

/**
 * @author <a href="mailto:moelholm@gmail.com">Nicky Moelholm</a>
 */
public class CustomHandler extends Handler {

    public static final CustomHandler INSTANCE = new CustomHandler();

    /* <custom class="com.achme.some.UserHttpHandler" module="com.achme" /> */

    public static final AttributeDefinition CLASS = new SimpleAttributeDefinitionBuilder(Constants.CLASS, ModelType.STRING)
            .setAllowNull(false).setAllowExpression(true).build();

    public static final AttributeDefinition MODULE = new SimpleAttributeDefinitionBuilder(Constants.MODULE, ModelType.STRING)
            .setAllowNull(false).setAllowExpression(true).build();

    private CustomHandler() {
        super(Constants.CUSTOM);
    }

    @Override
    public Collection<AttributeDefinition> getAttributes() {
        return Arrays.asList(CLASS, MODULE);
    }

    @Override
    public HttpHandler createHandler(OperationContext context, ModelNode model) throws OperationFailedException {
        String className = CLASS.resolveModelAttribute(context, model).asString();
        String moduleName = MODULE.resolveModelAttribute(context, model).asString();

        UndertowLogger.ROOT_LOGGER.creatingCustomHandler(className, moduleName);

        try {
            ModuleIdentifier moduleId = ModuleIdentifier.create(moduleName);
            Module module = Module.getCallerModuleLoader().loadModule(moduleId);
            Class<? extends HttpHandler> handlerClass = module.getClassLoader().loadClass(className)
                    .asSubclass(HttpHandler.class);
            return handlerClass.newInstance();
        } catch (Exception e) {
            throw new OperationFailedException(e, model);
        }
    }
}
