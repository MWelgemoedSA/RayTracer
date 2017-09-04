package io.github.mwelgemoedsa;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;

import static javax.swing.text.html.HTML.Attribute.N;
import static javax.swing.text.html.HTML.Tag.P;

public class Triangle implements SceneObject {
    private ArrayList<Vector3d> pointList;
    private Vector3d normal;
    private double distanceFromOrigin;

    Triangle(Vector3d p1, Vector3d p2, Vector3d p3) {
        pointList = new ArrayList<>();

        pointList.add(p1);
        pointList.add(p2);
        pointList.add(p3);

        Vector3d v1 = new Vector3d();
        v1.sub(p1, p2);
        Vector3d v2 = new Vector3d();
        v2.sub(p2, p3);

        v1.normalize();
        v2.normalize();

        normal = new Vector3d();
        normal.cross(v1, v2);

        distanceFromOrigin = p1.dot(normal); //Distance of the plane from the origin
    }

    @Override
    public String toString() {
        return "Triangle{" +
                "pointList=" + pointList +
                ", normal=" + normal +
                ", distanceFromOrigin=" + distanceFromOrigin +
                '}';
    }

    public double rayIntersect(Vector3d ray) {
        Vector3d orig = new Vector3d(0, 0, 0);

        double alignmentToRay = normal.dot(ray);
        if (alignmentToRay == 0) return -1; //Parallel, triangles are infinitely thin, so no collision

        double intersectDist = (normal.dot(orig) + distanceFromOrigin) / alignmentToRay;

        if (intersectDist < 0) return intersectDist;

        Vector3d intersectPoint = new Vector3d();
        intersectPoint.scale(intersectDist, ray);
        intersectPoint.add(orig);

        //Test that the point is inside the triangle
        for (int i = 0; i < 3; i++) {
            int j = i+1; if (j == 3) j = 0; //Next point, edge is between two points

            Vector3d edge = new Vector3d();
            edge.sub(pointList.get(j), pointList.get(i));

            Vector3d toPoint = new Vector3d();
            toPoint.sub(intersectPoint, pointList.get(i));

            Vector3d vectorOutOfTriangle = new Vector3d();

            vectorOutOfTriangle.cross(edge, toPoint);
            if (normal.dot(vectorOutOfTriangle) <= 0) return -1; //Outside the triangle
        }

        return intersectDist;
    }

    public Vector3d normalAtPoint(Vector3d point) {
        return normal;
    }
}
