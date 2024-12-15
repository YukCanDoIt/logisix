package com.sparta.delivery.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.delivery.dto.HubRoute;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PathService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // Redis에서 허브 경로 데이터 가져오기
    public List<HubRoute> getHubRoutes() {
        try {
            String hubRoutesJson = redisTemplate.opsForValue().get("hub_routes");
            if (Objects.isNull(hubRoutesJson)) {
                return null;
            }
            return objectMapper.readValue(hubRoutesJson, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("레디스 조회 실패", e);
        }
    }

    // 시작 허브부터 종착 허브까지 최단 경로 찾기
    public List<UUID> findShortestPath(List<HubRoute> hubRoutes, UUID startHubId, UUID endHubId) {

        // 그래프 구성
        Map<UUID, List<HubRoute>> graph = buildGraph(hubRoutes);

        return dijkstra(graph, startHubId, endHubId);
    }

    private Map<UUID, List<HubRoute>> buildGraph(List<HubRoute> hubRoutes) {
        Map<UUID, List<HubRoute>> graph = new HashMap<>();
        for (HubRoute route : hubRoutes) {
            graph.computeIfAbsent(route.departureHubId(), k -> new ArrayList<>()).add(route);
        }
        return graph;
    }

    private List<UUID> dijkstra(Map<UUID, List<HubRoute>> graph, UUID startHubId, UUID endHubId) {
        // 우선순위 큐 (최단 시간 기준)
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparing(Node::totalTime));
        Map<UUID, Duration> shortestTimes = new HashMap<>();
        Map<UUID, UUID> previousNodes = new HashMap<>();

        // 초기화
        queue.add(new Node(startHubId, Duration.ZERO));
        shortestTimes.put(startHubId, Duration.ZERO);

        while (!queue.isEmpty()) {
            Node currentNode = queue.poll();
            UUID currentHubId = currentNode.hubId();

            // 종착 허브에 도달하면 경로 복원
            if (currentHubId.equals(endHubId)) {
                return reconstructPath(previousNodes, endHubId);
            }

            // 인접 허브 탐색
            List<HubRoute> neighbors = graph.getOrDefault(currentHubId, Collections.emptyList());
            for (HubRoute route : neighbors) {
                UUID neighborHubId = route.arrivalHubId();
                Duration newTime = currentNode.totalTime().plus(route.estimateTime());

                if (newTime.compareTo(shortestTimes.getOrDefault(neighborHubId, Duration.ofDays(Long.MAX_VALUE))) < 0) {
                    shortestTimes.put(neighborHubId, newTime);
                    previousNodes.put(neighborHubId, currentHubId);
                    queue.add(new Node(neighborHubId, newTime));
                }
            }
        }

        return Collections.emptyList();
    }

    private List<UUID> reconstructPath(Map<UUID, UUID> previousNodes, UUID endHubId) {
        List<UUID> path = new LinkedList<>();
        for (UUID arrival = endHubId; arrival != null; arrival = previousNodes.get(arrival)) {
            path.add(0, arrival);
        }
        return path;
    }

    private static class Node {
        private final UUID hubId;
        private final Duration totalTime;

        public Node(UUID hubId, Duration totalTime) {
            this.hubId = hubId;
            this.totalTime = totalTime;
        }

        public UUID hubId() {
            return hubId;
        }

        public Duration totalTime() {
            return totalTime;
        }
    }

    public HubRoute findHubRoute(List<HubRoute> hubRoutes, UUID departureHubId, UUID arrivalHubId) {
        return hubRoutes.stream()
                .filter(route -> route.departureHubId().equals(departureHubId) && route.arrivalHubId().equals(arrivalHubId))
                .findFirst()
                .orElse(null);
    }

    public Map<UUID, Double> calculateDistancesFromHub(List<HubRoute> hubRoutes, UUID hubId) {
        return hubRoutes.stream()
                .filter(r -> r.departureHubId().equals(hubId))
                .collect(Collectors.toMap(HubRoute::arrivalHubId, HubRoute::estimatedDistance));
    }

}