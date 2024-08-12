package aircraftsimulator;

import aircraftsimulator.GameObject.Aircraft.Radar.Wave.ElectroMagneticWave;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.*;
import java.util.List;

public class GameMath {
    public static double directionToAngle(Vector2f direction)
    {
        double angle = - Math.toDegrees(Math.atan(direction.y / direction.x));

        if(direction.x >= 0)
        {
            return (360 + angle) % 360;
        }
        else
        {
            return (180 + angle) % 360;
        }
    }

    public static double sphereAreaForAngle(float radius, double angleInRad)
    {
        return 2 * Math.PI * (1 - Math.cos(angleInRad)) * radius * radius;
    }

    public static double getCosAngleToHorizontal(Vector3f direction)
    {
        Vector3f horizontalVector = new Vector3f(direction.x, direction.y, 0);
        return direction.dot(horizontalVector) / direction.length() / horizontalVector.length();
    }

    public static boolean isWithinAngle(Vector3f direction1, Vector3f direction2, float direction2Length, float angle)
    {
        float angleCos = direction1.dot(direction2) / direction1.length() / direction2Length;
        return angleCos > Math.cos(Math.toRadians(angle));
    }

    public static Vector3f rotatedDirection(double radian, Vector3f direction, Vector3f destinationVector)
    {
        Vector3f n = new Vector3f();
        n.cross(direction, destinationVector);
        if(n.lengthSquared() == 0)
            n.set(0, 0, 1);
        n.normalize();
        Matrix3f rotationMatrix = new Matrix3f(
                n.x * n.x, n.x * n.y, n.x * n.z,
                n.y * n.x, n.y * n.y, n.y * n.z,
                n.z * n.x, n.z * n.y, n.z * n.z);
        rotationMatrix.mul((float)(1 - Math.cos(radian)));
        rotationMatrix.add(new Matrix3f(
                (float)Math.cos(radian), (float)(- n.z * Math.sin(radian)), (float)(n.y * Math.sin(radian)),
                (float)(n.z * Math.sin(radian)), (float)Math.cos(radian), (float)(-n.x * Math.sin(radian)),
                (float)(- n.y * Math.sin(radian)), (float)(n.x * Math.sin(radian)), (float)Math.cos(radian))
        );
        Vector3f r = new Vector3f();
        rotationMatrix.transform(direction, r);
        r.normalize();
        return r;
    }

    public static Vector3f getVelocityVector(Vector3f lastPosition, Vector3f secondLastPosition, float timeDiff)
    {
        Vector3f diffA = new Vector3f(lastPosition);
        diffA.sub(secondLastPosition);
        diffA.scale(1/timeDiff);
        return diffA;
    }

    public static Vector3f getAccelerationVector(Vector3f lastVelocity, Vector3f secondLastVelocity, float timeDiff)
    {
        return  getVelocityVector(lastVelocity, secondLastVelocity, timeDiff);
    }

    public static Vector3f vector3fAveraging(List<Vector3f> vecs)
    {
        Vector3f result = new Vector3f();
        for(Vector3f v: vecs)
            result.add(v);
        result.scale(1F/vecs.size());
        return result;
    }

    public static Vector3f getAccelerationVector(Vector3f lastPosition, Vector3f secondLastPosition, Vector3f thirdLastPosition, float timeDiff)
    {
        Vector3f diffA = getVelocityVector(lastPosition, secondLastPosition, timeDiff);
        Vector3f diffB = getVelocityVector(secondLastPosition, thirdLastPosition, timeDiff);

        return  getAccelerationVector(diffA, diffB, timeDiff);
    }

    public static float additiveAverage(List<Float> values)
    {
        float valueSum = 0;
        int weightSum = 0;
        for(int i = 0; i < values.size(); i++)
        {
            valueSum += (i + 1) * values.get(i);
            weightSum += i + 1;
        }
        return valueSum / weightSum;
    }

    public static float average(List<Float> values)
    {
        return values.stream().reduce(Float::sum).get() / values.size();
    }

