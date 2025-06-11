package br.inatel.Biblioteca;

public class Multa {
    private int id;
    private double multa;
    private double juros;
    private Emprestimo emprestimo;

    public Multa(Emprestimo emprestimo, double juros, double multa, int id) {
        this.emprestimo = emprestimo;
        this.juros = juros;
        this.multa = multa;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public double getMulta() {
        return multa;
    }

    public double getJuros() {
        return juros;
    }

    public Emprestimo getEmprestimo() {
        return emprestimo;
    }
}
