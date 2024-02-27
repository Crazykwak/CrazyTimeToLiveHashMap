package util;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TimeToLiveHashMap<K, V> extends ConcurrentHashMap<K, V> {

    protected final Map<K, Long> createdTimeMap = new ConcurrentHashMap<>();
    protected final Map<K, Long> accessTimeMap = new ConcurrentHashMap<>();
    protected final Map<K, TimeToLiveEnum> expireTypeMap = new ConcurrentHashMap<>();
    protected final Map<K, Long> expireTimeMap = new ConcurrentHashMap<>();

    /**
     * 1000 * 60 * 60 = 1 Hour
     */
    private final long DEFAULT_EXPIRE_TIME = 1000 * 30;

    public TimeToLiveHashMap() {
        TimeToLiveWatcher expireWatcher = new TimeToLiveWatcher(this);
        expireWatcher.start();
    }

    public V putWithExpired(K key, V value, TimeToLiveEnum expiredType, long expireTimeMills) {
        expireTypeMap.put(key, expiredType);
        expireTimeMap.put(key, expireTimeMills);
        return this.put(key, value);
    }

    /**
     * 기본 put으로 사용하면 ExpireType은 Created, expireTime 은 1시간
     * ExpireType을 Access로 하려면 putWithExpired 메서드를 사용할 것
     */
    @Override
    public V put(K key, V value) {
        long currentTime = new Date().getTime();

        if (!expireTypeMap.containsKey(key)) {
            doTypeAccess(key, currentTime, DEFAULT_EXPIRE_TIME);
            return super.put(key, value);
        }

        TimeToLiveEnum expiredType = expireTypeMap.get(key);
        Long expireTime = expireTimeMap.get(key);
        if (expireTime == null) {
            expireTime = DEFAULT_EXPIRE_TIME;
        }

        if (expiredType == TimeToLiveEnum.ACCESS) {
            doTypeAccess(key, currentTime, expireTime);
            return super.put(key, value);
        }

        doTypeCreate(key, currentTime, expireTime);
        return super.put(key, value);
    }

    private void doTypeAccess(K key, long currentTime, long expireTime) {
        expireTimeMap.put(key, expireTime);
        accessTimeMap.put(key, currentTime);
    }

    private void doTypeCreate(K key, long currentTime, long expireTime) {
        expireTimeMap.put(key, expireTime);
        createdTimeMap.put(key, currentTime);
    }

    @Override
    public V get(Object key) {
        if (expireTypeMap.get(key) == TimeToLiveEnum.ACCESS) {
            Date date = new Date();
            accessTimeMap.put((K) key, date.getTime());
        }
        return super.get(key);
    }
}
