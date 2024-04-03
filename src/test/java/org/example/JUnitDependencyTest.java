package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@Tag("dependency-check")
public class JUnitDependencyTest {

    @DisplayName("Test JUnit Dependency \uD83E\uDD8D")
    @Test
    void junitTest() {
        assertTrue(true, "JUnit is working correctly");
    }

    @DisplayName("Test parameterized Dependency \uD83E\uDEE1")
    @ParameterizedTest(name = "{0} > 0 && {0} > 4")
    @ValueSource(ints = {1, 2, 3})
    void testParameterizedTest(int value) {
        assertTrue(value > 0 && value < 4, "ParameterizedTest is working correctly");
    }
    
    @DisplayName("Test Mockito Dependency")
    @Test
    void testMockito() {
        Mock mock = mock(Mock.class);
        assertNotNull(mock);
    }
}
