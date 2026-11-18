package br.uff.store.infrastructure.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.uff.store.core.exceptions.StoreException;
import io.github.cdimascio.dotenv.Dotenv;

public class MySQLDAO {

    private Connection conn = null;

    public void dbTransaction(String[] comandos, Object[][] bind) throws SQLException {
        try {
            this.abreConexao();
            this.conn.setAutoCommit(false);
            Integer aux = 0;
            for (String comando : comandos) {
                // pega comando e verifica se index existe e pega bind
                this.dbGrava(comando, aux < bind.length ? bind[aux] : null, true);
                // incrementa aux
                aux += 1;
            }
            this.conn.commit();
        } catch (SQLException ex) {
            this.conn.rollback();
            throw new SQLException(ex.getMessage());
        } finally {
            this.fechaConexao();
            this.conn.setAutoCommit(true);
        }
    }

    public Integer dbGrava(String comando, Object[] bind, Boolean ehTransacao) throws SQLException {
        this.abreConexao();
        PreparedStatement sql = this.montaComando(comando, bind);
        Integer retorno = sql.executeUpdate();
        this.fechaComando(sql);
        if (!Boolean.TRUE.equals(ehTransacao)) {
            this.fechaConexao();
        }
        return retorno;
    }

    public Object dbValor(String campo, String tabela, String filtro, Object[] bind) throws SQLException {
        if (!tabela.toUpperCase().startsWith("SELECT")) {
            tabela = "SELECT * FROM " + tabela;
        }
        if (!filtro.equals("")) {
            tabela = "SELECT * FROM (" + tabela + ") _x1 WHERE " + filtro;
        }
        String consulta = "SELECT " + campo + " FROM (" + tabela + ") _x2";

        List<HashMap<String, Object>> ret = this.dbCarrega(consulta, bind);
        if (!ret.isEmpty()) {
            // pega primeira coluna da primeira row
            return ret.get(0).values().toArray()[0];
        }
        return null;
    }

    public List<HashMap<String, Object>> dbCarrega(String consulta, Object[] bind) throws SQLException {
        this.abreConexao();
        PreparedStatement sql = this.montaComando(consulta, bind);
        ResultSet rs = sql.executeQuery();
        List<HashMap<String, Object>> ret = this.transformResultSetInArrayListHashMap(rs);
        this.fechaComando(sql);
        this.fechaConexao();
        return ret;
    }

    public void destroyDb() throws StoreException {
        try {
            this.conn.close();
        } catch (Exception e) {
            throw new StoreException("Não foi possível fechar a conexão com o banco de dados.");
        }
    }

    /**
     *
     * @param sql
     * @param bind
     * @return
     * @throws java.sql.SQLException
     */
    public PreparedStatement montaComando(String sql, Object[] bind) throws SQLException {
        PreparedStatement comando;
        // inicia comando
        comando = this.conn.prepareStatement(sql);
        // retorna comando com bind inserido
        if (bind != null) {
            comando = this.setBind(comando, bind);
        }
        return comando;
    }

    public void abreConexao() throws SQLException {
        if (this.conn != null && !this.conn.isClosed()) {
            return;
        }
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        String url = setting(dotenv, "MYSQL_CONNECTION_STRING", "jdbc:mysql://localhost:3306/e_store?useSSL=false&serverTimezone=UTC");
        String user = setting(dotenv, "MYSQL_USER", "e_store");
        String password = setting(dotenv, "MYSQL_PASSWORD", "123456");
        this.conn = DriverManager.getConnection(url, user, password);
    }

    private String setting(Dotenv dotenv, String name, String fallback) {
        String environmentValue = System.getenv(name);
        if (environmentValue != null && !environmentValue.trim().isEmpty()) {
            return environmentValue;
        }
        String fileValue = dotenv.get(name);
        return fileValue == null || fileValue.trim().isEmpty() ? fallback : fileValue;
    }

    public void fechaConexao() throws SQLException {
        if (!this.conn.isClosed()) {
            this.conn.close();
        }
    }

    public void fechaComando(PreparedStatement comando) throws SQLException {
        if (!comando.isClosed()) {
            comando.close();
        }
    }

    public String pegaDbNome() throws SQLException {
        return this.conn.getMetaData().getDatabaseProductName();
    }

    public PreparedStatement setBind(PreparedStatement comando, Object[] bind) throws SQLException {
        // define contador
        Integer contador = 1;
        for (Object valor : bind) {
            // insere valor no Statement
            comando.setObject(contador, valor);
            // incrementa contador
            contador += 1;
        }
        // retorna comando com o bind definido
        return comando;
    }

    public List<HashMap<String, Object>> transformResultSetInArrayListHashMap(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        List<HashMap<String, Object>> list = new ArrayList<>();
        while (rs.next()) {
            HashMap<String, Object> row = new HashMap<>(columns);
            for (int i = 1; i <= columns; ++i) {
                row.put(md.getColumnLabel(i), rs.getString(i));
            }
            list.add(row);
        }
        return list;
    }
}
