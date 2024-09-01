package com.example.myapplication.Profile;


public class UserProfile {
    private String name;
    private float weight;
    private int age;
    private float height;
    private byte[] image;
    private String sex;

    public UserProfile(String name, float weight, int age, float height, byte[] image,String sex) {
        this.name = name;
        this.weight = weight;
        this.age = age;
        this.height = height;
        this.image = image;
        this.sex =sex;
    }

    // Getters for the fields
    public String getName() { return name; }
    public float getWeight() { return weight; }
    public int getAge() { return age; }
    public float getHeight() { return height; }
    public byte[] getImage() { return image; }
    public String getSex() { return sex; }
}

