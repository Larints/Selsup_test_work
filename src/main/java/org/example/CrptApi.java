package org.example;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Larin_ts
 * Класс посылающий определенное количество
 * запросов на API, за определенное количество времени
 */
public class CrptApi {
    private static final Logger logger = LogManager.getLogger(CrptApi.class);
    private static final String URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    private final HttpClient client;
    private final ThreadLocal<AtomicInteger> requestCount;
    private final ThreadLocal<Long> lastRequestTime;
    private final int requestLimit;
    private final long intervalMillis;


    /**
     * @param timeUnit     - заданный интервал времени
     * @param requestLimit - заданное количество запросов к API
     */
    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.client = HttpClient.newHttpClient();
        this.requestLimit = requestLimit;
        this.intervalMillis = timeUnit.toMillis(1);

        // Для каждого потока создаёт экземпляр AtomicInteger
        this.requestCount = ThreadLocal.withInitial(() -> new AtomicInteger(0));
        // Время последнего запроса для каждого потока
        this.lastRequestTime = ThreadLocal.withInitial(System::currentTimeMillis);
    }

    /**
     * Метод отвечающий за отправку определенного количества запросов
     * к API
     *
     * @param requestBody - Строка формата json
     * @return - возвращает ответ от API
     * @throws Exception - проброс исключения
     */
    public HttpResponse<String> createDocument(String requestBody) throws Exception {
        AtomicInteger count = requestCount.get();
        long currentTime = System.currentTimeMillis();
        long lastTime = lastRequestTime.get();

        // Проверяем вышло ли время с последнего запроса
        if ((currentTime - lastTime) > intervalMillis) {
            throw new TimeLimitExceededException();
        }

        // Увеличиваем запрос и в случае выхода за лимит, выбрасываем исключение
        if (count.incrementAndGet() > requestLimit) {
            count.decrementAndGet();  // rollback the increment
            throw new OutOfLimitConnectionException();
        }

        try {
            // Обновляем время последнего запроса
            lastRequestTime.set(currentTime);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            count.decrementAndGet();  // rollback the increment in case of error
            logger.error("Failed to process request", e);
            throw new RuntimeException("Failed to process request", e);
        }
    }

    /**
     * POJO для JSON объекта и сериализации
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    private static
    class Document {
        private Description description;
        @JsonProperty("doc_id")
        private String docId;
        @JsonProperty("doc_status")
        private String docStatus;
        @JsonProperty("doc_type")
        private String docType;
        private boolean importRequest;
        @JsonProperty("owner_inn")
        private String ownerInn;
        @JsonProperty("participant_inn")
        private String participantInn;
        @JsonProperty("producer_inn")
        private String producerInn;
        @JsonProperty("production_date")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate productionDate;
        @JsonProperty("production_type")
        private String productionType;
        private List<Product> products;
        @JsonProperty("reg_date")
        private LocalDate regDate;
        @JsonProperty("reg_number")
        private String regNumber;
    }

    /**
     * POJO для JSON объекта
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    private static class Description {
        private String participantInn;
    }

    /**
     * POJO для JSON
     * объекта
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Product {
        @JsonProperty("certificate_document")
        private String certificateDocument;
        @JsonProperty("certificate_document_date")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate certificateDocumentDate;
        @JsonProperty("certificate_document_number")
        private String certificateDocumentNumber;
        @JsonProperty("owner_inn")
        private String ownerInn;
        @JsonProperty("producer_inn")
        private String producerInn;
        @JsonProperty("production_date")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate productionDate;
        @JsonProperty("owned_code")
        private String ownedCode;
        @JsonProperty("uit_code")
        private String uitCode;
        @JsonProperty("unit_code")
        private String unitCode;
    }

    /**
     * Кастомное исключение
     */
    private static class OutOfLimitConnectionException extends Exception {
        public OutOfLimitConnectionException() {
            super("Request limit exceeded");
        }
    }

    /**
     * Кастомное исключение
     */
    public static class TimeLimitExceededException extends Exception {
        public TimeLimitExceededException() {
            super("Time limit exceeded between requests");
        }
    }

    public static void main(String[] args) {
// Example: 5 requests per second per thread
        CrptApi api = new CrptApi(TimeUnit.SECONDS, 5);

        Runnable task = () -> {
            try {
                for (int i = 0; i < 10; i++) { // Each thread makes 10 requests
                    HttpResponse<String> response = api.createDocument("{\"key\":\"value\"}");
                    System.out.println("Response: " + response.body() + " in thread: " + Thread.currentThread().getName());
                }
            } catch (OutOfLimitConnectionException e) {
                System.err.println("Request limit exceeded. Please wait. Thread: " + Thread.currentThread().getName());
            } catch (TimeLimitExceededException e) {
                System.err.println("Time limit exceeded. Thread: " + Thread.currentThread().getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        // Create multiple threads to simulate concurrent requests
        for (int i = 0; i < 5; i++) {
            new Thread(task, "Thread-" + i).start();
        }
    }
}
