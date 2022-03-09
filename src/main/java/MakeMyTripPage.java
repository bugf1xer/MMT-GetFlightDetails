import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MakeMyTripPage {
    public static WebDriver driver;

    public static void main(String[] args) throws InterruptedException, IOException {

        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver.exe");

        driver = new ChromeDriver();
        driver.manage().deleteAllCookies();
        driver.get("https://www.makemytrip.com/");
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));


        By from = By.xpath("//div//label[@for = 'fromCity']");
        By to = By.xpath("//label[@for = 'toCity']//parent::div");
        By departure = By.xpath("//div//label[@for = 'departure']");
        By travellers = By.xpath("//div//label[@for = 'travellers']");

//      closing popups
        driver.findElement(By.xpath("//span[@class = 'langCardClose']")).click();
        driver.findElement(By.xpath("//*[@id=\"SW\"]/div[1]/div[1]/ul/li[3]/div[2]")).click();
//      selecting initial location
        driver.findElement(from).click();
        selectCity("Delhi");
//      selecting destination
        driver.findElement(to).click();
        selectCity("Bengaluru");

        driver.findElement(departure).click();
        selectTodayDate();

        driver.findElement(travellers).click();
        selectNumberOfTravellers(2);
        selectTravelClass("Economy");
        driver.findElement(By.xpath("//button[text() = 'APPLY']")).click();

        driver.findElement(By.linkText("SEARCH")).click();

        scrollToBottom();
        WebElement moreOptions= driver.findElement(By.xpath("//div[contains(@class, 'groupBookingCard')]//span[contains(@class, 'flight-count')]"));
//
        moreOptions.click();
        Thread.sleep(3000);
        List<WebElement> airlines = driver.findElements(By.xpath("//div/span[contains(@class, 'airlineName')]"));
        List<WebElement> departureTimings = driver.findElements(By.xpath("//div[contains(@class, 'timeInfoLeft')]//p[1]"));
        List<WebElement> arrivalTimings = driver.findElements(By.xpath("//div[contains(@class, 'timeInfoRight')]//p[1]"));
        List<WebElement> flightDuration = driver.findElements(By.xpath("//div[contains(@class, 'stop-info')]/p"));
        List<WebElement> layovers = driver.findElements(By.xpath("//p[contains(@class, 'flightsLayoverInfo')]"));
        List<WebElement> priceInfo = driver.findElements(By.xpath("//div[contains(@class, 'priceSection')]//p"));

        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{
           "AirLine Name", "Departure", "Arrival", "Duration", "Layover", "Price"
        });
        System.out.println(airlines.size());
        for(int i = 0; i < airlines.size(); i++){
            rows.add(new String[]{
                    airlines.get(i).getText(), departureTimings.get(i).getText(),
                    arrivalTimings.get(i).getText(), flightDuration.get(i).getText(),
                    layovers.get(i).getText(), priceInfo.get(i).getText(),
            });
        }

        MakeMyTripPage makeMyTripPage = new MakeMyTripPage();
        makeMyTripPage.writeDataToCSV(rows);

        driver.quit();

    }

    public static void scrollToBottom(){
        /*
        handling infinite scroll
         */
        JavascriptExecutor js = (JavascriptExecutor) driver;
        int last_height = Integer.parseInt(js.executeScript("return document.body.scrollHeight").toString());
        while (true){
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int newHeight = Integer.parseInt(js.executeScript("return document.body.scrollHeight").toString());
            if (newHeight == last_height){
                break;
            } else last_height = newHeight;
        }
    }

    public String escapeSpecialCharacters(String data){
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    public String convertToCSV(String[] data) {
        return Stream.of(data)
                .map(this::escapeSpecialCharacters)
                .collect(Collectors.joining(","));
    }

    public void writeDataToCSV(List<String[]> rows) throws IOException {
        File csvOutputFile = new File("src/main/csv/output.csv");
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            rows.stream()
                    .map(this::convertToCSV)
                    .forEach(pw::println);
        }
    }

    public static void selectCity(String cityName) {
        List<WebElement> cities = driver.findElements(By.xpath("//li[@role ='option']//p[contains(@class, 'blackText')]"));
        for (WebElement city : cities) {
            if (city.getText().contains(cityName)) {
                city.click();
                break;
            }
        }
    }

    public static void selectTodayDate(){
        Date d = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd YYYY");
        String date = formatter.format(d);
//        System.out.println(date);
        driver.findElement(By.xpath("//div[contains(@aria-label, '" + date +"')]")).click();
    }

    public  static void selectNumberOfTravellers(int n){
        driver.findElement(By.xpath("//li[@data-cy = 'adults-"+ n +"']")).click();
    }
    public static void selectTravelClass(String travelClass){
        driver.findElement(By.xpath("//ul[contains(@class, 'classSelect')]/li[starts-with(text(), '"+
                travelClass +"')]")).click();
    }
}
