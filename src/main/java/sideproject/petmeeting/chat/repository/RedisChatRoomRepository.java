package sideproject.petmeeting.chat.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import sideproject.petmeeting.chat.domain.ChatRoom;
import sideproject.petmeeting.chat.domain.RedisChatRoom;

import javax.annotation.PostConstruct;
import java.util.List;

@RequiredArgsConstructor
@Service
public class RedisChatRoomRepository {

    private static final String CHAT_ROOMS = "CHAT_ROOM";
    private final RedisTemplate<String, Object> redisTemplate;
    private HashOperations<String, String, RedisChatRoom> opsHashChatRoom;

    @PostConstruct
    private void init() {
        opsHashChatRoom = redisTemplate.opsForHash();
    }

    // 모든 채팅방 조회
    public List<RedisChatRoom> findAllRoom() {
        return opsHashChatRoom.values(CHAT_ROOMS);
    }

    // 특정 채팅방 조회
    public RedisChatRoom findRoomById(String id) {
        return opsHashChatRoom.get(CHAT_ROOMS, id);
    }

    // 채팅방 생성 : 서버간 채팅방 공유를 위해 redis hash에 저장한다.
    public RedisChatRoom createChatRoom(String name, String roomId) {
        RedisChatRoom redisChatRoom = RedisChatRoom.create(name, roomId);
        opsHashChatRoom.put(CHAT_ROOMS, redisChatRoom.getRoomId(), redisChatRoom);
        return redisChatRoom;
    }


}
