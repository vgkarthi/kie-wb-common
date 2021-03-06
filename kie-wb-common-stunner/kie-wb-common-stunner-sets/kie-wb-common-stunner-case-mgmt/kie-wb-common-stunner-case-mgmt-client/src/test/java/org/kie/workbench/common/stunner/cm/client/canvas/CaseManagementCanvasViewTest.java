/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.stunner.cm.client.canvas;

import com.ait.lienzo.client.core.shape.wires.WiresShape;
import com.ait.lienzo.test.LienzoMockitoTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.stunner.cm.client.wires.MockCaseManagementShape;
import org.kie.workbench.common.stunner.shapes.client.view.ConnectorView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(LienzoMockitoTestRunner.class)
public class CaseManagementCanvasViewTest {

    private CaseManagementCanvasView view;

    @Before
    public void setup() {
        this.view = new CaseManagementCanvasView();
        this.view.init();
    }

    @Test
    public void addWiresShape() {
        final MockCaseManagementShape shape = new MockCaseManagementShape();
        final String uuid = shape.uuid();
        shape.setUUID(uuid);

        view.addShape(shape);

        final WiresShape registeredShape = view.getWiresManager().getShape(uuid);
        assertNotNull(registeredShape);
        assertEquals(shape,
                     registeredShape);
    }

    @Test
    public void addWiresConnector() {
        final ConnectorView connector = new ConnectorView(0.0,
                                                          0.0);
        final String uuid = connector.uuid();
        connector.setUUID(uuid);

        view.addShape(connector);

        final WiresShape registeredConnector = view.getWiresManager().getShape(uuid);
        assertNull(registeredConnector);
    }

    @Test
    public void addChildShape() {
        final MockCaseManagementShape parent = new MockCaseManagementShape();
        final MockCaseManagementShape child = new MockCaseManagementShape();

        view.addChildShape(parent,
                           child,
                           0);

        assertEquals(1,
                     parent.getChildShapes().size());
        assertEquals(child,
                     parent.getChildShapes().get(0));
    }
}
