package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.Terrain;
import za.co.entelect.challenge.enums.PowerUps;

import java.util.*;

import static java.lang.Math.PI;
import static java.lang.Math.max;

public class Bot {

    private static final int maxSpeed = 9;
    private List<Integer> directionList = new ArrayList<>();

    private Random random;
    private GameState gameState;
    private Car opponent;
    private Car myCar;

    //commands
    private final static Command FIX = new FixCommand();
    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);


    public Bot(Random random, GameState gameState) {
        this.random = random;
        this.gameState = gameState;
        this.myCar = gameState.player;
        this.opponent = gameState.opponent;

        directionList.add(-1);
        directionList.add(1);
    }

    public Command run() {


        //strategy :
        //accelerate, kalo ada obstacle, belok
        //maxkan speed, minkan damage


        List<Object> blocks = getBlocksInFront(myCar.position.lane, myCar.position.block,myCar.speed);

        //safeguard
        if (myCar.damage >= 5) {
            return FIX;
        }
        else if (myCar.speed <= 3) {
            return ACCELERATE;
        }

        //use any powerup available
        else if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
            return BOOST;
        }
        else if (hasPowerUp(PowerUps.EMP,myCar.powerups)) {
            return EMP;
        }
        else if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
            return LIZARD;
        }
        else if (hasPowerUp(PowerUps.OIL,myCar.powerups)) {
            return OIL;
        }

        //move to safest lane
        else {
            return moveToLongestPath(myCar.position.lane,myCar.position.block, myCar.speed);
        }



    }

    public Command moveToLongestPath(int lane, int block, int speed) {
        //bakalan nyari jalan yang bisa paling jauh ditempuh pake speed mobil
        //paling jauh = jarak terjauh sebelum nyampe obstacle (sementara masih MUD doang)
        int index = getLongestPath(lane,block, speed);
        if (index == 0) {
            return TURN_LEFT;
        }
        else if (index == 1) {
            return ACCELERATE;
        }
        else {
            return TURN_RIGHT;
        }
    }

    //mencari indeks obstacle terdekat
//    public int getObstacleIndex(List<Object> blocks) {
//        int i = 0;
//        for (i = 0; i < blocks.size();i++) {
//            if (blocks.get(i) == Terrain.MUD) {
//                break;
//            }
//            else if (blocks.get(i) == Terrain.OIL_SPILL) {
//                break;
//            }
//            else if (blocks.get(i) == Terrain.WALL) {
//                break;
//            }
//            else {
//                i += 1;
//            }
//        }
//
//        return i;
//    }
    /**
     * Returns map of blocks and the objects in the for the current lanes, returns the amount of blocks that can be
     * traversed at max speed.
     **/
    private List<Object> getBlocksInFront(int lane, int block, int speed) {
        List<Lane[]> map = gameState.lanes;
        List<Object> blocks = new ArrayList<>();
        int startBlock = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + speed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i].terrain);

        }
        return blocks;
    }




    //mencari jalan yang bisa paling jauh ditempuh tanpa obstacle
    //mengembalikan 0 jika left, 1 jika front, 2 jika right
    private int getLongestPath(int lane, int block, int speed) {
        int a = -1;
        int b = -1;
        int c = -1;
        int aa = 0;
        int ab = 0;
        int ac = 0;
        int res;
        // right = 2, front = 1,  left = 0;
        if (lane > 1) {
            // posisi mobil tidak disamping kiri
            a = getClosestObstacle(lane - 2, block, speed);
            aa = countPowerups(lane-2,block,speed);
        }
        if (lane < 4) {
            //mobil tidak berada di samping kanan
            c = getClosestObstacle(lane, block, speed);
            ac = countPowerups(lane, block, speed);
        }

        b = getClosestObstacle(lane-1,block,speed);
        ab = countPowerups(lane-1, block, speed);
        if (a > -1) {
            if (c > -1) {
                res = getMax3(a,b,c,lane,block,speed);
            }
            else {
                if (a > b) {
                    res = 0;
                }
                else { //a == b
                    if (aa > ab) {
                        return 0;
                    }
                    else {
                        return 1;
                    }
                }
            }
        }
        else if (c > -1) {
            if (c > b) {
                res = 2;
            }
            else if (b > c){
                res = 1;
            }
            else {

                if (ab > ac) {
                    return 1;
                }
                else {
                    return 2;
                }
            }
        }
        else {
            res = 1;
        }

        return res;
    }

    //mencari nilai terbesar dari a,b,c
    //0 jika a, 1 jika b, 2 jika c
    private int getMax3(int a, int b, int c, int lane, int blocks, int speed) {
        int ax = 0;
        int ac = 0;
        int ab = 0;
        if (a > b) {
            if (a > c) {
                return 0;
            }
            else if (a < c) {
                return 2;
            }
            else { //a == c
                ax = countPowerups(lane-2,blocks,speed);
                ac = countPowerups(lane, blocks, speed);
                if (ax > ac) {
                    return 0;
                }
                else {
                    return 1;
                }
            }
        }
        else if (a < b) {
            if (b > c) {
                return 1;
            }
            else if (b < c) {
                return 2;
            }
            else {
                ab = countPowerups(lane-1,blocks,speed);
                ac = countPowerups(lane, blocks, speed);
                if(ab >= ac) {
                    return 1;
                }
                else{
                    return 2;
                }
            }
        }
        else {
            if (b > c) {
                return 1;
            }
            else if (b < c) {
                return 2;
            }
            else {
                ab = countPowerups(lane-1,blocks,speed);
                ac = countPowerups(lane, blocks, speed);
                if(ab >= ac) {
                    return 1;
                }
                else{
                    return 2;
                }
            }
        }
    }

    //menghitung banyak powerups di depan
    private int countPowerups(int lane, int block, int speed) {
        List<Lane[]> map = gameState.lanes;
        int startBlock = map.get(0)[0].position.block;
        int x = 0;
        Lane[] laneList = map.get(lane);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + speed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }
            else if (laneList[i].terrain == Terrain.BOOST || laneList[i].terrain == Terrain.EMP) {
                x += 1;
            }
            else if (laneList[i].terrain == Terrain.LIZARD || laneList[i].terrain == Terrain.OIL_POWER){
                x += 1;
            }
            else if (laneList[i].terrain == Terrain.TWEET) {
                x += 1;
            }

        }

        return x;
    }


    //mengecek apakah mobil memiliki powerup
    //kode merupakan referensi dari reference-bot
    private Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

    //mencari indeks obstacle terdekat dari block mobil
    private int getClosestObstacle(int lane, int block,int speed) {
        List<Lane[]> map = gameState.lanes;
        int startBlock = map.get(0)[0].position.block;
        int x = 0;
        Lane[] laneList = map.get(lane);
        for (int i = max(block - startBlock, 0); i <= block - startBlock + speed; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH || laneList[i].terrain == Terrain.MUD) {
                break;
            }
            else if (laneList[i].terrain == Terrain.OIL_SPILL || laneList[i].terrain == Terrain.WALL) {
                break;
            }

            x += 1;

        }

    return x;
    }

}