    public static Vector3f rotateUp90(Vector3f v)
    {
        float x2y2 = v.x * v.x + v.y * v.y;
        float x2y2Sqrt = (float)Math.sqrt(x2y2);
        return new Vector3f(-v.z * v.x / x2y2Sqrt,- v.z * v.y / x2y2Sqrt, x2y2Sqrt);
    }

    public static void drawArc(Graphics2D g2d, Vector3f position, double range, int startAngle, int totalAngle)
    {
        g2d.drawLine((int)(position.x), (int)(position.y), (int)(position.x + range * Math.cos(Math.toRadians(startAngle + totalAngle))), (int)(position.y - range * Math.sin(Math.toRadians(startAngle + totalAngle))));
        g2d.drawLine((int)(position.x), (int)(position.y), (int)(position.x + range * Math.cos(Math.toRadians(startAngle))), (int)(position.y - range * Math.sin(Math.toRadians(startAngle))));

        g2d.drawArc((int)(position.x - range), (int)(position.y - range), (int)(range * 2), (int)(range * 2), (startAngle), totalAngle);
    }

    public static double crossProduct2D(Vector2f a, Vector2f b)
    {
        return a.x * b.y - b.x * a.y;
    }

    public static float lightTimeToDestFromSource(Vector3f source, Vector3f dest)
    {
        Vector3f diff = new Vector3f(dest);
        diff.sub(source);
        return diff.length() / ElectroMagneticWave.LIGHT_SPEED;
    }

    public static Vector3f getPerpendicularComponent(Vector3f resolvingVector, Vector3f perpendicularTo)
    {
        Vector3f parrallel = new Vector3f(perpendicularTo);
        parrallel.scale(resolvingVector.dot(perpendicularTo) / perpendicularTo.dot(perpendicularTo));
        Vector3f perpendicularVector = new Vector3f(resolvingVector);
        perpendicularVector.sub(parrallel);

//        System.out.println(perpendicularVector.dot(perpendicularTo));
        return perpendicularVector;
    }

    public static Vector3f calculateAngularVelocity(Vector3f centripetalAcceleration, Vector3f linearVelocity)
    {
        Vector3f angularVelocity = new Vector3f();
        angularVelocity.cross(centripetalAcceleration, linearVelocity);
        angularVelocity.scale(1 / linearVelocity.lengthSquared());
        return angularVelocity;
    }

    public static Map<String, List<?>> getFuturePosition(Vector3f initialPosition, Vector3f angularVelocity, float totalAngle, float size)
    {
        Matrix3f K = new Matrix3f(
                0, -angularVelocity.z, angularVelocity.y,
                angularVelocity.z, 0, -angularVelocity.x,
                -angularVelocity.y, angularVelocity.x, 0
        );
        Matrix3f identity = new Matrix3f(1, 0, 0, 0, 1, 0, 0, 0, 1);
        Matrix3f KSquared = new Matrix3f(K);
        KSquared.mul(K);

        List<Vector3f> positions= new ArrayList<>();
        List<Float> times = new ArrayList<>();
        float angularSpeed = angularVelocity.length();
        for(int i = 0; i < size; i++)
        {
            float angle = totalAngle / size * (i + 1);
            float time = angle / angularSpeed;

            Matrix3f KCalc = new Matrix3f(K);
            Matrix3f KSquaredCalc = new Matrix3f(KSquared);
            KCalc.mul((float)Math.sin(angle));
            KSquaredCalc.mul((float)(1 - Math.cos(angle)));

            Matrix3f R = new Matrix3f(identity);
            R.add(KCalc);
            R.add(KSquaredCalc);

            Vector3f position = new Vector3f(initialPosition);
            R.transform(position);

            positions.add(position);
            times.add(time);
        }

        return new HashMap<>(){
            {
                put("Time", times);
                put("Position", positions);
            }
        };
    }

