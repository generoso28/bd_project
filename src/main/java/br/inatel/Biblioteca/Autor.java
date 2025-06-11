package br.inatel.Biblioteca;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Autor {
    private final int id;
    private final String nome;
    private final String nacionalidade;

    public Autor(int id, String nome, String nacionalidade) {
        this.id = id;
        this.nome = nome;
        this.nacionalidade = nacionalidade;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getNacionalidade() {
        return nacionalidade;
    }
}
