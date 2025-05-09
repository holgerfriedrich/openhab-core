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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.service.ReadyMarker;
import org.openhab.core.service.ReadyMarkerFilter;
import org.openhab.core.service.ReadyService;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the {@link ReadyService} interface.
 *
 * @author Simon Kaufmann - Initial contribution
 */
@Component
@NonNullByDefault
public class ReadyServiceImpl implements ReadyService {

    private final Logger logger = LoggerFactory.getLogger(ReadyServiceImpl.class);
    private static final ReadyMarkerFilter ANY = new ReadyMarkerFilter();

    private final Set<ReadyMarker> markers = new CopyOnWriteArraySet<>();

    private final Map<ReadyTracker, ReadyMarkerFilter> trackers = new HashMap<>();
    private final ReentrantReadWriteLock rwlTrackers = new ReentrantReadWriteLock(true);

    @Override
    public void markReady(ReadyMarker readyMarker) {
        rwlTrackers.readLock().lock();
        try {
            boolean isNew = markers.add(readyMarker);
            if (isNew) {
                notifyTrackers(readyMarker, tracker -> tracker.onReadyMarkerAdded(readyMarker));
                logger.trace("Added ready marker {}", readyMarker);
            }
        } finally {
            rwlTrackers.readLock().unlock();
        }
    }

    @Override
    public void unmarkReady(ReadyMarker readyMarker) {
        rwlTrackers.readLock().lock();
        try {
            boolean isRemoved = markers.remove(readyMarker);
            if (isRemoved) {
                notifyTrackers(readyMarker, tracker -> tracker.onReadyMarkerRemoved(readyMarker));
                logger.trace("Removed ready marker {}", readyMarker);
            }
        } finally {
            rwlTrackers.readLock().unlock();
        }
    }

    private void notifyTrackers(ReadyMarker readyMarker, Consumer<ReadyTracker> action) {
        trackers.entrySet().stream().filter(entry -> entry.getValue().apply(readyMarker)).map(Map.Entry::getKey)
                .forEach(action);
    }

    @Override
    public boolean isReady(ReadyMarker readyMarker) {
        return markers.contains(readyMarker);
    }

    @Override
    public void registerTracker(ReadyTracker readyTracker) {
        registerTracker(readyTracker, ANY);
    }

    @Override
    public void registerTracker(ReadyTracker readyTracker, ReadyMarkerFilter filter) {
        rwlTrackers.writeLock().lock();
        try {
            if (!trackers.containsKey(readyTracker)) {
                trackers.put(readyTracker, filter);
                notifyTracker(readyTracker, readyTracker::onReadyMarkerAdded);
            }
        } catch (RuntimeException e) {
            logger.error("Registering tracker '{}' failed!", readyTracker, e);
        } finally {
            rwlTrackers.writeLock().unlock();
        }
    }

    @Override
    public void unregisterTracker(ReadyTracker readyTracker) {
        rwlTrackers.writeLock().lock();
        try {
            if (trackers.containsKey(readyTracker)) {
                notifyTracker(readyTracker, readyTracker::onReadyMarkerRemoved);
            }
            trackers.remove(readyTracker);
        } finally {
            rwlTrackers.writeLock().unlock();
        }
    }

    private void notifyTracker(ReadyTracker readyTracker, Consumer<ReadyMarker> action) {
        ReadyMarkerFilter f = Objects.requireNonNull(trackers.get(readyTracker));
        markers.stream().filter(f::apply).forEach(action);
    }
}
