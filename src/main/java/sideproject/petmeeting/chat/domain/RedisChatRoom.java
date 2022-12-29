package sideproject.petmeeting.chat.domain;

import lombok.Getter;
import lombok.Setter;
import redis.embedded.Redis;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
public class RedisChatRoom implements Serializable {
    private static final long serialVersionUID = 6494678977089006639L;

    private String roomId;
    private String name;

    public static RedisChatRoom create(String name, String roomId) {
        RedisChatRoom redisChatRoom = new RedisChatRoom();
//        redisChatRoom.roomId = UUID.randomUUID().toString();
        redisChatRoom.roomId = roomId;
        redisChatRoom.name = name;
        return redisChatRoom;
    }
}

