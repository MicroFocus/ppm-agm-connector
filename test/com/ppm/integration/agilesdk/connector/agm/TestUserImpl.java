package com.ppm.integration.agilesdk.connector.agm;

import com.mercury.itg.core.model.Region;
import com.mercury.itg.core.model.UserData;
import com.mercury.itg.core.user.model.User;
import com.mercury.itg.rm.rsc.model.Identity;
import com.mercury.itg.rm.rsc.model.OrgUnit;
import org.hibernate.Session;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by libingc on 2/25/2016.
 */
public class TestUserImpl implements User {
    @Override
    public Long getUserId() {
        return null;
    }

    @Override
    public Identity getIdentity() {
        return null;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public String getFullName() {
        return null;
    }

    @Override
    public Region getRegion() {
        return null;
    }

    @Override
    public void setRegion(Region var1) {

    }

    @Override
    public Set<User> getUsers() {
        return null;
    }

    @Override
    public Boolean isEnabled() {
        return null;
    }

    @Override
    public Long getId() {
        return null;
    }

    @Override
    public List getSecurityGroups() {
        return null;
    }

    @Override
    public List getSecurityGroups(Session var1) {
        return null;
    }

    @Override
    public Date getEndDate() {
        return null;
    }

    @Override
    public Date getStartDate() {
        return null;
    }

    @Override
    public void setStartDate(Date var1) {

    }

    @Override
    public void setEndDate(Date var1) {

    }

    @Override
    public Set getOrgUnits() {
        return null;
    }

    @Override
    public OrgUnit getPrimaryOrgUnit() {
        return null;
    }

    @Override
    public String getEmailAddress() {
        return null;
    }

    @Override
    public void setEmailAddress(String var1) {

    }

    @Override
    public String getLaborCategory() {
        return null;
    }

    @Override
    public void setLaborCategory(String var1) {

    }

    @Override
    public UserData getUserData() {
        return null;
    }

    @Override
    public void setUserData(UserData userData) {

    }

    @Override
    public Integer getEntityID() {
        return null;
    }

    @Override
    public Integer getEntityPrimaryKey() {
        return null;
    }
}
