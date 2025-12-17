package com.heroku.java;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Map;

@SpringBootApplication
@Controller
public class GettingStartedApplication {
    private final DataSource dataSource;

    @Autowired
    public GettingStartedApplication(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/database")
    String database(Map<String, Object> model) {
        try (Connection connection = dataSource.getConnection()) {

            Statement statement = connection.createStatement();

            // Create table with timestamp and random string
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS table_timestamp_and_random_string (" +
                            "tick timestamp, " +
                            "random_string varchar(50))"
            );

            // Insert current timestamp and random string
            statement.executeUpdate(
                    "INSERT INTO table_timestamp_and_random_string VALUES (now(), '" +
                            getRandomString() + "')"
            );

            // Select all records
            ResultSet resultSet = statement.executeQuery(
                    "SELECT tick, random_string FROM table_timestamp_and_random_string"
            );

            ArrayList<String> output = new ArrayList<>();

            while (resultSet.next()) {
                Timestamp timestamp = resultSet.getTimestamp("tick");
                String randomString = resultSet.getString("random_string");

                output.add("Read from DB: " + timestamp + " | Random String: " + randomString);
            }

            model.put("records", output);
            return "database";

        } catch (Throwable t) {
            model.put("message", t.getMessage());
            return "error";
        }
    }

    /**
     * Generates a random alphanumeric string of length 10
     */
    private String getRandomString() {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        final int length = 10;

        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        SpringApplication.run(GettingStartedApplication.class, args);
    }
}
