package br.inatel.Interfaces;

import br.inatel.Biblioteca.Autor;

import java.sql.SQLException;
import java.util.List;

public interface Dao<T, K> {
    boolean create(T entity) throws SQLException;
    T read(K id) throws SQLException;
    List<T> readAll() throws SQLException;
    boolean delete(K id) throws SQLException;
}
