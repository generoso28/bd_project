package br.inatel.Biblioteca;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Autor {
    private int id;
    private String nome;
    private String nacionalidade;

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
