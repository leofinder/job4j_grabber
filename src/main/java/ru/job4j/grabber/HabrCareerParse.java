package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class HabrCareerParse {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final String SOURCE_LINK = "https://career.habr.com";
    public static final String PREFIX = "/vacancies?page=";
    public static final String SUFFIX = "&q=Java%20developer&type=all";
    private static final int PAGE_COUNT = 5;

    public static void main(String[] args) throws IOException {
        HabrCareerParse careerParse = new HabrCareerParse();
        for (int i = 1; i <= PAGE_COUNT; i++) {
            careerParse.parseVacancyCards(i);
        }
    }

    private void parseVacancyCards(int pageNumber) throws IOException {
        String fullLink = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, pageNumber, SUFFIX);
        Connection connection = Jsoup.connect(fullLink);
        Document document = connection.get();
        Elements rows = document.select(".vacancy-card__inner");
        rows.forEach(row -> {
            Element dateElement = row.select(".vacancy-card__date .basic-date").first();
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateElement.attr("datetime"), FORMATTER);
            Element titleElement = row.select(".vacancy-card__title").first();
            Element linkElement = titleElement.child(0);
            String vacancyName = titleElement.text();
            String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
            String description = retrieveDescription(link);
            System.out.printf("%s %s %s - %s%n", offsetDateTime.toLocalDate(), vacancyName, link, description);
        });
    }

    private String retrieveDescription(String link) {
        String description = "";
        try {
            Connection connection = Jsoup.connect(link);
            Document document = connection.get();
            description = document.select(".vacancy-description__text").first().text();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return description;
    }
}