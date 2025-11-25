package com.project.shopapp.controllers;

import com.project.shopapp.services.impl.FakeService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("${api.prefix}/admin")
@RequiredArgsConstructor
public class AdminController {

    private final FakeService fakeService;

    @PostMapping("/generateFakeUsers")
    public ResponseEntity<?> fakeUser() {
        final int total = 100000;
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 1; i <= total; i++) {
            CompletableFuture<Void> future = fakeService.createFakeUser(i);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .orTimeout(10, TimeUnit.MINUTES)
                .join();
        return ResponseEntity.ok("Fake user successfully");
    }

    @PostMapping("/generateFakeProducts")
    public ResponseEntity<String> generateFakeProducts() {
        final int total = 500;
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for(int i = 1; i <= total; i++){
            CompletableFuture<Void> future = fakeService.createFakeProduct(i);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .orTimeout(10, TimeUnit.MINUTES)
                .join();
        return ResponseEntity.ok("Fake product successfully");
    }

    @PostMapping("/testOrders")
    public ResponseEntity<String> runOrderLoadTest(
            @RequestParam(defaultValue = "1000") int totalOrders,
            @RequestParam(defaultValue = "100") int numThreads) {

        try {
            fakeService.executeLoadTest(totalOrders, numThreads);
            return ResponseEntity.ok("Load test completed");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Load test interrupted: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Load test failed: " + e.getMessage());
        }
    }
}
