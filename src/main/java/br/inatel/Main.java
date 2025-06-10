package br.inatel;

import br.inatel.Biblioteca.Autor;
import br.inatel.Biblioteca.Livro;
import br.inatel.Database.DatabaseFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Iniciando busca por livros no banco de dados...");

        // A mágica acontece aqui: usamos a factory para obter a conexão.
        // A classe Main não precisa mais saber URL, usuário ou senha.
        try (Connection conn = DatabaseFactory.getConnection()) {

            List<Livro> livros = Livro.readAll(conn);

            if (livros.isEmpty()) {
                System.out.println("Nenhum livro encontrado no banco de dados.");
            } else {
                System.out.println("--- LIVROS ENCONTRADOS ---");
                for (Livro livro : livros) {
                    System.out.println("Título: " + livro.getTitulo());
                    System.out.println("ISBN: " + livro.getIsbn());
                    System.out.println("Ano de Publicação: " + livro.getAnoPublicacao());
                    System.out.println("Categoria: " + livro.getCategoria().getNome());
                    System.out.println("  Autores:");

                    if (livro.getAutores().isEmpty()) {
                        System.out.println("    (Nenhum autor listado)");
                    } else {
                        for (Autor autor : livro.getAutores()) {
                            System.out.println("    - " + autor.getNome() + " (" + autor.getNacionalidade() + ")");
                        }
                    }
                    System.out.println("------------------------------------");
                }
            }

        } catch (SQLException e) {
            System.err.println("ERRO: Falha na comunicação com o banco de dados.");
            e.printStackTrace();
        }
//        try (Connection conn = DatabaseFactory.getConnection();
//             Scanner scanner = new Scanner(System.in)) {
//
//            while (true) {
//                System.out.println("\n1 - Adicionar");
//                System.out.println("2 - Buscar por ID");
//                System.out.println("3 - Listar todos");
//                System.out.println("4 - Deletar");
//                System.out.println("0 - Sair");
//                String op = scanner.nextLine();
//
//                switch (op) {
//                    case "1" -> {
//                        System.out.print("Nome: ");
//                        String nome = scanner.nextLine();
//                        System.out.print("Nacionalidade: ");
//                        String nacionalidade = scanner.nextLine();
//                        int id = Autor.create(conn, new Autor(0, nome, nacionalidade));
//                        System.out.println("Autor criado com ID: " + id);
//                    }
//                    case "2" -> {
//                        System.out.print("ID: ");
//                        int id = Integer.parseInt(scanner.nextLine());
//                        Autor autor = Autor.read(conn, id);
//                        if (autor != null) System.out.printf("ID: %d, Nome: %s\n", autor.getId(), autor.getNome());
//                        else System.out.println("Não encontrado");
//                    }
//                    case "3" -> {
//                        List<Autor> autores = Autor.readAll(conn);
//                        for (Autor a : autores) System.out.printf("ID: %d, Nome: %s\n", a.getId(), a.getNome());
//                    }
//                    case "4" -> {
//                        System.out.print("ID: ");
//                        int id = Integer.parseInt(scanner.nextLine());
//                        Autor.delete(conn, id);
//                        System.out.println("Deletado");
//                    }
//                    case "0" -> System.exit(0);
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}