package com.example.CloudStorage.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "user_file")

public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_email")
    private User user;

    @Column(name = "file_name")
    private String name;

    @Column(name = "file_path")
    private String path;

    @Column(name = "file_size")
    private Long size;

    public File() {
    }

    public File(User user, String name, String path, Long size) {
        this.user = user;
        this.name = name;
        this.path = path;
        this.size = size;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        File file = (File) o;
        return Objects.equals(id, file.id) && Objects.equals(user, file.user) && Objects.equals(name, file.name) && Objects.equals(path, file.path) && Objects.equals(size, file.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, name, path, size);
    }

    @Override
    public String toString() {
        return "File{" +
                "user=" + user +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", size='" + size + '\'' +
                '}';
    }
}
