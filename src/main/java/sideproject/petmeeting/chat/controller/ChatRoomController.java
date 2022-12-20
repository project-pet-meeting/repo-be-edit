package sideproject.petmeeting.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sideproject.petmeeting.chat.domain.ChatRoom;
import sideproject.petmeeting.chat.repository.ChatRoomRepository;

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

    @GetMapping("/room")
    public String rooms(Model model) {
        return "/chat/room";
    }

    @GetMapping("/rooms")
    @ResponseBody
    public List<ChatRoom> room() {
        return chatRoomRepository.findAll();
    }

    @PostMapping("/room")
    @ResponseBody
    public ChatRoom createRoom(@RequestParam String name) {
        ChatRoom chatroom = ChatRoom.builder()
                .roomName(name)
                .chatMembers(null)
                .post(null)
                .build();
        return chatRoomRepository.save(chatroom);
    }

    @GetMapping("/room/enter/{roomId}")
    public String roomDetail(Model model, @PathVariable String roomId) {
        model.addAttribute("roomId", roomId);
        return "/chat/roomdetail";
    }

    @GetMapping("/room/{roomId}")
    @ResponseBody
    public Optional<ChatRoom> roomInfo(@PathVariable Long roomId) {
        return chatRoomRepository.findById(roomId);
    }
}
