package br.inatel.Biblioteca;

import java.time.LocalDate;

public class Emprestimo {
    private final Integer id;
    private final LocalDate dataEmprestimo;
    private final LocalDate dataDevolucao;
    private final Usuario usuario;
    private final Exemplar_livro livro;

    public Emprestimo(LocalDate dataEmprestimo, Usuario usuario, Exemplar_livro livro) {
        this.id = null;
        this.dataEmprestimo = dataEmprestimo;
        this.dataDevolucao = null;
        this.usuario = usuario;
        this.livro = livro;
    }
    public Emprestimo(Integer id, LocalDate dataEmprestimo, LocalDate dataDevolucao, Usuario usuario, Exemplar_livro livro) {
        this.id = id;
        this.dataEmprestimo = dataEmprestimo;
        this.dataDevolucao = dataDevolucao;
        this.usuario = usuario;
        this.livro = livro;
    }

    public int getId() {
        return id;
    }

    public LocalDate getDataEmprestimo() {
        return dataEmprestimo;
    }

    public LocalDate getDataDevolucao() {
        return dataDevolucao;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Exemplar_livro getLivro() {
        return livro;
    }
}
