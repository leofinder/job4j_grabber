package ru.job4j.grabber.utils;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class HabrCareerDateTimeParserTest {

    @Test
    public void checkDateTimeString() {
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        String dateTimeString = "2024-06-25T15:59:01";
        LocalDateTime result = parser.parse(dateTimeString);
        LocalDateTime expected = LocalDateTime.of(2024, 6, 25, 15, 59, 1);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void checkDateTimeStringWithTimeOffset() {
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        String dateTimeString = "2024-06-25T15:59:01+03:00";
        LocalDateTime result = parser.parse(dateTimeString);
        LocalDateTime expected = LocalDateTime.of(2024, 6, 25, 15, 59, 1);
        assertThat(result).isEqualTo(expected);
    }
}