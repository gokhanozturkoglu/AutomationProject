import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class PageOperationTest {

    //Create common profile
    //Learn the driver used with the configuration file.(e.g. config groovy)
    public WebDriver driver;

    @BeforeClass
    public void setUp() {
        System.out.println("*******************");
        System.out.println("launching chrome browser");
        System.setProperty("webdriver.chrome.driver", Constants.driverPath);
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().deleteAllCookies();
    }

    @Test
    public void testN11Page() throws IOException, InterruptedException {
        driver.navigate().to(Constants.baseUrl);
        //TODO : title a göre değil içeriğe göre kontrol yapılmalı
        String strPageTitle = driver.getTitle();
        System.out.println("Page title: - " + strPageTitle);
        Assert.assertTrue(strPageTitle.equalsIgnoreCase("n11.com - Alışverişin Uğurlu Adresi"), "Page title doesn't match");

        driver.findElement(By.xpath("//span[text()=\"Tamam\"]")).click();

        WebElement signInButton = driver.findElement(By.xpath("//*[@class=\"btnSignIn\"]"));
        signInButton.click();

        FileOperations readFileData = new FileOperations();
        readFileData.fillForm(driver);
        driver.findElement(By.id("loginButton")).click();

        waitForPageLoaded();

        WebElement searchData = driver.findElement(By.id("searchData"));
        searchData.sendKeys("bilgisayar");
        searchData.sendKeys(Keys.ENTER);

        //click second page in pagination
        driver.findElement(By.xpath("//div[@class=\"pagination\"] //a[contains(@href,\"&pg=2\")]")).click();
        Assert.assertTrue(driver.getCurrentUrl().contains("&pg=2"), "Page number doesn't match");

        randomChoiceAndWriteFile(driver);

        //add to basket
        driver.findElement(By.xpath("//a[@class=\"btn btnGrey btnAddBasket\"]")).click();
        String productPrice = getPrice(driver);

        //check the basket price
        driver.findElement(By.xpath("//a[@class=\"myBasket\"]")).click();
        waitForPageLoaded();

        String priceInBasket = driver.findElement(By.xpath("//div[@class=\"priceArea\"] //span")).getText();
        Assert.assertTrue(priceInBasket.equals(productPrice), "The price in the basket did not match the price of the product!");

        String totalPrice = driver.findElement(By.xpath("//div[@class=\"dtl total\"] //span[@class=\"price\"]")).getText();
        Assert.assertTrue(totalPrice.equals(productPrice), "Total price didn't match the product price on product page");

        //increment quantity
        driver.findElement(By.xpath("//span[@class=\"spinnerUp spinnerArrow\"]")).click();
        String quantity = driver.findElement(By.xpath("//input[@class=\"quantity\"]")).getAttribute("value");
        Assert.assertEquals(quantity, "2");

        //"Sil"
        driver.findElement(By.xpath("//span[@class=\"removeProd svgIcon svgIcon_trash\"]")).click();
        Assert.assertTrue(strPageTitle.equalsIgnoreCase("n11.com - Alışverişin Uğurlu Adresi"), "Page title doesn't match");

        //Silindiğini kontrol et
        WebElement element = driver.findElement(By.xpath("//div[@class=\"cartEmptyText\"] //h2[@class=\"title\"]"));
        Assert.assertEquals("Sepetiniz Boş", element.getText());

    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            System.out.println("Closing chrome browser");
            driver.quit();
        }
    }

    public void waitForPageLoaded() {
        ExpectedCondition<Boolean> expectation = new
                ExpectedCondition<Boolean>() {
                    public Boolean apply(WebDriver driver) {
                        Long isActive = (Long) ((JavascriptExecutor) driver).executeScript("return typeof jQuery!='undefined'?jQuery.active:0");
                        return isActive == 0;
//                        return driver.findElement(By.id("searchData"))
//                                .isDisplayed();
                    }
                };
        try {
            Thread.sleep(1000);
            WebDriverWait wait = new WebDriverWait(driver, 30);
            wait.until(expectation);
        } catch (Throwable error) {
            Assert.fail("Timeout waiting for Page Load Request to complete.");
        }
    }

    public void randomChoiceAndWriteFile(WebDriver driver) throws IOException {
        List<WebElement> allProductsInPage = driver.findElements(By.xpath("//li[@class=\"column\"]"));
        Random random = new Random();
        int productNumber = random.nextInt(allProductsInPage.size());
        driver.findElement(By.xpath(
                String.format(("//div[@data-position='%s'] //div[@class=\"pro\"] //a[contains(@href,\"urun.n11.com/\")]"), productNumber)
        )).click();

        waitForPageLoaded();

        String productName = driver.findElement(By.xpath("//div[@class=\"proDetailArea\"] //h1[@class=\"proName\"]")).getText().trim();
        String productPrice = getPrice(driver);
        FileOperations fileOperations = new FileOperations();
        fileOperations.writeToTxt(productName + " " + productPrice);
    }

    public String getPrice(WebDriver driver) {
        String productPrice = driver.findElement(By.xpath("//div[@class=\"proDetailArea\"] //div[@class=\"newPrice\"] //ins")).getText().trim();
        String currency = driver.findElement(By.xpath("//div[@class=\"proDetailArea\"] //div[@class=\"newPrice\"] //ins //span")).getText().trim();

        return productPrice + currency;
    }
}
