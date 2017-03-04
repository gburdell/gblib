/*
 * The MIT License
 *
 * Copyright 2017 gburdell.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package gblib;

import static gblib.Util.abnormalExit;
import static gblib.Util.invariant;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Map;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Top-level interface to facilitate (more) convenient manipulation of
 * databases.
 *
 * @author kpfalzer
 */
public class Model {

    public static interface IConnection {

        public Connection getConnection();
    }

    public Model(String tblName, IConnection conn, boolean zeroOnNull) throws SQLException {
        m_tblName = tblName;
        m_conn = conn;
        m_zeroOnNull = zeroOnNull;
        setup();
    }

    public Model(String tblName, IConnection conn) throws SQLException {
        this(tblName, conn, true);
    }

    private final boolean m_zeroOnNull;

    private static final String LOCK_TABLE = "LOCK TABLES @TBL@ WRITE";
    private static final String UNLOCK_TABLE = "UNLOCK TABLES";

    public String subTable(String s) {
        return s.replace("@TBL@", getTableName());
    }

    public void lockTable(Connection conn) throws SQLException {
        conn.setAutoCommit(false);
        PreparedStatement stmt = Model.getPreparedStatement(conn, subTable(LOCK_TABLE));
        invariant(!stmt.execute());
    }

    public void unlockTable(Connection conn) throws SQLException {
        invariant(!createStatement(conn).execute(UNLOCK_TABLE));
        conn.setAutoCommit(true);
    }

    public long getMaxID(Connection conn) throws SQLException {
        String stmt = "SELECT MAX(ID) FROM " + getTableName();
        ResultSet rs = createStatement(conn).executeQuery(stmt);
        boolean b = rs.first();
        assert b;
        long id = rs.getLong(1);
        return id;
    }

    public static int getSqlType(String type) {
        int r = -1;
        switch (type) {
            case "LONG VARCHAR":
            case "VARCHAR":
            case "TEXT":
                r = Types.LONGVARCHAR;
                break;
            case "TIMESTAMP":
            case "DATETIME":
                r = Types.TIMESTAMP;
                break;
            case "INTEGER":
            case "INT":
                r = Types.INTEGER;
                break;
            case "REAL":
            case "FLOAT":
                r = Types.REAL;
                break;
            case "CHAR":
                r = Types.CHAR;
                break;
            case "BIT":
            case "TINYINT":
                r = Types.BIT;
                break;
            default:
                assert false;
        }
        return r;
    }

    private final IConnection m_conn;

    private Connection getConnection() {
        return m_conn.getConnection();
    }

    public String getTableName() {
        return m_tblName;
    }

    /**
     * Get column names in order (except ID).
     *
     * @return ordered column names.
     */
    public List<String> getColumnNames() {
        return m_colNames;
    }

    private static final String TABLE_COLTYPES
            = "select distinct column_name, data_type from information_schema.columns where table_name = ?";

    private static final String GET_TABLE_NAMES
            = "select distinct table_name from information_schema.tables";

    /**
     * Set the correct case-sensitive table name. In (real) *Nix, the table name
     * is case-sensitive; but in MacOSX/Windows it is not.
     */
    private void setTableName() throws SQLException {
        boolean done = false;
        try (Connection conn = getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery(GET_TABLE_NAMES);
            while (!done && rs.next()) {
                String realTblName = rs.getString(1);
                if (m_tblName.equalsIgnoreCase(realTblName)) {
                    m_tblName = realTblName;
                    done = true;
                }
            }
            invariant(done);
        }
    }

