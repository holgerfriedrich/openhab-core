/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.internal.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.core.service.ReadyMarker;
import org.openhab.core.service.ReadyMarkerFilter;
import org.openhab.core.service.ReadyService.ReadyTracker;

/**
 *
 * @author Simon Kaufmann - Initial contribution
 */
@NonNullByDefault
public class ReadyServiceImplTest {

    @Test
    public void testDifferentReadyMarkerInstances() {
        ReadyServiceImpl rs = new ReadyServiceImpl();
        assertFalse(rs.isReady(new ReadyMarker("test", "id")));
        rs.markReady(new ReadyMarker("test", "id"));
        assertTrue(rs.isReady(new ReadyMarker("test", "id")));
        rs.unmarkReady(new ReadyMarker("test", "id"));
        assertFalse(rs.isReady(new ReadyMarker("test", "id")));
    }

    @Test
    public void testTrackersAreInformedInitially() {
        ReadyTracker tracker = mock(ReadyTracker.class);
        ReadyServiceImpl rs = new ReadyServiceImpl();
        rs.markReady(new ReadyMarker("test", "id"));
        rs.registerTracker(tracker);
        verify(tracker).onReadyMarkerAdded(isA(ReadyMarker.class));
        verifyNoMoreInteractions(tracker);
    }

    @Test
    public void testTrackersAreInformedOnChange() {
        ReadyTracker tracker = mock(ReadyTracker.class);
        ReadyServiceImpl rs = new ReadyServiceImpl();
        rs.registerTracker(tracker);
        rs.markReady(new ReadyMarker("test", "id"));
        verify(tracker).onReadyMarkerAdded(isA(ReadyMarker.class));
        rs.unmarkReady(new ReadyMarker("test", "id"));
        verify(tracker).onReadyMarkerRemoved(isA(ReadyMarker.class));
        verifyNoMoreInteractions(tracker);
    }

    @Test
    public void testTrackersAreInformedOnlyOnMatch() {
        ReadyTracker tracker = mock(ReadyTracker.class);
        ReadyServiceImpl rs = new ReadyServiceImpl();
        rs.registerTracker(tracker, new ReadyMarkerFilter().withType("test"));
        rs.markReady(new ReadyMarker("foo", "id"));
        verifyNoMoreInteractions(tracker);
        rs.markReady(new ReadyMarker("test", "id"));
        verify(tracker).onReadyMarkerAdded(isA(ReadyMarker.class));
        verifyNoMoreInteractions(tracker);
    }

    @Test
    public void testUnregisterTracker() {
        ReadyTracker tracker = mock(ReadyTracker.class);
        ReadyServiceImpl rs = new ReadyServiceImpl();
        rs.markReady(new ReadyMarker("foo", "id"));
        rs.registerTracker(tracker, new ReadyMarkerFilter());
        verify(tracker).onReadyMarkerAdded(isA(ReadyMarker.class));
        rs.unregisterTracker(tracker);
        verify(tracker).onReadyMarkerRemoved(isA(ReadyMarker.class));
        verifyNoMoreInteractions(tracker);
    }

    @Test
    public void testReadyMarkerOrderPreserved() {
        ReadyTracker tracker = mock(ReadyTracker.class);
        ReadyServiceImpl rs = new ReadyServiceImpl();
        List<ReadyMarker> markers = List.of(new ReadyMarker("foo", "c"), new ReadyMarker("foo", "1"),
                new ReadyMarker("foo", "a"), new ReadyMarker("foo", "3"));
        markers.forEach(rs::markReady);
        rs.registerTracker(tracker, new ReadyMarkerFilter().withType("foo"));
        ArgumentCaptor<ReadyMarker> captor = ArgumentCaptor.forClass(ReadyMarker.class);
        verify(tracker, times(4)).onReadyMarkerAdded(captor.capture());
        assertThat(captor.getAllValues(), Matchers.contains(markers.toArray(ReadyMarker[]::new)));
    }
}
