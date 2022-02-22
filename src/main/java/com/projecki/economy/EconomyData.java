package com.projecki.economy;

import com.projecki.economy.data.Data;
import com.projecki.economy.data.MySQLEngine;
import com.projecki.economy.database.generated.tables.records.BalancesRecord;
import com.projecki.economy.util.UUIDUtil;
import org.bukkit.entity.Player;
import org.jooq.DSLContext;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

import static com.projecki.economy.database.generated.tables.Balances.BALANCES;

/**
 * @since February 22, 2022
 * @author Andavin
 */
public final class EconomyData {

    //create table economy
    //(
    //    uuid    binary(16)       not null,
    //    name    varchar(16)      not null,
    //    balance bigint default 0 not null,
    //    constraint economy_pk
    //        primary key (uuid)
    //);

    private static final MySQLEngine MYSQL = Data.getEngine("economy");
    private final UUID uuid;
    private final String name;
    private long balance;

    private EconomyData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public long getBalance() {
        return balance;
    }

    public void set(long balance) {
        this.balance = balance;
    }

    public void add(long amount) {
        this.balance += amount;
    }

    public void subtract(long amount) {
        this.balance -= amount;
    }

    public void save() {
        MYSQL.update(BALANCES)
                .set(BALANCES.BALANCE, balance)
                .where(BALANCES.UUID.eq(UUIDUtil.toBytes(uuid)))
                .executeAsync();
    }

    public static CompletionStage<EconomyData> load(String name) {
        return MYSQL.selectFrom(BALANCES)
                .where(BALANCES.NAME.eq(name))
                .limit(1)
                .fetchAsync()
                .thenApply(result -> result.stream().findAny()
                        .map(r -> new EconomyData(UUIDUtil.toUuid(r.getUuid()), r.getName()))
                        .orElse(null));
    }

    public static CompletionStage<EconomyData> load(Player player) {
        String name = player.getName();
        UUID uuid = player.getUniqueId();
        byte[] id = UUIDUtil.toBytes(uuid);
        EconomyData data = new EconomyData(uuid, name);
        DSLContext context = MYSQL.create();
        return context.selectFrom(BALANCES)
                .where(BALANCES.UUID.eq(id))
                .limit(1)
                .fetchAsync()
                .thenApply(result -> {

                    if (result.isEmpty()) {
                        // No data exists - insert
                        context.insertInto(BALANCES)
                                .set(BALANCES.UUID, id)
                                .set(BALANCES.NAME, name)
                                .set(BALANCES.BALANCE, 0L)
                                .executeAsync();
                    } else {

                        BalancesRecord record = result.get(0);
                        data.set(record.getBalance());
                        if (!record.getName().equals(name)) {
                            // Name does not match so update
                            context.update(BALANCES)
                                    .set(BALANCES.NAME, name)
                                    .where(BALANCES.UUID.eq(id))
                                    .executeAsync();
                        }
                    }

                    return data;
                });
    }
}
