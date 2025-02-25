package com.wiredbraincoffee.product_api_annotation.controller;


import com.wiredbraincoffee.product_api_annotation.model.Product;
import com.wiredbraincoffee.product_api_annotation.model.ProductEvent;
import com.wiredbraincoffee.product_api_annotation.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("/products")
public class ProductController {

    private ProductRepository productRepository;

    public ProductController(ProductRepository productRepository){
        this.productRepository=productRepository;
    }


    @GetMapping
    public Flux<Product> getAllProducts(){
        // if u observe there is no subscribe -> Spring will call it for you at right time :)
        return productRepository.findAll();
    }

    @GetMapping("{id}")
    public Mono<ResponseEntity<Product>> getProduct(@PathVariable String id){
        return productRepository.findById(id)
//                .map(product -> ResponseEntity.ok(product))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Product> addProduct(@RequestBody Product product){
        return productRepository.save(product);
    }


    @PutMapping("{id}")
    public Mono<ResponseEntity<Product>> updateProduct(@PathVariable(value = "id") String id,
                                                       @RequestBody Product product){
        return productRepository
                .findById(id)
                .flatMap(existingProduct -> {
                    existingProduct.setName(product.getName());
                    existingProduct.setPrice(product.getPrice());
                    return productRepository.save(existingProduct);
                })
                .map(updateProduct -> ResponseEntity.ok(updateProduct))
                .defaultIfEmpty(ResponseEntity.notFound().build());
                // an if-else statement in a declarative style
    }


    @DeleteMapping("{id}")
    public Mono<ResponseEntity<Void>> deleteProduct(@PathVariable(value = "id") String id){
        return productRepository
                .findById(id)
                .flatMap(existingProduct ->
                        productRepository.delete(existingProduct)
                                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                )
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    @DeleteMapping
    public Mono<Void> deleteProducts(){
        return productRepository.deleteAll();
    }


    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProductEvent> getProductEvents(){
        return Flux.interval(Duration.ofSeconds(1))
                .map(val ->
                        new ProductEvent(val,"ProductEvent")
                );
    }
}
