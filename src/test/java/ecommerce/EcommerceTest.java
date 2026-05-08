package ecommerce;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EcommerceTest {

    private WebDriver driver;
    private WebDriverWait wait;
    
    // Update BASE_URL if the app runs on a different host/port in CI pipeline
    private final String BASE_URL = "http://18.232.60.204:3000/";

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setupTest() {
        ChromeOptions options = new ChromeOptions();
        // Headless mode is required for Jenkins CI/CD environment
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    @DisplayName("1. Verify the landing page loads successfully for guests")
    void testLandingPageLoads() {
        driver.get(BASE_URL);
        WebElement heroTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("hero-title")));
        assertEquals("Welcome to NexCommerce", heroTitle.getText());
        
        // Verify 'Why Choose Us?' section is visible to guests
        WebElement whyChooseUs = driver.findElement(By.xpath("//h2[contains(text(), 'Why Choose Us?')]"));
        assertTrue(whyChooseUs.isDisplayed());
    }

    @Test
    @Order(2)
    @DisplayName("2. Verify Navbar contains Login link for guests")
    void testLoginLinkExists() {
        driver.get(BASE_URL);
        WebElement loginLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href, '/login')]")));
        assertTrue(loginLink.isDisplayed());
    }

    @Test
    @Order(3)
    @DisplayName("3. Verify Navbar contains Signup link for guests")
    void testSignupLinkExists() {
        driver.get(BASE_URL);
        WebElement signupLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href, '/signup')]")));
        assertTrue(signupLink.isDisplayed());
    }

    @Test
    @Order(4)
    @DisplayName("4. Verify Login page navigation and elements")
    void testNavigateToLogin() {
        driver.get(BASE_URL);
        WebElement loginLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href, '/login')]")));
        loginLink.click();
        
        WebElement authTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("auth-title")));
        assertEquals("Welcome Back", authTitle.getText());
        assertTrue(driver.getCurrentUrl().contains("/login"));
    }

    @Test
    @Order(5)
    @DisplayName("5. Verify Signup page navigation and elements")
    void testNavigateToSignup() {
        driver.get(BASE_URL);
        WebElement signupLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href, '/signup')]")));
        signupLink.click();
        
        WebElement authTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("auth-title")));
        assertEquals("Create Account", authTitle.getText());
        assertTrue(driver.getCurrentUrl().contains("/signup"));
    }

    @Test
    @Order(6)
    @DisplayName("6. Test user signup flow with dummy database fallback")
    void testUserSignup() {
        driver.get(BASE_URL + "/signup");
        
        driver.findElement(By.id("name")).sendKeys("Test User");
        driver.findElement(By.id("email")).sendKeys("test@example.com");
        driver.findElement(By.id("password")).sendKeys("password123");
        
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        
        // Wait for redirect to home page and verify user is logged in
        WebElement welcomeMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(), 'Welcome back')]")));
        assertTrue(welcomeMsg.getText().contains("Test User"));
    }

    @Test
    @Order(7)
    @DisplayName("7. Test user login flow with dummy database fallback")
    void testUserLogin() {
        driver.get(BASE_URL + "/login");
        
        driver.findElement(By.id("email")).sendKeys("test@example.com");
        driver.findElement(By.id("password")).sendKeys("password123");
        
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        
        // Wait for redirect to home page and verify user is logged in
        WebElement welcomeMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(), 'Welcome back')]")));
        assertTrue(welcomeMsg.getText().contains("Demo User")); // API returns 'Demo User' on dummy login
    }

    @Test
    @Order(8)
    @DisplayName("8. Verify products are displayed for logged-in users")
    void testProductsDisplayedAfterLogin() {
        loginDummyUser();
        
        List<WebElement> products = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.className("card")));
        assertTrue(products.size() > 0, "Products should be visible after logging in");
    }

    @Test
    @Order(9)
    @DisplayName("9. Test 'Add to Cart' functionality")
    void testAddToCart() {
        loginDummyUser();
        
        // Find all "Add to Cart" buttons and click the first one
        List<WebElement> addToCartButtons = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//button[contains(text(), 'Add to Cart')]")));
        addToCartButtons.get(0).click();
        
        // Verify cart badge changes
        WebElement cartBadge = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(@href, '/cart')]/span")));
        assertEquals("1", cartBadge.getText());
    }

    @Test
    @Order(10)
    @DisplayName("10. Verify Cart page displays added items")
    void testNavigateToCart() {
        loginDummyUser();
        
        // Add item
        List<WebElement> addToCartButtons = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//button[contains(text(), 'Add to Cart')]")));
        addToCartButtons.get(0).click();
        
        // Go to cart
        driver.findElement(By.xpath("//a[contains(@href, '/cart')]")).click();
        
        WebElement cartTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("page-title")));
        assertEquals("Shopping Cart", cartTitle.getText());
        
        // Verify item is in the cart list
        List<WebElement> cartItems = driver.findElements(By.className("cart-item"));
        assertEquals(1, cartItems.size());
    }

    @Test
    @Order(11)
    @DisplayName("11. Verify item removal from Cart")
    void testRemoveFromCart() {
        loginDummyUser();
        
        // Add item
        List<WebElement> addToCartButtons = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//button[contains(text(), 'Add to Cart')]")));
        addToCartButtons.get(0).click();
        
        // Go to cart
        driver.findElement(By.xpath("//a[contains(@href, '/cart')]")).click();
        
        // Click remove button (Trash icon inside btn-danger)
        WebElement removeBtn = wait.until(ExpectedConditions.elementToBeClickable(By.className("btn-danger")));
        removeBtn.click();
        
        // Verify empty cart message
        WebElement emptyMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(text(), 'Your Cart is Empty')]")));
        assertTrue(emptyMsg.isDisplayed());
    }

    @Test
    @Order(12)
    @DisplayName("12. Verify Checkout redirect if cart is empty")
    void testCheckoutRedirectIfCartEmpty() {
        loginDummyUser();
        
        // Trying to access checkout without items should redirect to home
        driver.get(BASE_URL + "/checkout");
        
        // Wait for redirect to home
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));
        assertTrue(driver.getCurrentUrl().endsWith("/"));
    }

    @Test
    @Order(13)
    @DisplayName("13. Verify full Checkout flow success")
    void testCheckoutSuccess() {
        loginDummyUser();
        
        // Add item
        List<WebElement> addToCartButtons = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//button[contains(text(), 'Add to Cart')]")));
        addToCartButtons.get(0).click();
        
        // Go to cart
        driver.findElement(By.xpath("//a[contains(@href, '/cart')]")).click();
        
        // Proceed to Checkout
        WebElement proceedBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Proceed to Checkout')]")));
        proceedBtn.click();
        
        // Fill Shipping Details
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h3[contains(text(), 'Shipping Address')]")));
        driver.findElements(By.className("form-input")).get(0).sendKeys("John Doe"); // Full Name
        driver.findElements(By.className("form-input")).get(1).sendKeys("123 Test St"); // Address
        driver.findElements(By.className("form-input")).get(2).sendKeys("New York"); // City
        driver.findElements(By.className("form-input")).get(3).sendKeys("10001"); // Postal Code
        driver.findElements(By.className("form-input")).get(4).sendKeys("USA"); // Country
        
        // Submit order
        WebElement placeOrderBtn = driver.findElement(By.xpath("//button[contains(text(), 'Place Order')]"));
        placeOrderBtn.click();
        
        // Verify success message
        WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(text(), 'Order Placed Successfully!')]")));
        assertTrue(successMsg.isDisplayed());
    }

    @Test
    @Order(14)
    @DisplayName("14. Verify Logout functionality")
    void testLogout() {
        loginDummyUser();
        
        // Click Logout
        WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Logout')]")));
        logoutBtn.click();
        
        // Verify redirect to login
        wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(driver.getCurrentUrl().contains("/login"));
    }

    @Test
    @Order(15)
    @DisplayName("15. Verify Cart calculates Order Summary correctly")
    void testCartSummaryCalculation() {
        loginDummyUser();
        
        // Add item (e.g. Wireless Headphones $89.99)
        List<WebElement> addToCartButtons = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//button[contains(text(), 'Add to Cart')]")));
        addToCartButtons.get(0).click(); // Assumes first item is present
        
        // Go to cart
        driver.findElement(By.xpath("//a[contains(@href, '/cart')]")).click();
        
        // Wait for summary
        WebElement summaryTotal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("summary-total")));
        assertTrue(summaryTotal.getText().contains("$"));
    }

    /**
     * Helper method to perform a dummy login so we have a valid session for the tests.
     */
    private void loginDummyUser() {
        driver.get(BASE_URL + "/login");
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        emailInput.sendKeys("test@example.com");
        driver.findElement(By.id("password")).sendKeys("password123");
        driver.findElement(By.xpath("//button[@type='submit']")).click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));
    }
}
