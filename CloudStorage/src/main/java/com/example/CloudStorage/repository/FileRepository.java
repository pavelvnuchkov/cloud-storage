package com.example.CloudStorage.repository;

import com.example.CloudStorage.model.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    @Query(value = "SELECT * FROM user_file f WHERE f.user_email = ?1 LIMIT :limit", nativeQuery = true)
     Optional<List<File>> findFirst3ByByUser_Login(String email, Long limit);

    Optional<File> findByUser_LoginAndName(String email, String fileName);
}
