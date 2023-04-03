package potato.model;

import org.joml.Vector4f;
import packets.data.ObjectData;
import potato.model.data.HeroState;
import potato.model.data.HeroType;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class HeroLocations {
    public float dist; // Hacky workaround to store distance. Distance to hero location from object.

    private final int index;
    private final String indexString;
    private final int x;
    private final int y;

    private Vector4f possibleSpawnColorMain;
    private Vector4f possibleSpawnColorSecondary;
    private String possibleSpawnShapeMain;
    private String possibleSpawnShapeSecondary;
    private boolean multiShapes = true;

    private int possibleHeroType;
    private long resetTimer = 0;
    private HeroType locationType = HeroType.UNVISITED;
    private HeroState locationState = HeroState.MARK_UNVISITED;
    public static int largest = 0;

    public HeroLocations(int index, int x, int y, int types) {
        this.index = index;
        indexString = Integer.toString(index + 1);
        this.x = x;
        this.y = y;
        setTypes(types);
    }

    private void setTypes(int types) {
        Vector4f cc = null;
        Vector4f cd = null;

        String sc = null;
        String sd = null;
        int mod = 1;
        int largestMod = 0;
        for (int i = 0; i < 9; i++) {
            if (cc == null && (types & mod) != 0) {
                cc = getTypeColor(mod);
                sc = getTypeShape(mod);
                largestMod = mod;
            } else if ((types & mod) != 0) {
                cd = getTypeColor(mod);
                sd = getTypeShape(mod);
                if (mod < 64) largestMod = mod; // ignore house as 64
                break;
            }
            mod = mod << 1;
        }

        if (cd == null) {
            cd = cc;
            sd = sc;
            multiShapes = false;
        }

        possibleSpawnColorMain = cc;
        possibleSpawnColorSecondary = cd;

        possibleSpawnShapeMain = sc;
        possibleSpawnShapeSecondary = sd;

        possibleHeroType = largestMod;
    }

    private Vector4f getTypeColor(int t) {
        switch (t) {
            case 1:
                return colorToVecColor(new Color(255, 0, 0));
            case 2:
                return colorToVecColor(new Color(255, 128, 0));
            case 4:
                return colorToVecColor(new Color(255, 0, 255));
            case 8:
                return colorToVecColor(new Color(255, 255, 0));
            case 16:
                return colorToVecColor(new Color(0, 255, 0));
            case 32:
                return colorToVecColor(new Color(0, 128, 255));
            case 64:
                return colorToVecColor(new Color(165, 42, 42));
            default:
                return colorToVecColor(new Color(255, 255, 255));
        }
    }

    private String getTypeShape(int t) {
        switch (t) {
            case 1:
                return "a";
            case 2:
                return "b";
            case 4:
                return "c";
            case 8:
                return "d";
            case 16:
                return "e";
            case 32:
                return "f";
            case 64:
                return "g";
            default:
                return "e";
        }
    }

    private Vector4f getColor(boolean main) {
        if (locationState == HeroState.MARK_UNVISITED) {
            if (Config.instance.singleColorShapes) return Config.instance.shapesColorVec;

            Vector4f color;
            if (main) {
                color = possibleSpawnColorMain;
            } else {
                color = possibleSpawnColorSecondary;
            }
            color.w = Config.instance.shapesColorVec.w;

            return color;
        } else if (locationState == HeroState.MARK_DEAD) {
            return Config.instance.deadColorVec;
        } else if (locationState == HeroState.MARK_VISITED) {
            return Config.instance.visitedColorVec;
        } else if (locationState == HeroState.MARK_ACTIVE) {
            return Config.instance.activeColorVec;
        }

        return null;
    }

    public static Vector4f colorToVecColor(Color c) {
        return new Vector4f(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f);
    }

    public float squareDistTo(int x, int y) {
        float dx = this.x - x;
        float dy = this.y - y;
        return dx * dx + dy * dy;
    }

    public String toString() {
        return String.format("Idx:%s x:%d y:%d", indexString, x, y);
    }

    public boolean hasType() {
        return locationType != HeroType.UNVISITED;
    }

    public HeroType getHeroType() {
        return locationType;
    }

    public void setType(HeroType type) {
        locationType = type;
        resetTimer = System.currentTimeMillis() + 2200;
    }

    public boolean setState(HeroState state) {
        if (locationState == state) return false;
        locationState = state;
        resetTimer = System.currentTimeMillis() + 2200;
        return true;
    }

    public boolean isMissing(HashMap<Integer, ObjectData> entitys) {
        if (locationState == HeroState.MARK_DEAD) return false;
        if (dist > locationType.missing()) return false;
        if (entitys.size() == 0) return true;
        for (Map.Entry<Integer, ObjectData> e : entitys.entrySet()) {
            ObjectData o = e.getValue();
            if (matchType(o) && squareDistTo((int) o.status.pos.x, (int) o.status.pos.y) < 500) return false;
        }
        return true;
    }

    public int getState() {
        return locationType.getIndex() + locationState.getIndex() * 16;
    }

    public void setMarker(int marker, boolean ignoreTimer) {
        long t = resetTimer - System.currentTimeMillis();
        if (!ignoreTimer && t > 0) return;

        int typeIndex = marker % 16;
        int stateIndex = marker / 16;
        locationType = HeroType.byOrdinal(typeIndex);
        locationState = HeroState.byOrdinal(stateIndex);
    }

    public boolean matchType(ObjectData found) {
        return locationType.typeMatch(found.objectType);
    }

    public int getIndex() {
        return index;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Vector4f getPossibleSpawnColorMain() {
        return getColor(true);
    }

    public Vector4f getPossibleSpawnColorSecondary() {
        return getColor(false);
    }

    public String shapeCharM() {
        if (locationType == HeroType.UNVISITED) return possibleSpawnShapeMain;
        return locationType.getShape();
    }

    public String shapeCharS() {
        if (locationType == HeroType.UNVISITED) return possibleSpawnShapeSecondary;
        return locationType.getShape();
    }

    public String getIndexString() {
        return indexString;
    }

    public boolean multipleShapes() {
        if (locationType == HeroType.UNVISITED) return multiShapes;
        return false;
    }

    public int getPossibleHeroType() {
        return possibleHeroType;
    }

    public HeroState getLocationState() {
        return locationState;
    }

    public void reset() {
        resetTimer = 0;
        locationType = HeroType.UNVISITED;
        locationState = HeroState.MARK_UNVISITED;
    }
}