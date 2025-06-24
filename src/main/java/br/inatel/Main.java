// Em: br/inatel/Main.java
package br.inatel;

import br.inatel.Biblioteca.Exemplar_livro;
import br.inatel.DAO.*;
import br.inatel.Database.DatabaseFactory;
import br.inatel.Menu.Menu; // Importe sua classe Menu

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        Connection conn = null;
        try {
            // 1. Obter a conexão com o banco de dados
            conn = DatabaseFactory.getConnection();

            // 2. Instanciar todos os DAOs, passando a mesma conexão para todos
            AutorDao autorDao = new AutorDao(conn);
            UsuarioDao usuarioDao = new UsuarioDao(conn);
            CategoriaDao categoriaDao = new CategoriaDao(conn);
            LivroDao livroDao = new LivroDao(conn);
            EmprestimoDao emprestimoDao = new EmprestimoDao(conn);
            ExemplarDao exemplarDao = new ExemplarDao(conn);
            MultaDao multaDao = new MultaDao(conn);

            // 3. Instanciar o Menu, injetando os DAOs
            Menu menu = new Menu(autorDao, usuarioDao, categoriaDao, livroDao, emprestimoDao, exemplarDao);

            // 4. Exibir o menu principal
            menu.showMainMenu();

        } catch (SQLException e) {
            System.err.println("Não foi possível conectar ao banco de dados. Encerrando a aplicação.");
            e.printStackTrace();
        } finally {
            // 5. Garantir que a conexão seja fechada ao final
            if (conn != null) {
                try {
                    conn.close();
                    System.out.println("Conexão com o banco de dados fechada.");
                } catch (SQLException e) {
                    System.err.println("Erro ao fechar a conexão com o banco de dados.");
                    e.printStackTrace();
                }
            }
        }
    }
}