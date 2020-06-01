import com.opencsv.CSVReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.Test;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileOperations {

    static final ClassLoader classLoader = ClassLoader.getSystemClassLoader();

    public void fillForm(WebDriver driver) throws InterruptedException, IOException {
        String source = classLoader.getResource("loginInfo.csv").getPath();

        CSVReader reader = new CSVReader(new FileReader(source));
        String[] cell;

        while ((cell = reader.readNext()) != null) {
            for (int i = 0; i < 1; i++) {
                String username = cell[i];
                String password = cell[i + 1];

                driver.findElement(By.id("email")).sendKeys(username);
                driver.findElement(By.id("password")).sendKeys(password);
            }
        }
    }

    public void writeToTxt(String content) throws IOException {
        String source = classLoader.getResource("productInfo.txt").getPath();
        Files.write(Paths.get(source), content.getBytes());
    }
}