    private void setup() throws SQLException {
        if (null == m_colInfo) {
            setTableName();
            int coli = 1;
            try (Connection conn = getConnection()) {
                PreparedStatement stmt = getPreparedStatement(conn, TABLE_COLTYPES);
                stmt.setString(1, m_tblName);
                ResultSet rs = stmt.executeQuery();
                String colNm, typeNm;
                while (rs.next()) {
                    colNm = rs.getString("COLUMN_NAME").toUpperCase();
                    typeNm = rs.getString("DATA_TYPE").toUpperCase();
                    if (null == m_colInfo) {
                        m_colInfo = new LinkedHashMap<>(); //keep insert/key order
                    }
                    assert !m_colInfo.containsKey(colNm);
                    m_colInfo.put(colNm, new PosType(coli, typeNm));
                    coli++;
                }
                m_colNames = new LinkedList<>(m_colInfo.keySet());
                invariant(m_colNames.remove("ID"));
                StringBuilder bld = new StringBuilder("INSERT INTO ");
                bld
                        .append(getTableName())
                        .append(" (")
                        .append(Util.toCSV(getColumnNames()))
                        .append(") VALUES (")
                        .append(Util.toCSV(Util.replicate("?", getColumnNames().size())))
                        .append(")");
                m_insertStmt = bld.toString();
            }
        }
    }

    /**
     * Get index of ID column.
     *
     * @return column index.
     */
    private int getIdCol() {
        return m_colInfo.get("ID").v1;
    }

    /**
     * Since we build up insert prepared statement without an ID, we need to
     * offset for cols occuring after ID.
     *
     * @param col column index to offset.
     * @return position to update in prepared statement.
     */
    private int getInsertIndex(int col) {
        if (col > getIdCol()) {
            col--;
        }
        return col;
    }

    public static java.sql.Date getCurrentTime() {
        return new java.sql.Date(System.currentTimeMillis());
    }

    public static Timestamp getCurrentTimeStamp() {
        return asTimestamp(getCurrentTime());
    }

    public static Timestamp asTimestamp(Date date) {
        return new Timestamp(date.getTime());
    }

    /**
     * Create PreparedStatement to insert values into table.
     *
     * @param conn database connection.
     * @param items Map of Object values by key aligned with column names.
     * @return completed PreparedStatement to insert.
     * @throws SQLException
     */
    public PreparedStatement insert(Connection conn, Map<String, Object> items) throws SQLException {
        PreparedStatement stmt = getPreparedStatement(conn, m_insertStmt);
        invariant(getColumnNames().size() == items.size());  //dont count ID
        for (String colNm : items.keySet()) {
            PosType pt = m_colInfo.get(colNm.toUpperCase());
            invariant(null != pt);
            Object val = items.get(colNm);
            int stmtPos = getInsertIndex(pt.v1);
            switch (pt.v2) {
                case Types.INTEGER:
                    Long lng;
                    if ((null == val) && m_zeroOnNull) {
                        lng = 0L;
                    } else if (val instanceof Number) {
                        Number n = Util.downCast(val);
                        lng = n.longValue();
                    } else {
                        //NOTE: m_zeroOnNull mitigates null here...
                        lng = Long.parseLong(val.toString());
                    }
                    stmt.setLong(stmtPos, lng);
                    break;
                case Types.LONGVARCHAR:
                case Types.CHAR:
                    stmt.setString(stmtPos, val.toString());
                    break;
                case Types.TIMESTAMP:
                    try {
                        Date date = (null != val) ? DATE_FMT.parse(val.toString()) : getCurrentTime();
                        stmt.setTimestamp(stmtPos, asTimestamp(date));
                    } catch (ParseException ex) {
                        abnormalExit(ex);
                    }
                    break;
                case Types.BIT:
                    Boolean bitv = (null != val) ? Boolean.parseBoolean(val.toString()) : false;
                    stmt.setBoolean(stmtPos, bitv);
                    break;
                case Types.REAL:
                    Double dbl;
                    if ((null == val) && m_zeroOnNull) {
                        dbl = 0.0;
                    } else if (val instanceof Number) {
                        Number n = Util.downCast(val);
                        dbl = n.doubleValue();
                    } else {
                        //NOTE: m_zeroOnNull mitigates null here...
                        dbl = Double.parseDouble(val.toString());
                    }
                    stmt.setDouble(stmtPos, dbl);
                    break;
                default:
                    invariant(false);
            }
        }
        return stmt;
    }

