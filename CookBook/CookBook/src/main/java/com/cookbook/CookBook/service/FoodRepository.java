package com.cookbook.CookBook.service;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cookbook.CookBook.models.*;

public interface FoodRepository extends JpaRepository<Cook,Integer> {
	
}
