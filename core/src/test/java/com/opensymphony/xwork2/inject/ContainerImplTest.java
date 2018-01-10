/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.opensymphony.xwork2.inject;

import junit.framework.TestCase;

import java.security.Permission;

/**
 * ContainerImpl Tester.
 *
 * @author Lukasz Lenart
 * @version 1.0
 * @since <pre>11/26/2008</pre>
 */
public class ContainerImplTest extends TestCase {

    private Container c;

    @Override
    protected void setUp() throws Exception {
        System.setSecurityManager(null);

        super.setUp();
        ContainerBuilder cb = new ContainerBuilder();
        cb.constant("methodCheck.name", "Lukasz");
        cb.constant("fieldCheck.name", "Lukasz");
        cb.factory(EarlyInitializable.class, EarlyInitializableBean.class, Scope.SINGLETON);
        cb.factory(Initializable.class, InitializableBean.class, Scope.SINGLETON);
        cb.factory(EarlyInitializable.class, "prototypeEarlyInitializable", EarlyInitializableBean.class, Scope.PROTOTYPE);
        cb.factory(Initializable.class, "prototypeInitializable", InitializableBean.class, Scope.PROTOTYPE);
        c = cb.create(false);

        Class.forName(FieldCheck.class.getName());
        Class.forName(ContainerImpl.FieldInjector.class.getName());
    }

    /**
     * Inject values into field
     */
    public void testFieldInjector() throws Exception {

        FieldCheck fieldCheck = new FieldCheck();

        try {
            c.inject(fieldCheck);
            assertTrue(true);
        } catch (DependencyException expected) {
            fail("No exception expected!");
        }

        assertEquals(fieldCheck.getName(), "Lukasz");
    }

    /**
     * Inject values into method
     */
    public void testMethodInjector() throws Exception {

        MethodCheck methodCheck = new MethodCheck();

        try {
            c.inject(methodCheck);
            assertTrue(true);
        } catch (DependencyException expected) {
            fail("No exception expected!");
        }
    }

    /**
     * Inject values into field under SecurityManager
     */
    public void testFieldInjectorWithSecurityEnabled() throws Exception {
        System.setSecurityManager(new TestSecurityManager());

        FieldCheck fieldCheck = new FieldCheck();

        try {
            c.inject(fieldCheck);
            fail("Exception should be thrown!");
        } catch (Error | DependencyException expected) {
            assertTrue(true);
        }
    }

    /**
     * Inject values into method under SecurityManager
     */
    public void testMethodInjectorWithSecurityEnabled() throws Exception {
        System.setSecurityManager(new TestSecurityManager());

        MethodCheck methodCheck = new MethodCheck();

        try {
            c.inject(methodCheck);
            fail("Exception should be thrown!");
        } catch (DependencyException | Error expected) {
            assertTrue(true);
        }
    }

    public void testEarlyInitializable() throws Exception {
        assertTrue("should being initialized already", EarlyInitializableBean.initializedEarly);

        EarlyInitializableCheck earlyInitializableCheck = new EarlyInitializableCheck();
        c.inject(earlyInitializableCheck);
        assertEquals("initialized early", ((EarlyInitializableBean) earlyInitializableCheck.getEarlyInitializable()).getMessage());
        assertEquals("initialized early", ((EarlyInitializableBean) earlyInitializableCheck.getPrototypeEarlyInitializable()).getMessage());

        EarlyInitializableCheck earlyInitializableCheck2 = new EarlyInitializableCheck();
        c.inject(earlyInitializableCheck2);
        assertEquals("initialized early", ((EarlyInitializableBean) earlyInitializableCheck2.getEarlyInitializable()).getMessage());
        assertEquals("initialized early", ((EarlyInitializableBean) earlyInitializableCheck2.getPrototypeEarlyInitializable()).getMessage());
    }

    public void testInitializable() throws Exception {
        assertFalse("should not being initialized already", InitializableBean.initialized);

        InitializableCheck initializableCheck = new InitializableCheck();
        c.inject(initializableCheck);
        assertTrue("should being initialized here", InitializableBean.initialized);
        assertEquals("initialized", ((InitializableBean) initializableCheck.getInitializable()).getMessage());
        assertEquals("initialized", ((InitializableBean) initializableCheck.getPrototypeInitializable()).getMessage());

        InitializableCheck initializableCheck2 = new InitializableCheck();
        c.inject(initializableCheck2);
        assertEquals("initialized", ((InitializableBean) initializableCheck2.getInitializable()).getMessage());
        assertEquals("initialized", ((InitializableBean) initializableCheck2.getPrototypeInitializable()).getMessage());
    }

    public static class FieldCheck {

        @Inject("fieldCheck.name")
        private String name;

        public String getName() {
            return name;
        }
    }

    public static class MethodCheck {

        private String name;

        @Inject("methodCheck.name")
        private void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }

    class InitializableCheck {

        private Initializable initializable;
        private Initializable prototypeInitializable;

        @Inject
        public void setInitializable(Initializable initializable) {
            this.initializable = initializable;
        }

        @Inject("prototypeInitializable")
        public void setPrototypeInitializable(Initializable prototypeInitializable) {
            this.prototypeInitializable = prototypeInitializable;
        }

        public Initializable getInitializable() {
            return initializable;
        }

        public Initializable getPrototypeInitializable() {
            return prototypeInitializable;
        }
    }

    class EarlyInitializableCheck {

        private EarlyInitializable earlyInitializable;
        private EarlyInitializable prototypeEarlyInitializable;

        @Inject
        public void setEarlyInitializable(EarlyInitializable earlyInitializable) {
            this.earlyInitializable = earlyInitializable;
        }

        @Inject("prototypeEarlyInitializable")
        public void setPrototypeEarlyInitializable(EarlyInitializable prototypeEarlyInitializable) {
            this.prototypeEarlyInitializable = prototypeEarlyInitializable;
        }

        public EarlyInitializable getEarlyInitializable() {
            return earlyInitializable;
        }

        public EarlyInitializable getPrototypeEarlyInitializable() {
            return prototypeEarlyInitializable;
        }
    }

    class TestSecurityManager extends SecurityManager {

        @Override
        public void checkPermission(Permission perm) {
            if (!"setSecurityManager".equals(perm.getName())) {
                super.checkPermission(perm);
            }
        }
    }
}
