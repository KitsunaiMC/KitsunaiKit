package cc.kitsunai.kit;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class KitDataBase {
    private final HikariDataSource dataSource;

    public KitDataBase() {
        // 配置数据库连接
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + new File(KitsunaiKit.getInstance().getDataFolder(), "gifts.db").getAbsolutePath());
        config.setMaximumPoolSize(5); // 连接池大小
        config.setConnectionTestQuery("SELECT 1");

        this.dataSource = new HikariDataSource(config);
        createMainTable(); // 创建主表
    }

    /**
     * 创建主表（记录所有礼包表信息）
     */
    private void createMainTable() {
        executeUpdate("CREATE TABLE IF NOT EXISTS gift_tables ("
                + "table_name TEXT PRIMARY KEY, "
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
    }

    /**
     * 为礼包创建专用表
     * @param giftId 礼包唯一ID
     */
    public void createGiftTable(String giftId) {
        validateTableName(giftId); // 安全验证

        // 创建礼包表
        String sql = String.format(
                "CREATE TABLE IF NOT EXISTS %s ("
                        + "uuid TEXT PRIMARY KEY, "
                        + "redeem_count INTEGER NOT NULL DEFAULT 0, "
                        + "last_redeem TIMESTAMP, "
                        + "FOREIGN KEY(uuid) REFERENCES players(uuid))",
                giftId
        );

        executeUpdate(sql);

        // 在主表中注册
        executeUpdate("INSERT OR IGNORE INTO gift_tables (table_name) VALUES (?)", giftId);
    }

    /**
     * 删除礼包表
     * @param giftId 礼包唯一ID
     */
    public void dropGiftTable(String giftId) {
        validateTableName(giftId);
        executeUpdate(String.format("DROP TABLE IF EXISTS %s", giftId));

        // 从主表中删除
        executeUpdate("DELETE FROM gift_tables WHERE table_name = ?", giftId);
    }

    /**
     * 记录玩家领取礼包
     * @param giftId 礼包ID
     * @param playerUUID 玩家UUID
     */
    public void recordRedemption(String giftId, UUID playerUUID) {
        validateTableName(giftId);

        // 原子操作：更新领取次数和时间
        String sql = String.format(
                "INSERT INTO %s (uuid, redeem_count, last_redeem) " +
                        "VALUES (?, 1, CURRENT_TIMESTAMP) " +
                        "ON CONFLICT(uuid) DO UPDATE SET " +
                        "redeem_count = redeem_count + 1, " +
                        "last_redeem = CURRENT_TIMESTAMP",
                giftId
        );

        executeUpdate(sql, playerUUID.toString());
    }

    /**
     * 获取玩家礼包领取次数
     * @param giftId 礼包ID
     * @param playerUUID 玩家UUID
     * @return 领取次数
     */
    public int getRedemptionCount(String giftId, UUID playerUUID) {
        validateTableName(giftId);

        String sql = String.format(
                "SELECT redeem_count FROM %s WHERE uuid = ?",
                giftId
        );

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerUUID.toString());
            ResultSet rs = stmt.executeQuery();

            return rs.next() ? rs.getInt("redeem_count") : 0;
        } catch (SQLException e) {
            handleSQLException(e);
            return 0;
        }
    }

    /**
     * 获取所有礼包表信息
     * @return 礼包表列表
     */
    public List<String> getAllGiftTables() {
        List<String> tables = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT table_name FROM gift_tables")) {

            while (rs.next()) {
                tables.add(rs.getString("table_name"));
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return tables;
    }

    /**
     * 异步执行SQL更新
     */
    private void executeUpdate(String sql, Object... params) {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }

                stmt.executeUpdate();
            } catch (SQLException e) {
                handleSQLException(e);
            }
        });
    }

    /**
     * 验证表名安全性
     */
    private void validateTableName(String tableName) {
        if (!tableName.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("Invalid table name: " + tableName);
        }
    }

    /**
     * 统一处理SQL异常
     */
    private void handleSQLException(SQLException e) {
        // 实际项目中应使用日志框架
        KitsunaiKit.getInstance().getLogger().severe("SQL Error: " + e);
    }

    /**
     * 关闭数据库连接
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

}
