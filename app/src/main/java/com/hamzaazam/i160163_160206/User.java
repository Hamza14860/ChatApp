package com.hamzaazam.i160163_160206;

public class User {

    public String username;
    public String imageURL;
    public String id;
    private String status;
    private String search;

    public User(String username, String imageURL, String id, String status, String search) {
        this.username = username;
        this.imageURL = imageURL;
        this.id = id;
        this.status=status;
        this.search=search;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public User(){}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
