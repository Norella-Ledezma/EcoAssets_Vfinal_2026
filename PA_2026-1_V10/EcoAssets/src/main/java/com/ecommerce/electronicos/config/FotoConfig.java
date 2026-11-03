package com.ecommerce.electronicos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;

@Configuration
public class FotoConfig {

    @Bean
    public Cloudinary cloudinary(@Value("${cloudinary.url}") String url) {
        return new Cloudinary(url);
    }
}
