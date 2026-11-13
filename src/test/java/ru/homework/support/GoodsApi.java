package ru.homework.support;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.preemptive;
import static org.hamcrest.Matchers.*;

public final class GoodsApi {
    private final List<Long> createdIds = new ArrayList<>();

    private RequestSpecification specification() {
        return new RequestSpecBuilder()
                .setBaseUri(System.getProperty("ui.baseUrl", "http://localhost:8080"))
                .setContentType(ContentType.JSON)
                .setAuth(preemptive().basic(
                        System.getProperty("api.username", "admin"),
                        System.getProperty("api.password", "secret123")))
                .build();
    }

    public Product createProduct() {
        String name = "drag-drop-" + UUID.randomUUID();
        double price = 25.0;
        long id = given().spec(specification()).body(Map.of("name", name, "price", price))
                .when().post("/goods/add")
                .then().statusCode(200).body("data.id", notNullValue())
                .extract().jsonPath().getLong("data.id");
        createdIds.add(id);
        return new Product(id, name);
    }

    public void cleanup() {
        for (long id : createdIds) {
            given().spec(specification()).when().delete("/goods/{id}", id)
                    .then().statusCode(anyOf(is(200), is(404)));
        }
        createdIds.clear();
    }

    public record Product(long id, String name) {
    }
}
