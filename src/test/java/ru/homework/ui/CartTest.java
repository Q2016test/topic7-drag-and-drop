package ru.homework.ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.DragAndDropOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.homework.support.GoodsApi;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

@Tag("ui")
class CartTest {
    private final GoodsApi api = new GoodsApi();
    private GoodsApi.Product product;

    @BeforeEach
    void startBrowser() {
        product = api.createProduct();
        Configuration.baseUrl = System.getProperty("ui.baseUrl", "http://localhost:8080");
        Configuration.browser = "chrome";
        Configuration.headless = Boolean.parseBoolean(System.getProperty("ui.headless", "true"));
        Configuration.browserSize = "1280x900";
        Configuration.timeout = 5000;
        Configuration.reportsFolder = "build/reports/selenide";
        String binary = System.getProperty("ui.browserBinary");
        if (binary != null) {
            Configuration.browserBinary = binary;
        }
        open("/");
        $("#card-" + product.id()).shouldBe(visible);
        $("#cart-count").shouldBe(exactText("0"));
    }

    @AfterEach
    void closeBrowserAndDeleteData() {
        try {
            closeWebDriver();
        } finally {
            api.cleanup();
        }
    }

    private void dragProductToCart() {
        $("#card-" + product.id() + " h4")
                .dragAndDrop(DragAndDropOptions.to("#open-cart-btn").usingSeleniumActions());
        $("#cart-count").shouldBe(exactText("1"));
        $("#open-cart-btn").click();
        $("#cartModal").shouldBe(visible);
    }

    @Test
    @DisplayName("1.1 Перетаскивание товара в корзину")
    void dragAndDropAddsProductToCart() {
        dragProductToCart();
        $("#cart-item-" + product.id() + " b").shouldBe(visible, exactText(product.name()));
        $("#cart-item-" + product.id() + " .qty-controls span").shouldBe(exactText("1"));
        $("#total-price").shouldBe(exactText("25"));
    }

    @Test
    @DisplayName("1.2 Удаление товара из корзины")
    void removedProductDisappearsFromCart() {
        dragProductToCart();
        $("#cart-item-" + product.id() + " b").shouldBe(visible, exactText(product.name()));
        $("#cart-item-" + product.id() + " [data-action='remove']").click();
        $("#cart-item-" + product.id()).shouldNot(exist);
        $("#empty-cart").shouldBe(visible, exactText("Пусто"));
        $("#cart-count").shouldBe(exactText("0"));
        $("#total-price").shouldBe(exactText("0"));
        $("#close-modal").click();
        $("#card-" + product.id() + " h4").shouldBe(visible, exactText(product.name()));
    }
}
