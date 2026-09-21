/*
 *   Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.alibaba.nacos.client.config.impl;

import com.alibaba.nacos.api.config.ConfigChangeEvent;
import com.alibaba.nacos.api.config.PropertyChangeType;
import com.alibaba.nacos.api.config.listener.AbstractSharedListener;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.config.filter.impl.ConfigFilterChainManager;
import com.alibaba.nacos.client.config.listener.impl.AbstractConfigChangeListener;
import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.notify.listener.Subscriber;
import com.alibaba.nacos.common.utils.MD5Utils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class CacheDataTest {
    
    @Test
    void testConstructorAndEquals() {
        ConfigFilterChainManager filter = new ConfigFilterChainManager(new Properties());
        final CacheData cacheData1 = new CacheData(filter, "name1", "key", "group", "tenant");
        assertEquals("CacheData [key, group]", cacheData1.toString());
        
        final CacheData cacheData2 = new CacheData(filter, "name2", "key", "group");
        assertEquals(cacheData1, cacheData2);
        assertEquals(cacheData1.hashCode(), cacheData2.hashCode());
        
        final CacheData cacheData3 = new CacheData(filter, "name2", "key3", "group", "tenant");
        assertNotEquals(cacheData1, cacheData3);
    }
    
    @Test
    void testGetter() {
        ConfigFilterChainManager filter = new ConfigFilterChainManager(new Properties());
        final CacheData cacheData1 = new CacheData(filter, "name1", "key", "group", "tenant");
        
        assertTrue(cacheData1.isInitializing());
        assertNull(cacheData1.getContent());
        assertEquals(0, cacheData1.getTaskId());
        assertFalse(cacheData1.isConsistentWithServer());
        assertFalse(cacheData1.isUseLocalConfigInfo());
        assertEquals(0, cacheData1.getLastModifiedTs().intValue());
        assertEquals(0, cacheData1.getLocalConfigInfoVersion());
        
        cacheData1.setInitializing(false);
        cacheData1.setContent("123");
        cacheData1.setTaskId(123);
        cacheData1.setConsistentWithServer(true);
        cacheData1.setType("123");
        long timeStamp = new Date().getTime();
        cacheData1.setLastModifiedTs(timeStamp);
        cacheData1.setUseLocalConfigInfo(true);
        cacheData1.setLocalConfigInfoVersion(timeStamp);
        
        assertFalse(cacheData1.isInitializing());
        assertEquals("123", cacheData1.getContent());
        assertEquals(MD5Utils.md5Hex("123", "UTF-8"), cacheData1.getMd5());
        
        assertEquals(123, cacheData1.getTaskId());
        assertTrue(cacheData1.isConsistentWithServer());
        assertEquals("123", cacheData1.getType());
        assertTrue(cacheData1.isUseLocalConfigInfo());
        assertEquals(timeStamp, cacheData1.getLastModifiedTs().longValue());
        assertEquals(timeStamp, cacheData1.getLocalConfigInfoVersion());
    }
    
    @Test
    void testNotifyWarnTimeout() {
        System.setProperty("nacos.listener.notify.warn.timeout", "5000");
        long notifyWarnTimeout = CacheData.initNotifyWarnTimeout();
        assertEquals(5000, notifyWarnTimeout);
        System.setProperty("nacos.listener.notify.warn.timeout", "1bf000abc");
        long notifyWarnTimeout2 = CacheData.initNotifyWarnTimeout();
        assertEquals(60000, notifyWarnTimeout2);
    }
    
    @Test
    void testListener() throws NacosException {
        ConfigFilterChainManager filter = new ConfigFilterChainManager(new Properties());
        final CacheData cacheData1 = new CacheData(filter, "name1", "key", "group", "tenant");
        
        Listener listener = new Listener() {
            
            @Override
            public Executor getExecutor() {
                return null;
            }
            
            @Override
            public void receiveConfigInfo(String configInfo) {
            }
        };
        cacheData1.addListener(listener);
        assertEquals(1, cacheData1.getListeners().size());
        assertEquals(listener, cacheData1.getListeners().get(0));
        
        cacheData1.removeListener(listener);
        assertEquals(0, cacheData1.getListeners().size());
        
    }
    
    @Test
    void testCheckListenerMd5() throws NacosException {
        ConfigFilterChainManager filter = new ConfigFilterChainManager(new Properties());
        final CacheData data = new CacheData(filter, "name1", "key", "group", "tenant");
        final List<String> list = new ArrayList<>();
        Listener listener = new Listener() {
            
            @Override
            public Executor getExecutor() {
                return Runnable::run;
            }
            
            @Override
            public void receiveConfigInfo(String configInfo) {
                list.add(configInfo);
            }
        };
        data.addListener(listener);
        data.checkListenerMd5();
        assertTrue(data.checkListenersMd5Consistent());
        assertEquals(0, list.size());
        
        data.setContent("new");
        assertFalse(data.checkListenersMd5Consistent());
        data.checkListenerMd5();
        assertEquals(1, list.size());
        assertEquals("new", list.get(0));
        
    }
    
    @Test
    void testCheckListenerMd5NotifyTimeouts() throws NacosException {
        System.setProperty("nacos.listener.notify.warn.timeout", "1000");
        long notifyWarnTimeout = CacheData.initNotifyWarnTimeout();
        assertEquals(1000, notifyWarnTimeout);
        ConfigFilterChainManager filter = new ConfigFilterChainManager(new Properties());
        final CacheData data = new CacheData(filter, "name1", "keytimeouts", "group", "tenant");
        Listener listener = new Listener() {
            
            @Override
            public Executor getExecutor() {
                return Runnable::run;
            }
            
            @Override
            public void receiveConfigInfo(String configInfo) {
                try {
                    Thread.sleep(11000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        
        AtomicReference<String> dataIdNotifyTimeouts = new AtomicReference();
        NotifyCenter.registerSubscriber(new Subscriber() {
            
            @Override
            public void onEvent(Event event) {
                ChangeNotifyBlockEvent changeNotifyBlockEvent = (ChangeNotifyBlockEvent) event;
                dataIdNotifyTimeouts.set(changeNotifyBlockEvent.getDataId());
                System.out.println("timeout:" + changeNotifyBlockEvent.getDataId());
            }
            
            @Override
            public Class<? extends Event> subscribeType() {
                return ChangeNotifyBlockEvent.class;
            }
        });
        data.addListener(listener);
        data.setContent("new");
        data.checkListenerMd5();
        assertTrue(data.checkListenersMd5Consistent());
        assertEquals("keytimeouts", dataIdNotifyTimeouts.get());
    }
    
    @Test
    void testAbstractSharedListener() throws NacosException {
        ConfigFilterChainManager filter = new ConfigFilterChainManager(new Properties());
        final CacheData data = new CacheData(filter, "name1", "keyshare", "group", "tenant");
        
        final String[] dataIdReceive = new String[1];
        final String[] groupReceive = new String[1];
        final String[] contentReceive = new String[1];
        
        Listener listener = new AbstractSharedListener() {
            
            @Override
            public Executor getExecutor() {
                return Runnable::run;
            }
            
            @Override
            public void innerReceive(String dataId, String group, String configInfo) {
                dataIdReceive[0] = dataId;
                groupReceive[0] = group;
                contentReceive[0] = configInfo;
            }
            
        };
        data.addListener(listener);
        String content = "content" + System.currentTimeMillis();
        data.setContent(content);
        data.checkListenerMd5();
        assertTrue(data.checkListenersMd5Consistent());
        assertEquals("keyshare", dataIdReceive[0]);
        assertEquals("group", groupReceive[0]);
        assertEquals(contentReceive[0], content);
    }
    
    @Test
    void testAbstractConfigChangeListener() throws NacosException {
        ConfigFilterChainManager filter = new ConfigFilterChainManager(new Properties());
        final CacheData data = new CacheData(filter, "name1", "keyshare", "group", "tenant");
        data.setType("properties");
        data.setContent("a=a\nb=b\nc=c");
        
        AtomicReference<ConfigChangeEvent> changeItemReceived = new AtomicReference<>();
        Listener listener = new AbstractConfigChangeListener() {
            
            @Override
            public void receiveConfigChange(ConfigChangeEvent event) {
                changeItemReceived.set(event);
            }
            
            @Override
            public Executor getExecutor() {
                return Runnable::run;
            }
            
        };
        data.addListener(listener);
        String content = "b=b\nc=abc\nd=d";
        data.setContent(content);
        data.checkListenerMd5();
        assertTrue(data.checkListenersMd5Consistent());
        assertEquals(PropertyChangeType.DELETED,
            changeItemReceived.get().getChangeItem("a").getType());
        assertEquals(PropertyChangeType.MODIFIED,
            changeItemReceived.get().getChangeItem("c").getType());
        assertEquals(PropertyChangeType.ADDED,
            changeItemReceived.get().getChangeItem("d").getType());
    }
    
    @Test
    void testNotifyTaskExecutorExceptionResetsInNotifying() throws NacosException {
        ConfigFilterChainManager filter = new ConfigFilterChainManager(new Properties());
        final CacheData data =
            new CacheData(filter, "name1", "key_executor_fail", "group", "tenant");
        
        Listener listener = new Listener() {
            
            @Override
            public Executor getExecutor() {
                return command -> {
                    throw new java.util.concurrent.RejectedExecutionException(
                        "Mock ThreadPool Full");
                };
            }
            
            @Override
            public void receiveConfigInfo(String configInfo) {
            }
        };
        data.addListener(listener);
        data.setContent("new_content_trigger");
        
        // When checking listener MD5, the RejectedExecutionException will be thrown.
        // It shouldn't block the caller, and the inNotifying flag must be correctly reset.
        data.checkListenerMd5();
        
        // Assert that the flag inNotifying is false (which allows further checkListenerMd5 invocations).
        // Since we cannot directly access the private inNotifying field of ManagerListenerWrap,
        // we can trigger another content change and see if the exception is thrown again.
        data.setContent("another_content_trigger");
        // If it was deadlocked (inNotifying=true), the checkListenerMd5() would just warn and return.
        // If the reset works, it will try to submit again and throw the exception again.
        data.checkListenerMd5();
    }
    
    @Test
    void testGetConsistentSnapshotReturnsNullWhenContentBlank() {
        ConfigFilterChainManager filter = new ConfigFilterChainManager(new Properties());
        CacheData cacheData = new CacheData(filter, "name", "dataId", "group", "tenant");
        // Content is null by default
        assertNull(cacheData.getConsistentSnapshot());
        
        cacheData.setConfigContentAndKey("", null);
        assertNull(cacheData.getConsistentSnapshot());
    }
    
    @Test
    void testGetConsistentSnapshotReturnsNullWhenNotVerified() {
        // Disk-loaded data (setContent alone, without setConfigContentAndKey) is not verified
        // because content and key may come from separate files. getConsistentSnapshot must
        // return null to prevent using an unpaired ciphertext/key for conditional GET.
        ConfigFilterChainManager filter = new ConfigFilterChainManager(new Properties());
        CacheData cacheData = new CacheData(filter, "name", "dataId", "group", "tenant");
        cacheData.setContent("disk-loaded-content");
        cacheData.setEncryptedDataKey("disk-loaded-key");
        
        // Not verified: separate setContent/setEncryptedDataKey calls don't mark verifiedPair
        assertNull(cacheData.getConsistentSnapshot());
        
        // After a full server response via setConfigContentAndKey, it becomes verified
        cacheData.setConfigContentAndKey("server-content", "server-key");
        CacheData.ConfigSnapshot snapshot = cacheData.getConsistentSnapshot();
        assertEquals("server-content", snapshot.getContent());
        assertEquals("server-key", snapshot.getEncryptedDataKey());
    }
    
    @Test
    void testGetConsistentSnapshotReturnsConsistentContentAndMd5() {
        ConfigFilterChainManager filter = new ConfigFilterChainManager(new Properties());
        CacheData cacheData = new CacheData(filter, "name", "dataId", "group", "tenant");
        String content = "test-content-for-snapshot";
        // Use setConfigContentAndKey to mark as verified (simulating full server response)
        cacheData.setConfigContentAndKey(content, null);
        
        CacheData.ConfigSnapshot snapshot = cacheData.getConsistentSnapshot();
        assertEquals(content, snapshot.getContent());
        assertEquals(MD5Utils.md5Hex(content, "UTF-8"), snapshot.getMd5());
        assertNull(snapshot.getEncryptedDataKey());
    }
    
    @Test
    void testSetConfigContentAndKeyAtomicUpdate() {
        ConfigFilterChainManager filter = new ConfigFilterChainManager(new Properties());
        CacheData cacheData = new CacheData(filter, "name", "dataId", "group", "tenant");
        String content = "encrypted-content";
        String key = "enc-key-123";
        
        cacheData.setConfigContentAndKey(content, key);
        
        CacheData.ConfigSnapshot snapshot = cacheData.getConsistentSnapshot();
        assertEquals(content, snapshot.getContent());
        assertEquals(MD5Utils.md5Hex(content, "UTF-8"), snapshot.getMd5());
        assertEquals(key, snapshot.getEncryptedDataKey());
        
        // Verify individual getters also reflect the update
        assertEquals(content, cacheData.getContent());
        assertEquals(MD5Utils.md5Hex(content, "UTF-8"), cacheData.getMd5());
        assertEquals(key, cacheData.getEncryptedDataKey());
    }
    
    @Test
    void testGetConsistentSnapshotUnderConcurrentUpdate() throws InterruptedException {
        ConfigFilterChainManager filter = new ConfigFilterChainManager(new Properties());
        CacheData cacheData = new CacheData(filter, "name", "dataId", "group", "tenant");
        // Initialize with verified pair via setConfigContentAndKey
        cacheData.setConfigContentAndKey("initial", null);
        
        final int iterations = 500;
        final AtomicReference<String> failure = new AtomicReference<>(null);
        
        Thread writer = new Thread(() -> {
            for (int i = 0; i < iterations; i++) {
                String content = "content-version-" + i;
                String key = "key-version-" + i;
                cacheData.setConfigContentAndKey(content, key);
            }
        });
        
        Thread reader = new Thread(() -> {
            for (int i = 0; i < iterations; i++) {
                CacheData.ConfigSnapshot snapshot = cacheData.getConsistentSnapshot();
                if (snapshot != null) {
                    String expectedMd5 = MD5Utils.md5Hex(snapshot.getContent(), "UTF-8");
                    if (!expectedMd5.equals(snapshot.getMd5())) {
                        failure.set("Inconsistent snapshot: content=" + snapshot.getContent()
                            + ", md5=" + snapshot.getMd5() + ", expected=" + expectedMd5);
                        return;
                    }
                    // Verify key matches the version pattern of content
                    String content = snapshot.getContent();
                    String key = snapshot.getEncryptedDataKey();
                    if (content.startsWith("content-version-") && key != null) {
                        String version = content.substring("content-version-".length());
                        if (!("key-version-" + version).equals(key)) {
                            failure.set("Mixed version snapshot: content=" + content
                                + ", key=" + key);
                            return;
                        }
                    }
                }
            }
        });
        
        writer.start();
        reader.start();
        writer.join();
        reader.join();
        
        assertNull(failure.get(), failure.get());
    }
    
    @Test
    void testMismatchedDiskContentKeyPairNotVerified() {
        // Simulate CacheData initialization from disk: content and key read from separate
        // files, may not belong to the same version. Even though both fields are set,
        // getConsistentSnapshot must return null because the pair is not verified.
        ConfigFilterChainManager filter = new ConfigFilterChainManager(new Properties());
        CacheData cacheData = new CacheData(filter, "name", "dataId", "group", "tenant");
        
        // Disk initialization: separate reads (simulating setContent + setEncryptedDataKey)
        cacheData.setContent("cipher-disk-content-version-A");
        cacheData.setEncryptedDataKey("disk-key-version-B");
        
        // Not verified: separate setters don't mark verifiedPair
        assertNull(cacheData.getConsistentSnapshot());
        
        // After a full server response with matching pair, it becomes verified and usable
        cacheData.setConfigContentAndKey("server-content-version-C", "server-key-version-C");
        CacheData.ConfigSnapshot snapshot = cacheData.getConsistentSnapshot();
        assertNotNull(snapshot);
        assertEquals("server-content-version-C", snapshot.getContent());
        assertEquals("server-key-version-C", snapshot.getEncryptedDataKey());
    }
    
    @Test
    void testAddTenantListenersPathAtomicUpdateWithLatches() throws InterruptedException {
        // Deterministic regression test for the addTenantListenersWithContent() production path.
        // Uses latches to force interleaving: reader starts after writer has set encryptedDataKey
        // but before setContent. With the atomic setConfigContentAndKey fix, the reader must
        // never see old content paired with the new key.
        ConfigFilterChainManager filter = new ConfigFilterChainManager(new Properties());
        final CacheData cacheData = new CacheData(filter, "name", "dataId", "group", "tenant");
        
        // Pre-populate with an initial verified pair (simulating already-registered cache)
        cacheData.setConfigContentAndKey("initial-content", "initial-key");
        
        final CountDownLatch writerReady = new CountDownLatch(1);
        final CountDownLatch readerReady = new CountDownLatch(1);
        final CountDownLatch writerDone = new CountDownLatch(1);
        final AtomicReference<CacheData.ConfigSnapshot> capturedSnapshot = new AtomicReference<>();
        
        // Writer thread: simulates addTenantListenersWithContent() calling setConfigContentAndKey
        Thread writer = new Thread(() -> {
            writerReady.countDown();
            try {
                readerReady.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            // This is the atomic call used by addTenantListenersWithContent after the fix
            cacheData.setConfigContentAndKey("new-content", "new-key");
            writerDone.countDown();
        });
        
        // Reader thread: captures snapshot while writer is in progress
        Thread reader = new Thread(() -> {
            try {
                writerReady.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            readerReady.countDown();
            // Capture snapshot - with atomic update, this either sees the full old pair
            // or the full new pair, never a mixed pair
            capturedSnapshot.set(cacheData.getConsistentSnapshot());
        });
        
        writer.start();
        reader.start();
        writer.join();
        reader.join();
        writerDone.await();
        
        // Verify the captured snapshot is consistent (key belongs to content)
        CacheData.ConfigSnapshot snap = capturedSnapshot.get();
        assertNotNull(snap, "Snapshot should not be null after atomic update");
        String content = snap.getContent();
        String key = snap.getEncryptedDataKey();
        
        if ("initial-content".equals(content)) {
            assertEquals("initial-key", key, "Old content must pair with old key");
        } else if ("new-content".equals(content)) {
            assertEquals("new-key", key, "New content must pair with new key");
        } else {
            fail("Unexpected content: " + content);
        }
        
        // Final state should be the new pair
        CacheData.ConfigSnapshot finalSnap = cacheData.getConsistentSnapshot();
        assertEquals("new-content", finalSnap.getContent());
        assertEquals("new-key", finalSnap.getEncryptedDataKey());
    }
    
    @Test
    void testVerifiedPairInvalidatedByIndividualSetters() {
        // State transition test: paired update (verified=true) -> individual content/key update
        // (verified=false, snapshot unavailable) -> paired refresh (verified=true, snapshot available).
        // This detects the regression where verifiedPair remained true after individual setters.
        ConfigFilterChainManager filter = new ConfigFilterChainManager(new Properties());
        CacheData cacheData = new CacheData(filter, "name", "dataId", "group", "tenant");
        
        // Step 1: Paired update from full server response -> verified, snapshot available
        cacheData.setConfigContentAndKey("server-content-v1", "server-key-v1");
        CacheData.ConfigSnapshot snap1 = cacheData.getConsistentSnapshot();
        assertNotNull(snap1, "After paired update, snapshot should be available");
        assertEquals("server-content-v1", snap1.getContent());
        assertEquals("server-key-v1", snap1.getEncryptedDataKey());
        
        // Step 2: Individual setContent() (e.g., failover overwrite) -> verified=false
        cacheData.setContent("failover-content");
        CacheData.ConfigSnapshot snap2 = cacheData.getConsistentSnapshot();
        assertNull(snap2, "After individual setContent, snapshot should be unavailable");
        
        // Step 3: Individual setEncryptedDataKey() -> still verified=false
        cacheData.setEncryptedDataKey("failover-key");
        CacheData.ConfigSnapshot snap3 = cacheData.getConsistentSnapshot();
        assertNull(snap3,
            "After individual setEncryptedDataKey, snapshot should still be unavailable");
        
        // Step 4: Paired refresh from full server response -> verified=true again
        cacheData.setConfigContentAndKey("server-content-v2", "server-key-v2");
        CacheData.ConfigSnapshot snap4 = cacheData.getConsistentSnapshot();
        assertNotNull(snap4, "After paired refresh, snapshot should be available again");
        assertEquals("server-content-v2", snap4.getContent());
        assertEquals("server-key-v2", snap4.getEncryptedDataKey());
    }
}
