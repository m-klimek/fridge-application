package com.infoshareacademy.controller;


import com.infoshareacademy.entity.food_preferences.FoodPreferences;
import com.infoshareacademy.service.FoodPreferencesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;


@Controller
public class FoodPreferencesController {

    @Autowired
    private FoodPreferencesService foodPreferencesService;

    public FoodPreferencesController(FoodPreferencesService foodPreferencesService) {
        this.foodPreferencesService = foodPreferencesService;
    }

    @GetMapping("/foodpreferences")
    public String listFoodPreferences(Model model) {
        model.addAttribute("getfoodpreferences", foodPreferencesService.getFoodPreferences());
        return "foodpreferences";
    }

    @GetMapping("/foodpreferences/{id}")
    public String getFoodPreferencesById(@PathVariable Long id, Model model) {
        model.addAttribute("foodpreferencesbyid", foodPreferencesService.getFoodPreferencesById(id));
        return "foodpreferencesbyid";
    }

    @GetMapping("/foodpreferences/recipe/{id}")
    public String getRecipeByAllergens(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("recipbeByFoodPreferences", foodPreferencesService.filterRecipeByFoodPreferences(id));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "recipebyfoodpreferences";
    }

    @GetMapping("/foodpreferences/set")
    public String createAllergensForm(Model model) {

        FoodPreferences foodPreferences = new FoodPreferences();
        model.addAttribute("foodpreferences", foodPreferences);
        return "setfoodpreferences";

    }

    @PostMapping("/foodpreferences")
    public String saveAllergens(@ModelAttribute("foodpreferences") FoodPreferences foodPreferences) {
        foodPreferencesService.setFoodPreferences(foodPreferences);
        return "redirect:/foodpreferences";
    }


}