    public static List<Vector3f> futurePos(Vector3f initialPos, Vector3f centripetalAcceleration, Vector3f velocity, List<Float> times, float shiftTime)
    {
        float radius = velocity.lengthSquared() / centripetalAcceleration.length();
        Vector3f centerDiff = new Vector3f(centripetalAcceleration);
        centerDiff.normalize();
        centerDiff.scale(radius);
        Vector3f center = new Vector3f(initialPos);
        center.add(centerDiff);
        centerDiff.negate();
        Vector3f normal = new Vector3f();
        normal.cross(centerDiff, velocity);
        normal.normalize();
        Vector3f cross = new Vector3f();
        cross.cross(normal, centerDiff);

        List<Vector3f> positions = new ArrayList<>();
        float angularSpeed = centripetalAcceleration.length() / velocity.length();
        for(int i = 0; i < times.size(); i++) {
            float angle = (times.get(i) - shiftTime) * angularSpeed;

            Matrix3f rotationMatrix = new Matrix3f(
                    normal.x * normal.x, normal.x * normal.y, normal.x * normal.z,
                    normal.y * normal.x, normal.y * normal.y, normal.y * normal.z,
                    normal.z * normal.x, normal.z * normal.y, normal.z * normal.z);
            rotationMatrix.mul((float)(1 - Math.cos(angle)));
            rotationMatrix.add(new Matrix3f(
                    (float)Math.cos(angle), (float)(- normal.z * Math.sin(angle)), (float)(normal.y * Math.sin(angle)),
                    (float)(normal.z * Math.sin(angle)), (float)Math.cos(angle), (float)(-normal.x * Math.sin(angle)),
                    (float)(- normal.y * Math.sin(angle)), (float)(normal.x * Math.sin(angle)), (float)Math.cos(angle))
            );
            Vector3f position = new Vector3f(initialPos);
            position.sub(center);
            Vector3f r = new Vector3f();
            rotationMatrix.transform(position, r);
            r.add(center);

//            Vector3f newPosition = new Vector3f(center);
//            newPosition.add(r);


//            Vector3f crossCalc = new Vector3f(cross);
//            crossCalc.scale((float) Math.sin(angle));
//
//            Vector3f centerDiffCalc = new Vector3f(centerDiff);
//            centerDiffCalc.scale((float) Math.cos(angle));
//
//            Vector3f newPosition = new Vector3f(center);
//            newPosition.add(crossCalc);
//            newPosition.add(centerDiffCalc);

            positions.add(r);
        }

        return positions;
    }

