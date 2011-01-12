/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.as.controller.registry;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ATTRIBUTES;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import org.jboss.as.controller.OperationHandler;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.dmr.ModelNode;

final class ConcreteNodeRegistration extends AbstractNodeRegistration {

    private volatile Map<String, NodeSubregistry> children;

    private volatile Map<String, OperationEntry> operations;

    private volatile DescriptionProvider descriptionProvider;

    private volatile Set<String> attributeNames;

    private static final AtomicMapFieldUpdater<ConcreteNodeRegistration, String, NodeSubregistry> childrenUpdater = AtomicMapFieldUpdater.newMapUpdater(AtomicReferenceFieldUpdater.newUpdater(ConcreteNodeRegistration.class, Map.class, "children"));
    private static final AtomicMapFieldUpdater<ConcreteNodeRegistration, String, OperationEntry> operationsUpdater = AtomicMapFieldUpdater.newMapUpdater(AtomicReferenceFieldUpdater.newUpdater(ConcreteNodeRegistration.class, Map.class, "operations"));
    private static final AtomicReferenceFieldUpdater<ConcreteNodeRegistration, DescriptionProvider> descriptionProviderUpdater = AtomicReferenceFieldUpdater.newUpdater(ConcreteNodeRegistration.class, DescriptionProvider.class, "descriptionProvider");

    ConcreteNodeRegistration(final String valueString, final NodeSubregistry parent, final DescriptionProvider provider) {
        super(valueString, parent);
        childrenUpdater.clear(this);
        operationsUpdater.clear(this);
        descriptionProviderUpdater.set(this, provider);
    }

    @Override
    public ModelNodeRegistration registerSubModel(final PathElement address, final DescriptionProvider descriptionProvider) {
        if (address == null) {
            throw new IllegalArgumentException("address is null");
        }
        if (descriptionProvider == null) {
            throw new IllegalArgumentException("descriptionProvider is null");
        }
        final String key = address.getKey();
        final NodeSubregistry child = getOrCreateSubregistry(key);
        return child.register(address.getValue(), descriptionProvider);
    }

    @Override
    OperationHandler getHandler(ListIterator<PathElement> iterator, String operationName) {
        final OperationEntry entry = operationsUpdater.get(this, operationName);
        if (entry != null && entry.isInherited()) {
            return entry.getOperationHandler();
        }
        if (! iterator.hasNext()) {
            return entry == null ? null : entry.getOperationHandler();
        }
        final PathElement next = iterator.next();
        final String key = next.getKey();
        final Map<String, NodeSubregistry> snapshot = childrenUpdater.get(this);
        final NodeSubregistry subregistry = snapshot.get(key);
        return subregistry == null ? null : subregistry.getHandler(iterator, next.getValue(), operationName);
    }

    @Override
    Map<String, DescriptionProvider> getOperationDescriptions(ListIterator<PathElement> iterator) {
        if (! iterator.hasNext()) {
            final HashMap<String, DescriptionProvider> map = new HashMap<String, DescriptionProvider>();
            for (Map.Entry<String, OperationEntry> entry : operationsUpdater.get(this).entrySet()) {
                map.put(entry.getKey(), entry.getValue().getDescriptionProvider());
            }
            return map;
        }
        final PathElement next = iterator.next();
        try {
            final String key = next.getKey();
            final Map<String, NodeSubregistry> snapshot = childrenUpdater.get(this);
            final NodeSubregistry subregistry = snapshot.get(key);
            return subregistry == null ? Collections.<String, DescriptionProvider>emptyMap() : subregistry.getHandlers(iterator, next.getValue());
        } finally {
            iterator.previous();
        }
    }

    @Override
    public void registerOperationHandler(final String operationName, final OperationHandler handler, final DescriptionProvider descriptionProvider, final boolean inherited) {
        if (operationsUpdater.putIfAbsent(this, operationName, new OperationEntry(handler, descriptionProvider, inherited)) != null) {
            throw new IllegalArgumentException("A handler named '" + operationName + "' is already registered at location '" + getLocationString() + "'");
        }
    }

    @Override
    public void registerProxySubModel(final PathElement address, final OperationHandler handler) throws IllegalArgumentException {
        getOrCreateSubregistry(address.getKey()).registerProxySubModel(address.getValue(), handler);
    }

