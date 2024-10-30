package weather.Content;

import weather.utils.JSONParser;

import java.io.*;
import java.nio.file.Paths;

/*
* Read data from a given resource
* */
public class ContentReaderService {
    private final String filePath;

    public ContentReaderService(String filePath) {
        this.filePath = filePath;
    }

    public String readFile() throws IOException {
        File file = Paths.get(filePath).normalize().toFile();
        System.out.println("Reading file from path: " + file.getAbsolutePath());

        if (!file.exists()) {
            throw new IOException("File does not exist: " + file.getAbsolutePath());
        }

        StringBuilder data = new StringBuilder();
        try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = fileReader.readLine()) != null) {
                data.append(line).append("\n");
            }
        }

        String content = data.toString().trim();
        return JSONParser.ensureJsonFormat(content).toString();
    }
}
