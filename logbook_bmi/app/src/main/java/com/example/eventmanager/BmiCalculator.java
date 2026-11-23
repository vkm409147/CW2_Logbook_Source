package com.example.eventmanager;

public final class BmiCalculator {

    private static final double POUNDS_TO_KILOGRAMS = 0.45359237;
    private static final double INCHES_TO_METRES = 0.0254;

    public enum UnitSystem {
        METRIC,
        IMPERIAL
    }

    public enum Category {
        UNDERWEIGHT("Underweight"),
        NORMAL("Normal"),
        OVERWEIGHT("Overweight"),
        OBESITY("Obesity");

        private final String label;

        Category(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public static final class Result {
        private final double bmi;
        private final Category category;

        private Result(double bmi, Category category) {
            this.bmi = bmi;
            this.category = category;
        }

        public double getBmi() {
            return bmi;
        }

        public Category getCategory() {
            return category;
        }
    }

    private BmiCalculator() {
    }

    public static Result calculate(double weight, double height, UnitSystem unitSystem) {
        if (!Double.isFinite(weight) || !Double.isFinite(height)) {
            throw new IllegalArgumentException("Enter valid numeric measurements.");
        }

        double weightKg;
        double heightMetres;
        if (unitSystem == UnitSystem.METRIC) {
            requireRange(weight, 1, 500, "Weight must be between 1 and 500 kg.");
            requireRange(height, 50, 300, "Height must be between 50 and 300 cm.");
            weightKg = weight;
            heightMetres = height / 100;
        } else {
            requireRange(weight, 2.2, 1100, "Weight must be between 2.2 and 1100 lb.");
            requireRange(height, 20, 118, "Height must be between 20 and 118 in.");
            weightKg = weight * POUNDS_TO_KILOGRAMS;
            heightMetres = height * INCHES_TO_METRES;
        }

        double bmi = weightKg / (heightMetres * heightMetres);
        Category category;
        if (bmi < 18.5) {
            category = Category.UNDERWEIGHT;
        } else if (bmi < 25) {
            category = Category.NORMAL;
        } else if (bmi < 30) {
            category = Category.OVERWEIGHT;
        } else {
            category = Category.OBESITY;
        }
        return new Result(bmi, category);
    }

    private static void requireRange(double value, double minimum, double maximum, String message) {
        if (value < minimum || value > maximum) {
            throw new IllegalArgumentException(message);
        }
    }
}
