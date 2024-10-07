package com.cookbook.CookBook.models;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class FoodDto {
	@NotEmpty(message="Required")
	private String name;

	@NotEmpty(message="Required")
	private String category;

	@Min(1)
	private double price;

	@Size(min=10, message="Please make the description longer than 10 characters")
	@Size(max=200, message="Please make the description shorter than 200 characters")
	private String description;

	private MultipartFile imageFile;

	// New id field
	private int id;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public MultipartFile getImageFile() {
		return imageFile;
	}

	public void setImageFile(MultipartFile imageFile) {
		this.imageFile = imageFile;
	}

	// Getters and setters for id
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
