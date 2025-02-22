package com.kafka.demo.producer.service;


import com.kafka.demo.producer.model.FolderPath;
import com.kafka.demo.producer.model.MessageType;
import com.kafka.demo.producer.producer.MessageProducer;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@Service
public class MessageService {

    private final MessageProducer messageProducer;

    public MessageService(MessageProducer messageProducer) {
        this.messageProducer = messageProducer;
    }

    public String sendMessage(FolderPath folderPath) throws IOException {
        Path absolutePath = validateFolderPath(folderPath);
        MessageType messageType = MessageType.valueOf(folderPath.type());

        List<Path> filesPath = getFilesPath(absolutePath);

        if (messageType == MessageType.CONSUMER) {
            for (Path file : filesPath) {
                messageProducer.sendCsvToConsumer(file);
            }
        } else if (messageType == MessageType.KAFKA) {
            for (Path file : filesPath) {
                messageProducer.sendCsvToKafka(file);
            }
        }

        return "Todos os arquivos CSV da pasta foram processados com sucesso!";
    }

    private static Path validateFolderPath(FolderPath folderPath) throws NotDirectoryException {
        String folderPaths = folderPath.path().trim();

        Path path = Paths.get(folderPaths).normalize().toAbsolutePath();
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            throw new NotDirectoryException("O caminho informado não é um diretório válido.\n Path: " + folderPath);
        }
        return path;
    }

    private List<Path> getFilesPath(Path path) throws IOException {
        try (Stream<Path> files = Files.list(path)) {
            return files.filter(file -> file.toString().endsWith(".csv"))
                    .toList();
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }
}
