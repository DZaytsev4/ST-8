package com.mycompany.app;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class App {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.setProperty("webdriver.chrome.driver",
                "D:\chromedriver-win64\chromedriver.exe");

        String downloadFilepath = "D:\\project\\ST-8\\result";
        Path outputDir = Paths.get(downloadFilepath);
        Files.createDirectories(outputDir);

        ChromeOptions options = new ChromeOptions();
        HashMap<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", downloadFilepath);
        prefs.put("download.prompt_for_download", false);
        prefs.put("plugins.always_open_pdf_externally", true);
        options.setExperimentalOption("prefs", prefs);

        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        try {
            driver.get("http://www.papercdcase.com/index.php");

            List<String> lines = new ArrayList<>();
            String dataPath = "D:\\project\\ST-8\\data\\data.txt";
            try (BufferedReader reader = new BufferedReader(new FileReader(dataPath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isBlank()) {
                        lines.add(line.trim());
                    }
                }
            }

            if (lines.size() < 3) {
                System.out.println("Недостаточно данных в data.txt");
                return;
            }

            String artist = lines.get(0).substring(lines.get(0).indexOf(":") + 1).trim();
            String title = lines.get(1).substring(lines.get(1).indexOf(":") + 1).trim();
            List<String> tracks = lines.subList(2, lines.size());

            driver.findElement(By.xpath("/html/body/table[2]/tbody/tr/td[1]/div/form/table/tbody/tr[1]/td[2]/input"))
                    .sendKeys(artist);
            driver.findElement(By.xpath("/html/body/table[2]/tbody/tr/td[1]/div/form/table/tbody/tr[2]/td[2]/input"))
                    .sendKeys(title);

            int maxTracks = Math.min(tracks.size(), 16);
            for (int i = 0; i < maxTracks; i++) {
                int row = (i % 8) + 1;
                String xpath = (i < 8)
                        ? "/html/body/table[2]/tbody/tr/td[1]/div/form/table/tbody/tr[3]/td[2]/table/tbody/tr/td[1]/table/tbody/tr[" + row + "]/td[2]/input"
                        : "/html/body/table[2]/tbody/tr/td[1]/div/form/table/tbody/tr[3]/td[2]/table/tbody/tr/td[2]/table/tbody/tr[" + row + "]/td[2]/input";
                driver.findElement(By.xpath(xpath)).sendKeys(tracks.get(i));
            }

            WebElement a4Radio = driver.findElement(By.xpath(
                    "/html/body/table[2]/tbody/tr/td[1]/div/form/table/tbody/tr[4]/td[2]/input[2]"));
            a4Radio.click();

            WebElement jewelRadio = driver.findElement(By.xpath(
                    "/html/body/table[2]/tbody/tr/td[1]/div/form/table/tbody/tr[5]/td[2]/input[2]"));
            jewelRadio.click();

            driver.findElement(By.xpath("/html/body/table[2]/tbody/tr/td[1]/div/form/p/input")).click();

            Thread.sleep(6000);

            File[] pdfs = outputDir.toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
            if (pdfs != null && pdfs.length > 0) {
                File newest = Arrays.stream(pdfs)
                        .max(Comparator.comparingLong(File::lastModified))
                        .orElse(null);
                if (newest != null) {
                    Path target = outputDir.resolve("cd.pdf");
                    Files.deleteIfExists(target);
                    Files.move(newest.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("PDF сохранён: " + target.toAbsolutePath());
                }
            } else {
                System.err.println("PDF в папке не найден.");
            }

            System.out.println("Готово!");
        } finally {
            driver.quit();
        }
    }
}
