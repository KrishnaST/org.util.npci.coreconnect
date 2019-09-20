package org.util.npci.coreconnect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.util.datautil.Strings;
import org.util.iso8583.ISO8583Message;
import org.util.iso8583.ISO8583PropertyName;
import org.util.nanolog.Logger;

//@formatter:off
public abstract class CoreDatabaseService {

	protected final CoreConfig config;
	protected final String     txTableName;
	protected final boolean    encrypted;
	protected final boolean    isdisabled;

	public CoreDatabaseService(final CoreConfig config) {
		this.config      = config;
		this.txTableName = config.txTableName;
		this.encrypted   = config.getBooleanSupressException(CorePropertyName.PAN_ENCRYPTED);
		this.isdisabled  = config.getBooleanSupressException(CorePropertyName.IS_CORE_DATABASE_SERVICE_DISABLED);
	}

	public final long registerTransaction(final ISO8583Message message, final String type, final Logger logger) {
		if (isdisabled || message == null || Strings.isNullOrEmpty(txTableName)) return 0;
		try(final Connection con = config.dataSource.getConnection();
				final PreparedStatement ps = con.prepareStatement(
						"INSERT INTO " + txTableName + " (" + "MKEY, TKEY, F000, F002, E002, F003, F004, F007, F011, F012, F013, F014, F015, F016, F018, F019, "
								+ "F022, F023, F025, F032, F033, F037, F038, F039, F040, F041, F042, F043, F044, F048, F049, "
								+ "F054, F060, F061, F070, F090, F095, F102, F103, F104, F105, F120, FXTIME, RXTIME) "
								+ "VALUES (CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', ?), 2) ,CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', ?), 2) , "
								+ "?, ?, ENCRYPTBYKEY(key_guid('sk_card'), ?, 1, ?), ?, "
								+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
								+ "?, ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())",
						Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, message.getUniqueKey());
			ps.setString(2, message.getTransactionKey());
			ps.setString(3, message.get(0));
			final String pan = message.get(2);
			if(pan == null) {
				ps.setString(4, null);
				ps.setBytes(5, null);
				ps.setBytes(6, null);
			}
			else if(encrypted) {
				ps.setString(4, mask(pan));
				ps.setBytes(5, pan.getBytes());
				ps.setBytes(6, getLast4Chars(pan).getBytes());
			}
			else {
				ps.setString(4, pan);
				ps.setBytes(5, pan.getBytes());
				ps.setBytes(6, getLast4Chars(pan).getBytes());
			}
			ps.setString(7, message.get(3));
			ps.setString(8, message.get(4));
			ps.setString(9, message.get(7));
			ps.setString(10, message.get(11));
			ps.setString(11, message.get(12));
			ps.setString(12, message.get(13));
			ps.setString(13, message.get(14));
			ps.setString(14, message.get(15));
			ps.setString(15, message.get(16));
			ps.setString(16, message.get(18));
			ps.setString(17, message.get(19));
			ps.setString(18, message.get(22));
			ps.setString(19, message.get(23));
			ps.setString(20, message.get(25));
			ps.setString(21, message.get(32));
			ps.setString(22, message.get(33));
			ps.setString(23, message.get(37));
			ps.setString(24, message.get(38));
			ps.setString(25, message.get(39));
			ps.setString(26, message.get(40));
			ps.setString(27, message.get(41));
			ps.setString(28, message.get(42));
			ps.setString(29, message.get(43));
			ps.setString(30, message.get(44));
			ps.setString(31, message.get(48));
			ps.setString(32, message.get(49));
			ps.setString(33, message.get(54));
			ps.setString(34, message.get(60));
			ps.setString(35, message.get(61));
			ps.setString(36, message.get(70));
			ps.setString(37, message.get(90));
			ps.setString(38, message.get(95));
			ps.setString(39, message.get(102));
			ps.setString(40, message.get(103));
			ps.setString(41, message.get(104));
			ps.setString(42, message.get(105));
			ps.setString(43, message.get(120));
			if (ps.executeUpdate() > 0) {
				try(ResultSet rs = ps.getGeneratedKeys()) {
					if (rs.next()) {
						final long id = rs.getLong(1);
						message.putAdditional(ISO8583PropertyName.TRANSACTION_ID, id);
						return id;
					}
				}
			}
		} catch (final Exception e) {logger.error(e);}
		return 0;
	}

