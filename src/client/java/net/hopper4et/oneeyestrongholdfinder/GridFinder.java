package net.hopper4et.oneeyestrongholdfinder;


public class GridFinder {

    double x, z, originX, originZ, ratio;
    int signX, distance;
    public GridFinder(double originX, double originZ, double ratio, int signX) {
        this.originX = originX;
        this.originZ = originZ;
        this.ratio = ratio;
        this.signX = signX;
        this.distance = -1;
        next();
    }
    public Stronghold next() {
        distance++;
        x = (Math.round(originX / 16) + distance * signX) * 16;
        z = (x - originX) * ratio + originZ;
        return new Stronghold((long) (1 / (Math.abs(alignToGrid(z) - z))), alignToGrid(x), alignToGrid(z));
    }
    public boolean isInRing() {
        double distance = Math.round(Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2)));
        return distance < 21248 ? (distance + 240) % 3072 > 1504 : distance < 24336;
    }
    public int alignToGrid(double x) {
        return (int) (Math.round(x / 16) * 16);
    }
}
