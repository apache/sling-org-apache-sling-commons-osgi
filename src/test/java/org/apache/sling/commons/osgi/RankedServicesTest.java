/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.commons.osgi;

import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import org.apache.sling.commons.osgi.RankedServices.ChangeListener;
import org.junit.Test;
import org.osgi.framework.Constants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class RankedServicesTest {

    private static final String SERVICE_1 = "service1";
    private static final Map<String, Object> SERVICE_1_PROPS =
            Map.of(Constants.SERVICE_RANKING, 50, Constants.SERVICE_ID, 1L);
    private static final String SERVICE_2 = "service2";
    private static final Map<String, Object> SERVICE_2_PROPS =
            Map.of(Constants.SERVICE_RANKING, 10, Constants.SERVICE_ID, 2L);
    private static final String SERVICE_3 = "service3";
    private static final Map<String, Object> SERVICE_3_PROPS =
            Map.of(Constants.SERVICE_RANKING, 100, Constants.SERVICE_ID, 3L);

    @Test
    public void testSortedServicesAscending() {
        RankedServices<Comparable> underTest = new RankedServices<Comparable>();
        assertEquals(0, underTest.get().size());

        underTest.bind(SERVICE_1, SERVICE_1_PROPS);
        assertEquals(1, underTest.get().size());
        Comparable[] services = toArray(underTest.get().iterator(), Comparable.class);
        assertSame(SERVICE_1, services[0]);

        underTest.bind(SERVICE_2, SERVICE_2_PROPS);
        underTest.bind(SERVICE_3, SERVICE_3_PROPS);
        assertEquals(3, underTest.get().size());
        services = toArray(underTest.get().iterator(), Comparable.class);
        assertSame(SERVICE_2, services[0]);
        assertSame(SERVICE_1, services[1]);
        assertSame(SERVICE_3, services[2]);

        underTest.unbind(SERVICE_2, SERVICE_2_PROPS);
        assertEquals(2, underTest.get().size());
        services = toArray(underTest.get().iterator(), Comparable.class);
        assertSame(SERVICE_1, services[0]);
        assertSame(SERVICE_3, services[1]);
    }

    private Comparable[] toArray(Iterator<Comparable> iterator, Class<Comparable> class1) {
        Spliterator<Comparable> spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, false) // false for sequential stream
                .toArray(Comparable[]::new);
    }

    @Test
    public void testSortedServicesDescending() {
        RankedServices<Comparable> underTest = new RankedServices<Comparable>(Order.DESCENDING);
        assertEquals(0, underTest.get().size());

        underTest.bind(SERVICE_1, SERVICE_1_PROPS);
        assertEquals(1, underTest.get().size());
        Comparable[] services = toArray(underTest.get().iterator(), Comparable.class);
        assertSame(SERVICE_1, services[0]);

        underTest.bind(SERVICE_2, SERVICE_2_PROPS);
        underTest.bind(SERVICE_3, SERVICE_3_PROPS);
        assertEquals(3, underTest.get().size());
        services = toArray(underTest.get().iterator(), Comparable.class);
        assertSame(SERVICE_3, services[0]);
        assertSame(SERVICE_1, services[1]);
        assertSame(SERVICE_2, services[2]);

        underTest.unbind(SERVICE_2, SERVICE_2_PROPS);
        assertEquals(2, underTest.get().size());
        services = toArray(underTest.get().iterator(), Comparable.class);
        assertSame(SERVICE_3, services[0]);
        assertSame(SERVICE_1, services[1]);
    }

    @Test
    public void testChangeListener() {
        ChangeListener changeListener = mock(ChangeListener.class);

        RankedServices<Comparable> underTest = new RankedServices<Comparable>(changeListener);
        underTest.bind(SERVICE_1, SERVICE_1_PROPS);
        verify(changeListener).changed();
    }
}
