package org.olesya.musicaldevapp.data.repository;

import lombok.NonNull;
import org.olesya.musicaldevapp.data.entity.RequirementType;
import org.olesya.musicaldevapp.utils.BaseTable;
import org.olesya.musicaldevapp.utils.CommonException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RequirementTypeRepository extends BaseTable {
    public RequirementTypeRepository() throws SQLException {
    }

    public UUID saveRequirementType(@NonNull RequirementType requirementType) throws CommonException {
        if (requirementType.getRequirementTypeName() == null || requirementType.getRequirementTypeName().isEmpty())
            throw new CommonException("При сохранении типа требования все поля должны быть заполнены");
        try {
            PreparedStatement ps = super.prepareStatement("INSERT INTO requirement_types VALUES (?,?);");
            UUID requirementTypeId = UUID.randomUUID();
            ps.setObject(1, requirementTypeId);
            ps.setString(2, requirementType.getRequirementTypeName());
            super.executeSqlStatementUpdate(ps);
            return requirementTypeId;
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public void updateRequirementType(@NonNull RequirementType requirementType) throws CommonException {
        if (requirementType.getRequirementTypeId() == null || requirementType.getRequirementTypeName() == null || requirementType.getRequirementTypeName().isEmpty())
            throw new CommonException("При обновлении типа требования все поля должны быть заполнены");
        try {
            PreparedStatement ps = super.prepareStatement("UPDATE requirement_types SET requirement_type_name=? WHERE requirement_type_id=?;");
            ps.setString(1, requirementType.getRequirementTypeName());
            ps.setObject(2, requirementType.getRequirementTypeId());
            super.executeSqlStatementUpdate(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public void deleteRequirementType(@NonNull UUID requirementTypeId) throws CommonException {
        try {
            PreparedStatement ps = super.prepareStatement(
                    "DELETE FROM requirement_types WHERE requirement_type_id=?;"
            );
            ps.setObject(1, requirementTypeId);
            super.executeSqlStatementUpdate(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public RequirementType getRequirementTypeById(@NonNull UUID requirementTypeId) throws CommonException {
        try {
            PreparedStatement ps = super.prepareStatement(
                    "SELECT rt.requirement_type_id,rt.requirement_type_name FROM requirement_types rt WHERE rt.requirement_type_id=?;"
            );
            ps.setObject(1, requirementTypeId);
            return getRequirementType(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public List<RequirementType> getAllRequirementTypes() throws CommonException {
        try {
            PreparedStatement ps = super.prepareStatement(
                    "SELECT rt.requirement_type_id,rt.requirement_type_name FROM requirement_types rt;"
            );
            return getRequirementTypeList(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public RequirementType getRequirementTypeByName(@NonNull String requirementTypeName) throws CommonException {
        try {
            PreparedStatement ps = super.prepareStatement(
                    "SELECT rt.requirement_type_id,rt.requirement_type_name FROM requirement_types rt WHERE rt.requirement_type_name=?;"
            );
            ps.setString(1, requirementTypeName);
            return getRequirementType(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public RequirementType getRequirementType(@NonNull PreparedStatement ps) throws CommonException {
        try {
            ResultSet rs = super.executeSqlStatementRead(ps);
            RequirementType requirementType = null;
            if (rs.next()) {
                requirementType = new RequirementType(
                        rs.getObject(1, UUID.class),
                        rs.getString(2)
                );
            }

            rs.close();
            ps.close();
            super.close();
            return requirementType;
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public List<RequirementType> getRequirementTypeList(@NonNull PreparedStatement ps) throws CommonException {
        try {
            ResultSet rs = super.executeSqlStatementRead(ps);
            List<RequirementType> requirementTypeList = new ArrayList<>();
            while (rs.next()) {
                requirementTypeList.add(new RequirementType(
                        rs.getObject(1, UUID.class),
                        rs.getString(2)
                ));
            }

            rs.close();
            ps.close();
            super.close();
            return requirementTypeList;
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }
}
