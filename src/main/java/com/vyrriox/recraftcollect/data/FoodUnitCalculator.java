package com.vyrriox.recraftcollect.data;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

public class FoodUnitCalculator {

    /**
     * Returns the number of food units for the given stack.
     * Checks item value overrides first, then falls back to nutrition value.
     */
    public static long getFoodUnits(ItemStack stack, Map<String, Integer> overrides) {
        if (stack.isEmpty()) return 0;

        // Check for custom override
        if (overrides != null && !overrides.isEmpty()) {
            String itemId = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
            Integer override = overrides.get(itemId);
            if (override != null) {
                return (long) override * stack.getCount();
            }
        }

        // Default: use nutrition value
        if (!stack.getItem().isEdible()) return 0;
        FoodProperties food = stack.getItem().getFoodProperties();
        if (food == null) return 0;
        return (long) food.getNutrition() * stack.getCount();
    }

    /**
     * Returns true if the item can be deposited (has override or is edible).
     */
    public static boolean isDepositable(ItemStack stack, Map<String, Integer> overrides) {
        if (stack.isEmpty()) return false;
        if (overrides != null && !overrides.isEmpty()) {
            String itemId = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
            if (overrides.containsKey(itemId)) return true;
        }
        return stack.getItem().isEdible();
    }
}
