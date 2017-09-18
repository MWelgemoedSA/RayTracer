package io.github.mwelgemoedsa;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import java.util.ArrayList;

public class Triangle implements SceneObject {
    private ArrayList<Vector3d> pointList;
    private Vector3d normal;
    private double distanceFromOrigin;

    private ArrayList<Vector2d> vertexTextureCoordinates;

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
        normal.normalize();

        distanceFromOrigin = p1.dot(normal); //Distance of the plane from the origin

        vertexTextureCoordinates = null;
    }

    @Override
    public String toString() {
        return "Triangle{" +
                "pointList=" + pointList +
                ", normal=" + normal +
                ", distanceFromOrigin=" + distanceFromOrigin +
                '}';
    }

    public double rayIntersect(Ray ray) {
        double alignmentToRay = normal.dot(ray.getDirection());
        if (alignmentToRay == 0) return -1; //Parallel, triangles are infinitely thin, so no collision

        Vector3d fromOriginToPlane = new Vector3d();
        fromOriginToPlane.sub(pointList.get(0), ray.getOrigin());
        if (fromOriginToPlane.dot(ray.getDirection()) < 0) { //Behind the ray
            return -1;
        }

        double distFromRayOrigin = normal.dot(ray.getOrigin());

        double intersectDist = (distFromRayOrigin + distanceFromOrigin) / alignmentToRay;

        if (intersectDist < 0) return intersectDist;

        Vector3d intersectPoint = new Vector3d();
        intersectPoint.scale(intersectDist, ray.getDirection());
        intersectPoint.add(ray.getOrigin());

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

    @Override
    public boolean isLitInternally() {
        return false;
    }

    @Override
    public Vector2d calculateTextureCoordinates(Vector3d pointOnSurface) { if (vertexTextureCoordinates == null) return null;

        //Calculate the barycentry coordinates of the point on the surface and use it to interpolate between the texture coordinates of the corners
        Vector3d toPoint1 = new Vector3d();
        toPoint1.sub(pointList.get(0), pointOnSurface);

        Vector3d toPoint2 = new Vector3d();
        toPoint2.sub(pointList.get(1), pointOnSurface);

        Vector3d toPoint3 = new Vector3d();
        toPoint3.sub(pointList.get(2), pointOnSurface);

        Vector3d v1 = new Vector3d();
        v1.sub(pointList.get(0), pointList.get(1));
        Vector3d v2 = new Vector3d();
        v2.sub(pointList.get(1), pointList.get(2));

        Vector3d areaVector = v1;
        areaVector.cross(v1, v2);
        double triangleArea = areaVector.length();

        areaVector.cross(toPoint2, toPoint3);
        double a1 = areaVector.length() / triangleArea;

        areaVector.cross(toPoint3, toPoint1);
        double a2 = areaVector.length() / triangleArea;

        areaVector.cross(toPoint1, toPoint2);
        double a3 = areaVector.length() / triangleArea;

        Vector2d textureCoordsSum = new Vector2d();

        Vector2d textureCoords1 = new Vector2d();
        textureCoords1.scale(a1, vertexTextureCoordinates.get(0));

        Vector2d textureCoords2 = new Vector2d();
        textureCoords2.scale(a2, vertexTextureCoordinates.get(1));

        Vector2d textureCoords3 = new Vector2d();
        textureCoords3.scale(a3, vertexTextureCoordinates.get(2));

        textureCoordsSum.add(textureCoords1);
        textureCoordsSum.add(textureCoords2);
        textureCoordsSum.add(textureCoords3);
        return textureCoordsSum;
    }

    public Vector3d normalAtPoint(Vector3d point) {
        return normal;
    }

    public void setVertexTextureCoordinates(ArrayList<Vector2d> vertexTextureCoordinates) {
        this.vertexTextureCoordinates = vertexTextureCoordinates;
    }
}
