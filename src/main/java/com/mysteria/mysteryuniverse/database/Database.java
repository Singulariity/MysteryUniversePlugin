package com.mysteria.mysteryuniverse.database;

import com.mysteria.mysteryuniverse.MysteryUniversePlugin;
import com.mysteria.mysteryuniverse.database.enums.Column;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

@SuppressWarnings("unused")
public class Database {

	public Database() {
		if (MysteryUniversePlugin.getDatabase() != null) {
			throw new IllegalStateException();
		}
	}

	public void createData(@Nonnull UUID uuid) {
		try {
			PreparedStatement prst = MysteryUniversePlugin.getConnection()
					.prepareStatement("INSERT INTO player_data (Player) VALUES (?)");
			prst.setString(1, uuid.toString());
			prst.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean hasData(@Nonnull UUID uuid) {
		try {
			Statement st = MysteryUniversePlugin.getConnection().createStatement();
			String query = "SELECT 1 FROM player_data WHERE Player = '"+ uuid +"'";
			ResultSet resultSet = st.executeQuery(query);

			if (resultSet.next()) return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}


	public void setObject(@Nonnull UUID uuid, @Nonnull Column column, Object newValue) {
		try {
			PreparedStatement prst = MysteryUniversePlugin.getConnection()
					.prepareStatement("UPDATE player_data SET (" + column + ") = (?) WHERE Player = ?");
			prst.setObject(1, newValue);
			prst.setString(2, uuid.toString());
			prst.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Nullable
	public Object getObject(@Nonnull UUID uuid, @Nonnull Column column) {
		try {
			Statement st = MysteryUniversePlugin.getConnection().createStatement();
			String query = "SELECT " + column + " FROM player_data WHERE Player = '"+ uuid +"'";
			ResultSet resultSet = st.executeQuery(query);

			if (resultSet.next()) return resultSet.getObject(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setInt(@Nonnull UUID uuid, @Nonnull Column column, int newValue) {
		try {
			PreparedStatement prst = MysteryUniversePlugin
					.getConnection()
					.prepareStatement("UPDATE player_data SET (" + column + ") = (?) WHERE Player = ?");
			prst.setInt(1, newValue);
			prst.setString(2, uuid.toString());
			prst.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Nullable
	public Integer getInt(@Nonnull UUID uuid, @Nonnull Column column) {
		try {
			Statement st = MysteryUniversePlugin.getConnection().createStatement();
			String query = "SELECT " + column + " FROM player_data WHERE Player = '"+ uuid +"'";
			ResultSet resultSet = st.executeQuery(query);

			if (resultSet.next()) return resultSet.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setBoolean(@Nonnull UUID uuid, @Nonnull Column column, boolean newValue) {
		int flag = (newValue) ? 1 : 0;
		setInt(uuid, column, flag);
	}

	@Nullable
	public Boolean getBoolean(@Nonnull UUID uuid, @Nonnull Column column) {
		Integer flag = getInt(uuid, column);
		if (flag != null) {
			return (flag) == 1;
		}
		return null;
	}

	@Nullable
	public Long getLong(@Nonnull UUID uuid, @Nonnull Column column) {
		try {
			Statement st = MysteryUniversePlugin.getConnection().createStatement();
			String query = "SELECT " + column + " FROM player_data WHERE Player = '"+ uuid +"'";
			ResultSet resultSet = st.executeQuery(query);

			if (resultSet.next()) return resultSet.getLong(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setLong(@Nonnull UUID uuid, @Nonnull Column column, long newValue) {
		try {
			PreparedStatement prst = MysteryUniversePlugin
					.getConnection()
					.prepareStatement("UPDATE player_data SET (" + column + ") = (?) WHERE Player = ?");
			prst.setLong(1, newValue);
			prst.setString(2, uuid.toString());
			prst.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


}
