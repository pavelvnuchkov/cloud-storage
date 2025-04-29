package com.example.CloudStorage.service;

import com.example.CloudStorage.Exception.DataException;
import com.example.CloudStorage.Exception.ServerException;
import com.example.CloudStorage.Exception.UnauthorizedException;
import com.example.CloudStorage.dto.ExceptionDto;
import com.example.CloudStorage.dto.InfoFileDto;
import com.example.CloudStorage.model.File;
import com.example.CloudStorage.model.User;
import com.example.CloudStorage.repository.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FileService {

    @Value("${path.storage}")
    private String pathStorage;

    private final FileRepository fileRepository;


    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;

    }


    public boolean addFile(String fileName, MultipartFile file, User user) {
        if (fileName == null || file == null || fileName.isBlank()) {
            throw new DataException(new ExceptionDto("Error input data", 15));
        }
        Optional<File> optionalFile = fileRepository.findByUser_LoginAndName(user.getLogin(), fileName);
        if (optionalFile.isPresent()) {
            throw new DataException(new ExceptionDto("Error input data", 14));
        }
        Path filepath = Path.of(pathStorage + user.getLogin(), fileName);
        try {
            Files.createDirectories(filepath.getParent());
            Path path = Files.write(filepath, file.getInputStream().readAllBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            java.io.File fileStorage = new java.io.File(path.toString());
            fileRepository.save(new File(user, fileName, path.toString(), fileStorage.length()));
            return true;
        } catch (IOException e) {
            throw new UnauthorizedException(new ExceptionDto(" Unauthorized error", 13));
        }

    }

    public List<InfoFileDto> getInfoFile(Long limit, User user) {
        if (limit == null || limit <= 0) {
            throw new DataException(new ExceptionDto("Error input data", 10));
        }
        Optional<List<File>> optionalList = fileRepository.findFirst3ByByUser_Login(user.getLogin(), limit);
        List<InfoFileDto> infoFileList = new ArrayList<>();
        if (optionalList.isPresent()) {

            for (int i = 0; i < optionalList.get().size(); i++) {
                try {
                    infoFileList.add(new InfoFileDto(optionalList.get().get(i).getName(), optionalList.get().get(i).getSize()));
                } catch (RuntimeException e) {
                    throw new ServerException(new ExceptionDto("Error getting file list", 11));
                }
            }
        } else {
            throw new UnauthorizedException(new ExceptionDto("Unauthorized error", 12));
        }
        return infoFileList;
    }

    public void renameFile(String fileName, String fileNameNew, User user) {
        if (fileName == null || fileName.isBlank()) {
            throw new DataException(new ExceptionDto("Error input data", 8));
        }
        if (user == null) {
            throw new UnauthorizedException(new ExceptionDto("Unauthorized error", 9));
        }
        Optional<File> optionalFile = fileRepository.findByUser_LoginAndName(user.getLogin(), fileName);
        if (optionalFile.isEmpty()) {
            throw new UnauthorizedException(new ExceptionDto("Unauthorized error", 9));
        }
        try {
            Path path = Path.of(optionalFile.get().getPath());
            java.io.File fileNew = new java.io.File(path.getParent() + "\\" + fileNameNew);
            java.io.File fileOld = new java.io.File(path.toString());
            if (fileOld.renameTo(fileNew)) {
                optionalFile.get().setPath(fileNew.getPath());
                optionalFile.get().setName(fileNameNew);
                File fileBase = fileRepository.save(optionalFile.get());
                if (fileBase != optionalFile.get()) {
                    throw new ServerException(new ExceptionDto("Error upload file", 7));
                }
            }
        } catch (RuntimeException e) {
            throw new ServerException(new ExceptionDto("Error upload file", 7));
        }

    }

    public void deleteFile(String fileName, User user) {
        if (fileName == null || fileName.isBlank()) {
            throw new DataException(new ExceptionDto("Error input data", 5));
        }
        Optional<File> optionalFile = fileRepository.findByUser_LoginAndName(user.getLogin(), fileName);
        if (optionalFile.isEmpty()) {
            throw new UnauthorizedException(new ExceptionDto("Error input data", 5));
        }
        try {
            java.io.File file = new java.io.File(optionalFile.get().getPath());
            if (file.delete()) {
                fileRepository.delete(optionalFile.get());
            } else {
                throw new UnauthorizedException(new ExceptionDto("Error input data", 5));
            }
        } catch (RuntimeException e) {
            throw new ServerException(new ExceptionDto("Error delete file", 4));
        }
    }

    public byte[] getFile(String fileName, User user) {
        if (user == null || fileName == null || fileName.isBlank()) {
            throw new DataException(new ExceptionDto("Error input data", 2));
        }
        Optional<File> optionalFile = fileRepository.findByUser_LoginAndName(user.getLogin(), fileName);
        if (optionalFile.isEmpty()) {
            throw new UnauthorizedException(new ExceptionDto("Unauthorized error", 3));
        }
        try {
            java.io.File file = new java.io.File(optionalFile.get().getPath());
            InputStream inputStream = new FileInputStream(file);
            byte[] arrByte = inputStream.readAllBytes();
            return arrByte;
        } catch (IOException e) {
            throw new ServerException(new ExceptionDto("Error upload file", 1));
        }

    }
}
