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
package org.openhab.core.internal.items;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.items.ManagedMetadataProvider;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.service.ReadyService;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateOption;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

/**
 * @author Yannick Schaus - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class MetadataStateDescriptionFragmentProviderTest {

    private static final String ITEM_NAME = "itemName";

    @SuppressWarnings("rawtypes")
    private @Mock @NonNullByDefault({}) ServiceReference managedProviderRefMock;
    private @Mock @NonNullByDefault({}) BundleContext bundleContextMock;
    private @Mock @NonNullByDefault({}) ManagedMetadataProvider managedProviderMock;

    private @Mock @NonNullByDefault({}) MetadataRegistryImpl metadataRegistryMock;
    private @Mock @NonNullByDefault({}) ReadyService readyServiceMock;

    private @NonNullByDefault({}) MetadataStateDescriptionFragmentProvider stateDescriptionFragmentProvider;

    private @NonNullByDefault({}) ServiceListener providerTracker;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setup() throws Exception {
        when(bundleContextMock.getService(same(managedProviderRefMock))).thenReturn(managedProviderMock);

        metadataRegistryMock = new MetadataRegistryImpl(readyServiceMock);
        metadataRegistryMock.setManagedProvider(managedProviderMock);
        metadataRegistryMock.activate(bundleContextMock);
        metadataRegistryMock.waitForCompletedAsyncActivationTasks();

        ArgumentCaptor<ServiceListener> captor = ArgumentCaptor.forClass(ServiceListener.class);
        verify(bundleContextMock).addServiceListener(captor.capture(), any());
        providerTracker = captor.getValue();
        providerTracker.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, managedProviderRefMock));

        stateDescriptionFragmentProvider = new MetadataStateDescriptionFragmentProvider(metadataRegistryMock, Map.of());
    }

    @Test
    public void testEmpty() throws Exception {
        StateDescriptionFragment stateDescriptionFragment = stateDescriptionFragmentProvider
                .getStateDescriptionFragment(ITEM_NAME, null);
        assertNull(stateDescriptionFragment);
    }

    @SuppressWarnings("null")
    @Test
    public void testFragment() throws Exception {
        MetadataKey metadataKey = new MetadataKey("stateDescription", ITEM_NAME);
        Map<String, Object> metadataConfig = new HashMap<>();
        metadataConfig.put("pattern", "%.1f %unit%");
        metadataConfig.put("min", 18.5);
        metadataConfig.put("max", "34");
        metadataConfig.put("step", 3);
        metadataConfig.put("readOnly", "true");
        metadataConfig.put("options", "OPTION1,OPTION2 , 3 =Option 3 ,\"4=4\"=\" Option=4 \" ");
        Metadata metadata = new Metadata(metadataKey, "N/A", metadataConfig);
        metadataRegistryMock.added(managedProviderMock, metadata);

        StateDescriptionFragment stateDescriptionFragment = stateDescriptionFragmentProvider
                .getStateDescriptionFragment(ITEM_NAME, null);
        assertNotNull(stateDescriptionFragment);
        assertEquals("%.1f %unit%", stateDescriptionFragment.getPattern());
        assertEquals(new BigDecimal(18.5), stateDescriptionFragment.getMinimum());
        assertEquals(new BigDecimal(34), stateDescriptionFragment.getMaximum());
        assertEquals(new BigDecimal(3), stateDescriptionFragment.getStep());
        assertEquals(true, stateDescriptionFragment.isReadOnly());
        assertNotNull(stateDescriptionFragment.getOptions());
        Iterator<StateOption> it = stateDescriptionFragment.getOptions().iterator();
        StateOption stateOption = it.next();
        assertEquals("OPTION1", stateOption.getValue());
        assertNull(stateOption.getLabel());
        stateOption = it.next();
        assertEquals("OPTION2", stateOption.getValue());
        assertNull(stateOption.getLabel());
        stateOption = it.next();
        assertEquals("3", stateOption.getValue());
        assertEquals("Option 3", stateOption.getLabel());
        stateOption = it.next();
        assertEquals("4=4", stateOption.getValue());
        assertEquals("Option=4", stateOption.getLabel());
    }
}
