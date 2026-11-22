package com.fullstack.fullstack.service;

import com.fullstack.fullstack.exception.ReglaNegocioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Guarda y lee los archivos subidos en una carpeta del servidor (app.uploads.dir).
 * Los archivos se guardan con un nombre generado (UUID + extensión permitida), nunca con el nombre que
 * envía el usuario, y toda ruta se comprueba para que no pueda salir de la carpeta.
 */
@Component
public class AlmacenArchivos {

    private final Path raiz;

    public AlmacenArchivos(@Value("${app.uploads.dir}") String carpeta) {
        this.raiz = Path.of(carpeta).toAbsolutePath().normalize();
    }

    /** Copia el archivo a la carpeta y devuelve el nombre con el que quedó guardado. */
    public String guardar(MultipartFile archivo, String extension) {
        String nombre = UUID.randomUUID() + "." + extension;
        try {
            Files.createDirectories(raiz);
            try (InputStream entrada = archivo.getInputStream()) {
                Files.copy(entrada, resolver(nombre), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar el archivo", e);
        }
        return nombre;
    }

    /** Ruta de un archivo ya guardado; falla si no existe. */
    public Path ruta(String nombre) {
        Path ruta = resolver(nombre);
        if (!Files.isRegularFile(ruta)) {
            throw new ReglaNegocioException("El archivo ya no está disponible en el servidor");
        }
        return ruta;
    }

    /** Borra el archivo si existe (no falla si ya no estaba). */
    public void borrar(String nombre) {
        try {
            Files.deleteIfExists(resolver(nombre));
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo borrar el archivo", e);
        }
    }

    private Path resolver(String nombre) {
        Path ruta = raiz.resolve(nombre).normalize();
        if (!ruta.startsWith(raiz)) {
            throw new ReglaNegocioException("Nombre de archivo no válido");
        }
        return ruta;
    }
}
