package com.example.CloudStorage;

import com.example.CloudStorage.dto.InfoFileDto;
import com.example.CloudStorage.dto.UserDto;
import com.example.CloudStorage.model.File;
import com.example.CloudStorage.model.User;
import com.example.CloudStorage.repository.FileRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;

@Testcontainers
@TestConfiguration(proxyBeanMethods = false)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostgresIntegrationITest {

    @LocalServerPort
    private Integer port;

    private static String token = "eyJhbGciOiJIUzM4NCJ9.eyJqdGkiOiI1ZmY2NjkwNi0wMmI2LTRmNTctOWEwZC1lOWUwMTg4NjViZTMiLCJzdWIiOiJwYXZlbCJ9.8M9_1g8K7-kPNMnuaZDJHcMuXRFkIcjwRL90Fpe4VTQ7b3Zb2A-HMohQSnjr17sU";

    @Autowired
    TestRestTemplate template;

    @Autowired
    FileRepository fileRepository;


    @Container
    @ServiceConnection
    private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.2");

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        fileRepository.deleteAll();
    }

    @Test
    @DisplayName("Авторизация - позитивный исход")
    void getTokenTest() {

        UserDto userDto = new UserDto();
        userDto.setLogin("pavel");
        userDto.setPassword("123");
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(userDto)
                .when()
                .request(Method.POST, "/login")
                .then()
                .statusCode(200)
                .body("id", Matchers.nullValue())
                .body("auth-token", Matchers.notNullValue())
                .extract()
                .response()
                .getBody().jsonPath();
    }

    @Test
    @DisplayName("Авторизация - Bad Request")
    void getTokenExceptionTest() {

        UserDto userDto = new UserDto();
        userDto.setLogin("pavel");
        userDto.setPassword("223");
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(userDto)
                .when()
                .request(Method.POST, "/login")
                .then()
                .statusCode(400);


    }

    @Test
    @DisplayName("Содержимое хранилища пользователя - благоприятный исход")
    void getInfoFileTest() {
        User user = new User("pavel", "123");
        File file1 = new File(user, "file1", "c:/1/file1.txt", 1L);
        File file2 = new File(user, "file2", "c:/1/file2.txt", 2L);
        fileRepository.save(file1);
        fileRepository.save(file2);

        UserDto userDto = new UserDto();
        userDto.setLogin("pavel");
        userDto.setPassword("123");
        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("auth-token", token)
                .param("limit", 3)
                .when()
                .request(Method.GET, "/list")
                .then()
                .statusCode(200)
                .body(".", Matchers.hasSize(2));

    }

    @Test
    @DisplayName("Содержимое хранилища пользователя - Bad Request")
    void getInfoFileExceptionDataTest() {
        User user = new User("pavel", "123");
        File file1 = new File(user, "file1", "c:/1/file1.txt", 1L);
        File file2 = new File(user, "file2", "c:/1/file2.txt", 2L);
        fileRepository.save(file1);
        fileRepository.save(file2);
        UserDto userDto = new UserDto();
        userDto.setLogin("pavel");
        userDto.setPassword("123");
        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("auth-token", token)
                .param("limit", 0)
                .when()
                .request(Method.GET, "/list")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Добавление файла в хранилище - благоприятный")
    void addFileTest() {
        java.io.File file = new java.io.File("src/test/test.txt");
        RestAssured.given()
                .contentType(ContentType.MULTIPART)
                .cookie("auth-token", token)
                .queryParam("filename", file.getName())
                .multiPart(file)
                .when()
                .request(Method.POST, "/file")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Добавление файла в хранилище - Bad Request")
    void addFileExceptionDataTest() throws IOException {
        java.io.File file = new java.io.File("src/test/test.txt");
        RestAssured.given()
                .contentType(ContentType.MULTIPART)
                .cookie("auth-token", token)
                .queryParam("filename", "")
                .multiPart(file)
                .when()
                .request(Method.POST, "/file")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Изменение файла в хранилище - благоприятный")
    void putFileTest() {
        User user = new User("pavel", "123");
        File file = new File(user, "file1", "c:/1/file1.txt", 1L);
        InfoFileDto infoFileDto = new InfoFileDto("new.txt", null);
        fileRepository.save(file);
        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("auth-token", token)
                .queryParam("filename", file.getName())
                .body(infoFileDto)
                .when()
                .request(Method.PUT, "/file")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Изменение файла в хранилище - Bad Request")
    void putFileExceptionDataTest() {
        User user = new User("pavel", "123");
        File file = new File(user, "file1", "c:/1/file1.txt", 1L);
        InfoFileDto infoFileDto = new InfoFileDto("new.txt", null);
        fileRepository.save(file);
        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("auth-token", token)
                .queryParam("filename", "")
                .body(infoFileDto)
                .when()
                .request(Method.PUT, "/file")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Удаление файла в хранилище - благоприятный")
    void deleteFileTest() {
        java.io.File file = new java.io.File("D:\\Java\\Netologia\\Diplom\\CloudStorage\\src\\main\\resources\\application.yaml");
        RestAssured.given()
                .contentType(ContentType.MULTIPART)
                .cookie("auth-token", token)
                .queryParam("filename", file.getName())
                .multiPart(file)
                .when()
                .request(Method.POST, "/file");

        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("auth-token", token)
                .queryParam("filename", file.getName())
                .when()
                .request(Method.DELETE, "/file")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Удаление файла в хранилище - Bad Request")
    void deleteFileExceptionDataTest() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("auth-token", token)
                .queryParam("filename", "")
                .when()
                .request(Method.DELETE, "/file")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Получение файла - благоприятный")
    void getFileTest() {
        java.io.File file = new java.io.File("src/test/test.txt");
        RestAssured.given()
                .contentType(ContentType.MULTIPART)
                .cookie("auth-token", token)
                .queryParam("filename", file.getName())
                .multiPart(file)
                .when()
                .request(Method.POST, "/file")
                .then()
                .statusCode(200);
        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("auth-token", token)
                .queryParam("filename", file.getName())
                .when()
                .request(Method.GET, "/file")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Получение файла - Bad Request")
    void getFileExceptionDataTest() {

        RestAssured.given()
                .contentType(ContentType.JSON)
                .cookie("auth-token", token)
                .queryParam("filename", "")
                .when()
                .request(Method.GET, "/file")
                .then()
                .statusCode(400);
    }


}
