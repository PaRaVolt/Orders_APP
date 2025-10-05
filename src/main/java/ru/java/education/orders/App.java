package ru.java.education.orders;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class App {

    public static void main(String[] args) throws SQLException {
        System.out.println("Начинаем работу с order_db!");

        PropertiesLoader loader = new PropertiesLoader();
        try (Connection conn = DriverManager.getConnection(loader.getProperty("db.url"), loader.getProperty("db.username"), loader.getProperty("db.password"))) {
            conn.setAutoCommit(true);

            System.out.println("Заполняем БД тестовыми данными");
            Execute_Tasks(conn, "src/main/resources/fill_db.sql");

            System.out.println("");
            System.out.println("Пример применения commit и rollback");
            Commit_Rollback(conn);

            System.out.println("");
            System.out.println("Пример создания заказа и обновления занных на складе");
            Create_Order(conn, "Слава","Аллахов","Отвёртка",5);

            System.out.println("");
            System.out.println("Пример вывода данных через select с LEFT JOIN");
            Select_Task(conn);

            System.out.println("");
            System.out.println("Обновление записи");
            Update_Price(conn, "Отвёртка", 303.30);

            System.out.println("");
            System.out.println("Удаление лишних записей в таблице заказчиков");
            Delete_Customers(conn);

            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void Execute_Tasks(Connection conn, String source_file) throws SQLException {
        try
        {
            Statement st = conn.createStatement();
            BufferedReader in = new BufferedReader(new FileReader(source_file));
            String str;
            while ((str = in.readLine()) != null) {
                if(str.isEmpty()){
                    continue;
                }

                try {
                    System.out.println("Отправляем запрос в БД: " + str);
                    ResultSet rs = st.executeQuery(str);

                    while (rs.next()) {
                        System.out.println(rs.getString(1));
                    }

                    rs.close();
                }
                catch(SQLException err){
                    System.out.println(err.getMessage());
                }
            }
            in.close();
            st.close();
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
        }
    }

    private static final String INSERT_CUSTOMERS_SQL = "INSERT INTO customer (name, surname, phone) VALUES (?, ?, ?);";
//    private static final String INSERT_PRODUCTS_SQL = "INSERT INTO product (name, product_type_id, cost) VALUES (?, (select id from product_type where name='?'), ?);";
    private static final String INSERT_PRODUCTS_SQL = "INSERT INTO product (name, product_type_id, cost) VALUES (?, ?, ?);";
    private static final String UPDATE_SKLAD_SQL = "UPDATE sklad SET pr_count = ? WHERE product_id = ?;";

    private static void Commit_Rollback(Connection conn) throws SQLException {
        conn.setAutoCommit(false);
        Statement st = conn.createStatement();
        ResultSet rs;

        try (PreparedStatement insertCustomerStmt = conn.prepareStatement(INSERT_CUSTOMERS_SQL)) {
            insertCustomerStmt.setString(1, "Удар");
            insertCustomerStmt.setString(2, "Морозов");
            insertCustomerStmt.setString(3, "47534545");
            insertCustomerStmt.execute();
            conn.commit();
            System.out.println("Transaction committed successfully.");

        } catch (SQLException e) {
            conn.rollback();
            System.out.println(e.getMessage());
        }

        rs = st.executeQuery("select id from product_type where name='Инструмент'");
        int pt_id = 0;
        if(rs.next()){
            pt_id = rs.getInt(1);
        }
        if(pt_id == 0){
            return;
        }

        try (PreparedStatement insertProductStmt = conn.prepareStatement(INSERT_PRODUCTS_SQL)) {

            insertProductStmt.setString(1, "Болгарка");
            insertProductStmt.setInt(2, pt_id);
            insertProductStmt.setDouble(3, 12000);
            insertProductStmt.execute();

            conn.commit();
            System.out.println("Transaction committed successfully.");

        } catch (SQLException e) {
            conn.rollback();
            System.out.println(e.getMessage());
        }

        try (PreparedStatement updateSkladStmt = conn.prepareStatement(UPDATE_SKLAD_SQL)) {

            rs = st.executeQuery("select id from product where name='Болгарка'");
            int p_id = 0;
            if(rs.next()){
                p_id = rs.getInt(1);
            }
            if(p_id == 0){
                return;
            }

            updateSkladStmt.setInt(1, 3);
            updateSkladStmt.setInt(2, p_id);
            updateSkladStmt.executeUpdate();

            conn.commit();
            System.out.println("Transaction committed successfully.");

        } catch (SQLException e) {
            conn.rollback();
            System.out.println(e.getMessage());
        }
        conn.setAutoCommit(true);
    }

    private static void Create_Order(Connection conn, String name, String surname, String product, int count) throws SQLException {

        try{
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("select SUM(s.pr_count),s.product_id from sklad s, product p where s.product_id = p.id and p.name='"+product+"' GROUP BY s.product_id");
            int kol = 0;
            int id = 0;
            if(rs.next()){
                kol = rs.getInt(1);
                id = rs.getInt(2);
            }

            if(!surname.isEmpty()){
                rs = st.executeQuery("select id from customer where name='"+name+"' and surname='"+surname+"'");
            }
            else{
                rs = st.executeQuery("select id from customer where name='"+name+"'");
            }
            int c_id = 0;
            if(rs.next()){
                c_id = rs.getInt(1);
            }


            if(kol > 0)
            {
                if(kol >= count){
                    st.execute("INSERT INTO orders (customer_id, product_id, order_status_id, pr_count, order_date) values ("+c_id+", (select id from product where name='"+product+"'), (select id from order_status where name='Новый'), "+count+",NOW());");
                    st.execute("UPDATE sklad SET pr_count=" + (kol-count) +" WHERE product_id="+id);
                    System.out.println("Заказ успешно создан");
                }
                else{
                    System.out.println("Запрашиваемого количества товара на складе нет!");
                }
            }
            else{
                System.out.println("Указанного товара на складе нет!");
            }

            rs.close();
            st.close();


        }
        catch(Exception err){
            System.out.println(err.getMessage());
        }
    }

    public static void Select_Task(Connection conn){
        try{
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(getSelectString());

            String str = getResults(rs);
            System.out.println(str);

            System.out.println("");

            rs = st.executeQuery(getMostPopularString());
            str = getResults(rs);
            System.out.println(str);

            rs.close();
            st.close();
        }
        catch(SQLException err){
            System.out.println(err.getMessage());
        }
    }

    private static String getSelectString() {
        String query = "SELECT o.order_date as Дата, os.name as Статус, c.name as Имя, c.surname as Фамилия, pt.name as Категория,p.name as Продукт, o.pr_count as Количество FROM orders o LEFT JOIN customer c ON c.id = o.customer_id";
        query += " LEFT JOIN product p ON p.id = o.product_id";
        query += " LEFT JOIN order_status os ON os.id = o.order_status_id";
        query += " LEFT JOIN product_type pt ON pt.id = p.product_type_id";
        query += " ORDER BY o.order_date DESC LIMIT 5";
        return query;
    }
    private static String getMostPopularString() {
        String query = "select COUNT(o.product_id) as Количество, p.name as Название FROM orders o";
        query += " LEFT JOIN product p ON p.id = o.product_id";
        query += " GROUP BY p.name ORDER BY Количество DESC LIMIT 3";
        return query;
    }

    public static String getResults(ResultSet rs) {
        try {
            String result = "";

            ResultSetMetaData rsMeta = rs.getMetaData();
            int count = rsMeta.getColumnCount();
            int i, j = 1;
            result += "\n| ";
            while (j <= count) {
                String format = "%1$-" + rsMeta.getColumnDisplaySize(j) + "s";
                String formatedValue = String.format(format, rsMeta.getColumnLabel(j));
                result += formatedValue + "| ";
                j++;
            }
            result += "\n" + new String(new char[result.length()]).replace("\0", "-");
            while (rs.next()) {
                i = 1;
                result += "\n| ";
                while (i <= count) {
                    String format = "%1$-" + rsMeta.getColumnDisplaySize(i) + "s";
                    String formatedValue = String.format(format, new String(rs.getBytes(i), StandardCharsets.UTF_8));
                    result += formatedValue + "| ";
                    i++;
                }
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace(System.out);

            return "";
        }
    }

    public static void Update_Price(Connection conn, String name, Double new_price){
        try{
            Statement st = conn.createStatement();
            st.execute("UPDATE product SET cost=" + (new_price) +" WHERE name='"+name+"'");

            st.close();
        }
        catch(SQLException err){
            System.out.println(err.getMessage());
        }
    }

    public static void Delete_Customers(Connection conn){
        try{
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT COUNT(id) FROM customer");
            int c_count_before = 0;
            if(rs.next()){
                c_count_before = rs.getInt(1);
            }

            st.execute("DELETE FROM customer WHERE id NOT IN (SELECT customer_id FROM orders)");

            rs = st.executeQuery("SELECT COUNT(id) FROM customer");
            int c_count_after = 0;
            if(rs.next()){
                c_count_after = rs.getInt(1);
            }

            System.out.println("Удалено " + (c_count_before - c_count_after) + " записей.");

            st.close();
        }
        catch(SQLException err){
            System.out.println(err.getMessage());
        }
    }
}