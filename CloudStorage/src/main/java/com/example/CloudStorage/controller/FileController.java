package com.example.CloudStorage.controller;

import com.example.CloudStorage.dto.InfoFileDto;
import com.example.CloudStorage.dto.TokenDto;
import com.example.CloudStorage.dto.UserDto;
import com.example.CloudStorage.model.User;
import com.example.CloudStorage.service.FileService;
import com.example.CloudStorage.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.AuthenticationException;
import java.util.List;

@RestController
public class FileController {

    private final UserService userService;
    private final FileService fileService;

    public FileController(UserService userService, FileService fileService) {
        this.userService = userService;
        this.fileService = fileService;

    }

    @PostMapping(path = "/login")
    public ResponseEntity<TokenDto> getUser(@RequestBody UserDto userDto) throws AuthenticationException {
        return ResponseEntity.ok(userService.auth(userDto));
    }

    @GetMapping("/login{logout}")
    public ResponseEntity getLogout(@RequestHeader("COOKIE") String autoToken) {
        if (autoToken != null && autoToken.startsWith("auth-token=")) {
            String token = autoToken.substring(11);
            userService.blockedToken(token);
        }
        return ResponseEntity.ok("Success logout");
    }

    @GetMapping("/list")
    public ResponseEntity<List<InfoFileDto>> get(@RequestParam("limit") Long limit,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserAuthentication(userDetails);
        List<InfoFileDto> list = fileService.getInfoFile(limit, user);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/file")
    public ResponseEntity addFile(@RequestParam("filename") String filename,
                                  @RequestBody MultipartFile file,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserAuthentication(userDetails);
        System.out.println("filename - " + filename);
        fileService.addFile(filename, file, user);
        return ResponseEntity.ok("Success upload");
    }

    @PutMapping("/file")
    public ResponseEntity putFile(@RequestParam("filename") String fileName,
                                  @RequestBody InfoFileDto infoFileDto,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserAuthentication(userDetails);
        fileService.renameFile(fileName, infoFileDto.getName(), user);
        return ResponseEntity.ok("Success upload");
    }

    @DeleteMapping("/file")
    public ResponseEntity deleteFile(@RequestParam("filename") String fileName,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserAuthentication(userDetails);
        fileService.deleteFile(fileName, user);
        return ResponseEntity.ok("Success deleted");
    }

    @GetMapping("/file")
    public ResponseEntity<byte[]> getFile(@RequestParam("filename") String fileName,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUserAuthentication(userDetails);
        return new ResponseEntity<>(fileService.getFile(fileName, user), HttpStatus.OK);
    }

    private User getUserAuthentication(UserDetails userDetails) {
        return new User(userDetails.getUsername(), userDetails.getPassword());
    }
}
