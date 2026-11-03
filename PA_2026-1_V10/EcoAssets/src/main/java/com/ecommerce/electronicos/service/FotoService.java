package com.ecommerce.electronicos.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class FotoService {

    private final Cloudinary cloudinary;

    public FotoService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String subir(MultipartFile archivo) throws IOException {
        String tipo = archivo.getContentType();
        if (tipo == null || !List.of("image/jpeg", "image/png", "image/webp").contains(tipo)) {
            throw new IllegalArgumentException("Solo se permiten imágenes JPG, PNG o WEBP");
        }
        Map<?, ?> resultado = cloudinary.uploader().upload(
            archivo.getBytes(),
            ObjectUtils.asMap("folder", "ecoasset", "resource_type", "image")
        );
        return (String) resultado.get("secure_url");
    }
}