    //2017-02-26 15:03:52 -0800
    private static final DateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

    public static Statement createStatement(Connection conn) throws SQLException {
        return createStatement(conn, true);
    }

    public static Statement createStatement(Connection conn, boolean updatable) throws SQLException {
        return conn.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                (updatable) ? ResultSet.CONCUR_UPDATABLE : ResultSet.CONCUR_READ_ONLY);
    }

    public static PreparedStatement getPreparedStatement(Connection conn, String stmt) throws SQLException {
        return getPreparedStatement(conn, stmt, true);
    }

    public static PreparedStatement getPreparedStatement(Connection conn, String stmt, boolean updatable) throws SQLException {
        return conn.prepareStatement(
                stmt,
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                (updatable) ? ResultSet.CONCUR_UPDATABLE : ResultSet.CONCUR_READ_ONLY);
    }

    /**
     * Get column type.
     *
     * @param colnm column name.
     * @return sql.Types value for valid colnm, else NULL (type).
     */
    public int getColType(String colnm) {
        colnm = colnm.toUpperCase();
        return (m_colInfo.containsKey(colnm)) ? m_colInfo.get(colnm).v2 : Types.NULL;
    }

    /**
     * Wrap value as Java representation of SqlType.
     */
    public static class SqlVal {

        public SqlVal(char s[]) {
            m_sqlType = Types.CHAR;
            m_val = s;
        }

        public SqlVal(String s) {
            m_sqlType = Types.LONGVARCHAR;
            m_val = s;
        }

        public void set(String s) {
            assert (m_sqlType == Types.LONGVARCHAR || m_sqlType == Types.CHAR);
            m_val = s;
        }

        public SqlVal(float s) {
            m_sqlType = Types.REAL;
            m_val = s;
        }

        public SqlVal(final ResultSet rs, int col) throws SQLException {
            m_sqlType = rs.getMetaData().getColumnType(col);
            switch (m_sqlType) {
                case Types.INTEGER:
                    m_val = rs.getLong(col);
                    break;
                case Types.LONGVARCHAR:
                case Types.CHAR:
                    m_val = rs.getString(col);
                    break;
                case Types.TIMESTAMP:
                    m_val = rs.getTimestamp(col);
                    break;
                case Types.REAL:
                    m_val = rs.getFloat(col);
                    break;
                default:
                    assert false;
            }
        }

        public SqlVal(final ResultSet rs, String col, int sqlType) throws SQLException {
            m_sqlType = sqlType;
            switch (m_sqlType) {
                case Types.INTEGER:
                    m_val = rs.getLong(col);
                    break;
                case Types.LONGVARCHAR:
                case Types.CHAR:
                    m_val = rs.getString(col);
                    break;
                case Types.TIMESTAMP:
                    m_val = rs.getTimestamp(col);
                    break;
                case Types.REAL:
                    m_val = rs.getFloat(col);
                    break;
                default:
                    assert false;
            }
        }

        public Long asLong() {
            return (Long) m_val;
        }

        public Timestamp asTimeStamp() {
            return (Timestamp) m_val;
        }

        public String asString() {
            return (String) m_val;
        }

        public Float asFloat() {
            return (Float) m_val;
        }

        public final static String EMPTY = "";

        @Override
        public String toString() {
            return (m_val != null) ? m_val.toString() : EMPTY;
        }

        public int getType() {
            return m_sqlType;
        }
        private final int m_sqlType;
        private Object m_val = false;
    }

    /**
     * Column index and type.
     */
    public static class PosType extends Pair<Integer, Integer> {

        public PosType(int col, String type) {
            super(col, getSqlType(type));
        }
    }

    private String m_insertStmt;
    private Map<String, PosType> m_colInfo = null;
    private List<String> m_colNames = Collections.EMPTY_LIST;
    private String m_tblName;
}
