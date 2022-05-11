package com.udnahc.locationmanager;



public class GpsMessage {

    public static class MileageUpdate extends GpsMessage {
        private Mileage mileageImpl;

        public MileageUpdate(Mileage mileageImpl) {
            this.mileageImpl = mileageImpl;
        }

        public Mileage getMileage() {
            return mileageImpl;
        }
    }

    public static class StopGpsUpdates extends GpsMessage {
        private String reason;

        public StopGpsUpdates(String reason) {
            this.reason = reason;
        }

        String getReason() {
            return reason;
        }
    }

    public static class StopBackgroundGpsUpdates extends GpsMessage {
        private String reason;

        public StopBackgroundGpsUpdates(String reason) {
            this.reason = reason;
        }

        public String getReason() {
            return reason;
        }
    }

    public static class RestartBackgroundGpsUpdates extends GpsMessage {
        private String reason;

        public RestartBackgroundGpsUpdates(String reason) {
            this.reason = reason;
        }

        public String getReason() {
            return reason;
        }
    }

    public static class ShowGooglePlayServicesUtilError extends GpsMessage {
        private int status;

        public ShowGooglePlayServicesUtilError(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }
    }
}
