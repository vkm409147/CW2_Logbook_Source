package com.example.eventmanager;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ExampleUnitTest {
    @Test
    public void calculatesMetricBmiAndCategory() {
        BmiCalculator.Result result = BmiCalculator.calculate(
                70, 175, BmiCalculator.UnitSystem.METRIC);

        assertEquals(22.86, result.getBmi(), 0.01);
        assertEquals(BmiCalculator.Category.NORMAL, result.getCategory());
    }

    @Test
    public void imperialAndMetricMeasurementsProduceEquivalentBmi() {
        BmiCalculator.Result metric = BmiCalculator.calculate(
                70, 175, BmiCalculator.UnitSystem.METRIC);
        BmiCalculator.Result imperial = BmiCalculator.calculate(
                154.323584, 68.897638, BmiCalculator.UnitSystem.IMPERIAL);

        assertEquals(metric.getBmi(), imperial.getBmi(), 0.01);
    }

    @Test
    public void rejectsMeasurementsOutsideReasonableRange() {
        assertThrows(IllegalArgumentException.class, () -> BmiCalculator.calculate(
                0, 170, BmiCalculator.UnitSystem.METRIC));
    }
}