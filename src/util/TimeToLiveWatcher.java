package util;

import java.util.Date;
import java.util.Map;

class TimeToLiveWatcher<K, V> extends Thread {

    private final TimeToLiveHashMap expireHashMap;
    private final Map<K, Long> createdTimeMap;
    private final Map<K, Long> accessTimeMap;
    private final Map<K, Long> expireTimeMap;
    private final Map<K, TimeToLiveEnum> expireTypeMap;
    private long currentTime = new Date().getTime();

    public TimeToLiveWatcher(TimeToLiveHashMap expireHashMap) {
        this.expireHashMap = expireHashMap;
        this.createdTimeMap = expireHashMap.createdTimeMap;
        this.accessTimeMap = expireHashMap.accessTimeMap;
        this.expireTypeMap = expireHashMap.expireTypeMap;
        this.expireTimeMap = expireHashMap.expireTimeMap;
    }

    @Override
    public void run() {
        while (true) {
            this.currentTime = new Date().getTime();
            cleanCreateMap();
            cleanAccessMap();
            try {
                sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void cleanCreateMap() {
        for (Object key : createdTimeMap.keySet()) {
            long expireTime = expireTimeMap.getOrDefault(key, -1L);
            long createdTime = createdTimeMap.getOrDefault(key, -1L);
            if (this.currentTime > (expireTime + createdTime)) {
                removeKey(key);
            }
        }
    }

    private void cleanAccessMap() {
        for (Object key : accessTimeMap.keySet()) {
            long expireTime = expireTimeMap.get(key);
            long accessTime = accessTimeMap.get(key);
            if (this.currentTime > (expireTime + accessTime)) {
                removeKey(key);
            }
        }
    }

    private void removeKey(Object key) {
        keyRemoveInSynchronized(expireHashMap, key);
        keyRemoveInSynchronized(createdTimeMap, key);
        keyRemoveInSynchronized(accessTimeMap, key);
        keyRemoveInSynchronized(expireTypeMap, key);
        keyRemoveInSynchronized(expireTimeMap, key);
    }

    private void keyRemoveInSynchronized(Map targetMap, Object key) {
        synchronized (targetMap) {
            targetMap.remove(key);
        }

    }
}
