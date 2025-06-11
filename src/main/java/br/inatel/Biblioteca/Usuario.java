package br.inatel.Biblioteca;

public class Usuario {
    private final int id;
    private final String nome;
    private final String email;
    private final String telefone;
    private final String tipo;

    public Usuario(int id, String nome, String email, String telefone, String tipo) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.tipo = tipo;
    }

    public String getTipo() {
        return tipo;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEmail() {
        return email;
    }

    public String getNome() {
        return nome;
    }

    public int getId() {
        return id;
    }
}