    NodeSubregistry getOrCreateSubregistry(String key) {
        for (;;) {
            final Map<String, NodeSubregistry> snapshot = childrenUpdater.get(this);
            final NodeSubregistry subregistry = snapshot.get(key);
            if (subregistry != null) {
                return subregistry;
            } else {
                final NodeSubregistry newRegistry = new NodeSubregistry(key, this);
                final NodeSubregistry appearing = childrenUpdater.putAtomic(this, key, newRegistry, snapshot);
                if (appearing == null) {
                    return newRegistry;
                } else if (appearing != newRegistry) {
                    // someone else added one
                    return appearing;
                }
                // otherwise, retry the loop because the map changed
            }
        }
    }

    void registerProxySubModel(final ListIterator<PathElement> iterator, final OperationHandler handler) {
        if (! iterator.hasNext()) {
            throw new IllegalArgumentException("Handlers have already been registered at location '" + getLocationString() + "'");
        }
        final PathElement element = iterator.next();
        getOrCreateSubregistry(element.getKey()).registerProxySubModel(element.getValue(), handler);
    }

    @Override
    DescriptionProvider getOperationDescription(final Iterator<PathElement> iterator, final String operationName) {
        if (iterator.hasNext()) {
            final PathElement next = iterator.next();
            final NodeSubregistry subregistry = children.get(next.getKey());
            if (subregistry == null) {
                return null;
            }
            return subregistry.getOperationDescription(iterator, next.getValue(), operationName);
        } else {
            final OperationEntry entry = operations.get(operationName);
            return entry == null ? null : entry.getDescriptionProvider();
        }
    }

    @Override
    DescriptionProvider getModelDescription(final Iterator<PathElement> iterator) {
        if (iterator.hasNext()) {
            final PathElement next = iterator.next();
            final NodeSubregistry subregistry = children.get(next.getKey());
            if (subregistry == null) {
                return null;
            }
            return subregistry.getModelDescription(iterator, next.getValue());
        } else {
            return descriptionProvider;
        }
    }

    @Override
    Set<String> getAttributeNames(Iterator<PathElement> iterator) {
        if (iterator.hasNext()) {
            final PathElement next = iterator.next();
            final NodeSubregistry subregistry = children.get(next.getKey());
            if (subregistry == null) {
                return Collections.emptySet();
            }
            return subregistry.getAttributeNames(iterator, next.getValue());
        } else {
            Set<String> attrs = attributeNames;
            if (attrs == null) {
                if (descriptionProvider != null) {
                    ModelNode node = descriptionProvider.getModelDescription(null);
                    node = node.get(ATTRIBUTES);
                    if (node.isDefined()) {
                        attrs = node.keys();
                    }
                    attrs = Collections.unmodifiableSet(attrs);
                }
                if (attrs == null) {
                    attrs = Collections.emptySet();
                }
                attributeNames = Collections.unmodifiableSet(attrs);
            }
            return attrs;
        }
    }

    @Override
    Set<String> getChildNames(Iterator<PathElement> iterator) {
        if (iterator.hasNext()) {
            final PathElement next = iterator.next();
            final NodeSubregistry subregistry = children.get(next.getKey());
            if (subregistry == null) {
                return Collections.emptySet();
            }
            return subregistry.getChildNames(iterator, next.getValue());
        } else {
            Map<String, NodeSubregistry> children = this.children;
            if (children != null) {
                return Collections.unmodifiableSet(children.keySet());
            }
            return Collections.emptySet();
        }
    }



    private static final class OperationEntry {
        private final OperationHandler operationHandler;
        private final DescriptionProvider descriptionProvider;
        private final boolean inherited;

        private OperationEntry(final OperationHandler operationHandler, final DescriptionProvider descriptionProvider, final boolean inherited) {
            this.operationHandler = operationHandler;
            this.descriptionProvider = descriptionProvider;
            this.inherited = inherited;
        }

        OperationHandler getOperationHandler() {
            return operationHandler;
        }

        DescriptionProvider getDescriptionProvider() {
            return descriptionProvider;
        }

        boolean isInherited() {
            return inherited;
        }
    }
}

