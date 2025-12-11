package com.dowson.codegen.util;

import com.dowson.codegen.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DbPreflightChecker {
    public static void assertReachable(String url, String username, String password) {
        try (Connection ignored = DriverManager.getConnection(url, username, password)) {
        } catch (SQLException e) {
            throw new BusinessException(400, "数据库连接失败: " + e.getMessage());
        }
    }

    public static void assertTablesExist(String url, String username, String password, String tablesCsv) {
        List<String> missing = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            for (String raw : tablesCsv.split(",")) {
                String table = raw.trim();
                if (table.isEmpty()) continue;
                String sql = "SELECT 1 FROM `" + table + "` LIMIT 1";
                try (Statement st = conn.createStatement()) {
                    st.executeQuery(sql);
                } catch (SQLException ex) {
                    missing.add(table);
                }
            }
        } catch (SQLException e) {
            throw new BusinessException(400, "数据库连接失败: " + e.getMessage());
        }
        if (!missing.isEmpty()) {
            throw new BusinessException(400, "以下表不存在: " + String.join(",", missing));
        }
    }
}

