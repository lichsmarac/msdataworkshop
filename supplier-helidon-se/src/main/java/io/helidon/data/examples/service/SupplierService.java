package io.helidon.data.examples.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import io.helidon.config.Config;
import io.helidon.webserver.Routing.Rules;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

public class SupplierService implements Service {

    private PoolDataSource pool = null;

    public SupplierService(Config config) throws SQLException {

        String url = config.get("url").asString().get();
        String user = config.get("user").asString().get();
        String password = config.get("password").asString().get();
        System.out.printf("Using url: %s%n", url);
        pool = PoolDataSourceFactory.getPoolDataSource();
        pool.setURL(url);
        pool.setUser(user);
        pool.setPassword(password);
        pool.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
    }

    @Override
    public void update(Rules rules) {
        rules
                .get("/test", this::test)
                .get("/addInventory", this::addInventory)
                .get("/removeInventory", this::removeInventory)
                .get("/getInventoryCount", this::getInventoryCount);
    }


    void test(ServerRequest serverRequest, ServerResponse serverResponse) {
        System.out.println("SupplierService.addInventory test");
        serverResponse.send("test reply");
    }

    void addInventory(ServerRequest serverRequest, ServerResponse serverResponse) {
        String response;
        String itemid = serverRequest.queryParams().first("itemid").get();
        System.out.println("SupplierService.addInventory itemid:" + itemid);
        try (Connection conn = pool.getConnection()) {
            conn.createStatement().execute(
                    "UPDATE inventory SET inventorycount = inventorycount + 1 where inventoryid = '" + itemid + "'");
//              "update inventory set inventorycount = 1 where inventoryid = '"+ itemid + "'");?
            response = getInventoryCount(itemid, conn);
        } catch (SQLException ex) {
            response = ex.getMessage();
        }
        serverResponse.send(response);
    }

    void removeInventory(ServerRequest serverRequest, ServerResponse serverResponse) {
        String response;
        String itemid = serverRequest.queryParams().first("itemid").get();
        System.out.println("SupplierService.removeInventory itemid:" + itemid);
        try (Connection conn = pool.getConnection()) {
            conn.createStatement().execute(
                    "UPDATE inventory SET inventorycount = inventorycount - 1 where inventoryid = '" + itemid + "'");
            response = getInventoryCount(itemid, conn);
        } catch (SQLException ex) {
            response = ex.getMessage();
        }
        serverResponse.send(response);
    }

    void getInventoryCount(ServerRequest serverRequest, ServerResponse serverResponse) {
        String response;
        String itemid = serverRequest.queryParams().first("itemid").get();
        System.out.println("SupplierService.getInventoryCount itemid:" + itemid);
        try (Connection conn = pool.getConnection()) {
            response = getInventoryCount(itemid, conn);
        } catch (SQLException ex) {
            response = ex.getMessage();
        }
        serverResponse.send(response);
    }

    private String getInventoryCount(String itemid, Connection conn) throws SQLException {
        String response;
        ResultSet resultSet = conn.createStatement().executeQuery(
                "select inventorycount from inventory  where inventoryid = '" + itemid + "'");
        int inventorycount;
        if (resultSet.next()) {
            inventorycount = resultSet.getInt("inventorycount");
            System.out.println("MessagingService.doIncomingOutgoing inventorycount:" + inventorycount);
        } else inventorycount = 0;
        conn.close();
        response = "inventorycount for " + itemid + " is now " + inventorycount;
        return response;
    }

}