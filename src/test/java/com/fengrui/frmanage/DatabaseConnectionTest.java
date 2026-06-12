package com.fengrui.frmanage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testDatabaseConnection() throws SQLException {
        System.out.println("=== 开始测试数据库连接 ===");
        
        // 测试获取连接
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "数据库连接不应为null");
            System.out.println("✓ 成功获取数据库连接");
            
            // 获取数据库元数据
            DatabaseMetaData metaData = connection.getMetaData();
            System.out.println("✓ 数据库产品名称: " + metaData.getDatabaseProductName());
            System.out.println("✓ 数据库版本: " + metaData.getDatabaseProductVersion());
            System.out.println("✓ JDBC驱动: " + metaData.getDriverName());
            System.out.println("✓ JDBC驱动版本: " + metaData.getDriverVersion());
            System.out.println("✓ 数据库URL: " + metaData.getURL());
            System.out.println("✓ 当前用户: " + metaData.getUserName());
            
            // 测试执行简单查询
            try (var statement = connection.createStatement();
                 var resultSet = statement.executeQuery("SELECT version()")) {
                if (resultSet.next()) {
                    System.out.println("✓ PostgreSQL版本信息: " + resultSet.getString(1));
                }
            }
            
            System.out.println("=== 数据库连接测试成功 ===");
        } catch (SQLException e) {
            System.err.println("✗ 数据库连接失败: " + e.getMessage());
            fail("数据库连接失败: " + e.getMessage());
        }
    }

    @Test
    void testDataSourceConfiguration() {
        System.out.println("=== 数据源配置信息 ===");
        assertNotNull(dataSource, "数据源不应为null");
        System.out.println("✓ 数据源类型: " + dataSource.getClass().getName());
        System.out.println("=== 数据源配置验证通过 ===");
    }

    @Test
    void testListDatabaseTables() throws SQLException {
        System.out.println("\n=== 数据库表列表 ===");
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // 获取所有表（包括视图）
            try (var resultSet = metaData.getTables(null, "public", "%", new String[]{"TABLE"})) {
                int tableCount = 0;
                System.out.println("\n找到的表：");
                System.out.println("-".repeat(80));
                System.out.printf("%-30s | %-20s | %s%n", "表名", "表类型", "备注");
                System.out.println("-".repeat(80));
                
                while (resultSet.next()) {
                    String tableName = resultSet.getString("TABLE_NAME");
                    String tableType = resultSet.getString("TABLE_TYPE");
                    String remarks = resultSet.getString("REMARKS");
                    
                    System.out.printf("%-30s | %-20s | %s%n", 
                        tableName, tableType, remarks != null ? remarks : "无备注");
                    tableCount++;
                }
                System.out.println("-".repeat(80));
                System.out.println("总计: " + tableCount + " 个表");
                
                if (tableCount == 0) {
                    System.out.println("\n⚠ 数据库中没有找到任何表！");
                    System.out.println("提示：你可能需要执行 SQL 脚本来创建表结构。");
                }
            }
            
            System.out.println("\n=== 表列表查询完成 ===");
        }
    }

    @Test
    void testTableStructure() throws SQLException {
        System.out.println("\n=== 数据库表结构详情 ===");
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // 先获取所有表
            var tables = new java.util.ArrayList<String>();
            try (var resultSet = metaData.getTables(null, "public", "%", new String[]{"TABLE"})) {
                while (resultSet.next()) {
                    tables.add(resultSet.getString("TABLE_NAME"));
                }
            }
            
            if (tables.isEmpty()) {
                System.out.println("⚠ 数据库中没有表，无法显示表结构");
                return;
            }
            
            // 遍历每个表，显示其列信息
            for (String tableName : tables) {
                System.out.println("\n" + "=".repeat(80));
                System.out.println("表名: " + tableName);
                System.out.println("=".repeat(80));
                
                try (var columns = metaData.getColumns(null, "public", tableName, "%")) {
                    System.out.printf("%-25s | %-15s | %-8s | %-8s | %s%n", 
                        "列名", "数据类型", "大小", "可空", "默认值");
                    System.out.println("-".repeat(80));
                    
                    while (columns.next()) {
                        String columnName = columns.getString("COLUMN_NAME");
                        String dataType = columns.getString("TYPE_NAME");
                        int columnSize = columns.getInt("COLUMN_SIZE");
                        String nullable = "YES".equals(columns.getString("IS_NULLABLE")) ? "YES" : "NO";
                        String defaultValue = columns.getString("COLUMN_DEF");
                        
                        System.out.printf("%-25s | %-15s | %-8d | %-8s | %s%n",
                            columnName, dataType, columnSize, nullable, 
                            defaultValue != null ? defaultValue : "null");
                    }
                }
                
                // 显示主键信息
                try (var primaryKeys = metaData.getPrimaryKeys(null, "public", tableName)) {
                    var pkList = new java.util.ArrayList<String>();
                    while (primaryKeys.next()) {
                        pkList.add(primaryKeys.getString("COLUMN_NAME"));
                    }
                    if (!pkList.isEmpty()) {
                        System.out.println("\n主键: " + String.join(", ", pkList));
                    }
                }
            }
            
            System.out.println("\n" + "=".repeat(80));
            System.out.println("=== 表结构查询完成 ===");
        }
    }
}