package br.inatel.Menu;

import br.inatel.Biblioteca.*;
import br.inatel.DAO.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLOutput;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Menu {
    private final Scanner scanner;
    // Os DAOs são agora campos de instância e são 'final' porque serão definidos uma vez no construtor.
    private final AutorDao autorDao;
    private final UsuarioDao usuarioDao;
    private final CategoriaDao categoriaDao;
    private final LivroDao livroDao;
    private final EmprestimoDao emprestimoDao;
    private final ExemplarDao exemplarDao;

    // O construtor recebe todos os DAOs de que precisa (Injeção de Dependência)
    public Menu(AutorDao autorDao, UsuarioDao usuarioDao, CategoriaDao categoriaDao, LivroDao livroDao, EmprestimoDao emprestimoDao, ExemplarDao exemplarDao) {
        this.scanner = new Scanner(System.in);
        this.autorDao = autorDao;
        this.usuarioDao = usuarioDao;
        this.categoriaDao = categoriaDao;
        this.livroDao = livroDao;
        this.emprestimoDao = emprestimoDao;
        this.exemplarDao = exemplarDao;
    }

    public void showMainMenu() throws SQLException {
        while (true) {
            System.out.println("\n=== Sistema de Biblioteca ===");
            System.out.println("1. Gerenciar Usuários");
            System.out.println("2. Gerenciar Autores");
            System.out.println("3. Gerenciar Categorias");
            System.out.println("4. Gerenciar Livros");
            System.out.println("5. Gerenciar Empréstimos");
            System.out.println("0. Sair");
            System.out.print("Escolha uma opção: ");

            int option = scanner.nextInt();
            scanner.nextLine();

            switch (option) {
                case 1:
                    showUserMenu();
                    break;
                case 2:
                    showAuthorMenu();
                    break;
                case 3:
                    showCategoryMenu();
                    break;
                case 4:
                    showBookMenu();
                    break;
                case 5:
                    showLoanMenu();
                    break;
                case 0:
                    System.out.println("Saindo do sistema...");
                    return;
                default:
                    System.out.println("Opção inválida!");
            }
        }
    }

    private void showUserMenu() {
        while (true) {
            System.out.println("\n=== Gestão de Usuários ===");
            System.out.println("1. Inserir usuário");
            System.out.println("2. Visualizar usuários");
            System.out.println("3. Excluir usuário");
            System.out.println("0. Voltar");
            System.out.print("Escolha uma opção: ");

            int option = scanner.nextInt();
            scanner.nextLine(); // --> CORREÇÃO: Consome a nova linha deixada pelo nextInt()

            switch (option) {
                case 1:
                    insertUser(); // Chama um metodo dedicado para maior clareza
                    break;
                case 2:
                    viewUsers();
                    break;
                case 3:
                    deleteUser();
                    break;
                case 0:
                    return; // Retorna ao menu principal
                default:
                    System.out.println("Opção inválida!");
            }
        }
    }

    private void deleteUser() {
        System.out.println("\n--- Excluir Autor ---");
        // Primeiro, mostramos os usuários existentes para que o usuário saiba qual ID usar.
        viewUsers();

        try {
            System.out.print("\nDigite o ID do usuário que deseja excluir: ");
            int id = scanner.nextInt();
            scanner.nextLine(); // Consome a nova linha

            // O metodo delete do DAO retorna true se a exclusão foi bem-sucedida
            boolean success = usuarioDao.delete(id); // Usa o DAO para deletar

            if (success) {
                System.out.println("Usuário excluído com sucesso!");
            } else {
                // Isso geralmente acontece se não houver um autor com o ID fornecido.
                System.out.println("Não foi possível encontrar um usuário com o ID " + id + ".");
            }
        } catch (SQLException e) {
            // Este erro é comum se o autor estiver ligado a um livro (violação de chave estrangeira).
            System.err.println("Erro de banco de dados ao excluir usuário.");
            // Opcional: imprimir o stack trace para depuração
            // e.printStackTrace();
        } catch (java.util.InputMismatchException e) {
            System.err.println("Entrada inválida. Por favor, digite um número de ID válido.");
            scanner.nextLine(); // Limpa o buffer do scanner para evitar um loop infinito
        }
    }

    private void viewUsers() {
        try {
            List<Usuario> usuarios = usuarioDao.readAll();
            if (usuarios.isEmpty()) {
                System.out.println("Nenhum usuário cadastrado.");
                return;
            }
            System.out.println("\n--- Lista de Usuários ---");
            for (Usuario usuario : usuarios) {
                System.out.printf("ID: %d, Nome: %s, Telefone: %s, Email: %s, Tipo: %s\n",
                        usuario.getId(), usuario.getNome(), usuario.getTelefone(), usuario.getEmail(), usuario.getTipo());
            }
        } catch (SQLException e) {
            System.err.println("Erro de banco de dados ao visualizar usuários: " + e.getMessage());
        }
    }

    private void insertUser() {
        try {
            System.out.print("Nome do usuário: ");
            String userName = scanner.nextLine();
            System.out.print("Telefone: ");
            String userPhone = scanner.nextLine();
            System.out.print("Email: ");
            String userEmail = scanner.nextLine();
            System.out.print("Tipo: ");
            String userType = scanner.nextLine();
            // ID do usuário deve ser gerado pelo banco de dados, então passamos 0 ou um construtor sem ID.
            Usuario usuario = new Usuario(0, userName, userPhone, userEmail, userType);
            if (usuarioDao.create(usuario)) { // A chamada ao DAO agora funciona
                System.out.println("Usuário inserido com sucesso!");
            } else {
                System.out.println("Falha ao inserir usuário.");
            }
        } catch (SQLException e) {
            System.err.println("Erro de banco de dados ao inserir usuário: " + e.getMessage());
        }
    }

    private void showAuthorMenu() {
        while (true) {
            System.out.println("\n=== Gestão de Autores ===");
            System.out.println("1. Inserir Autor");
            System.out.println("2. Visualizar Autores");
            System.out.println("3. Excluir Autor");
            System.out.println("0. Voltar");
            System.out.print("Escolha uma opção: ");

            int option = scanner.nextInt();
            scanner.nextLine();

            switch (option) {
                case 1:
                    insertAuthor();
                    break;
                case 2:
                    viewAuthors();
                    break;
                case 3:
                    deleteAuthor();
                    break;
                case 0:
                    return; // Retorna ao menu principal
                default:
                    System.out.println("Opção inválida!");
            }
        }
    }

    private void deleteAuthor() {
        System.out.println("\n--- Excluir Autor ---");
        // Primeiro, mostramos os autores existentes para que o usuário saiba qual ID usar.
        viewAuthors();

        try {
            System.out.print("\nDigite o ID do autor que deseja excluir: ");
            int id = scanner.nextInt();
            scanner.nextLine(); // Consome a nova linha

            // O metodo delete do DAO retorna true se a exclusão foi bem-sucedida
            boolean success = autorDao.delete(id); // Usa o DAO para deletar

            if (success) {
                System.out.println("Autor excluído com sucesso!");
            } else {
                // Isso geralmente acontece se não houver um autor com o ID fornecido.
                System.out.println("Não foi possível encontrar um autor com o ID " + id + ".");
            }
        } catch (SQLException e) {
            // Este erro é comum se o autor estiver ligado a um livro (violação de chave estrangeira).
            System.err.println("Erro de banco de dados ao excluir autor. " +
                    "Verifique se o autor não está associado a nenhum livro.");
            // Opcional: imprimir o stack trace para depuração
            // e.printStackTrace();
        } catch (java.util.InputMismatchException e) {
            System.err.println("Entrada inválida. Por favor, digite um número de ID válido.");
            scanner.nextLine(); // Limpa o buffer do scanner para evitar um loop infinito
        }
    }

    private void viewAuthors() {
        try {
            List<Autor> autores = autorDao.readAll();
            if (autores.isEmpty()) {
                System.out.println("Nenhum autor cadastrado.");
                return;
            }
            System.out.println("\n--- Lista de Autores ---");
            for (Autor autor : autores) {
                System.out.printf("ID: %d, Nome: %s, País: %s\n",
                        autor.getId(), autor.getNome(), autor.getNacionalidade());
            }
        } catch (SQLException e) {
            System.err.println("Erro de banco de dados ao visualizar autores: " + e.getMessage());
        }
    }

    private void insertAuthor() {
        try {
            System.out.print("Nome do autor: ");
            String authorName = scanner.nextLine();
            System.out.print("País de origem: ");
            String country = scanner.nextLine();
            // ID do autor deve ser gerado pelo banco de dados, então passamos 0 ou um construtor sem ID.
            Autor autor = new Autor(0, authorName, country);
            if (autorDao.create(autor)) { // A chamada ao DAO agora funciona
                System.out.println("Autor inserido com sucesso!");
            } else {
                System.out.println("Falha ao inserir autor.");
            }
        } catch (SQLException e) {
            System.err.println("Erro de banco de dados ao inserir autor: " + e.getMessage());
        }
    }

    private void showCategoryMenu() {
        while (true) {
            System.out.println("\n=== Gestão de Categorias ===");
            System.out.println("1. Inserir categoria");
            System.out.println("2. Visualizar categorias");
            System.out.println("3. Excluir categoria");
            System.out.println("0. Voltar");
            System.out.print("Escolha uma opção: ");

            int option = scanner.nextInt();
            scanner.nextLine();

            switch (option) {
                case 1:
                    insertCategory();
                    break;
                case 2:
                    viewCategories();
                    break;
                case 3:
                    deleteCategory();
                    break;
                case 0:
                    return; // Retorna ao menu principal
                default:
                    System.out.println("Opção inválida!");
            }
        }
    }

    private void deleteCategory() {
        System.out.println("\n--- Excluir Categoria ---");
        // Primeiro, mostramos os usuários existentes para que o usuário saiba qual ID usar.
        viewCategories();

        try {
            System.out.print("\nDigite o ID da categoria que deseja excluir: ");
            int id = scanner.nextInt();
            scanner.nextLine(); // Consome a nova linha

            // O metodo delete do DAO retorna true se a exclusão foi bem-sucedida
            boolean success = categoriaDao.delete(id); // Usa o DAO para deletar

            if (success) {
                System.out.println("Categoria excluída com sucesso!");
            } else {
                // Isso geralmente acontece se não houver um autor com o ID fornecido.
                System.out.println("Não foi possível encontrar uma categoria com o ID " + id + ".");
            }
        } catch (SQLException e) {
            // Este erro é comum se o autor estiver ligado a um livro (violação de chave estrangeira).
            System.err.println("Erro de banco de dados ao excluir categoria.");
            // Opcional: imprimir o stack trace para depuração
            // e.printStackTrace();
        } catch (java.util.InputMismatchException e) {
            System.err.println("Entrada inválida. Por favor, digite um número de ID válido.");
            scanner.nextLine(); // Limpa o buffer do scanner para evitar um loop infinito
        }
    }

    private void viewCategories() {
        try {
            List<Categoria> categorias = categoriaDao.readAll();
            if (categorias.isEmpty()) {
                System.out.println("Nenhuma categoria cadastrado.");
                return;
            }
            System.out.println("\n--- Lista de Categorias ---");
            for (Categoria categoria : categorias) {
                System.out.printf("ID: %d, Nome: %s\n",
                        categoria.getId(), categoria.getNome());
            }
        } catch (SQLException e) {
            System.err.println("Erro de banco de dados ao visualizar categorias: " + e.getMessage());
        }
    }

    private void insertCategory() {
        try {
            System.out.print("Nome da categoria: ");
            String categoryName = scanner.nextLine();
            // ID da categoria deve ser gerado pelo banco de dados, então passamos 0 ou um construtor sem ID.
            Categoria categoria = new Categoria(0, categoryName);
            if (categoriaDao.create(categoria)) {
                System.out.println("Categoria inserida com sucesso!");
            } else {
                System.out.println("Falha ao inserir categoria.");
            }
        } catch (SQLException e) {
            System.err.println("Erro de banco de dados ao inserir categoria: " + e.getMessage());
        }
    }

    private void showBookMenu() {
        while (true) {
            System.out.println("\n=== Gestão de Livros ===");
            System.out.println("1. Inserir livro");
            System.out.println("2. Visualizar livros");
            System.out.println("3. Excluir livro");
            System.out.println("4. Adicionar exemplar");
            System.out.println("5. Remover exemplar");
            System.out.println("0. Voltar");
            System.out.print("Escolha uma opção: ");

            int option = scanner.nextInt();
            scanner.nextLine();

            switch (option) {
                case 1:
                    insertBook();
                    break;
                case 2:
                    viewBook();
                    break;
                case 3:
                    deleteBook();
                    break;
                case 4:
                    insertCopy();
                    break;
                case 5:
                    deleteCopy();
                    break;
                case 0:
                    return; // Retorna ao menu principal
                default:
                    System.out.println("Opção inválida!");
            }
        }
    }

    private void deleteCopy() {
        System.out.println("\n--- Excluir Exemplar ---");
        // Primeiro, mostramos os exemplares existentes para que o usuário saiba qual ID usar.
        viewCopies();

        try {
            System.out.print("\nDigite o ID do exemplar que deseja excluir: ");
            int id = scanner.nextInt();
            scanner.nextLine(); // Consome a nova linha

            // O metodo delete do DAO retorna true se a exclusão foi bem-sucedida
            boolean success = exemplarDao.delete(id); // Usa o DAO para deletar

            if (success) {
                System.out.println("Exemplar excluído com sucesso!");
            } else {
                // Isso geralmente acontece se não houver um livro com o ID fornecido.
                System.out.println("Não foi possível encontrar um exemplar com o id " + id + ".");
            }
        } catch (SQLException e) {
            // Este erro é comum se o autor estiver ligado a um livro (violação de chave estrangeira).
            System.err.println("Erro de banco de dados ao excluir exemplar. ");
            // Opcional: imprimir o stack trace para depuração
            // e.printStackTrace();
        } catch (java.util.InputMismatchException e) {
            System.err.println("Entrada inválida. Por favor, digite um número de ID válido.");
            scanner.nextLine(); // Limpa o buffer do scanner para evitar um loop infinito
        }
    }

    private void insertCopy() {
        try {
            System.out.println("\n--- Adicionar exemplares ---");
            viewBook();
            System.out.print("ISBN do livro: ");
            String bookIsbn = scanner.nextLine();
            Livro livro = livroDao.read(bookIsbn);
            System.out.print("Quantidade de exemplares: ");
            int copies = scanner.nextInt();
            for (int i = 0; i < copies; i++) {
                Exemplar_livro exemplarLivro = new Exemplar_livro(0, livro, false);
                if (exemplarDao.create(exemplarLivro)) {
                    System.out.println("Exemplar inserido com sucesso!");
                } else {
                    System.out.println("Falha ao inserir exemplar.");
                }
            }
        }catch(SQLException e){
            System.err.println("Erro de banco de dados ao inserir exemplar: " + e.getMessage());
        }
    }
    private void viewCopies(){
        try {
            List<Exemplar_livro> exemplares = exemplarDao.readAll();
            if (exemplares.isEmpty()) {
                System.out.println("Nenhuma exemplar cadastrado.");
                return;
            }
            System.out.println("\n--- Lista de exemplares ---");
            for (Exemplar_livro exemplar : exemplares) {
                System.out.printf("ID: %s, Título: %s, ISBN: %s, Status: %b\n",
                        exemplar.getId(), exemplar.getLivro().getTitulo(), exemplar.getLivro().getIsbn(), exemplar.isStatus());
            }
        } catch (SQLException e) {
            System.err.println("Erro de banco de dados ao visualizar livros: " + e.getMessage());
        }
    }

    private void deleteBook() {
        System.out.println("\n--- Excluir Livro ---");
        // Primeiro, mostramos os livros existentes para que o usuário saiba qual ID usar.
        viewBook();

        try {
            System.out.print("\nDigite o ISBN do livro que deseja excluir: ");
            String id = scanner.nextLine();

            // O metodo delete do DAO retorna true se a exclusão foi bem-sucedida
            boolean success = livroDao.delete(id); // Usa o DAO para deletar

            if (success) {
                System.out.println("Livro excluído com sucesso!");
            } else {
                // Isso geralmente acontece se não houver um livro com o ID fornecido.
                System.out.println("Não foi possível encontrar um livro com o ISBN " + id + ".");
            }
        } catch (SQLException e) {
            // Este erro é comum se o autor estiver ligado a um livro (violação de chave estrangeira).
            System.err.println("Erro de banco de dados ao excluir livro. ");
            // Opcional: imprimir o stack trace para depuração
            // e.printStackTrace();
        } catch (java.util.InputMismatchException e) {
            System.err.println("Entrada inválida. Por favor, digite um número de ID válido.");
            scanner.nextLine(); // Limpa o buffer do scanner para evitar um loop infinito
        }
    }

    private void viewBook() {
        try {
            List<Livro> livros = livroDao.readAll();
            if (livros.isEmpty()) {
                System.out.println("Nenhuma livro cadastrado.");
                return;
            }
            System.out.println("\n--- Lista de livros ---");
            for (Livro livro : livros) {
                System.out.printf("ISBN: %s, Título: %s, Ano de publicação: %s, Categoria: %s, Quantidade de exemplares: %d\n",
                        livro.getIsbn(), livro.getTitulo(), livro.getAnoPublicacao(), livro.getCategoria().getNome(), livro.getQuantidadeExemplares());
            }
        } catch (SQLException e) {
            System.err.println("Erro de banco de dados ao visualizar livros: " + e.getMessage());
        }
    }

    private void insertBook() {
        try {
            System.out.print("ISBN do livro: ");
            String bookIsbn = scanner.nextLine().trim();
            System.out.print("Título do livro: ");
            String bookName = scanner.nextLine().trim();
            int year;
            while (true) {
                try {
                    System.out.print("Ano de publicação: ");
                    year = scanner.nextInt();
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Por favor, digite um ano válido.");
                }
            }
            viewCategories();
            int categoryId;
            while (true) {
                try {
                    System.out.print("Inserir id da categoria: ");
                    categoryId = scanner.nextInt();
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Por favor, digite um ID válido.");
                }
            }
            Categoria categoria = categoriaDao.read(categoryId);
            if (categoria == null) {
                System.out.println("Categoria não encontrada!");
                return;
            }
            viewAuthors();
            List<Autor> authors = new ArrayList<>();
            while (true) {
                try {
                    System.out.println("Insira o id do autor: ");
                    Integer authorId = scanner.nextInt();
                    scanner.nextLine();
                    Autor autor = autorDao.read(authorId);
                    if (autor != null) {
                        authors.add(autor);
                        System.out.println("Autor adicionado com sucesso!");
                    } else {
                        System.out.println("Autor não encontrado!");
                    }
                    System.out.print("Inserir mais autores? (S/N): ");
                    String answer = scanner.nextLine().toUpperCase();
                    if (answer.equals("N")) {
                        break;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Por favor, digite um ID válido.");
                }
            }

            if (authors.isEmpty()) {
                System.out.println("É necessário adicionar pelo menos um autor!");
                return;
            }
            Livro livro = new Livro(bookIsbn, categoria, bookName, year, authors);
            if (livroDao.create(livro)) {
                System.out.println("Livro inserido com sucesso!");
            } else {
                System.out.println("Falha ao inserir livro.");
            }
        } catch (SQLException e) {
            System.err.println("Erro de banco de dados ao inserir livro: " + e.getMessage());
        }
    }

    private void showLoanMenu() {
        while (true) {
            System.out.println("\n=== Gestão de empréstimos ===");
            System.out.println("1. Cadastrar empréstimo");
            System.out.println("2. Visualizar empréstimos");
            System.out.println("3. Cadastrar devolução");
            System.out.println("0. Voltar");
            System.out.print("Escolha uma opção: ");

            int option = scanner.nextInt();
            scanner.nextLine();

            switch (option) {
                case 1:
                    insertLoan();
                    break;
                case 2:
                    viewLoans();
                    break;
                case 3:
                    updateLoan();
                    break;
                case 0:
                    return; // Retorna ao menu principal
                default:
                    System.out.println("Opção inválida!");
            }
        }
}

    private void updateLoan() {
        viewLoans();
        Scanner scanner = new Scanner(System.in);
        LocalDate devolutionDate = null;
        DateTimeFormatter brazilianFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        System.out.println("ID do empréstimo: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        while (devolutionDate == null) {
            // Informa ao usuário o formato esperado.
            System.out.print("Digite a data da devolução (formato DD/MM/AAAA): ");
            String dateAsString = scanner.nextLine();

            try {
                // 2. Use o formatador para fazer o "parse" da String.
                devolutionDate = LocalDate.parse(dateAsString, brazilianFormat);

            } catch (DateTimeParseException e) {
                // 3. Se o formato for inválido, o erro será capturado.
                System.err.println("Formato de data inválido! Por favor, use o formato DD/MM/AAAA.");
            }
        }
        try {
            emprestimoDao.update(id, devolutionDate);
        }catch (SQLException e){
            System.out.println("Erro ao cadastrar devolução: " + e.getMessage());
        }
    }

    private void viewLoans() {
        try {
            List<Emprestimo> emprestimos = emprestimoDao.readAll();
            if (emprestimos.isEmpty()) {
                System.out.println("Nenhum empréstimo cadastrado.");
                return;
            }
            System.out.println("\n--- Lista de empréstimos ---");
            for (Emprestimo emprestimo : emprestimos) {
                System.out.printf("ID: %s, Data do empréstimo: %s, Data da devolução: %s, Usuário: %s, Livro: %s\n",
                        emprestimo.getId(), emprestimo.getDataEmprestimo(), emprestimo.getDataDevolucao(), emprestimo.getUsuario().getNome(), emprestimo.getLivro().getLivro().getTitulo());
            }
        } catch (SQLException e) {
            System.err.println("Erro de banco de dados ao visualizar livros: " + e.getMessage());
        }
    }

    private void insertLoan() {
        try {
            Scanner scanner = new Scanner(System.in);
            LocalDate loanDate = null; // Inicia como nulo

            // 1. Crie um DateTimeFormatter com o padrão brasileiro.
            DateTimeFormatter brazilianFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            while (loanDate == null) {
                // Informa ao usuário o formato esperado.
                System.out.print("Digite a data do empréstimo (formato DD/MM/AAAA): ");
                String dateAsString = scanner.nextLine();

                try {
                    // 2. Use o formatador para fazer o "parse" da String.
                    loanDate = LocalDate.parse(dateAsString, brazilianFormat);

                } catch (DateTimeParseException e) {
                    // 3. Se o formato for inválido, o erro será capturado.
                    System.err.println("Formato de data inválido! Por favor, use o formato DD/MM/AAAA.");
                }
            }
            viewUsers();
            System.out.print("Usuário: ");
            int userId = scanner.nextInt();
            Usuario user = usuarioDao.read(userId);
            viewCopies();
            System.out.print("ID do exemplar: ");
            int exemplarId = scanner.nextInt();
            Exemplar_livro exemplar = exemplarDao.read(exemplarId);
            Emprestimo emprestimo = new Emprestimo(loanDate, user, exemplar);
            if (emprestimoDao.create(emprestimo)) { // A chamada ao DAO agora funciona
                System.out.println("Empréstimo cadastrado com sucesso!");
            } else {
                System.out.println("Falha ao cadastrar empréstimo.");
            }
        } catch (SQLException e) {
            System.err.println("Erro de banco de dados ao inserir empréstimo: " + e.getMessage());
        }
    }
}