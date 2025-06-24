package br.inatel.Biblioteca;

import br.inatel.Interfaces.Dao;

import java.sql.*;
import java.util.*;
import java.time.Year;

public class Livro{
    private final String isbn;
    private final String titulo;
    private final Integer anoPublicacao;
    private final Categoria categoria;
    private final List<Autor> autores;
    private int quantidadeExemplares;

    public Livro(String isbn, Categoria categoria, String titulo, Integer anoPublicacao, List<Autor> autores) {
        this.isbn = isbn;
        this.categoria = categoria;
        this.titulo = titulo;
        this.anoPublicacao = anoPublicacao;
        this.autores = autores;
        this.quantidadeExemplares = 0;
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

    public int getAnoPublicacao() {
        return anoPublicacao;
    }

    public Categoria getCategoria() {
        return categoria;
    }
    public void addAutor(Autor autor) {
        this.autores.add(autor);
    }

    public int getQuantidadeExemplares() {
        return quantidadeExemplares;
    }

    public void setQuantidadeExemplares(int quantidadeExemplares) {
        this.quantidadeExemplares = quantidadeExemplares;
    }
}
