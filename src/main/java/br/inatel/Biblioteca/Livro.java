package br.inatel.Biblioteca;

import java.sql.*;
import java.util.*;
import java.time.Year;

public class Livro {
    private String isbn;
    private String titulo;
    private Year anoPublicacao;
    private Categoria categoria;
    private List<Autor> autores;

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
        // Adiciona diretamente à lista interna, que é modificável
        this.autores.add(autor);
    }
    public void create(Connection conn) throws SQLException {
        // Desabilita o auto-commit para controlar a transação manualmente
        conn.setAutoCommit(false);

        String sqlLivro = "INSERT INTO livro (isbn, titulo, anoPublicacao, categoria_idCategoria) VALUES (?, ?, ?, ?)";
        String sqlAutores = "INSERT INTO livro_has_autor (livro_ISBN, autor_idAutor) VALUES (?, ?)";

        try (PreparedStatement stmtLivro = conn.prepareStatement(sqlLivro);
             PreparedStatement stmtAutores = conn.prepareStatement(sqlAutores)) {

            // 1. Inserir o livro
            stmtLivro.setString(1, this.getIsbn());
            stmtLivro.setString(2, this.getTitulo());
            stmtLivro.setObject(3, this.getAnoPublicacao());
            stmtLivro.setInt(4, this.categoria.getId());
            stmtLivro.executeUpdate();

            // 2. Inserir os autores
            for (Autor autor : this.getAutores()) {
                stmtAutores.setString(1, this.getIsbn());
                stmtAutores.setInt(2, autor.getId());
                stmtAutores.addBatch();
            }
            stmtAutores.executeBatch();

            // Se tudo deu certo, efetiva a transação
            conn.commit();

        } catch (SQLException e) {
            // Se qualquer erro ocorrer, desfaz a transação
            conn.rollback();
            System.err.println("Erro ao criar livro. Transação revertida.");
            throw e; // Lança a exceção para a camada superior tratar
        } finally {
            // Garante que o auto-commit será reativado
            conn.setAutoCommit(true);
        }
    }
    public static Livro read(Connection conn, String isbn) throws SQLException {
        String sql = """
        SELECT 
            livro.isbn, 
            livro.titulo, 
            livro.anoPublicacao, 
            categoria.idCategoria,
            categoria.nomeCategoria,
            autor.idAutor,
            autor.nome,
            autor.paisOrigem
        FROM livro_has_autor
        JOIN autor ON livro_has_autor.autor_idAutor = autor.idAutor
        JOIN livro ON livro.isbn = livro_has_autor.livro_ISBN
        JOIN categoria ON livro.categoria_idCategoria = categoria.idCategoria
        WHERE livro.isbn = ?
    """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, isbn);
            ResultSet rs = stmt.executeQuery();

            Livro livro = null;
            List<Autor> autores = new ArrayList<>();

            while (rs.next()) {
                if (livro == null) {
                    Categoria categoria = new Categoria(
                            rs.getInt("idCategoria"),
                            rs.getString("nomeCategoria")
                    );

                    livro = new Livro(
                            rs.getString("isbn"),
                            categoria,
                            rs.getString("titulo"),
                            Year.of(rs.getInt("anoPublicacao")),
                            autores
                    );
                }

                Autor autor = new Autor(
                        rs.getInt("idAutor"),
                        rs.getString("nome"),
                        rs.getString("paisOrigem")
                );
                autores.add(autor);
            }

            return livro;
        }
    }

    public static List<Livro> readAll(Connection conn) throws SQLException {
        String sql = """
        SELECT 
            livro.isbn, 
            livro.titulo, 
            livro.anoPublicacao, 
            categoria.idCategoria,
            categoria.nomeCategoria,
            autor.idAutor,
            autor.nome,
            autor.paisOrigem
        FROM livro_has_autor
        JOIN autor ON livro_has_autor.autor_idAutor = autor.idAutor
        JOIN livro ON livro.isbn = livro_has_autor.livro_ISBN
        JOIN categoria ON livro.categoria_idCategoria = categoria.idCategoria
    """;

        Map<String, Livro> livroMap = new HashMap<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String isbn = rs.getString("isbn");

                Livro livro = livroMap.get(isbn);
                if (livro == null) {
                    Categoria categoria = new Categoria(
                            rs.getInt("idCategoria"),
                            rs.getString("nomeCategoria")
                    );

                    livro = new Livro(
                            isbn,
                            categoria,
                            rs.getString("titulo"),
                            Year.of(rs.getInt("anoPublicacao")),
                            new ArrayList<>()
                    );

                    livroMap.put(isbn, livro);
                }

                Autor autor = new Autor(
                        rs.getInt("idAutor"),
                        rs.getString("nome"),
                        rs.getString("paisOrigem")  // Corrigido aqui
                );

                livro.addAutor(autor);
            }
        }

        return new ArrayList<>(livroMap.values());
    }


    public void delete(Connection conn) throws SQLException {
        conn.setAutoCommit(false); // Inicia a transação

        String sqlDeleteAutores = "DELETE FROM livro_has_autor WHERE livro_ISBN = ?";
        String sqlDeleteLivro = "DELETE FROM livro WHERE isbn = ?";

        // O try-with-resources garante que os PreparedStatements serão fechados
        try (PreparedStatement stmtAutores = conn.prepareStatement(sqlDeleteAutores);
             PreparedStatement stmtLivro = conn.prepareStatement(sqlDeleteLivro)) {

            // Ordem é importante: primeiro as dependências, depois o principal
            stmtAutores.setString(1, this.getIsbn());
            stmtAutores.executeUpdate();

            stmtLivro.setString(1, this.getIsbn());
            stmtLivro.executeUpdate();

            conn.commit(); // Efetiva a transação se tudo deu certo

        } catch (SQLException e) {
            conn.rollback(); // Desfaz a transação em caso de erro
            System.err.println("Erro ao deletar livro. Transação revertida.");
            throw e;
        } finally {
            conn.setAutoCommit(true); // Devolve a conexão ao estado original
        }
    }
}
