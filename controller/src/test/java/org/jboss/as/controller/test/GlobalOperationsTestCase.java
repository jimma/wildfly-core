/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.as.controller.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADDRESS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ATTRIBUTES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.CHILDREN;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MIN_LENGTH;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MIN_OCCURS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MODEL_DESCRIPTION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OPERATIONS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OPERATION_NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PROFILE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_OPERATION_DESCRIPTION_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_OPERATION_NAMES_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_RESOURCE_DESCRIPTION_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RECURSIVE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUEST_PROPERTIES;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REQUIRED;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.TYPE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE_TYPE;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.jboss.as.controller.BasicModelController;
import org.jboss.as.controller.Cancellable;
import org.jboss.as.controller.NewOperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationHandler;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ResultHandler;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.common.GlobalDescriptions;
import org.jboss.as.controller.operations.global.GlobalOperationHandlers;
import org.jboss.as.controller.registry.ModelNodeRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.dmr.Property;
import org.junit.Test;

/**
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class GlobalOperationsTestCase {

    private static final ModelNode MODEL = createTestNode();
    private static final TestModelController CONTROLLER = new TestModelController();

    public static ModelNode createTestNode() {
        ModelNode model = new ModelNode();

        //Atttributes
        model.get("profile", "profileA", "subsystem", "subsystem1", "attr1").add(1);
        model.get("profile", "profileA", "subsystem", "subsystem1", "attr1").add(2);
        //Children
        model.get("profile", "profileA", "subsystem", "subsystem1", "type1", "thing1", "name").set("Name11");
        model.get("profile", "profileA", "subsystem", "subsystem1", "type1", "thing1", "value").set("201");
        model.get("profile", "profileA", "subsystem", "subsystem1", "type1", "thing2", "name").set("Name12");
        model.get("profile", "profileA", "subsystem", "subsystem1", "type1", "thing2", "value").set("202");
        model.get("profile", "profileA", "subsystem", "subsystem1", "type2", "other", "name").set("Name2");


        model.get("profile", "profileA", "subsystem", "subsystem2", "bigdecimal").set(new BigDecimal(100));
        model.get("profile", "profileA", "subsystem", "subsystem2", "biginteger").set(new BigInteger("101"));
        model.get("profile", "profileA", "subsystem", "subsystem2", "boolean").set(true);
        model.get("profile", "profileA", "subsystem", "subsystem2", "bytes").set(new byte[] {1, 2, 3});
        model.get("profile", "profileA", "subsystem", "subsystem2", "double").set(Double.MAX_VALUE);
        model.get("profile", "profileA", "subsystem", "subsystem2", "expression").setExpression("{expr}");
        model.get("profile", "profileA", "subsystem", "subsystem2", "int").set(102);
        model.get("profile", "profileA", "subsystem", "subsystem2", "list").add("l1A");
        model.get("profile", "profileA", "subsystem", "subsystem2", "list").add("l1B");
        model.get("profile", "profileA", "subsystem", "subsystem2", "long").set(Long.MAX_VALUE);
        model.get("profile", "profileA", "subsystem", "subsystem2", "object", "value").set("objVal");
        model.get("profile", "profileA", "subsystem", "subsystem2", "property").set(new Property("prop1", new ModelNode().set("value1")));
        model.get("profile", "profileA", "subsystem", "subsystem2", "string1").set("s1");
        model.get("profile", "profileA", "subsystem", "subsystem2", "string2").set("s2");
        model.get("profile", "profileA", "subsystem", "subsystem2", "type").set(ModelType.TYPE);


        model.get("profile", "profileB", "name").set("Profile B");

        model.get("profile", "profileC", "subsystem", "subsystem4");
        model.get("profile", "profileC", "subsystem", "subsystem5", "name").set("Test");

        return model;
    }

    @Test
    public void testRecursiveReadSubModelOperationSimple() throws Exception {
        ModelNode operation = createOperation(READ_RESOURCE_OPERATION, "profile", "profileA", "subsystem", "subsystem1");
        operation.get(RECURSIVE).set(true);

        ModelNode result = CONTROLLER.execute(operation);
        assertNotNull(result);
        checkRecursiveSubSystem1(result);
    }

    @Test
    public void testNonRecursiveReadSubModelOperationSimple() throws Exception {
        ModelNode operation = createOperation(READ_RESOURCE_OPERATION, "profile", "profileA", "subsystem", "subsystem1");
        operation.get(RECURSIVE).set(false);

        ModelNode result = CONTROLLER.execute(operation);
        assertNotNull(result);

        assertEquals(3, result.keys().size());
        ModelNode content = result.require("attr1");
        List<ModelNode> list = content.asList();
        assertEquals(2, list.size());
        assertEquals(1, list.get(0).asInt());
        assertEquals(2, list.get(1).asInt());
        assertEquals(2, result.require("type1").keys().size());
        assertTrue(result.require("type1").has("thing1"));
        assertFalse(result.require("type1").get("thing1").isDefined());
        assertTrue(result.require("type1").has("thing2"));
        assertFalse(result.require("type1").get("thing2").isDefined());
        assertEquals(1, result.require("type2").keys().size());
        assertTrue(result.require("type2").has("other"));
        assertFalse(result.require("type2").get("other").isDefined());
    }

    @Test
    public void testRecursiveReadSubModelOperationComplex() throws Exception {
        ModelNode operation = createOperation(READ_RESOURCE_OPERATION, "profile", "profileA", "subsystem", "subsystem2");
        operation.get(RECURSIVE).set(true);


        ModelNode result = CONTROLLER.execute(operation);
        assertNotNull(result);
        checkRecursiveSubsystem2(result);
    }

    @Test
    public void testReadAttributeValue() throws Exception {
        ModelNode operation = createOperation(READ_ATTRIBUTE_OPERATION, "profile", "profileA", "subsystem", "subsystem2");

        operation.get(NAME).set("int");
        ModelNode result = CONTROLLER.execute(operation);
        assertNotNull(result);
        assertEquals(ModelType.INT, result.getType());
        assertEquals(102, result.asInt());

        operation.get(NAME).set("string1");
        result = CONTROLLER.execute(operation);
        assertNotNull(result);
        assertEquals(ModelType.STRING, result.getType());
        assertEquals("s1", result.asString());

        operation.get(NAME).set("list");
        result = CONTROLLER.execute(operation);
        assertNotNull(result);
        assertEquals(ModelType.LIST, result.getType());
        List<ModelNode> list = result.asList();
        assertEquals(2, list.size());
        assertEquals("l1A", list.get(0).asString());
        assertEquals("l1B", list.get(1).asString());

        operation.get(NAME).set("non-existant-attribute");
        try {
            result = CONTROLLER.execute(operation);
            fail("Expected error for non-existant attribute");
        } catch (OperationFailedException expected) {
        }

        operation = createOperation(READ_ATTRIBUTE_OPERATION, "profile", "profileC", "subsystem", "subsystem4");
        operation.get(NAME).set("name");
        result = CONTROLLER.execute(operation);
        assertNotNull(result);
        assertFalse(result.isDefined());

        operation = createOperation(READ_ATTRIBUTE_OPERATION, "profile", "profileC", "subsystem", "subsystem5");
        operation.get(NAME).set("value");
        result = CONTROLLER.execute(operation);
        assertNotNull(result);
        assertFalse(result.isDefined());
    }

    @Test
    public void testReadChildrenNames() throws Exception {
        ModelNode operation = createOperation(READ_CHILDREN_NAMES_OPERATION, "profile", "profileA");
        operation.get(CHILD_TYPE).set("subsystem");

        ModelNode result = CONTROLLER.execute(operation);
        assertNotNull(result);
        assertEquals(ModelType.LIST, result.getType());
        assertEquals(2, result.asList().size());
        assertTrue(modelNodeListToStringList(result.asList()).contains("subsystem1"));
        assertTrue(modelNodeListToStringList(result.asList()).contains("subsystem2"));

        operation = createOperation(READ_CHILDREN_NAMES_OPERATION, "profile", "profileA", "subsystem", "subsystem1");
        operation.get(CHILD_TYPE).set("type2");
        result = CONTROLLER.execute(operation);
        assertNotNull(result);
        assertEquals(ModelType.LIST, result.getType());
        assertEquals(1, result.asList().size());
        assertTrue(modelNodeListToStringList(result.asList()).contains("other"));


        operation.get(CHILD_TYPE).set("non-existant-child");
        try {
            result = CONTROLLER.execute(operation);
            fail("Expected error for non-existant child");
        } catch (OperationFailedException expected) {
        }

        operation = createOperation(READ_CHILDREN_NAMES_OPERATION, "profile", "profileC", "subsystem", "subsystem4");
        operation.get(CHILD_TYPE).set("type1");
        result = CONTROLLER.execute(operation);
        assertNotNull(result);
        assertEquals(ModelType.LIST, result.getType());
        assertTrue(result.asList().isEmpty());

        operation = createOperation(READ_CHILDREN_NAMES_OPERATION, "profile", "profileC", "subsystem", "subsystem5");
        operation.get(CHILD_TYPE).set("type1");
        result = CONTROLLER.execute(operation);
        assertNotNull(result);
        assertEquals(ModelType.LIST, result.getType());
        assertTrue(result.asList().isEmpty());
    }

    @Test
    public void testReadOperationNamesOperation() throws Exception {
        ModelNode operation = createOperation(READ_OPERATION_NAMES_OPERATION, "profile", "profileA", "subsystem", "subsystem1");
        ModelNode result = CONTROLLER.execute(operation);

        assertEquals(ModelType.LIST, result.getType());
        assertEquals(2, result.asList().size());
        List<String> names = modelNodeListToStringList(result.asList());
        assertTrue(names.contains("testA1-1"));
        assertTrue(names.contains("testA1-2"));

        operation = createOperation(READ_OPERATION_NAMES_OPERATION, "profile", "profileA", "subsystem", "subsystem2");

        result = CONTROLLER.execute(operation);
        assertEquals(ModelType.LIST, result.getType());
        assertEquals(1, result.asList().size());
        names = modelNodeListToStringList(result.asList());
        assertTrue(names.contains("testA2"));

        operation = createOperation(READ_OPERATION_NAMES_OPERATION, "profile", "profileB");
        result = CONTROLLER.execute(operation);
        assertEquals(ModelType.LIST, result.getType());
        assertEquals(0, result.asList().size());

    }

    @Test
    public void testReadOperationDescriptionOperation() throws Exception {
        ModelNode operation = createOperation(READ_OPERATION_DESCRIPTION_OPERATION, "profile", "profileA", "subsystem", "subsystem1");

        operation.get(NAME).set("Nothing");
        ModelNode result = CONTROLLER.execute(operation);
        assertFalse(result.isDefined());

        operation.get(NAME).set("testA1-2");
        result = CONTROLLER.execute(operation);
        assertEquals(ModelType.OBJECT, result.getType());
        assertEquals("testA2", result.require(OPERATION_NAME).asString());
        assertEquals(ModelType.STRING, result.require(REQUEST_PROPERTIES).require("paramA2").require(TYPE).asType());
    }

    @Test
    public void testReadResourceDescriptionOperation() throws Exception {
        ModelNode operation = createOperation(READ_RESOURCE_DESCRIPTION_OPERATION);
        ModelNode result = CONTROLLER.execute(operation);
        checkRootNodeDescription(result, false, false);
        assertFalse(result.get(OPERATIONS).isDefined());

        operation = createOperation(READ_RESOURCE_DESCRIPTION_OPERATION, "profile", "profileA");
        result = CONTROLLER.execute(operation);
        checkProfileNodeDescription(result, false, false);

        //TODO this is not possible - the wildcard address does not correspond to anything in the real model
        //operation = createOperation(READ_RESOURCE_DESCRIPTION_OPERATION, "profile", "*");
        //result = CONTROLLER.execute(operation);
        //checkProfileNodeDescription(result, false);

        operation = createOperation(READ_RESOURCE_DESCRIPTION_OPERATION, "profile", "profileA", "subsystem", "subsystem1");
        result = CONTROLLER.execute(operation);
        checkSubsystem1Description(result, false, false);
        assertFalse(result.get(OPERATIONS).isDefined());

        operation = createOperation(READ_RESOURCE_DESCRIPTION_OPERATION, "profile", "profileA", "subsystem", "subsystem1", "type1", "thing1");
        result = CONTROLLER.execute(operation);
        checkType1Description(result);
        assertFalse(result.get(OPERATIONS).isDefined());

        operation = createOperation(READ_RESOURCE_DESCRIPTION_OPERATION, "profile", "profileA", "subsystem", "subsystem1", "type1", "thing2");
        result = CONTROLLER.execute(operation);
        checkType1Description(result);
        assertFalse(result.get(OPERATIONS).isDefined());

        operation = createOperation(READ_RESOURCE_DESCRIPTION_OPERATION, "profile", "profileA", "subsystem", "subsystem1", "type2", "other");
        result = CONTROLLER.execute(operation);
        checkType2Description(result);
        assertFalse(result.get(OPERATIONS).isDefined());
    }

    @Test
    public void testReadRecursiveResourceDescriptionOperation() throws Exception {
        ModelNode operation = createOperation(READ_RESOURCE_DESCRIPTION_OPERATION);
        operation.get(RECURSIVE).set(true);
        ModelNode result = CONTROLLER.execute(operation);
        checkRootNodeDescription(result, true, false);
        assertFalse(result.get(OPERATIONS).isDefined());

        operation = createOperation(READ_RESOURCE_DESCRIPTION_OPERATION, "profile", "profileA");
        operation.get(RECURSIVE).set(true);
        result = CONTROLLER.execute(operation);
        checkProfileNodeDescription(result, true, false);

        //TODO this is not possible - the wildcard address does not correspond to anything in the real model
        //operation = createOperation(READ_RESOURCE_DESCRIPTION_OPERATION, "profile", "*");
        //operation.get(RECURSIVE).set(true);
        //result = CONTROLLER.execute(operation);
        //checkProfileNodeDescription(result, false);

        operation = createOperation(READ_RESOURCE_DESCRIPTION_OPERATION, "profile", "profileA", "subsystem", "subsystem1");
        operation.get(RECURSIVE).set(true);
        result = CONTROLLER.execute(operation);
        checkSubsystem1Description(result, true, false);
        assertFalse(result.get(OPERATIONS).isDefined());

        operation = createOperation(READ_RESOURCE_DESCRIPTION_OPERATION, "profile", "profileA", "subsystem", "subsystem1", "type1", "thing1");
        operation.get(RECURSIVE).set(true);
        result = CONTROLLER.execute(operation);
        checkType1Description(result);
        assertFalse(result.get(OPERATIONS).isDefined());

        operation = createOperation(READ_RESOURCE_DESCRIPTION_OPERATION, "profile", "profileA", "subsystem", "subsystem1", "type1", "thing2");
        operation.get(RECURSIVE).set(true);
        result = CONTROLLER.execute(operation);
        checkType1Description(result);
        assertFalse(result.get(OPERATIONS).isDefined());

        operation = createOperation(READ_RESOURCE_DESCRIPTION_OPERATION, "profile", "profileA", "subsystem", "subsystem1", "type2", "other");
        operation.get(RECURSIVE).set(true);
        result = CONTROLLER.execute(operation);
        checkType2Description(result);
        assertFalse(result.get(OPERATIONS).isDefined());
    }

    @Test
    public void testReadResourceDescriptionWithOperationsOperation() throws Exception {
        ModelNode operation = createOperation(READ_RESOURCE_DESCRIPTION_OPERATION);
        operation.get(OPERATIONS).set(true);
        ModelNode result = CONTROLLER.execute(operation);
        checkRootNodeDescription(result, false, true);
        assertTrue(result.require(OPERATIONS).isDefined());
        Set<String> ops = result.require(OPERATIONS).keys();
        assertTrue(ops.contains(READ_ATTRIBUTE_OPERATION));
        assertTrue(ops.contains(READ_CHILDREN_NAMES_OPERATION));
        assertTrue(ops.contains(READ_OPERATION_DESCRIPTION_OPERATION));
        assertTrue(ops.contains(READ_OPERATION_NAMES_OPERATION));
        assertTrue(ops.contains(READ_RESOURCE_DESCRIPTION_OPERATION));
        assertTrue(ops.contains(READ_RESOURCE_OPERATION));
        for (String op : ops) {
            assertEquals(op, result.require(OPERATIONS).require(op).require(OPERATION_NAME).asString());
        }

        operation = createOperation(READ_RESOURCE_DESCRIPTION_OPERATION, "profile", "profileA", "subsystem", "subsystem1");
        operation.get(OPERATIONS).set(true);
        result = CONTROLLER.execute(operation);
        checkSubsystem1Description(result, false, true);

        operation = createOperation(READ_RESOURCE_DESCRIPTION_OPERATION, "profile", "profileA", "subsystem", "subsystem1", "type1", "thing1");
        operation.get(OPERATIONS).set(true);
        result = CONTROLLER.execute(operation);
        checkType1Description(result);

        operation = createOperation(READ_RESOURCE_DESCRIPTION_OPERATION, "profile", "profileA", "subsystem", "subsystem1", "type1", "thing2");
        operation.get(OPERATIONS).set(true);
        result = CONTROLLER.execute(operation);
        checkType1Description(result);

        operation = createOperation(READ_RESOURCE_DESCRIPTION_OPERATION, "profile", "profileA", "subsystem", "subsystem1", "type2", "other");
        operation.get(OPERATIONS).set(true);
        result = CONTROLLER.execute(operation);
        checkType2Description(result);
    }

    @Test
    public void testRecursiveReadResourceDescriptionWithOperationsOperation() throws Exception {
        ModelNode operation = createOperation(READ_RESOURCE_DESCRIPTION_OPERATION);
        operation.get(OPERATIONS).set(true);
        operation.get(RECURSIVE).set(true);
        ModelNode result = CONTROLLER.execute(operation);
        checkRootNodeDescription(result, true, true);


        operation = createOperation(READ_RESOURCE_DESCRIPTION_OPERATION, "profile", "profileA", "subsystem", "subsystem1");
        operation.get(OPERATIONS).set(true);
        operation.get(RECURSIVE).set(true);
        result = CONTROLLER.execute(operation);
        checkSubsystem1Description(result, true, true);

        operation = createOperation(READ_RESOURCE_DESCRIPTION_OPERATION, "profile", "profileA", "subsystem", "subsystem1", "type1", "thing1");
        operation.get(OPERATIONS).set(true);
        operation.get(RECURSIVE).set(true);
        result = CONTROLLER.execute(operation);
        checkType1Description(result);

        operation = createOperation(READ_RESOURCE_DESCRIPTION_OPERATION, "profile", "profileA", "subsystem", "subsystem1", "type1", "thing2");
        operation.get(OPERATIONS).set(true);
        operation.get(RECURSIVE).set(true);
        result = CONTROLLER.execute(operation);
        checkType1Description(result);

        operation = createOperation(READ_RESOURCE_DESCRIPTION_OPERATION, "profile", "profileA", "subsystem", "subsystem1", "type2", "other");
        operation.get(OPERATIONS).set(true);
        operation.get(RECURSIVE).set(true);
        result = CONTROLLER.execute(operation);
        checkType2Description(result);
    }



    private void checkRecursiveSubSystem1(ModelNode result) {
        assertEquals(3, result.keys().size());
        List<ModelNode> list = result.require("attr1").asList();
        assertEquals(2, list.size());
        assertEquals(1, list.get(0).asInt());
        assertEquals(2, list.get(1).asInt());
        assertEquals("Name11", result.require("type1").require("thing1").require("name").asString());
        assertEquals(201, result.require("type1").require("thing1").require("value").asInt());
        assertEquals("Name12", result.require("type1").require("thing2").require("name").asString());
        assertEquals(202, result.require("type1").require("thing2").require("value").asInt());
        assertEquals("Name2", result.require("type2").require("other").require("name").asString());
    }

    private void checkRecursiveSubsystem2(ModelNode result) {
        assertEquals(14, result.keys().size());

        assertEquals(new BigDecimal(100), result.require("bigdecimal").asBigDecimal());
        assertEquals(new BigInteger("101"), result.require("biginteger").asBigInteger());
        assertTrue(result.require("boolean").asBoolean());
        assertEquals(3, result.require("bytes").asBytes().length);
        assertEquals(1, result.require("bytes").asBytes()[0]);
        assertEquals(2, result.require("bytes").asBytes()[1]);
        assertEquals(3, result.require("bytes").asBytes()[2]);
        assertEquals(Double.MAX_VALUE, result.require("double").asDouble());
        assertEquals("{expr}", result.require("expression").asString());
        assertEquals(102, result.require("int").asInt());
        List<ModelNode> list = result.require("list").asList();
        assertEquals(2, list.size());
        assertEquals("l1A", list.get(0).asString());
        assertEquals("l1B", list.get(1).asString());
        assertEquals(Long.MAX_VALUE, result.require("long").asLong());
        assertEquals("objVal", result.require("object").require("value").asString());
        Property prop = result.require("property").asProperty();
        assertEquals("prop1", prop.getName());
        assertEquals("value1", prop.getValue().asString());
        assertEquals("s1", result.require("string1").asString());
        assertEquals("s2", result.require("string2").asString());
        assertEquals(ModelType.TYPE, result.require("type").asType());
    }

    private void checkRootNodeDescription(ModelNode result, boolean recursive, boolean operations) {
        assertEquals("The root node of the test management API", result.require(DESCRIPTION).asString());
        assertEquals("A list of profiles", result.require(CHILDREN).require(PROFILE).require(DESCRIPTION).asString());
        assertEquals(1, result.require(CHILDREN).require(PROFILE).require(MIN_OCCURS).asInt());

        if (operations) {
            assertTrue(result.require(OPERATIONS).isDefined());
            Set<String> ops = result.require(OPERATIONS).keys();
            assertTrue(ops.contains(READ_ATTRIBUTE_OPERATION));
            assertTrue(ops.contains(READ_CHILDREN_NAMES_OPERATION));
            assertTrue(ops.contains(READ_OPERATION_DESCRIPTION_OPERATION));
            assertTrue(ops.contains(READ_OPERATION_NAMES_OPERATION));
            assertTrue(ops.contains(READ_RESOURCE_DESCRIPTION_OPERATION));
            assertTrue(ops.contains(READ_RESOURCE_OPERATION));
            for (String op : ops) {
                assertEquals(op, result.require(OPERATIONS).require(op).require(OPERATION_NAME).asString());
            }
        } else {
            assertFalse(result.get(OPERATIONS).isDefined());
        }


        if (!recursive) {
            assertFalse(result.require(CHILDREN).require(PROFILE).require(MODEL_DESCRIPTION).isDefined());
            return;
        }
        assertTrue(result.require(CHILDREN).require(PROFILE).require(MODEL_DESCRIPTION).isDefined());
        assertEquals(1, result.require(CHILDREN).require(PROFILE).require(MODEL_DESCRIPTION).keys().size());
        checkProfileNodeDescription(result.require(CHILDREN).require(PROFILE).require(MODEL_DESCRIPTION).require("*"), true, operations);
    }

    private void checkProfileNodeDescription(ModelNode result, boolean recursive, boolean operations) {
        assertEquals("A named set of subsystem configs", result.require(DESCRIPTION).asString());
        assertEquals(ModelType.STRING, result.require(ATTRIBUTES).require(NAME).require(TYPE).asType());
        assertEquals("The name of the profile", result.require(ATTRIBUTES).require(NAME).require(DESCRIPTION).asString());
        assertEquals(true, result.require(ATTRIBUTES).require(NAME).require(REQUIRED).asBoolean());
        assertEquals(1, result.require(ATTRIBUTES).require(NAME).require(MIN_LENGTH).asInt());
        assertEquals("The subsystems that make up the profile", result.require(CHILDREN).require(SUBSYSTEM).require(DESCRIPTION).asString());
        assertEquals(1, result.require(CHILDREN).require(SUBSYSTEM).require(MIN_OCCURS).asInt());
        if (!recursive) {
            assertFalse(result.require(CHILDREN).require(SUBSYSTEM).require(MODEL_DESCRIPTION).isDefined());
            return;
        }
        assertTrue(result.require(CHILDREN).require(SUBSYSTEM).require(MODEL_DESCRIPTION).isDefined());
        assertEquals(5, result.require(CHILDREN).require(SUBSYSTEM).require(MODEL_DESCRIPTION).keys().size());
        checkSubsystem1Description(result.require(CHILDREN).require(SUBSYSTEM).require(MODEL_DESCRIPTION).require("subsystem1"), recursive, operations);
    }

    private void checkSubsystem1Description(ModelNode result, boolean recursive, boolean operations) {
        assertNotNull(result);

        assertEquals("A test subsystem 1", result.require(DESCRIPTION).asString());
        assertEquals(ModelType.LIST, result.require(ATTRIBUTES).require("attr1").require(TYPE).asType());
        assertEquals(ModelType.INT, result.require(ATTRIBUTES).require("attr1").require(VALUE_TYPE).asType());
        assertEquals("The values", result.require(ATTRIBUTES).require("attr1").require(DESCRIPTION).asString());
        assertTrue(result.require(ATTRIBUTES).require("attr1").require(REQUIRED).asBoolean());
        assertEquals("The children1", result.require(CHILDREN).require("type1").require(DESCRIPTION).asString());
        assertEquals(1, result.require(CHILDREN).require("type1").require(MIN_OCCURS).asInt());

        assertEquals("The children1", result.require(CHILDREN).require("type1").require(DESCRIPTION).asString());
        assertEquals("The children2", result.require(CHILDREN).require("type2").require(DESCRIPTION).asString());
        assertEquals(1, result.require(CHILDREN).require("type2").require(MIN_OCCURS).asInt());
        assertEquals(1, result.require(CHILDREN).require("type2").require(MIN_OCCURS).asInt());

        if (operations) {
            assertTrue(result.require(OPERATIONS).isDefined());
            Set<String> ops = result.require(OPERATIONS).keys();
            //TODO should the inherited ops be picked up?
            assertEquals(2, ops.size());
            assertTrue(ops.contains("testA1-1"));
            assertTrue(ops.contains("testA1-2"));
        } else {
            assertFalse(result.get(OPERATIONS).isDefined());
        }

        if (!recursive) {
            assertFalse(result.require(CHILDREN).require("type1").require(MODEL_DESCRIPTION).isDefined());
            assertFalse(result.require(CHILDREN).require("type2").require(MODEL_DESCRIPTION).isDefined());
            return;
        }

        checkType1Description(result.require(CHILDREN).require("type1").require(MODEL_DESCRIPTION).require("*"));
        checkType2Description(result.require(CHILDREN).require("type2").require(MODEL_DESCRIPTION).require("other"));
    }

    private void checkType1Description(ModelNode result) {
        assertNotNull(result);
        assertEquals("A type 1", result.require(DESCRIPTION).asString());
        assertEquals(ModelType.STRING, result.require(ATTRIBUTES).require("name").require(TYPE).asType());
        assertEquals("The name of the thing", result.require(ATTRIBUTES).require("name").require(DESCRIPTION).asString());
        assertTrue(result.require(ATTRIBUTES).require("name").require(REQUIRED).asBoolean());
        assertEquals(ModelType.INT, result.require(ATTRIBUTES).require("value").require(TYPE).asType());
        assertEquals("The value of the thing", result.require(ATTRIBUTES).require("value").require(DESCRIPTION).asString());
        assertTrue(result.require(ATTRIBUTES).require("value").require(REQUIRED).asBoolean());
        //TODO should the inherited ops be picked up?
        if (result.has(OPERATIONS)) {
            assertTrue(result.get(OPERATIONS).asList().isEmpty());
        }

    }

    private void checkType2Description(ModelNode result) {
        assertNotNull(result);
        assertEquals("A type 2", result.require(DESCRIPTION).asString());
        assertEquals(ModelType.STRING, result.require(ATTRIBUTES).require("name").require(TYPE).asType());
        assertEquals("The name of the thing", result.require(ATTRIBUTES).require("name").require(DESCRIPTION).asString());
        assertTrue(result.require(ATTRIBUTES).require("name").require(REQUIRED).asBoolean());
        //TODO should the inherited ops be picked up?
        if (result.has(OPERATIONS)) {
            assertTrue(result.get(OPERATIONS).asList().isEmpty());
        }
    }

    private ModelNode createOperation(String operationName, String...address) {
        ModelNode operation = new ModelNode();
        operation.get(OPERATION_NAME).set(operationName);
        if (address.length > 0) {
            for (String addr : address) {
                operation.get(ADDRESS).add(addr);
            }
        } else {
            operation.get(ADDRESS).setEmptyList();
        }

        return operation;
    }

    private List<String> modelNodeListToStringList(List<ModelNode> nodes){
        List<String> result = new ArrayList<String>();
        for (ModelNode node : nodes) {
            result.add(node.asString());
        }
        return result;
    }

    private static ModelNode getProfileModelDescription() {
        ModelNode node = new ModelNode();
        node.get(DESCRIPTION).set("A named set of subsystem configs");
        node.get(ATTRIBUTES, NAME, TYPE).set(ModelType.STRING);
        node.get(ATTRIBUTES, NAME, DESCRIPTION).set("The name of the profile");
        node.get(ATTRIBUTES, NAME, REQUIRED).set(true);
        node.get(ATTRIBUTES, NAME, MIN_LENGTH).set(1);
        node.get(CHILDREN, SUBSYSTEM, DESCRIPTION).set("The subsystems that make up the profile");
        node.get(CHILDREN, SUBSYSTEM, MIN_OCCURS).set(1);
        node.get(CHILDREN, SUBSYSTEM, MODEL_DESCRIPTION);
        return node;
    }


    private static class TestModelController extends BasicModelController {
        protected TestModelController() {
            super(MODEL, null, new DescriptionProvider() {
                @Override
                public ModelNode getModelDescription(Locale locale) {
                    ModelNode node = new ModelNode();
                    node.get(DESCRIPTION).set("The root node of the test management API");
                    node.get(CHILDREN, PROFILE, DESCRIPTION).set("A list of profiles");
                    node.get(CHILDREN, PROFILE, MIN_OCCURS).set(1);
                    node.get(CHILDREN, PROFILE, MODEL_DESCRIPTION);
                    return node;
                }
            });

            getRegistry().registerOperationHandler(READ_RESOURCE_OPERATION, GlobalOperationHandlers.READ_RESOURCE, GlobalDescriptions.getReadResourceOperationDescription(), true);
            getRegistry().registerOperationHandler(READ_ATTRIBUTE_OPERATION, GlobalOperationHandlers.READ_ATTRIBUTE, GlobalDescriptions.getReadAttributeOperationDescription(), true);
            getRegistry().registerOperationHandler(READ_RESOURCE_DESCRIPTION_OPERATION, GlobalOperationHandlers.READ_RESOURCE_DESCRIPTION, GlobalDescriptions.getReadResourceDescriptionOperationDescription(), true);
            getRegistry().registerOperationHandler(READ_CHILDREN_NAMES_OPERATION, GlobalOperationHandlers.READ_CHILDREN_NAMES, GlobalDescriptions.getReadChildrenNamesOperationDescription(), true);
            getRegistry().registerOperationHandler(READ_OPERATION_NAMES_OPERATION, GlobalOperationHandlers.READ_OPERATION_NAMES, GlobalDescriptions.getReadOperationNamesOperation(), true);
            getRegistry().registerOperationHandler(READ_OPERATION_DESCRIPTION_OPERATION, GlobalOperationHandlers.READ_OPERATION_DESCRIPTION, GlobalDescriptions.getReadOperationOperation(), true);



            ModelNodeRegistration profileReg = getRegistry().registerSubModel(PathElement.pathElement("profile", "*"), new DescriptionProvider() {

                @Override
                public ModelNode getModelDescription(Locale locale) {
                    return getProfileModelDescription();
                }
            });

            ModelNodeRegistration profileSub1Reg = profileReg.registerSubModel(PathElement.pathElement("subsystem", "subsystem1"), new DescriptionProvider() {

                @Override
                public ModelNode getModelDescription(Locale locale) {
                    ModelNode node = new ModelNode();
                    node.get(DESCRIPTION).set("A test subsystem 1");
                    node.get(ATTRIBUTES, "attr1", TYPE).set(ModelType.LIST);
                    node.get(ATTRIBUTES, "attr1", VALUE_TYPE).set(ModelType.INT);
                    node.get(ATTRIBUTES, "attr1", DESCRIPTION).set("The values");
                    node.get(ATTRIBUTES, "attr1", REQUIRED).set(true);
                    node.get(CHILDREN, "type1", DESCRIPTION).set("The children1");
                    node.get(CHILDREN, "type1", MIN_OCCURS).set(1);
                    node.get(CHILDREN, "type1", MODEL_DESCRIPTION);
                    node.get(CHILDREN, "type2", DESCRIPTION).set("The children2");
                    node.get(CHILDREN, "type2", MIN_OCCURS).set(1);
                    node.get(CHILDREN, "type2", MODEL_DESCRIPTION);
                    return node;
                }
            });

            DescriptionProvider thingProvider = new DescriptionProvider() {

                @Override
                public ModelNode getModelDescription(Locale locale) {
                    ModelNode node = new ModelNode();
                    node.get(DESCRIPTION).set("A type 1");
                    node.get(ATTRIBUTES, "name", TYPE).set(ModelType.STRING);
                    node.get(ATTRIBUTES, "name", DESCRIPTION).set("The name of the thing");
                    node.get(ATTRIBUTES, "name", REQUIRED).set(true);
                    node.get(ATTRIBUTES, "value", TYPE).set(ModelType.INT);
                    node.get(ATTRIBUTES, "value", DESCRIPTION).set("The value of the thing");
                    node.get(ATTRIBUTES, "value", REQUIRED).set(true);
                    return node;
                }
            };

            ModelNodeRegistration profileSub1RegChildType11 = profileSub1Reg.registerSubModel(PathElement.pathElement("type1", "*"), thingProvider);
            ModelNodeRegistration profileSub1RegChildType2 = profileSub1Reg.registerSubModel(PathElement.pathElement("type2", "other"), new DescriptionProvider() {

                @Override
                public ModelNode getModelDescription(Locale locale) {
                    ModelNode node = new ModelNode();
                    node.get(DESCRIPTION).set("A type 2");
                    node.get(ATTRIBUTES, "name", TYPE).set(ModelType.STRING);
                    node.get(ATTRIBUTES, "name", DESCRIPTION).set("The name of the thing");
                    node.get(ATTRIBUTES, "name", REQUIRED).set(true);
                    return node;
                }
            });

            ModelNodeRegistration profileASub2Reg = profileReg.registerSubModel(PathElement.pathElement("subsystem", "subsystem2"), new DescriptionProvider() {

                @Override
                public ModelNode getModelDescription(Locale locale) {
                    ModelNode node = new ModelNode();
                    node.get(DESCRIPTION).set("A test subsystem 2");
                    node.get(ATTRIBUTES, "bigdecimal", TYPE).set(ModelType.BIG_DECIMAL);
                    node.get(ATTRIBUTES, "bigdecimal", DESCRIPTION).set("A big decimal");
                    node.get(ATTRIBUTES, "bigdecimal", REQUIRED).set(true);
                    node.get(ATTRIBUTES, "biginteger", TYPE).set(ModelType.BIG_DECIMAL);
                    node.get(ATTRIBUTES, "biginteger", DESCRIPTION).set("A big integer");
                    node.get(ATTRIBUTES, "biginteger", REQUIRED).set(true);
                    node.get(ATTRIBUTES, "boolean", TYPE).set(ModelType.BOOLEAN);
                    node.get(ATTRIBUTES, "boolean", DESCRIPTION).set("A boolean");
                    node.get(ATTRIBUTES, "boolean", REQUIRED).set(true);
                    node.get(ATTRIBUTES, "bytes", TYPE).set(ModelType.BYTES);
                    node.get(ATTRIBUTES, "bytes", DESCRIPTION).set("A bytes");
                    node.get(ATTRIBUTES, "bytes", REQUIRED).set(true);
                    node.get(ATTRIBUTES, "double", TYPE).set(ModelType.DOUBLE);
                    node.get(ATTRIBUTES, "double", DESCRIPTION).set("A double");
                    node.get(ATTRIBUTES, "double", REQUIRED).set(true);
                    node.get(ATTRIBUTES, "expression", TYPE).set(ModelType.EXPRESSION);
                    node.get(ATTRIBUTES, "expression", DESCRIPTION).set("A double");
                    node.get(ATTRIBUTES, "expression", REQUIRED).set(true);
                    node.get(ATTRIBUTES, "int", TYPE).set(ModelType.INT);
                    node.get(ATTRIBUTES, "int", DESCRIPTION).set("A int");
                    node.get(ATTRIBUTES, "int", REQUIRED).set(true);
                    node.get(ATTRIBUTES, "list", TYPE).set(ModelType.LIST);
                    node.get(ATTRIBUTES, "list", VALUE_TYPE).set(ModelType.STRING);
                    node.get(ATTRIBUTES, "list", DESCRIPTION).set("A list");
                    node.get(ATTRIBUTES, "list", REQUIRED).set(true);
                    node.get(ATTRIBUTES, "long", TYPE).set(ModelType.LONG);
                    node.get(ATTRIBUTES, "long", DESCRIPTION).set("A long");
                    node.get(ATTRIBUTES, "long", REQUIRED).set(true);
                    node.get(ATTRIBUTES, "object", TYPE).set(ModelType.OBJECT);
                    node.get(ATTRIBUTES, "object", VALUE_TYPE).set(ModelType.STRING);
                    node.get(ATTRIBUTES, "object", DESCRIPTION).set("A object");
                    node.get(ATTRIBUTES, "object", REQUIRED).set(true);
                    node.get(ATTRIBUTES, "property", TYPE).set(ModelType.PROPERTY);
                    node.get(ATTRIBUTES, "property", VALUE_TYPE).set(ModelType.STRING);
                    node.get(ATTRIBUTES, "property", DESCRIPTION).set("A property");
                    node.get(ATTRIBUTES, "property", REQUIRED).set(true);
                    node.get(ATTRIBUTES, "string1", TYPE).set(ModelType.STRING);
                    node.get(ATTRIBUTES, "string1", DESCRIPTION).set("A string");
                    node.get(ATTRIBUTES, "string1", REQUIRED).set(true);
                    node.get(ATTRIBUTES, "string2", TYPE).set(ModelType.STRING);
                    node.get(ATTRIBUTES, "string2", DESCRIPTION).set("A string");
                    node.get(ATTRIBUTES, "string2", REQUIRED).set(true);
                    node.get(ATTRIBUTES, "type", TYPE).set(ModelType.TYPE);
                    node.get(ATTRIBUTES, "type", DESCRIPTION).set("A type");
                    node.get(ATTRIBUTES, "type", REQUIRED).set(true);


                    return node;
                }
            });

            ModelNodeRegistration profileBSub3Reg = profileReg.registerSubModel(PathElement.pathElement("subsystem", "subsystem3"), new DescriptionProvider() {

                @Override
                public ModelNode getModelDescription(Locale locale) {
                    ModelNode node = new ModelNode();
                    node.get(DESCRIPTION).set("A test subsystem 1");
                    node.get(ATTRIBUTES, "attr1", TYPE).set(ModelType.INT);
                    node.get(ATTRIBUTES, "attr1", DESCRIPTION).set("The value");
                    node.get(ATTRIBUTES, "attr1", REQUIRED).set(true);
                    node.get(CHILDREN).setEmptyObject();
                    return node;
                }
            });



            profileSub1Reg.registerOperationHandler("testA1-1",
                    new OperationHandler() {
                        @Override
                        public Cancellable execute(NewOperationContext context, ModelNode operation, ResultHandler resultHandler) {
                            return null;
                        }
                    },
                    new DescriptionProvider() {

                        @Override
                        public ModelNode getModelDescription(Locale locale) {
                            ModelNode node = new ModelNode();
                            node.get(OPERATION_NAME).set("testA1");
                            node.get(REQUEST_PROPERTIES, "paramA1", TYPE).set(ModelType.INT);
                            return node;
                        }
                    },
                    false);
            profileSub1Reg.registerOperationHandler("testA1-2",
                    new OperationHandler() {

                        @Override
                        public Cancellable execute(NewOperationContext context, ModelNode operation, ResultHandler resultHandler) {
                            return null;
                        }
                    },
                    new DescriptionProvider() {

                        @Override
                        public ModelNode getModelDescription(Locale locale) {
                            ModelNode node = new ModelNode();
                            node.get(OPERATION_NAME).set("testA2");
                            node.get(REQUEST_PROPERTIES, "paramA2", TYPE).set(ModelType.STRING);
                            return node;
                        }
                    },
                    false);


            profileASub2Reg.registerOperationHandler("testA2",
                    new OperationHandler() {

                        @Override
                        public Cancellable execute(NewOperationContext context, ModelNode operation, ResultHandler resultHandler) {
                            return null;
                        }
                    },
                    new DescriptionProvider() {

                        @Override
                        public ModelNode getModelDescription(Locale locale) {
                            ModelNode node = new ModelNode();
                            node.get(OPERATION_NAME).set("testB");
                            node.get(REQUEST_PROPERTIES, "paramB", TYPE).set(ModelType.LONG);
                            return node;
                        }
                    },
                    false);

            ModelNodeRegistration profileCSub4Reg = profileReg.registerSubModel(PathElement.pathElement("subsystem", "subsystem4"), new DescriptionProvider() {

                @Override
                public ModelNode getModelDescription(Locale locale) {
                    ModelNode node = new ModelNode();
                    node.get(DESCRIPTION).set("A subsystem");
                    node.get(ATTRIBUTES, "name", TYPE).set(ModelType.STRING);
                    node.get(ATTRIBUTES, "name", DESCRIPTION).set("The name of the thing");
                    node.get(ATTRIBUTES, "name", REQUIRED).set(false);
                    node.get(CHILDREN, "type1", DESCRIPTION).set("The children1");
                    node.get(CHILDREN, "type1", MIN_OCCURS).set(0);
                    node.get(CHILDREN, "type1", MODEL_DESCRIPTION);
                    return node;
                }
            });

            ModelNodeRegistration profileCSub5Reg = profileReg.registerSubModel(PathElement.pathElement("subsystem", "subsystem5"), new DescriptionProvider() {

                @Override
                public ModelNode getModelDescription(Locale locale) {
                    ModelNode node = new ModelNode();
                    node.get(DESCRIPTION).set("A subsystem");
                    node.get(ATTRIBUTES, "name", TYPE).set(ModelType.STRING);
                    node.get(ATTRIBUTES, "name", DESCRIPTION).set("The name of the thing");
                    node.get(ATTRIBUTES, "name", REQUIRED).set(true);
                    node.get(DESCRIPTION).set("A value");
                    node.get(ATTRIBUTES, "value", TYPE).set(ModelType.STRING);
                    node.get(ATTRIBUTES, "value", DESCRIPTION).set("The name of the thing");
                    node.get(ATTRIBUTES, "value", REQUIRED).set(false);
                    node.get(CHILDREN, "type1", DESCRIPTION).set("The children1");
                    node.get(CHILDREN, "type1", MIN_OCCURS).set(0);
                    node.get(CHILDREN, "type1", MODEL_DESCRIPTION);
                    return node;
                }
            });

            ModelNodeRegistration profileCSub5Type1Reg = profileCSub5Reg.registerSubModel(PathElement.pathElement("type1", "thing1"), new DescriptionProvider() {

                @Override
                public ModelNode getModelDescription(Locale locale) {
                    ModelNode node = new ModelNode();
                    node.get(DESCRIPTION).set("A subsystem");
                    return node;
                }
            });
        }
    }

}
