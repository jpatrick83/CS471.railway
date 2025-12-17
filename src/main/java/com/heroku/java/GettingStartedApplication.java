package com.heroku.java;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping("/dbinput")
    public String dbInputForm() {
        return "dbinput";
    }

    @GetMapping("/database")
    String database(Map<String, Object> model) {

        System.out.println("Database endpoint accessed by John Patrick");

        try (Connection connection = dataSource.getConnection()) {

            Statement statement = connection.createStatement();

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS table_timestamp_and_random_string (" +
                            "tick timestamp, random_string varchar(50))"
            );

            ResultSet resultSet = statement.executeQuery(
                    "SELECT tick, random_string FROM table_timestamp_and_random_string " +
                            "ORDER BY tick DESC"
            );

            ArrayList<String> output = new ArrayList<>();

            while (resultSet.next()) {
                output.add(
                        "Read from DB: " +
                                resultSet.getTimestamp("tick") +
                                " | Input String: " +
                                resultSet.getString("random_string")
                );
            }

            model.put("records", output);
            return "database";

        } catch (Throwable t) {
            model.put("message", t.getMessage());
            return "error";
        }
    }

    @PostMapping("/dbinput")
    public String handleDbInput(
            @RequestParam("userString") String userString,
            Map<String, Object> model) {

        try (Connection connection = dataSource.getConnection()) {

            var statement = connection.createStatement();

            // Ensure table exists
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS table_timestamp_and_random_string (" +
                            "tick timestamp, random_string varchar(50))"
            );

            // Insert user-provided string
            statement.executeUpdate(
                    "INSERT INTO table_timestamp_and_random_string VALUES (now(), '" +
                            userString + "')"
            );

            model.put("message", "Successfully inserted: " + userString);
            return "dbinput";

        } catch (Exception e) {
            model.put("message", "Error: " + e.getMessage());
            return "dbinput";
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