	public final ISO8583Message getTransactionById(final long id, final Logger logger) {
		if (isdisabled || id == 0 || Strings.isNullOrEmpty(txTableName)) return null;
		try(final Connection con = config.dataSource.getConnection();
				final PreparedStatement ps = con
						.prepareStatement("SELECT CONVERT(VARCHAR, DECRYPTBYKEY(E002, 1, SUBSTRING(F002, (DATALENGTH(F002)-4), 4))) AS PAN, * FROM "
								+ txTableName + " WHERE TXID = " + id);
				final ResultSet rs = ps.executeQuery()) {
			if (rs.next()) {
				final ISO8583Message transaction = new ISO8583Message();
				transaction.put(0, rs.getString("F000"));
				transaction.put(2, encrypted ? rs.getString("PAN") : rs.getString("F002"));
				transaction.put(3, rs.getString("F003"));
				transaction.put(4, rs.getString("F004"));
				transaction.put(7, rs.getString("F007"));
				transaction.put(11, rs.getString("F011"));
				transaction.put(12, rs.getString("F012"));
				transaction.put(13, rs.getString("F013"));
				transaction.put(14, rs.getString("F014"));
				transaction.put(15, rs.getString("F015"));
				transaction.put(16, rs.getString("F016"));
				transaction.put(18, rs.getString("F018"));
				transaction.put(19, rs.getString("F019"));
				transaction.put(22, rs.getString("F022"));
				transaction.put(23, rs.getString("F023"));
				transaction.put(25, rs.getString("F025"));
				transaction.put(32, rs.getString("F032"));
				transaction.put(33, rs.getString("F033"));
				transaction.put(37, rs.getString("F037"));
				transaction.put(38, rs.getString("F038"));
				transaction.put(39, rs.getString("F039"));
				transaction.put(40, rs.getString("F040"));
				transaction.put(41, rs.getString("F041"));
				transaction.put(42, rs.getString("F042"));
				transaction.put(43, rs.getString("F043"));
				transaction.put(44, rs.getString("F044"));
				transaction.put(48, rs.getString("F048"));
				transaction.put(49, rs.getString("F049"));
				transaction.put(54, rs.getString("F054"));
				transaction.put(60, rs.getString("F060"));
				transaction.put(61, rs.getString("F061"));
				transaction.put(70, rs.getString("F070"));
				transaction.put(90, rs.getString("F090"));
				transaction.put(95, rs.getString("F095"));
				transaction.put(102, rs.getString("F102"));
				transaction.put(103, rs.getString("F103"));
				transaction.put(104, rs.getString("F104"));
				transaction.put(120, rs.getString("F120"));
				return transaction;
			}
		} catch (final Exception e) {
			logger.error(e);
		}
		return null;
	}

	public final ISO8583Message getTransactionByMKey(final String mkey, final Logger logger) {
		if (isdisabled || mkey == null || Strings.isNullOrEmpty(txTableName)) return null;
		try(final Connection con = config.dataSource.getConnection();
			final PreparedStatement ps = con
						.prepareStatement("SELECT CONVERT(VARCHAR, DECRYPTBYKEY(E002, 1, SUBSTRING(F002, (DATALENGTH(F002)-4), 4))) AS PAN, * FROM "
								+ txTableName + " WHERE MKEY = CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', '" + mkey + "'), 2)");
			final ResultSet rs = ps.executeQuery()) {
			if (rs.next()) {
				final ISO8583Message transaction = new ISO8583Message();
				transaction.put(0, rs.getString("F000"));
				transaction.put(2, encrypted ? rs.getString("PAN") : rs.getString("F002"));
				transaction.put(3, rs.getString("F003"));
				transaction.put(4, rs.getString("F004"));
				transaction.put(7, rs.getString("F007"));
				transaction.put(11, rs.getString("F011"));
				transaction.put(12, rs.getString("F012"));
				transaction.put(13, rs.getString("F013"));
				transaction.put(14, rs.getString("F014"));
				transaction.put(15, rs.getString("F015"));
				transaction.put(16, rs.getString("F016"));
				transaction.put(18, rs.getString("F018"));
				transaction.put(19, rs.getString("F019"));
				transaction.put(22, rs.getString("F022"));
				transaction.put(23, rs.getString("F023"));
				transaction.put(25, rs.getString("F025"));
				transaction.put(32, rs.getString("F032"));
				transaction.put(33, rs.getString("F033"));
				transaction.put(37, rs.getString("F037"));
				transaction.put(38, rs.getString("F038"));
				transaction.put(39, rs.getString("F039"));
				transaction.put(40, rs.getString("F040"));
				transaction.put(41, rs.getString("F041"));
				transaction.put(42, rs.getString("F042"));
				transaction.put(43, rs.getString("F043"));
				transaction.put(44, rs.getString("F044"));
				transaction.put(48, rs.getString("F048"));
				transaction.put(49, rs.getString("F049"));
				transaction.put(54, rs.getString("F054"));
				transaction.put(60, rs.getString("F060"));
				transaction.put(61, rs.getString("F061"));
				transaction.put(70, rs.getString("F070"));
				transaction.put(90, rs.getString("F090"));
				transaction.put(95, rs.getString("F095"));
				transaction.put(102, rs.getString("F102"));
				transaction.put(103, rs.getString("F103"));
				transaction.put(104, rs.getString("F104"));
				transaction.put(120, rs.getString("F120"));
				return transaction;
			}
		} catch (final Exception e) {
			logger.error(e);
		}
		return null;
	}
	
