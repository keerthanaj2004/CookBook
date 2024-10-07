package com.cookbook.CookBook.controllers;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.cookbook.CookBook.models.Cook;
import com.cookbook.CookBook.models.FoodDto;
import com.cookbook.CookBook.service.FoodRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/foods")
public class FoodController {

    @Autowired
    private FoodRepository foodRepository; // renamed for clarity
    
    @GetMapping({"","/"})
    public String showFoodList(Model model) {
        List<Cook> foods = foodRepository.findAll(Sort.by(Sort.Direction.DESC, "id")); // call on foodRepository
        model.addAttribute("foods", foods);
        return "book/index";
    }
    
    @GetMapping("/edit")
    public String showEditPage(Model model, @RequestParam int id) {
        try {
            Cook c = foodRepository.findById(id).orElse(null);
            if (c == null) {
                return "redirect:/foods"; // Redirect if not found
            }
            model.addAttribute("cook", c);  // Make sure this object is named 'cook'
            
            // If you're also passing a FoodDto
            FoodDto foodDto = new FoodDto();
            foodDto.setName(c.getFoodname());
            foodDto.setCategory(c.getFoodcategory());
            foodDto.setPrice(c.getPrice());
            foodDto.setDescription(c.getDescription());

            model.addAttribute("foodDto", foodDto); // Ensure 'foodDto' is passed to the model
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            return "redirect:/foods";
        }

        return "book/editfood";
    }


    
    
 // Handle the update for the food item
    @PostMapping("/edit")
    public String updateProduct(@RequestParam int id,
                                @Valid @ModelAttribute FoodDto food,
                                BindingResult result,
                                Model model) {
        if (result.hasErrors()) {
            model.addAttribute("foodDto", food);
            return "foods/editfood"; // Show the form again with errors
        }

        try {
            Cook c = foodRepository.findById(id).get();

            // Update food details
            c.setFoodname(food.getName());
            c.setFoodcategory(food.getCategory());
            c.setDescription(food.getDescription());
            c.setPrice(food.getPrice());

            // Handle the image upload
            if (!food.getImageFile().isEmpty()) {
                // Delete old image
                String uploadDir = "src/main/resources/static/foodmood/";
                Path oldImagePath = Paths.get(uploadDir + c.getImageFileName());
                try {
                    Files.delete(oldImagePath);
                } catch (Exception ex) {
                    System.out.println("Exception: " + ex.getMessage());
                }

                // Save new image file
                MultipartFile image = food.getImageFile();
                String storageFileName = image.getOriginalFilename();
                try (InputStream inputStream = image.getInputStream()) {
                    Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
                }
                c.setImageFileName(storageFileName);
            }

            foodRepository.save(c);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return "redirect:/foods";
    }
    
    
    @GetMapping("/delete")
    public String deleteProduct(@RequestParam int id) {
        try {
            Optional<Cook> optionalCook = foodRepository.findById(id);
            if (optionalCook.isPresent()) {
                Cook c = optionalCook.get();
                Path imagePath = Paths.get("src/main/resources/static/foodmood/" + c.getImageFileName());

                // Delete the image file
                try {
                    Files.delete(imagePath);
                } catch (Exception e) {
                    System.out.println("Error deleting image: " + e.getMessage());
                }

                // Delete the product from the repository
                foodRepository.delete(c);
            } else {
                System.out.println("Product not found for id: " + id);
            }
        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }

        return "redirect:/foods"; // Redirect to the correct food list URL
    }



    
    @GetMapping("/create")
    public String showCreatePage(Model model) {
        FoodDto foodDto = new FoodDto();
        model.addAttribute("foodDto", foodDto);
        return "book/addfood";
    }
    
    
    
    @PostMapping("/create")
    public String addFood(@Valid @ModelAttribute FoodDto food, BindingResult result) {
        // Check for validation errors
        if (result.hasErrors()) {
            return "book/addfood"; // Return to form with errors if there are any
        }

        // Check if file is empty
        if (food.getImageFile().isEmpty()) {
            result.addError(new FieldError("FoodDto", "imageFile", "Image file is required"));
            return "book/addfood"; // Return to form with error if image file is empty
        }

        MultipartFile image = food.getImageFile();
        String storageFileName = image.getOriginalFilename(); // Add unique identifier

        String uploadDir = "src/main/resources/static/foodmood/";
        Path uploadPath = Paths.get(uploadDir);

        try {
            // Create directories if they don't exist
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Save the file to the directory
            try (InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, uploadPath.resolve(storageFileName), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception ex) {
            System.out.println("Error uploading image: " + ex.getMessage());
            result.addError(new FieldError("FoodDto", "imageFile", "Could not upload image. Please try again."));
            return "book/addfood"; // Return to form with error
        }

        // Save food data to database
        Cook cook = new Cook();
        cook.setFoodname(food.getName());
        cook.setFoodcategory(food.getCategory());
        cook.setPrice(food.getPrice());
        cook.setDescription(food.getDescription());
        cook.setImageFileName(storageFileName); // Save the file name to the database

        foodRepository.save(cook); // Save the Cook object

        return "redirect:/foods"; // Redirect to the food list
    }

    
}
