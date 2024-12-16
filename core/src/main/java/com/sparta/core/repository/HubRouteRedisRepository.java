package com.sparta.core.repository;

import com.sparta.core.entity.HubRoute;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HubRouteRedisRepository {

  private final RedisTemplate<String, HubRoute> redisTemplate;

  private static final String REDIS_LIST_KEY = "HubRouteList";

  public void saveAll(List<HubRoute> hubRoutes) {
    redisTemplate.opsForList().rightPushAll(REDIS_LIST_KEY, hubRoutes);
  }


  public List<HubRoute> findAll() {
    return redisTemplate.opsForList().range(REDIS_LIST_KEY, 0, -1);
  }

}
