package com.fastbee.framework.manager;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;

@Component
public class DeleteKeyGenerator implements KeyGenerator {

    @Override
    public Object generate(Object target, Method method, Object... params) {
        if (params.length > 0) {
            Object ids = params[0];
            Collection<?> collection = Collections.singletonList(ids);
            if (collection.stream().allMatch(item -> item instanceof Long)) {
                // 将 Collection<Long> 转换为 String[]
                String[] keys = collection.stream()
                        .map(Object::toString)
                        .toArray(String[]::new);
                // 使用 keys
                List<Object> keysList = new ArrayList<>(keys.length);
                keysList.addAll(Arrays.asList(keys));
                return keysList;
            }
        }
        return new String[0];
    }
}
