package com.example.demo.service;


import com.example.demo.entity.Product;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private List<Product> products = new ArrayList<>();

    public List<Product> getAllProducts(){
        return products;
    }

    public Product getProductById(int id){
        return products.stream()
                .filter(p->p.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public Product addProduct(Product product){
        products.add(product);
        return product;
    }

    public void delete(int id) {
        products.removeIf(p -> p.getId() == id);
    }
}