	public final List<ISO8583Message> getTransactionsByTKey(final String tkey, final Logger logger) {
		if (isdisabled || tkey == null || Strings.isNullOrEmpty(txTableName)) return null;
		final String query = "SELECT CONVERT(VARCHAR, DECRYPTBYKEY(E002, 1, SUBSTRING(F002, (DATALENGTH(F002)-4), 4))) AS PAN, * FROM "
				+ txTableName + " WHERE TKEY = CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', '" + tkey + "'), 2)";
		final List<ISO8583Message> transactions = new ArrayList<ISO8583Message>();
		try(final Connection con = config.dataSource.getConnection();
			final PreparedStatement ps = con.prepareStatement(query);
			final ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				final ISO8583Message transaction = new ISO8583Message();
				transaction.put(0, rs.getString("F000"));
				transaction.put(2, encrypted ? rs.getString("PAN") : rs.getString("F002"));
				transaction.put(3, rs.getString("F003"));
				transaction.put(4, rs.getString("F004"));
				transaction.put(7, rs.getString("F007"));
				transaction.put(11, rs.getString("F011"));
				transaction.put(12, rs.getString("F012"));
				transaction.put(13, rs.getString("F013"));
				transaction.put(14, rs.getString("F014"));
				transaction.put(15, rs.getString("F015"));
				transaction.put(16, rs.getString("F016"));
				transaction.put(18, rs.getString("F018"));
				transaction.put(19, rs.getString("F019"));
				transaction.put(22, rs.getString("F022"));
				transaction.put(23, rs.getString("F023"));
				transaction.put(25, rs.getString("F025"));
				transaction.put(32, rs.getString("F032"));
				transaction.put(33, rs.getString("F033"));
				transaction.put(37, rs.getString("F037"));
				transaction.put(38, rs.getString("F038"));
				transaction.put(39, rs.getString("F039"));
				transaction.put(40, rs.getString("F040"));
				transaction.put(41, rs.getString("F041"));
				transaction.put(42, rs.getString("F042"));
				transaction.put(43, rs.getString("F043"));
				transaction.put(44, rs.getString("F044"));
				transaction.put(48, rs.getString("F048"));
				transaction.put(49, rs.getString("F049"));
				transaction.put(54, rs.getString("F054"));
				transaction.put(60, rs.getString("F060"));
				transaction.put(61, rs.getString("F061"));
				transaction.put(70, rs.getString("F070"));
				transaction.put(90, rs.getString("F090"));
				transaction.put(95, rs.getString("F095"));
				transaction.put(102, rs.getString("F102"));
				transaction.put(103, rs.getString("F103"));
				transaction.put(104, rs.getString("F104"));
				transaction.put(120, rs.getString("F120"));
				transactions.add(transaction);
			}
		} catch (final Exception e) {logger.error(e);}
		return transactions;
	}
	
	public final ISO8583Message getTransactionByTKey(final String mti, final String tkey, final Logger logger) {
		if (isdisabled || tkey == null || Strings.isNullOrEmpty(txTableName)) return null;
		final String query = "SELECT CONVERT(VARCHAR, DECRYPTBYKEY(E002, 1, SUBSTRING(F002, (DATALENGTH(F002)-4), 4))) AS PAN, * FROM "
				+ txTableName + " WHERE F000 = "+mti+" AND TKEY = CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', '" + tkey + "'), 2)";
		try(final Connection con = config.dataSource.getConnection();
			final PreparedStatement ps = con.prepareStatement(query);
			final ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				final ISO8583Message transaction = new ISO8583Message();
				transaction.put(0, rs.getString("F000"));
				transaction.put(2, encrypted ? rs.getString("PAN") : rs.getString("F002"));
				transaction.put(3, rs.getString("F003"));
				transaction.put(4, rs.getString("F004"));
				transaction.put(7, rs.getString("F007"));
				transaction.put(11, rs.getString("F011"));
				transaction.put(12, rs.getString("F012"));
				transaction.put(13, rs.getString("F013"));
				transaction.put(14, rs.getString("F014"));
				transaction.put(15, rs.getString("F015"));
				transaction.put(16, rs.getString("F016"));
				transaction.put(18, rs.getString("F018"));
				transaction.put(19, rs.getString("F019"));
				transaction.put(22, rs.getString("F022"));
				transaction.put(23, rs.getString("F023"));
				transaction.put(25, rs.getString("F025"));
				transaction.put(32, rs.getString("F032"));
				transaction.put(33, rs.getString("F033"));
				transaction.put(37, rs.getString("F037"));
				transaction.put(38, rs.getString("F038"));
				transaction.put(39, rs.getString("F039"));
				transaction.put(40, rs.getString("F040"));
				transaction.put(41, rs.getString("F041"));
				transaction.put(42, rs.getString("F042"));
				transaction.put(43, rs.getString("F043"));
				transaction.put(44, rs.getString("F044"));
				transaction.put(48, rs.getString("F048"));
				transaction.put(49, rs.getString("F049"));
				transaction.put(54, rs.getString("F054"));
				transaction.put(60, rs.getString("F060"));
				transaction.put(61, rs.getString("F061"));
				transaction.put(70, rs.getString("F070"));
				transaction.put(90, rs.getString("F090"));
				transaction.put(95, rs.getString("F095"));
				transaction.put(102, rs.getString("F102"));
				transaction.put(103, rs.getString("F103"));
				transaction.put(104, rs.getString("F104"));
				transaction.put(120, rs.getString("F120"));
				return transaction;
			}
		} catch (final Exception e) {logger.error(e);}
		return null;
	}

	public final boolean registerResponse(final long id, final ISO8583Message response, final Logger logger) {
		if (isdisabled || id == 0 || Strings.isNullOrEmpty(txTableName)) return false;
		try(final Connection con = config.dataSource.getConnection();
			final PreparedStatement ps = con.prepareStatement("UPDATE " + txTableName + " SET R039 = ?, R038 = ?, RXTIME = GETDATE() WHERE TXID = ?")) {
			ps.setString(1, response.get(39));
			ps.setString(2, response.get(38));
			ps.setLong(3, id);
			return ps.executeUpdate() > 0;
		} catch (final Exception e) {logger.error(e);}
		return false;
	}

	public final boolean isDuplicateTransaction(final String key, final Logger logger) {
		if (isdisabled || key == null || Strings.isNullOrEmpty(txTableName)) return false;
		final String query = "SELECT CASE WHEN EXISTS (SELECT TXID FROM " + txTableName + " WITH (NOLOCK) WHERE MKEY = CONVERT(VARCHAR(64), HASHBYTES('SHA2_256', '"
						+ key + "'), 2)) THEN CAST (1 AS BIT)ELSE CAST (0 AS BIT) END AS EXIST";
		try(final Connection con = config.dataSource.getConnection();
			final PreparedStatement ps = con.prepareStatement(query);
				final ResultSet rs = ps.executeQuery()) {
			if (rs.next()) return rs.getBoolean("EXIST");
		} catch (final Exception e) {logger.error(e);}
		return false;
	}

	protected static final String getLast4Chars(final String string) {
		if (string == null || string.length() < 4) return "0000";
		else return string.substring(string.length() - 4);
	}

	protected static final String mask(final String data) {
		if (data == null || data.length() < 11) return data;
		return data.substring(0, 6) + String.format("%0" + (data.length() - 10) + "d", 0).replaceAll("0", "X") + data.substring(data.length() - 4);
	}
}
