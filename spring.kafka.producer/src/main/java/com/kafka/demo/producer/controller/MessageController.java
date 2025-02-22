package com.kafka.demo.producer.controller;

import com.kafka.demo.producer.model.FolderPath;
import com.kafka.demo.producer.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/csv")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> uploadCsv(@RequestBody FolderPath folderPath) {
        try {

            return ResponseEntity.ok(messageService.sendMessage(folderPath));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao processar a pasta: " + e.getMessage());
        }
    }


}
