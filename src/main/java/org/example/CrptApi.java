package org.example;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CrptApi {

    private final AtomicInteger requestCount;
    private TimeUnit timeUnit;
    private static final String URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";


    public CrptApi(AtomicInteger requestCount, TimeUnit timeUnit) {
        this.requestCount = requestCount;
        this.timeUnit = timeUnit;
    }

    public synchronized Document createDocument() {
        String requestBody = """
                {"description": { "participantInn": "string" },\s
                "doc_id": "string",\s
                "doc_status": "string",\s
                "doc_type": "LP_INTRODUCE_GOODS",\s
                "importRequest": true,\s
                "owner_inn": "string",\s
                "participant_inn": "string",\s
                "producer_inn": "string",\s
                "production_date": "2020-01-23",\s
                "production_type": "string",\s
                "products": [ { "certificate_document": "string",\s
                "certificate_document_date": "2020-01-23",\s
                "certificate_document_number": "string",\s
                "owner_inn": "string", "producer_inn": "string",\s
                "production_date": "2020-01-23", "owned_code": "string",\s
                "uit_code": "string", "unit_code": "string" } ],\s
                "reg_date": "2020-01-23",\s
                "reg_number": "string"}
                """;
        try {
            if (this.requestCount.get() == 0) {
                throw new OutOfLimitConnectionException();
            } else {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(URL))
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            }
        } catch (Exception e) {
            System.out.println();
        }
return null;
    }

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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    private static class Description {
        private String participantInn;
    }

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

    private static class OutOfLimitConnectionException extends Exception {

    }

    public static void main(String[] args) {
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.registerModule(new JavaTimeModule());
//        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
//        Document document = Document.builder()
//                .description(new Description("test"))
//                .docId("test")
//                .docStatus("test")
//                .docType("test")
//                .importRequest(true)
//                .ownerInn("test")
//                .participantInn("test")
//                .producerInn("test")
//                .productionDate(LocalDate.now())
//                .productionType("test")
//                .products(List.of(Product.builder()
//                        .certificateDocument("test")
//                        .certificateDocumentDate(LocalDate.now())
//                        .certificateDocumentNumber("test")
//                        .ownerInn("test")
//                        .producerInn("test")
//                        .productionDate(LocalDate.now())
//                        .ownedCode("test")
//                        .uitCode("test")
//                        .unitCode("test")
//                        .build()))
//                .regDate(LocalDate.now())
//                .regNumber("test")
//                .build();
//        try {
//            String json = mapper.writeValueAsString(document);
//            System.out.println(json);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//        try {
//            Document document2 = mapper.readValue(requestBody, Document.class);
//            System.out.println(document2);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }

    }


}