    public static void main(String[] argv)
    {
//        System.out.println(GameMath.directionToAngle(new Vector2f(1, 1.5F)));
//        System.out.println(GameMath.sphereAreaForAngle(1, Math.toRadians(180)));

//        Vector2f v1 = new Vector2f(1, 1);
//        Vector2f v2 = new Vector2f(1, 5);
//        System.out.println(v2.angle(v1));
//        System.out.println(GameMath.crossProduct2D(v1, v2));

//        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
//        double[] y = new double[]{1, 7, 9, 15, 22};
//        double[][] x = new double[5][];
//        x[0] = new double[]{0, 0, 0};
//        x[1] = new double[]{1, 1, 1};
//        x[2] = new double[]{0, 1, 2};
//        x[3] = new double[]{1, 2, 3};
//        x[4] = new double[]{3, 3, 4};
//        regression.newSampleData(y, x);
//
//        double[] beta = regression.estimateRegressionParameters();
//
//        for (double bVal : beta) {
//            System.out.println(bVal);
//        }
//
//        final PolynomialCurveFitter fitter = PolynomialCurveFitter.create(2);
//        final WeightedObservedPoints obs = new WeightedObservedPoints();
        List<List<Double>> list = new ArrayList<>();
        list.add(Arrays.asList(-12.0, 131.0));
        list.add(Arrays.asList(-11.5, 119.75));
        list.add(Arrays.asList(-11.0, 108.0));
        list.add(Arrays.asList(-10.5, 96.75));
        list.add(Arrays.asList(-10.0, 86.0));
        list.add(Arrays.asList(-9.5, 75.75));
        list.add(Arrays.asList(-9.0, 66.0));
        list.add(Arrays.asList(-8.5, 56.75));
        list.add(Arrays.asList(-8.0, 48.0));
        list.add(Arrays.asList(-7.5, 39.75));
        list.add(Arrays.asList(-7.0, 32.0));
        list.add(Arrays.asList(-6.5, 24.75));
        list.add(Arrays.asList(-6.0, 18.0));
        list.add(Arrays.asList(-5.5, 11.75));
        list.add(Arrays.asList(-5.0, 6.0));
        list.add(Arrays.asList(-4.5, 0.75));
        list.add(Arrays.asList(-4.0, -4.0));
        list.add(Arrays.asList(-3.5, -8.25));
        list.add(Arrays.asList(-3.0, -12.0));
        list.add(Arrays.asList(-2.5, -15.25));
        list.add(Arrays.asList(-2.0, -18.0));
        list.add(Arrays.asList(-1.5, -20.25));
        list.add(Arrays.asList(-1.0, -22.0));
        list.add(Arrays.asList(-0.5, -23.25));
        list.add(Arrays.asList(0.0, -24.0));
        list.add(Arrays.asList(0.5, -24.25));
        list.add(Arrays.asList(1.0, -24.0));
        list.add(Arrays.asList(1.5, -23.25));
        list.add(Arrays.asList(2.0, -22.0));
        list.add(Arrays.asList(2.5, -20.25));
        list.add(Arrays.asList(3.0, -18.0));
        list.add(Arrays.asList(3.5, -15.25));
        list.add(Arrays.asList(4.0, -12.0));
        list.add(Arrays.asList(4.5, -8.25));
        list.add(Arrays.asList(5.0, -4.0));
        list.add(Arrays.asList(5.5, 0.75));
        list.add(Arrays.asList(6.0, 6.0));
        list.add(Arrays.asList(6.5, 11.75));
        list.add(Arrays.asList(7.0, 18.0));
        list.add(Arrays.asList(7.5, 24.75));
        list.add(Arrays.asList(8.0, 32.0));
        list.add(Arrays.asList(8.5, 39.75));
        list.add(Arrays.asList(9.0, 48.0));
        list.add(Arrays.asList(9.5, 56.75));
        list.add(Arrays.asList(10.0, 66.0));
        list.add(Arrays.asList(10.5, 75.75));
        list.add(Arrays.asList(11.0, 86.0));
        list.add(Arrays.asList(11.5, 96.75));
        list.add(Arrays.asList(12.0, 108.0));
//        for (List<java.lang.Double> point : list) {
//            obs.add(point.get(1), point.get(0));
//        }
//        double[] fit = fitter.fit(obs.toList());
//        for(Double v: fit)
//            System.out.println(v);

//        OLSMultipleLinearRegression regression1 = new OLSMultipleLinearRegression();
//
//        double[] y1 = new double[list.size()];
//        double[][] x2 = new double[list.size()][];
//        for(int i = 0; i < list.size(); i++)
//        {
//            y1[i] = list.get(i).get(1);
//            double[] poly = new double[2+1];
//            double xj=1;
//            double x = list.get(i).get(0);
//            for(int j=0; j<=2; j++) {
//                poly[j]=xj;
//                xj*=x;
//            }
//            x2[i] = poly;
//        }
//        regression1.newSampleData(y1, x2);
//
//        double[] beta1 = regression1.estimateRegressionParameters();
//
//        for (double bVal : beta1) {
//            System.out.println(bVal);
//        }

        Vector3f velocity = new Vector3f(20, 0, 0);
        Vector3f acceleration = GameMath.getPerpendicularComponent(new Vector3f(0, -1, 0), velocity);
        Vector3f angularVelocity = GameMath.calculateAngularVelocity(acceleration, velocity);
        System.out.println(angularVelocity);
        System.out.println(acceleration.length() / velocity.length());

        List<Float> times = new ArrayList<>();
        for(int i = 0; i < 20; i++)
            times.add(i * 2F);

//        List<Vector3f> positions = GameMath.futurePos(new Vector3f(0, 1, 0), acceleration, velocity, times);
//
//
////        Map<String, List<?>> map = GameMath.getFuturePosition(new Vector3f(0, 1, 0), angularVelocity, (float)Math.PI / 2, 10);
////        List<Float> times = (List<Float>)map.get("Time");
////        List<Vector3f> positions = (List<Vector3f>)map.get("Position");
////
//        for(int i = 0; i < positions.size(); i++)
//        {
//            System.out.println(positions.get(i));
//        }

    }
}
