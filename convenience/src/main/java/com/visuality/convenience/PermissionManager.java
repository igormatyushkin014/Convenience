package com.visuality.convenience;

import android.app.Activity;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.Arrays;

public class PermissionManager {

    private static final int REQUEST_CODE = Integer.MAX_VALUE;

    private OnRequestListener onRequestListener;

    private boolean waitingForResponse;

    public boolean isWaitingForResponse() {
        return this.waitingForResponse;
    }

    private boolean enabled;

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    PermissionManager() {
        this.waitingForResponse = false;
        this.enabled = true;
    }

    public void check(
            String[] permissions,
            Activity activity,
            final OnCheckListener listener
    ) {
        if (!this.enabled) {
            return;
        }

        ArrayList<String> allowedPermissionsList = new ArrayList<>();
        ArrayList<String> blockedPermissionsList = new ArrayList<>();

        for (String permission : permissions) {
            int checkResult = activity.checkSelfPermission(permission);
            boolean permissionAllowed = checkResult == PackageManager.PERMISSION_GRANTED;

            if (permissionAllowed) {
                allowedPermissionsList.add(permission);
            } else {
                blockedPermissionsList.add(permission);
            }
        }

        String[] allowedPermissionsArray = allowedPermissionsList.toArray(
                new String[] {}
        );
        String[] blockedPermissionsArray = blockedPermissionsList.toArray(
                new String[] {}
        );
        CheckResult checkResult = new CheckResult(
                allowedPermissionsArray,
                blockedPermissionsArray
        );

        if (listener != null) {
            listener.onResult(
                    checkResult
            );
        }
    }

    public void request(
            String[] permissions,
            Activity activity,
            final OnRequestListener listener
    ) {
        if (!this.enabled) {
            return;
        }

        this.onRequestListener = listener;
        activity.requestPermissions(
                permissions,
                REQUEST_CODE
        );
        this.waitingForResponse = true;
    }

    public void requestIfNeeded(
            String[] permissions,
            final Activity activity,
            final OnRequestListener listener
    ) {
        if (!this.enabled) {
            return;
        }

        this.check(
                permissions,
                activity,
                new OnCheckListener() {
                    @Override
                    public void onResult(CheckResult checkResult) {
                        if (checkResult.getBlockedPermissions().length == 0) {
                            RequestResult requestResult = new RequestResult(
                                    checkResult.getAllowedPermissions(),
                                    checkResult.getBlockedPermissions()
                            );

                            if (listener != null) {
                                listener.onResult(
                                        requestResult
                                );
                            }
                        } else {
                            final ArrayList<String> allowedPermissionsList = new ArrayList<>(
                                    Arrays.asList(
                                            checkResult.getAllowedPermissions()
                                    )
                            );

                            PermissionManager.this.request(
                                    checkResult.getBlockedPermissions(),
                                    activity,
                                    new OnRequestListener() {
                                        @Override
                                        public void onResult(RequestResult result) {
                                            allowedPermissionsList.addAll(
                                                    Arrays.asList(
                                                            result.getAllowedPermissions()
                                                    )
                                            );

                                            String[] allowedPermissionsArray = allowedPermissionsList.toArray(
                                                    new String[]{}
                                            );
                                            String[] blockedPermissionsArray = result.getBlockedPermissions();

                                            RequestResult requestResult = new RequestResult(
                                                    result.getAllowedPermissions(),
                                                    result.getBlockedPermissions()
                                            );

                                            if (listener != null) {
                                                listener.onResult(
                                                        requestResult
                                                );
                                            }
                                        }
                                    }
                            );
                        }
                    }
                }
        );
    }

    public boolean onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] results
    ) {
        if (requestCode != REQUEST_CODE) {
            return false;
        }

        this.waitingForResponse = false;

        if (this.onRequestListener == null) {
            return false;
        }

        ArrayList<String> allowedPermissionsList = new ArrayList<>();
        ArrayList<String> blockedPermissionsList = new ArrayList<>();

        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            int result = results[i];
            boolean permissionAllowed = result == PackageManager.PERMISSION_GRANTED;

            if (permissionAllowed) {
                allowedPermissionsList.add(permission);
            } else {
                blockedPermissionsList.add(permission);
            }
        }

        String[] allowedPermissionsArray = allowedPermissionsList.toArray(
                new String[] {}
        );
        String[] blockedPermissionsArray = blockedPermissionsList.toArray(
                new String[] {}
        );
        RequestResult requestResult = new RequestResult(
                allowedPermissionsArray,
                blockedPermissionsArray
        );

        this.onRequestListener.onResult(
                requestResult
        );
        this.onRequestListener = null;

        return true;
    }

    public static interface OnCheckListener {
        void onResult(
                CheckResult checkResult
        );
    }

    public static interface OnRequestListener {
        void onResult(
                RequestResult requestResult
        );
    }
}
