package com.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.infrastructure.Building;

import java.io.File;
import java.io.IOException;

public class YamlParser {

    public static Building parseFromYaml(String path) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        try {
            return mapper.readValue(new File(path), Building.class);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return null;
        }
    }
}
