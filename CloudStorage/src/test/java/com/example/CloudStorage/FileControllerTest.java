package com.example.CloudStorage;

import com.example.CloudStorage.Exception.DataException;
import com.example.CloudStorage.Exception.ServerException;
import com.example.CloudStorage.Exception.UnauthorizedException;
import com.example.CloudStorage.config.CustomUserDetails;
import com.example.CloudStorage.config.jwt.JwtService;
import com.example.CloudStorage.controller.FileController;
import com.example.CloudStorage.dto.InfoFileDto;
import com.example.CloudStorage.dto.TokenDto;
import com.example.CloudStorage.dto.UserDto;
import com.example.CloudStorage.model.File;
import com.example.CloudStorage.model.User;
import com.example.CloudStorage.repository.FileRepository;
import com.example.CloudStorage.repository.TokenRepository;
import com.example.CloudStorage.repository.UserRepository;
import com.example.CloudStorage.service.FileService;
import com.example.CloudStorage.service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import javax.naming.AuthenticationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class FileControllerTest {


    @Test
    @DisplayName("Авторизация пользователя - благоприятная")
    void getUserTest() {
        JwtService jwtService = new JwtService();
        jwtService.setJwtSecret("DfRJcgWFW4wrZpGtkCkzKVjjjzIDfuoowFoCoihhunAo3Ho0oHoxvrZo6Pymloul");
        UserRepository userRepository = Mockito.spy(UserRepository.class);
        TokenRepository tokenRepository = Mockito.spy(TokenRepository.class);
        UserService userService = new UserService(userRepository, jwtService, tokenRepository);
        FileService fileService = Mockito.mock(FileService.class);
        FileController fileController = new FileController(userService, fileService);
        Mockito.when(userRepository.findByLogin("oleg@yandex.ru")).thenReturn(Optional.of(new User("oleg@yandex.ru", "123")));
        UserDto userDto = new UserDto();
        userDto.setLogin("oleg@yandex.ru");
        userDto.setPassword("123");
        ResponseEntity<TokenDto> result;
        try {
            result = fileController.getUser(userDto);
        } catch (AuthenticationException e) {
            throw new RuntimeException(e);
        }

        Assertions.assertNotNull(result);

    }

    @Test
    @DisplayName("Авторизация пользователя - ошибка Status Code 400")
    void getUserDataExceptionTest() {
        JwtService jwtService = new JwtService();
        jwtService.setJwtSecret("DfRJcgWFW4wrZpGtkCkzKVjjjzIDfuoowFoCoihhunAo3Ho0oHoxvrZo6Pymloul");
        UserRepository userRepository = Mockito.spy(UserRepository.class);
        TokenRepository tokenRepository = Mockito.spy(TokenRepository.class);
        UserService userService = new UserService(userRepository, jwtService, tokenRepository);
        FileService fileService = Mockito.mock(FileService.class);
        FileController fileController = new FileController(userService, fileService);
        Mockito.when(userRepository.findByLogin("oleg@yandex.ru")).thenReturn(Optional.of(new User("oleg@yandex.ru", "123")));
        UserDto userDto = new UserDto();
        userDto.setLogin("oleg@yandex.ru");
        userDto.setPassword("13");
        ResponseEntity<TokenDto> result;


        Assertions.assertThrows(DataException.class, () -> {
            fileController.getUser(userDto);
        });

    }

    @Test
    @DisplayName("logout пользователя - благоприятная")
    void getLogoutTest() {
        JwtService jwtService = new JwtService();
        jwtService.setJwtSecret("DfRJcgWFW4wrZpGtkCkzKVjjjzIDfuoowFoCoihhunAo3Ho0oHoxvrZo6Pymloul");
        UserRepository userRepository = Mockito.spy(UserRepository.class);
        TokenRepository tokenRepository = Mockito.spy(TokenRepository.class);
        UserService userService = new UserService(userRepository, jwtService, tokenRepository);
        FileService fileService = Mockito.mock(FileService.class);
        FileController fileController = new FileController(userService, fileService);
        byte[] keyBytes = Decoders.BASE64.decode("DfRJcgWFW4wrZpGtkCkzKVjjjzIDfuoowFoCoihhunAo3Ho0oHoxvrZo6Pymloul");
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(UUID.randomUUID().toString())
                .subject("oleg")
                .signWith(key)
                .compact();
        ResponseEntity result = fileController.getLogout("auth-token=" + token);


        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());

    }

    @Test
    @DisplayName("Добавление файла - благоприятный исход")
    void addFileTest() {
        String filename = "one.txt";
        String contentType = "text/plain";
        String content = "Hello, World!";
        MultipartFile file = new MockMultipartFile(filename, content, contentType, content.getBytes(StandardCharsets.UTF_8));
        CustomUserDetails userDto = new CustomUserDetails(new User("oleg@yandex.ru", "123"));
        User user = new User("oleg@yandex.ru", "123");
        File fileStorage = new File(user, filename, "nulloleg@yandex.ru\\one.txt", file.getSize());

        FileRepository fileRepository = Mockito.spy(FileRepository.class);
        Mockito.when(fileRepository.findByUser_LoginAndName(user.getLogin(), filename)).thenReturn(Optional.empty());
        Mockito.when(fileRepository.save(fileStorage)).thenReturn(fileStorage);
        UserService userService = Mockito.mock(UserService.class);
        FileService fileService = new FileService(fileRepository);
        FileController fileController = new FileController(userService, fileService);

        ResponseEntity result = fileController.addFile(filename, file, userDto);

        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    @DisplayName("Добавление файла - ошибка Status Code 400")
    void addFileExceptionTest() {
        String filename = "one.txt";
        String contentType = "text/plain";
        String content = "Hello, World!";
        MultipartFile file = new MockMultipartFile(filename, content, contentType, content.getBytes(StandardCharsets.UTF_8));
        CustomUserDetails userDto = new CustomUserDetails(new User("oleg@yandex.ru", "123"));
        User user = new User("oleg@yandex.ru", "123");
        File fileStorage = new File(user, filename, "nulloleg@yandex.ru\\one.txt", file.getSize());

        FileRepository fileRepository = Mockito.spy(FileRepository.class);
        Mockito.when(fileRepository.findByUser_LoginAndName(user.getLogin(), filename)).thenReturn(null);
        Mockito.when(fileRepository.save(fileStorage)).thenReturn(fileStorage);

        UserService userService = Mockito.mock(UserService.class);
        FileService fileService = new FileService(fileRepository);
        FileController fileController = new FileController(userService, fileService);

        Assertions.assertThrows(DataException.class, () -> {
            fileController.addFile(null, file, userDto);
        });
    }

    @Test
    @DisplayName("Удаление файла - благоприятный исход")
    void deleteFileTest() {
        String filename = "one.txt";
        CustomUserDetails userDto = new CustomUserDetails(new User("oleg@yandex.ru", "123"));
        User user = new User("oleg@yandex.ru", "123");
        File fileStorage = new File(user, filename, "nulloleg@yandex.ru\\one.txt", 1L);
        FileRepository fileRepository = Mockito.spy(FileRepository.class);
        Mockito.when(fileRepository.findByUser_LoginAndName(user.getLogin(), filename)).thenReturn(Optional.of(fileStorage));
        Mockito.when(fileRepository.save(fileStorage)).thenReturn(fileStorage);

        UserService userService = Mockito.mock(UserService.class);
        FileService fileService = new FileService(fileRepository);
        FileController fileController = new FileController(userService, fileService);

        ResponseEntity result = fileController.deleteFile(filename, userDto);

        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    @DisplayName("Удаление файла - ошибка Status Code 400")
    void deleteFileExceptionDataTest() {
        String filename = "one.txt";
        CustomUserDetails userDto = new CustomUserDetails(new User("oleg@yandex.ru", "123"));
        User user = new User("oleg@yandex.ru", "123");
        File fileStorage = new File(user, filename, "nulloleg@yandex.ru\\one.txt", 1L);
        FileRepository fileRepository = Mockito.spy(FileRepository.class);
        Mockito.when(fileRepository.findByUser_LoginAndName(user.getLogin(), filename)).thenReturn(Optional.of(fileStorage));
        Mockito.when(fileRepository.save(fileStorage)).thenReturn(fileStorage);

        UserService userService = Mockito.mock(UserService.class);
        FileService fileService = new FileService(fileRepository);
        FileController fileController = new FileController(userService, fileService);

        Assertions.assertThrows(DataException.class, () -> {
            fileController.deleteFile("", userDto);
        });
    }

    @Test
    @DisplayName("Удаление файла - ошибка Status Code 401")
    void deleteFileExceptionUnauthorizedTest() {
        String filename = "one.txt";
        CustomUserDetails userDto = new CustomUserDetails(new User("oleg@yandex.ru", "123"));
        User user = new User("oleg@yandex.ru", "123");
        File fileStorage = new File(user, filename, "nulloleg@yandex.ru\\one.txt", 1L);
        FileRepository fileRepository = Mockito.spy(FileRepository.class);
        Mockito.when(fileRepository.findByUser_LoginAndName(user.getLogin(), filename)).thenReturn(Optional.empty());
        Mockito.when(fileRepository.save(fileStorage)).thenReturn(fileStorage);

        UserService userService = Mockito.mock(UserService.class);
        FileService fileService = new FileService(fileRepository);
        FileController fileController = new FileController(userService, fileService);

        Assertions.assertThrows(UnauthorizedException.class, () -> {
            fileController.deleteFile(filename, userDto);
        });
    }

    @Test
    @DisplayName("Удаление файла - ошибка Status Code 500")
    void deleteFileExceptionServerTest() {
        String filename = "one.txt";
        CustomUserDetails userDto = new CustomUserDetails(new User("oleg@yandex.ru", "123"));
        User user = new User("oleg@yandex.ru", "123");
        File fileStorage = new File(user, filename, "null", 1L);
        FileRepository fileRepository = Mockito.spy(FileRepository.class);
        Mockito.when(fileRepository.findByUser_LoginAndName(user.getLogin(), filename)).thenReturn(Optional.of(fileStorage));
        Mockito.when(fileRepository.save(fileStorage)).thenReturn(fileStorage);

        UserService userService = Mockito.mock(UserService.class);
        FileService fileService = new FileService(fileRepository);
        FileController fileController = new FileController(userService, fileService);

        Assertions.assertThrows(ServerException.class, () -> {
            fileController.deleteFile(filename, userDto);
        });
    }

    @Test
    @DisplayName("Получение файла - благоприятный исход")
    void getFileTest() {
        String filename = "one.txt";
        java.io.File file =
                new java.io.File("src/main/resources/application.yaml");
        CustomUserDetails userDto = new CustomUserDetails(new User("oleg@yandex.ru", "123"));
        User user = new User("oleg@yandex.ru", "123");
        File fileStorage = new File(user, filename,
                "src/main/resources/application.yaml", file.length());
        FileRepository fileRepository = Mockito.spy(FileRepository.class);
        Mockito.when(fileRepository.findByUser_LoginAndName(user.getLogin(), filename)).thenReturn(Optional.of(fileStorage));


        UserService userService = Mockito.mock(UserService.class);
        FileService fileService = new FileService(fileRepository);
        FileController fileController = new FileController(userService, fileService);

        ResponseEntity<byte[]> resultByte = fileController.getFile(filename, userDto);
        byte[] result = resultByte.getBody();
        byte[] expected;
        try {
            InputStream inputStream = new FileInputStream(file);
            expected = inputStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Assertions.assertEquals(result.length, expected.length);
    }

    @Test
    @DisplayName("Получение файла - ошибка Status Code 400")
    void getFileDataExceptionTest() {
        String filename = "one.txt";
        CustomUserDetails userDto = new CustomUserDetails(new User("oleg@yandex.ru", "123"));
        User user = new User("oleg@yandex.ru", "123");
        File fileStorage = new File(user, filename, "null", 1L);
        FileRepository fileRepository = Mockito.spy(FileRepository.class);
        Mockito.when(fileRepository.findByUser_LoginAndName(user.getLogin(), filename)).thenReturn(Optional.of(fileStorage));

        UserService userService = Mockito.mock(UserService.class);
        FileService fileService = new FileService(fileRepository);
        FileController fileController = new FileController(userService, fileService);

        Assertions.assertThrows(DataException.class, () -> {
            fileController.getFile(null, userDto);
        });
    }

    @Test
    @DisplayName("Получение файла - ошибка Status Code 401")
    void getFileUnauthorizedExceptionTest() {
        String filename = "one.txt";
        CustomUserDetails userDto = new CustomUserDetails(new User("oleg@yandex.ru", "123"));
        User user = new User("oleg@yandex.ru", "123");
        File fileStorage = new File(user, filename, "null", 1L);
        FileRepository fileRepository = Mockito.spy(FileRepository.class);
        Mockito.when(fileRepository.findByUser_LoginAndName(user.getLogin(), filename)).thenReturn(Optional.empty());

        UserService userService = Mockito.mock(UserService.class);
        FileService fileService = new FileService(fileRepository);
        FileController fileController = new FileController(userService, fileService);

        Assertions.assertThrows(UnauthorizedException.class, () -> {
            fileController.getFile(filename, userDto);
        });
    }

    @Test
    @DisplayName("Получение файла - ошибка Status Code 500")
    void getFileServerExceptionTest() {
        String filename = "one.txt";
        CustomUserDetails userDto = new CustomUserDetails(new User("oleg@yandex.ru", "123"));
        User user = new User("oleg@yandex.ru", "123");
        File fileStorage = new File(user, filename, "null", 1L);
        FileRepository fileRepository = Mockito.spy(FileRepository.class);
        Mockito.when(fileRepository.findByUser_LoginAndName(user.getLogin(), filename)).thenReturn(Optional.of(fileStorage));

        UserService userService = Mockito.mock(UserService.class);
        FileService fileService = new FileService(fileRepository);
        FileController fileController = new FileController(userService, fileService);

        Assertions.assertThrows(ServerException.class, () -> {
            fileController.getFile(filename, userDto);
        });
    }

    @Test
    @DisplayName("Переименование файла - благоприятный исход")
    void renameFileTest() {
        String filename = "test.txt";
        String fileNameNew = "fileNew.txt";
        java.io.File file =
                new java.io.File("src/test/test.txt");

        CustomUserDetails userDto = new CustomUserDetails(new User("oleg@yandex.ru", "123"));
        User user = new User("oleg@yandex.ru", "123");
        File fileStorage = new File(user, filename,
                "src/test/test.txt", file.length());
        FileRepository fileRepository = Mockito.spy(FileRepository.class);
        Mockito.when(fileRepository.findByUser_LoginAndName(user.getLogin(), filename)).thenReturn(Optional.of(fileStorage));
        File fileNew = new File(user, filename,
                "src/test/fileNew.txt", file.length());
        Mockito.when(fileRepository.findByUser_LoginAndName(user.getLogin(), filename)).thenReturn(Optional.of(fileNew));

        UserService userService = Mockito.mock(UserService.class);
        FileService fileService = new FileService(fileRepository);
        FileController fileController = new FileController(userService, fileService);

        ResponseEntity result = fileController.putFile(filename, new InfoFileDto(fileNameNew, 1L), userDto);

        java.io.File newName = new java.io.File("src/test/fileNew.txt");
        newName.renameTo(file);
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    @DisplayName("Переименование файла - ошибка Status Code 400")
    void renameFileDataExceptionTest() {
        String filename = "test.txt";
        String fileNameNew = "fileNew.txt";
        java.io.File file =
                new java.io.File("src/test/test.txt");

        CustomUserDetails userDto = new CustomUserDetails(new User("oleg@yandex.ru", "123"));
        User user = new User("oleg@yandex.ru", "123");
        File fileStorage = new File(user, filename,
                "src/test/test.txt", file.length());
        FileRepository fileRepository = Mockito.spy(FileRepository.class);
        Mockito.when(fileRepository.findByUser_LoginAndName(user.getLogin(), filename)).thenReturn(Optional.of(fileStorage));

        UserService userService = Mockito.mock(UserService.class);
        FileService fileService = new FileService(fileRepository);
        FileController fileController = new FileController(userService, fileService);

        Assertions.assertThrows(DataException.class, () -> {
            fileController.putFile(null, new InfoFileDto(fileNameNew, 1L), userDto);
        });
    }

    @Test
    @DisplayName("Переименование файла - ошибка Status Code 401")
    void renameFileUnauthorizedExceptionTest() {
        String filename = "test.txt";
        String fileNameNew = "fileNew.txt";
        java.io.File file =
                new java.io.File("src/test/test.txt");

        CustomUserDetails userDto = new CustomUserDetails(new User("oleg@yandex.ru", "123"));
        User user = new User("oleg@yandex.ru", "123");
        File fileStorage = new File(user, filename,
                "src/test/test.txt", file.length());
        FileRepository fileRepository = Mockito.spy(FileRepository.class);
        Mockito.when(fileRepository.findByUser_LoginAndName(user.getLogin(), filename)).thenReturn(Optional.empty());

        UserService userService = Mockito.mock(UserService.class);
        FileService fileService = new FileService(fileRepository);
        FileController fileController = new FileController(userService, fileService);

        Assertions.assertThrows(UnauthorizedException.class, () -> {
            fileController.putFile(filename, new InfoFileDto(fileNameNew, 1L), userDto);
        });
    }

    @Test
    @DisplayName("Переименование файла - ошибка Status Code 500")
    void renameFileServerExceptionTest() {
        String filename = "test.txt";
        String fileNameNew = "fileNew.txt";
        java.io.File file =
                new java.io.File("src/test/test.txt");

        CustomUserDetails userDto = new CustomUserDetails(new User("oleg@yandex.ru", "123"));
        User user = new User("oleg@yandex.ru", "123");
        File fileStorage = new File(user, filename,
                "src/test/test.txt", file.length());
        FileRepository fileRepository = Mockito.spy(FileRepository.class);
        Mockito.when(fileRepository.findByUser_LoginAndName(user.getLogin(), filename)).thenReturn(Optional.of(fileStorage));
        File fileNew = new File(user, filename,
                null, file.length());
        Mockito.when(fileRepository.findByUser_LoginAndName(user.getLogin(), fileStorage.getName())).thenReturn(Optional.of(fileNew));

        UserService userService = Mockito.mock(UserService.class);
        FileService fileService = new FileService(fileRepository);
        FileController fileController = new FileController(userService, fileService);

        Assertions.assertThrows(ServerException.class, () -> {
            fileController.putFile(filename, new InfoFileDto(fileNameNew, 1L), userDto);
        });
    }

    @Test
    @DisplayName("Получение списка файлов - благоприятный исход")
    void getListFileTest() {
        String filename = "one.txt";
        CustomUserDetails userDto = new CustomUserDetails(new User("oleg@yandex.ru", "123"));
        User user = new User("oleg@yandex.ru", "123");
        File fileStorage = new File(user, filename, "nulloleg@yandex.ru\\one.txt", 1L);
        FileRepository fileRepository = Mockito.spy(FileRepository.class);
        List<File> list = new ArrayList<>();
        Mockito.when(fileRepository.findFirst3ByByUser_Login(user.getLogin(), 3L)).thenReturn(Optional.of(list));

        UserService userService = Mockito.mock(UserService.class);
        FileService fileService = new FileService(fileRepository);
        FileController fileController = new FileController(userService, fileService);

        ResponseEntity result = fileController.get(3L, userDto);

        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    @DisplayName("Получение списка файлов - ошибка Status Code 400")
    void getListFileDataExceptionTest() {
        String filename = "one.txt";
        CustomUserDetails userDto = new CustomUserDetails(new User("oleg@yandex.ru", "123"));
        User user = new User("oleg@yandex.ru", "123");
        FileRepository fileRepository = Mockito.spy(FileRepository.class);
        Mockito.when(fileRepository.findFirst3ByByUser_Login(user.getLogin(), 3L)).thenReturn(Optional.empty());

        UserService userService = Mockito.mock(UserService.class);
        FileService fileService = new FileService(fileRepository);
        FileController fileController = new FileController(userService, fileService);

        Assertions.assertThrows(DataException.class, () -> {
            fileController.get(0L, userDto);
        });
    }

    @Test
    @DisplayName("Получение списка файлов - ошибка Status Code 401")
    void getListFileUnauthorizedExceptionTest() {
        CustomUserDetails userDto = new CustomUserDetails(new User("oleg@yandex.ru", "123"));
        User user = new User("oleg@yandex.ru", "123");
        FileRepository fileRepository = Mockito.spy(FileRepository.class);
        Mockito.when(fileRepository.findFirst3ByByUser_Login(user.getLogin(), 3L)).thenReturn(Optional.empty());

        UserService userService = Mockito.mock(UserService.class);
        FileService fileService = new FileService(fileRepository);
        FileController fileController = new FileController(userService, fileService);

        Assertions.assertThrows(UnauthorizedException.class, () -> {
            fileController.get(3L, userDto);
        });
    }

    @Test
    @DisplayName("Получение списка файлов - ошибка Status Code 500")
    void getListFileServerExceptionTest() {
        String filename = "one.txt";
        CustomUserDetails userDto = new CustomUserDetails(new User("oleg@yandex.ru", "123"));
        User user = new User("oleg@yandex.ru", "123");
        File fileStorage = new File(user, filename, "nulloleg@yandex.ru\\one.txt", 1L);
        FileRepository fileRepository = Mockito.spy(FileRepository.class);
        List<File> list = new ArrayList<>();
        list.add(null);
        Mockito.when(fileRepository.findFirst3ByByUser_Login(user.getLogin(), 3L)).thenReturn(Optional.of(list));

        UserService userService = Mockito.mock(UserService.class);
        FileService fileService = new FileService(fileRepository);
        FileController fileController = new FileController(userService, fileService);

        Assertions.assertThrows(ServerException.class, () -> {
            fileController.get(3L, userDto);
        });
    }

}
