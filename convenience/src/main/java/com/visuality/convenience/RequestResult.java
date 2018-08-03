package com.visuality.convenience;

public class RequestResult {

    private String[] allowedPermissions;

    public String[] getAllowedPermissions() {
        return this.allowedPermissions;
    }

    private String[] blockedPermissions;

    public String[] getBlockedPermissions() {
        return this.blockedPermissions;
    }

    public RequestResult(
            String[] allowedPermissions,
            String[] blockedPermissions
    ) {
        this.allowedPermissions = allowedPermissions;
        this.blockedPermissions = blockedPermissions;
    }
}