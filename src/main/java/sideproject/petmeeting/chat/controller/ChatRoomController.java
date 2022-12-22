package sideproject.petmeeting.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sideproject.petmeeting.chat.domain.ChatRoom;
import sideproject.petmeeting.chat.domain.RedisChatRoom;
import sideproject.petmeeting.chat.repository.ChatRoomRepository;
import sideproject.petmeeting.chat.repository.RedisChatRoomRepository;

import java.util.List;
import java.util.Optional;

/**
 * Test 전용 Controller
 */
@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/chat")
public class ChatRoomController {

    private final ChatRoomRepository chatRoomRepository;
    private final RedisChatRoomRepository redisChatRoomRepository;

    @GetMapping("/room")
    public String rooms(Model model) {
        return "/chat/room";
    }

    @GetMapping("/rooms")
    @ResponseBody
    public List<RedisChatRoom> room() {
        return redisChatRoomRepository.findAllRoom();
    }

    @PostMapping("/room")
    @ResponseBody
    public RedisChatRoom createRoom(@RequestParam String name) {
//        ChatRoom chatroom = ChatRoom.builder()
//                .roomName(name)
//                .chatMembers(null)
//                .post(null)
//                .build();
//        return chatRoomRepository.save(chatroom);
        return redisChatRoomRepository.createChatRoom(name,"1");
    }

    @GetMapping("/room/enter/{roomId}")
    public String roomDetail(Model model, @PathVariable String roomId) {
        model.addAttribute("roomId", roomId);
        return "/chat/roomdetail";
    }

    @GetMapping("/room/{roomId}")
    @ResponseBody
    public RedisChatRoom roomInfo(@PathVariable String roomId) {
        return redisChatRoomRepository.findRoomById(roomId);
    }
}
