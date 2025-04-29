package com.example.CloudStorage;

import com.example.CloudStorage.model.User;
import com.example.CloudStorage.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CloudStorageApplication{

    public static void main(String[] args) {
        SpringApplication.run(CloudStorageApplication.class, args);
    }

}
