package br.inatel.Biblioteca;

public class Exemplar_livro {
    private final int id;
    private final Livro livro;
    private final boolean status;

    public Exemplar_livro(int id, Livro livro, boolean status) {
        this.id = id;
        this.livro = livro;
        this.status = status;
    }

    public int getId() {
        return id;
    }
    public Livro getLivro() {
        return livro;
    }

    public boolean isStatus() {
        return status;
    }
}
