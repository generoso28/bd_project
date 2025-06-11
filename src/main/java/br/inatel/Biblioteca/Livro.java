package br.inatel.Biblioteca;

import br.inatel.Interfaces.Dao;

import java.sql.*;
import java.util.*;
import java.time.Year;

public class Livro{
    private final String isbn;
    private final String titulo;
    private final Year anoPublicacao;
    private final Categoria categoria;
    private final List<Autor> autores;

    public Livro(String isbn, Categoria categoria, String titulo, Year anoPublicacao, List<Autor> autores) {
        this.isbn = isbn;
        this.categoria = categoria;
        this.titulo = titulo;
        this.anoPublicacao = anoPublicacao;
        this.autores = autores;
    }

    public List<Autor> getAutores() {
        return Collections.unmodifiableList(this.autores);
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitulo() {
        return titulo;
    }

    public Year getAnoPublicacao() {
        return anoPublicacao;
    }

    public Categoria getCategoria() {
        return categoria;
    }
    public void addAutor(Autor autor) {
        this.autores.add(autor);
    }
}
