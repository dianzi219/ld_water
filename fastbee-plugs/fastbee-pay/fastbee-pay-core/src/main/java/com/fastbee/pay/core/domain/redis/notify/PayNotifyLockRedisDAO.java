package com.fastbee.pay.core.domain.redis.notify;

import org.springframework.stereotype.Repository;


/**
 * 支付通知的锁 Redis DAO
 *
 * @author fastbee
 */
@Repository
public class PayNotifyLockRedisDAO {

//    @Resource
//    private RedissonClient redissonClient;
//
//    public void lock(Long id, Long timeoutMillis, Runnable runnable) {
//        String lockKey = formatKey(id);
//        RLock lock = redissonClient.getLock(lockKey);
//        try {
//            lock.lock(timeoutMillis, TimeUnit.MILLISECONDS);
//            // 执行逻辑
//            runnable.run();
//        } finally {
//            lock.unlock();
//        }
//    }
//
//    private static String formatKey(Long id) {
//        return String.format(PAY_NOTIFY_LOCK, id);
//    }

}
