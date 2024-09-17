package com.coldary.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ResourceLoader {

    public static String readFileFromResources(String fileName) {
        StringBuilder contentBuilder = new StringBuilder();

        try (InputStream inputStream = ResourceLoader.class.getClassLoader().getResourceAsStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            if (inputStream == null) {
                throw new IllegalArgumentException("File not found: " + fileName);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append(System.lineSeparator());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return contentBuilder.toString();
    }
}
