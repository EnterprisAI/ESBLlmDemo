package com.esb.llm.ESBLlmDemo.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import com.esb.llm.ESBLlmDemo.model.SourceDto;
import org.mapstruct.Named;

public class UserMapperHelp {

    /**
     * Calculates experience bonus based on years of service
     */
    public BigDecimal calculateExperienceBonus(LocalDate doj) {
        if (doj == null) {
            return BigDecimal.ZERO;
        }
        
        long yearsOfService = ChronoUnit.YEARS.between(doj, LocalDate.now());
        
        // Bonus calculation: 5% for each year of service, capped at 50%
        BigDecimal bonusPercentage = BigDecimal.valueOf(Math.min(yearsOfService * 5, 50));
        return bonusPercentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
    }

    /**
     * Calculates performance multiplier based on age and experience
     */
    public BigDecimal calculatePerformanceMultiplier(int age, LocalDate doj) {
        if (doj == null) {
            return BigDecimal.ONE;
        }
        
        long yearsOfService = ChronoUnit.YEARS.between(doj, LocalDate.now());
        
        // Performance multiplier based on age and experience
        // Younger employees (25-35) with 2-5 years experience get higher multiplier
        // Senior employees (35-50) with 5+ years get moderate multiplier
        // Very senior employees (50+) get standard multiplier
        
        BigDecimal baseMultiplier = BigDecimal.ONE;
        
        if (age >= 25 && age <= 35 && yearsOfService >= 2 && yearsOfService <= 5) {
            baseMultiplier = BigDecimal.valueOf(1.15); // High potential
        } else if (age >= 35 && age <= 50 && yearsOfService >= 5) {
            baseMultiplier = BigDecimal.valueOf(1.08); // Experienced
        } else if (age > 50) {
            baseMultiplier = BigDecimal.valueOf(1.02); // Senior
        }
        
        return baseMultiplier;
    }

    /**
     * Calculates market adjustment factor based on current market conditions
     */
    public BigDecimal calculateMarketAdjustment() {
        // Simulate market conditions - in real scenario this would come from external service
        // For demo purposes, we'll use a random-like calculation based on current date
        
        LocalDate now = LocalDate.now();
        int dayOfYear = now.getDayOfYear();
        int year = now.getYear();
        
        // Create a pseudo-random but deterministic market factor
        double marketFactor = 0.95 + (Math.sin(dayOfYear * 0.1) * 0.1) + (year % 10) * 0.01;
        
        return BigDecimal.valueOf(marketFactor).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * Main salary calculation method that combines all factors
     */
    public BigDecimal calculateAdjustedSalary(BigDecimal baseSalary, int age, LocalDate doj) {
        if (baseSalary == null || baseSalary.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        // Get all adjustment factors
        BigDecimal experienceBonus = calculateExperienceBonus(doj);
        BigDecimal performanceMultiplier = calculatePerformanceMultiplier(age, doj);
        BigDecimal marketAdjustment = calculateMarketAdjustment();
        
        // Apply all adjustments
        BigDecimal adjustedSalary = baseSalary
            .multiply(BigDecimal.ONE.add(experienceBonus))
            .multiply(performanceMultiplier)
            .multiply(marketAdjustment);
        
        return adjustedSalary.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * MapStruct @Named method for salary calculation from SourceDto
     */
    @Named("calculateAdjustedSalaryFromSource")
    public BigDecimal calculateAdjustedSalaryFromSource(BigDecimal salary, SourceDto sourceDto) {
        if (salary == null || sourceDto == null) {
            return BigDecimal.ZERO;
        }
        return calculateAdjustedSalary(salary, sourceDto.getAge(), sourceDto.getDoj());
    }
} 