package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class HelloWorldTest {

    private final Main main = new Main();

    @DisplayName("Basic Test")
    @Test
    void testHelloWorld() {
        final String expected = "HELLO WORLD";
        final String actual = main.helloWorld();
        assertEquals(expected, actual);
    }

    @DisplayName("Test parameterized \uD83E\uDEE1")
    @ParameterizedTest(name = "{0} + {1} = {2}")
    @CsvSource({
            "1,2,3",
            "2,2,4",
            "5,5,10"
    })
    void testParameterizedTest(int a, int b, int expected) {
        assertEquals(main.addition(a,b), expected);
    }
    
    @DisplayName("Test Mockito")
    @Test
    void testMockito() {
        Main mockedMain = Mockito.mock(Main.class);

        final int a = 2;

        int expectedMockValue = 5;
        when(mockedMain.addition(a,a)).thenReturn(expectedMockValue);
        assertEquals(mockedMain.addition(a,a), expectedMockValue);

        int expectedRealValue = 4;
        assertEquals(main.addition(a,a), expectedRealValue);
    }
}
