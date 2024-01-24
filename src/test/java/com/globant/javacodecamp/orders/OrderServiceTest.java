package com.globant.javacodecamp.orders;


import static org.junit.jupiter.api.Assertions.*;


import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Slf4j
@Testcontainers
class OrderServiceTest {
    private OrderService orderService;

    // We´ll use Testcontainer to emulate the MySQL database and to mock the tests
    @Container
    private MySQLContainer<?> mysql = getMySqlContainer();
    @NonNull
    private MySQLContainer<?> getMySqlContainer(){
        return new MySQLContainer<>(DockerImageName.parse("mysql:8.0.32"))
                .withDatabaseName("shop")
                .withCopyFileToContainer(MountableFile.forClasspathResource(
                        "init.sql"),
                        "/docker-entrypoint-initdb.d/init.sql")
                .withUsername("root");
    }
    // -----------------------------------------------------------------------
    //TESTS
    @Test
    void testOrderDispatched(){

        orderService = new OrderService(mysql.getJdbcUrl());

        // create a jdbc connection
        var jdbcUrl = mysql.getJdbcUrl();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        // Dispatcher------------------------------------------
        // the dispatcher must go before the connection to run the test
        var order = orderService.dispatchOrder(1L);
        assertEquals(OrderState.DISPATCHED, order.getState());
        // -----------------------------------------------------

        try(Connection connection = DriverManager.getConnection(
                jdbcUrl,"root","test")){

        /*  TO REFACTOR the code below into line 73:
            Right-click on the selection and choose "Refactor" from the dropdown menu.
            Then, select "Extract Method":
                On Windows/Linux: Ctrl + Alt + M
                On macOS: Cmd + Alt + M
        */

/*            var resultSet = connection.createStatement()
                    .executeQuery("SELECT * FROM item WHERE id = %d".formatted(1L));
            resultSet.next();

            var actualStock = resultSet.getInt("stock");*/

            int actualStock = getActualStock(connection);

            assertEquals(98, actualStock);

        }catch(SQLException e){
            fail();
        }
    }
    @Test
    void testNotPayedDispatchedOrder(){
        var jdbcUrl = mysql.getJdbcUrl();

        orderService = new OrderService(jdbcUrl);

        // add an assertion that runs a runtime exception, because it is supposed to fail
        var exception = assertThrows(RuntimeException.class,() -> orderService.dispatchOrder(4L));
        assertTrue(exception.getMessage().contains("Not yet paid"));
    }

    @Test
    void testDispatchedNonExistingOrder(){
        var jdbcUrl = mysql.getJdbcUrl();

        orderService = new OrderService(jdbcUrl);

        // we are going to change the exception for a new one
        // change the name - create the class in the production code
        //assertThrows(NullPointerException.class,() -> orderService.dispatchOrder(5L));
        assertThrows(OrderNotFoundException.class, () -> orderService.dispatchOrder(5L));
    }

    private static int getActualStock(Connection connection) throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        var resultSet = connection.createStatement()
                .executeQuery("SELECT * FROM item WHERE id = %d".formatted(1L));
        resultSet.next();

        var actualStock = resultSet.getInt("stock");
        return actualStock;
    }

}