package imu_math;

import java.util.*;

public class IMUConverter {
    public static double[] eulerToQuaternion(double roll, double pitch, double yaw) {
        double cosRoll = Math.cos(roll / 2);
        double sinRoll = Math.sin(roll / 2);
        double cosPitch = Math.cos(pitch / 2);
        double sinPitch = Math.sin(pitch / 2);
        double cosYaw = Math.cos(yaw / 2);
        double sinYaw = Math.sin(yaw / 2);

        double qx = sinRoll * cosPitch * cosYaw - cosRoll * sinPitch * sinYaw;
        double qy = cosRoll * sinPitch * cosYaw + sinRoll * cosPitch * sinYaw;
        double qz = cosRoll * cosPitch * sinYaw - sinRoll * sinPitch * cosYaw;
        double qw = cosRoll * cosPitch * cosYaw + sinRoll * sinPitch * sinYaw;

        return new double[] { qx, qy, qz, qw };
    }
}