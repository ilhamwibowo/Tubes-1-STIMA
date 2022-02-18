package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import java.security.SecureRandom;

public class Bot {

    private static final int maxSpeed = 9;
    private List<Command> directionList = new ArrayList<>();

    private final Random random;

    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();
    private final static Command DO_NOTHING = new DoNothingCommand();

    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);

    public Bot() {
        this.random = new SecureRandom();
        directionList.add(TURN_LEFT);
        directionList.add(TURN_RIGHT);
    }

    public Command run(GameState gameState) {
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;

        //Basic fix logic
        List<Lane> frontBlocks = new ArrayList<>();
        frontBlocks = getBlocksInFront(myCar.position.lane, myCar.position.block, gameState);

        int frontSpeedReduction;
        int leftSpeedReduction;
        int rightSpeedReduction;

        frontSpeedReduction = countSpeedReduction(frontBlocks, Bot.maxSpeed);
        if (myCar.position.lane > 1) {
            List<Lane> leftBlocks = getBlocksOnLeft(myCar.position.lane, myCar.position.block, gameState);
            leftSpeedReduction = countSpeedReduction(leftBlocks, Bot.maxSpeed);
        } else {
            List<Lane> leftBlocks = new ArrayList<>();
            leftSpeedReduction = 99;
        }
        if (myCar.position.lane < 4) {
            List<Lane> rightBlocks = getBlocksOnRight(myCar.position.lane, myCar.position.block, gameState);
            rightSpeedReduction = countSpeedReduction(rightBlocks, Bot.maxSpeed);
        } else {
            List<Lane> rightBlocks = new ArrayList<>();
            rightSpeedReduction = 99;
        }


        /*Fix jika Damage >= 2*/
        if (myCar.damage >= 2) {
            return FIX;
        } else {
            /* speed < 15 */
            if (myCar.speed < 15) {
                /* hitung pengurangan speed jika boost diaktifkan */
                int frontSpeedReduction15 = countSpeedReduction(frontBlocks, 15);
                /* Jika tidak ada pengurangan speed di depan
                   Tidak belok ke kiri ataupun ke kanan*/
                if (frontSpeedReduction15 == 0) {
                    /* Jika punya boost */
                    if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
                        /* Jika damage = 0 dan punya Lizard ATAU damage = 1 dan speed = 3 */
                        if ((myCar.damage == 0 && hasPowerUp(PowerUps.LIZARD, myCar.powerups)) || (myCar.damage == 1 && myCar.speed == 3)) {
                            return BOOST;
                            /* Jika damage = 0 dan tidak punya Lizard ATAU damage = 1 dan speed > 3 */
                        } else {
                            /* Jika speed belum maksimum */
                            if (myCar.speed < maxSpeed) {
                                return ACCELERATE;
                                /* Jika speed sudah maksumum */
                            } else {
                                /* Jika musuh terlihat di map */
                                if (opponent.position.block > myCar.position.block - 6 && opponent.position.block < myCar.position.block + 21) {
                                    /* Jika musuh di depan mobil player */
                                    if (opponent.position.block > myCar.position.block) {
                                        /* Jika punya EMP, tembak EMP */
                                        if (hasPowerUp(PowerUps.EMP, myCar.powerups)) {
                                            return EMP;
                                            /* Jika punya Tweet, prediksi posisi musuh dan letakkan CyberTruck */
                                        } else if (hasPowerUp(PowerUps.TWEET, myCar.powerups)) {
                                            //prediksi posisi musuh selanjutnya
                                            //return TWEET;
                                            /* Jika tidak punya EMP ataupun Tweet */
                                        } else {
                                            return DO_NOTHING;
                                        }
                                        /* Jika musuh di belakang mobil player */
                                    } else {
                                        return DO_NOTHING;
                                    }
                                    /* Jika musuh tidak terlihat di map */
                                } else {
                                    /* Jika musuh ada di belakang player dan player punya Oil, gunakan oil */
                                    if (opponent.position.block < myCar.position.block && hasPowerUp(PowerUps.OIL, myCar.powerups)) {
                                        return OIL;
                                        /* Jika musuh di depan player atau tidak punya Oil */
                                    } else {
                                        return DO_NOTHING;
                                    }
                                }
                            }
                        }
                        /* Jika tidak punya Boost */
                    } else {
                        /* Jika speed belum maksimum, Accelerate */
                        if (myCar.speed < Bot.maxSpeed) {
                            return ACCELERATE;
                            /* Jika speed sudah maksimum */
                        } else {
                            /* Jika musuh terlihat di map */
                            if (opponent.position.block > myCar.position.block - 6 && opponent.position.block < myCar.position.block + 21) {
                                /* Jika musuh ada di depan player */
                                if (opponent.position.block > myCar.position.block) {
                                    /* Jika punya EMP, tembak EMP */
                                    if (hasPowerUp(PowerUps.EMP, myCar.powerups)) {
                                        return EMP;
                                        /* Jika punya Tweet, prediksi posisi musuh dan letakkan CyberTruck */
                                    } else if (hasPowerUp(PowerUps.TWEET, myCar.powerups)) {
                                        //prediksi posisi musuh selanjutnya
                                        //return TWEET
                                        /* Jika tidak punya EMP ataupun Tweet */
                                    } else {
                                        return DO_NOTHING;
                                    }
                                    /* Jika musuh ada di belakang player */
                                } else {
                                    return DO_NOTHING;
                                }
                                /* Jika musuh tidak terlihat di map */
                            } else {
                                /* Jika musuh ada di belakang player dan player punya Oil, gunakan oil */
                                if (opponent.position.block < myCar.position.block && hasPowerUp(PowerUps.OIL, myCar.powerups)) {
                                    return OIL;
                                    /* Jika musuh ada di depan player atau player tidak punya Oil */
                                } else {
                                    return DO_NOTHING;
                                }
                            }
                        }
                    }
                    /* Jika ada speed reduction di depan */
                } else {
                    /* Jika speed belum maksimum */
                    if (myCar.speed < Bot.maxSpeed) {
                        /* Jika frontSpeedReduction paling rendah */
                        if (frontSpeedReduction <= leftSpeedReduction || frontSpeedReduction <= rightSpeedReduction) {
                            return ACCELERATE;
                        /* Jika speed reduction di kiri atau kanan lebih rendah */
                        } else {
                            /* Jika leftSpeedReduction lebih rendah, belok kiri, belok kiri */
                            if (leftSpeedReduction < rightSpeedReduction) {
                                return TURN_LEFT;
                            /* Jika speed reduction di kanan lebih rendah dari kiri, belok kanan */
                            } else if (leftSpeedReduction > rightSpeedReduction) {
                                return TURN_RIGHT;
                            /* Jika speed reduction di kanan sama dengan speed reduction di kiri, random antara kiri dan kanan */
                            } else {
                                Random rand1 = new Random();
                                int x1 = rand1.nextInt(2);
                                if (x1 == 0) {
                                    return TURN_LEFT;
                                } else {
                                    return TURN_RIGHT;
                                }
                            }
                        }
                    /* Jika speed sudah maksimum */
                    } else {

                        if (leftSpeedReduction == 0 && rightSpeedReduction == 0) {
                            Random rand2 = new Random();
                            int x2 = rand2.nextInt(2);
                            if (x2 == 0) {
                                return TURN_LEFT;
                            } else {
                                return TURN_RIGHT;
                            }
                        } else if (leftSpeedReduction > 0 && rightSpeedReduction == 0) {
                            return TURN_RIGHT;
                        } else if (leftSpeedReduction == 0 && rightSpeedReduction > 0) {
                            return TURN_LEFT;
                        } else {
                            if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
                                return LIZARD;
                            } else {
                                if (frontSpeedReduction < leftSpeedReduction || frontSpeedReduction < rightSpeedReduction) {
                                    /* Jika musuh terlihat di map */
                                    if (opponent.position.block > myCar.position.block - 6 && opponent.position.block < myCar.position.block + 21) {
                                        /* Jika musuh ada di depan player */
                                        if (opponent.position.block > myCar.position.block) {
                                            /* Jika punya EMP, tembak EMP */
                                            if (hasPowerUp(PowerUps.EMP, myCar.powerups)) {
                                                return EMP;
                                                /* Jika punya Tweet, prediksi posisi musuh dan letakkan CyberTruck */
                                            } else if (hasPowerUp(PowerUps.TWEET, myCar.powerups)) {
                                                //prediksi posisi musuh selanjutnya
                                                //return TWEET
                                                /* Jika tidak punya EMP ataupun Tweet */
                                            } else {
                                                return DO_NOTHING;
                                            }
                                            /* Jika musuh ada di belakang player */
                                        } else {
                                            return DO_NOTHING;
                                        }
                                        /* Jika musuh tidak terlihat di map */
                                    } else {
                                        /* Jika musuh ada di belakang player dan player punya Oil, gunakan oil */
                                        if (opponent.position.block < myCar.position.block && hasPowerUp(PowerUps.OIL, myCar.powerups)) {
                                            return OIL;
                                            /* Jika musuh ada di depan player atau player tidak punya Oil */
                                        } else {
                                            return DO_NOTHING;
                                        }
                                    }
                                } else {
                                    if (leftSpeedReduction < rightSpeedReduction) {
                                        return TURN_LEFT;
                                    } else if (leftSpeedReduction > rightSpeedReduction) {
                                        return TURN_RIGHT;
                                    } else {
                                        Random rand3 = new Random();
                                        int x3 = rand3.nextInt(2);
                                        if (x3 == 0) {
                                            return TURN_LEFT;
                                        } else {
                                            return TURN_RIGHT;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return ACCELERATE;
    }

    private Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns map of blocks and the objects in the for the current lanes, returns
     * the amount of blocks that can be traversed at max speed.
     **/
    private List<Lane> getBlocksInFront(int lane, int block, GameState gameState) {
        List<Lane[]> map = gameState.lanes;
        List<Lane> blocks = new ArrayList<>();
        int startBlock1 = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 1);
        for (int i = block - startBlock1 + 1; i <= block - startBlock1 + 16; i++) {
            if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[i]);
        }
        return blocks;
    }

    private List<Lane> getBlocksOnLeft(int lane, int block, GameState gameState) {
        List<Lane[]> map = gameState.lanes;
        List<Lane> blocks = new ArrayList<>();
        int startBlock2 = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane - 2);
        for (int j = block - startBlock2; j <= block - startBlock2 + 15; j++) {
            if (laneList[j] == null || laneList[j].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[j]);
        }
        return blocks;
    }

    private List<Lane> getBlocksOnRight(int lane, int block, GameState gameState) {
        List<Lane[]> map = gameState.lanes;
        List<Lane> blocks = new ArrayList<>();
        int startBlock3 = map.get(0)[0].position.block;

        Lane[] laneList = map.get(lane);
        for (int k = block - startBlock3; k <= block - startBlock3 + 15; k++) {
            if (laneList[k] == null || laneList[k].terrain == Terrain.FINISH) {
                break;
            }

            blocks.add(laneList[k]);
        }
        return blocks;
    }

    private int getSpeedState(int maxSpeed) {
        int speedState = 0;
        switch (maxSpeed) {
            case 15:
                speedState = 6;
                break;
            case 9:
                speedState = 5;
                break;
            case 8:
                speedState = 4;
                break;
            case 6:
                speedState = 3;
                break;
            case 5:
                speedState = 2;
                break;
            case 3:
                speedState = 1;
                break;
        }
        return speedState;
    }

    private int countSpeedReduction(List<Lane> blocks, int maxSpeed) {
        int total = 0;
        int tempMaxSpeed = maxSpeed;
        if (maxSpeed > blocks.size()) {
            tempMaxSpeed = blocks.size();
        }
        for (int l = 0; l < tempMaxSpeed; l++) {
            if (blocks.get(l).terrain == Terrain.MUD || blocks.get(l).terrain == Terrain.OIL_SPILL) {
                total += 1;
            } else if (blocks.get(l).terrain == Terrain.WALL || blocks.get(l).isOccupiedByCyberTruck) {
                total += 5;
            }
            if (total > 5) {
                total = 5;
            }
        }
        if (getSpeedState(maxSpeed) - total == 2) {
            total += 1;
        } else if (getSpeedState(maxSpeed) - total < 1) {
            total = getSpeedState(maxSpeed) - 1;
        }
        return total;
    }

}
